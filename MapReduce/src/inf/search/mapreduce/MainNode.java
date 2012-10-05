package inf.search.mapreduce;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainNode {
    private String txtDir = "txt";
    private String workingDir = "dir";
    private BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);
    private volatile boolean finished = false;
    private Object mutex = new Object();
    private Object reducerMutex = new Object();
    private volatile int reducerThreadCounter = 0;

    public void run() {
	Thread pthread = new ParsersThread();
	pthread.start();
	Thread rthread = new ReducersThread();
	rthread.start();

	try {
	    pthread.join();
	    rthread.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	String res = queue.peek();
	if (res != null) {
	    for (int i = 0; i < IndexChunk.getChunkNumber(); i++) {
		IndexChunk c = IndexChunk.loadChunk(res, i);
		c.saveToTXT(new File(workingDir, i + ".txt").getAbsolutePath());
	    }
	}
    }
    private class ParsersThread extends Thread {
	private static final int PARSERS_NUM = 5;

	@Override
	public void run() {
	    File[] files1 = new File(txtDir).listFiles();
	    String[] files = new String[files1.length];
	    int i =0;
	    for(File f:files1){
		files[i++] = f.getAbsolutePath();
	    }
	    int d = files.length/PARSERS_NUM;
	    ExecutorService executor = Executors.newFixedThreadPool(8);
	    for (i = 0; i < PARSERS_NUM; i++) {
		int to = i == PARSERS_NUM - 1 ? (files.length - 1) : (i + 1)
			* d;
		String[] parseTask = Arrays.copyOfRange(files, i * d, to);
		executor.execute(new ParserRunnable(parseTask));
	    }
	    executor.shutdown();
	    while (!executor.isTerminated()) {
		try {
		    executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	    finished = true;
	    synchronized (mutex) {
		System.out.println("End parsing");
	    }
	}


    }

    private class ReducersThread extends Thread {

	@Override
	public void run() {
	    ExecutorService executor = Executors.newFixedThreadPool(8);

	    while (!finished || queue.size() > 1 || reducerThreadCounter > 0) {

		try {
		    if (queue.size() <= 1)
			executor.awaitTermination(80, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		if (queue.size() <= 1)
		    continue;
		String task = queue.poll();
		String task1 = queue.poll();
		increaseReducerCounter();
		executor.execute(new ReducerRunnable(task, task1));
	    }
	    executor.shutdown();
	    synchronized (mutex) {
		System.out.println("End merging.");
	    }
	}

    }

    private class ParserRunnable implements Runnable {
	private final String[] task;

	public ParserRunnable(String[] task) {
	    this.task = task;
	}

	@Override
	public void run() {
	    synchronized (mutex) {
		System.out.println("Start parsing: " + Arrays.toString(task));
	    }
	    Parser parser = new Parser();
	    String resDir = parser.run(task, workingDir);
	    queue.add(resDir);
	    synchronized (mutex) {
		System.out.println("End parsing: " + Arrays.toString(task));
	    }
	}
    };

    private class ReducerRunnable implements Runnable {
	private final String dir1, dir2;

	public ReducerRunnable(String dir1, String dir2) {
	    this.dir1 = dir1;
	    this.dir2 = dir2;
	}

	@Override
	public void run() {
	    synchronized (mutex) {
		System.out.println("Start merging: " + dir1 + " | " + dir2);
	    }
	    Reducer reducer = new Reducer();
	    String resDir = reducer.reduce(workingDir, dir1, dir2);
	    queue.add(resDir);
	    Utils.deleteDir(dir1);
	    Utils.deleteDir(dir2);
	    synchronized (mutex) {
		System.out.println("End merging: " + dir1 + " | " + dir2);
	    }
	    decreaseReducerCounter();
	}
    };

    private void increaseReducerCounter() {
	synchronized (reducerMutex) {
	    reducerThreadCounter++;
	}
    }

    private void decreaseReducerCounter() {
	synchronized (reducerMutex) {
	    reducerThreadCounter--;
	}
    }
}
