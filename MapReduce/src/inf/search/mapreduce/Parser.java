package inf.search.mapreduce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public class Parser {

    private final int parserID;
    private ArrayList<String> filesNames;
    private TreeMap<String, TreeSet<Integer>> invertIndex;
    private FilenameFilter txtFilter = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	    return isTextFile(name);
	    }
	};
    private FileFilter dirFilter = new FileFilter() {
	    @Override
	    public boolean accept(File dir) {
		if (dir.isDirectory())
		    return true;
		else
		    return false;
	    }
	};

    public Parser() {
	parserID = 0;
    }

    public Parser(int parserID) {
	this.parserID = parserID;
    }

    private boolean isTextFile(String fileName) {
	if (fileName.endsWith(".txt"))
	    return true;
	else
	    return false;
    }

    private ArrayList<String> proccessDir(File dirFile, FileAction fileAction) {
	ArrayList<String> res = new ArrayList<String>();

	if (dirFile.isDirectory()) {
	    for (File f : dirFile.listFiles(txtFilter)) {
		filesNames.add(f.getAbsolutePath());
		fileAction.proccessFile(f, filesNames.size() - 1);
	    }
	    for (File f : dirFile.listFiles(dirFilter)) {
		proccessDir(f, fileAction);
	    }
	}

	return res;
    }

    public String run(String[] fileList, String resultDir) {
	proccessFiles(fileList);
	return IndexChunk.saveIndexIntoChunks(resultDir, invertIndex, parserID,
		filesNames);
    }

    private void proccessFiles(String[] fileList) {
	invertIndex = new TreeMap<String, TreeSet<Integer>>();
	filesNames = new ArrayList<String>();
	for (String f : fileList) {
	    File file = new File(f);
	    if (file.isDirectory()) {
		proccessDir(file, fileAction);
	    } else if (isTextFile(file.getName())) {
		filesNames.add(file.getAbsolutePath());
		fileAction.proccessFile(file, filesNames.size() - 1);
	    }
	}
    }

    protected TreeMap<String, TreeSet<Integer>> getInverseIndex() {
	return invertIndex;
    }

    protected TreeSet<Integer> intersectInverseLists(TreeSet<Integer> invList1,
	    TreeSet<Integer> invList2) {
	TreeSet<Integer> res = new TreeSet<Integer>();
	Iterator<Integer> it1 = invList1.iterator();
	Iterator<Integer> it2 = invList2.iterator();
	int i1;
	int i2;
	if (it1.hasNext() && it2.hasNext()) {
	    i1 = it1.next();
	    i2 = it2.next();
	} else
	    return res;
	do {
	    if (i1 == i2) {
		res.add(i1);
		i1 = it1.hasNext() ? it1.next() : -1;
		i2 = it2.hasNext() ? it2.next() : -1;
	    } else if (i1 > i2) {
		i2 = it2.hasNext() ? it2.next() : -1;
	    } else
		i1 = it1.hasNext() ? it1.next() : -1;
	} while (i1 != -1 && i2 != -1);
	if (i1 == i2 && i1 != -1)
	    res.add(i1);
	return res;
    }

    private void proccessFile(FileReader fin,
	    TreeMap<String, TreeSet<Integer>> res, Integer fileNum)
	    throws IOException {
	BufferedReader breader = new BufferedReader(fin);
	String line;
	while ((line = breader.readLine()) != null) {
	    String[] words = line.split("\\W+");
	    for (String word : words) {
		word = normalizeWord(word);
		if (!res.containsKey(word))
		    res.put(word, new TreeSet<Integer>());
		TreeSet<Integer> invList = res.get(word);
		invList.add(fileNum);
	    }
	}
    }

    public int getWordsCount() {
	return invertIndex != null ? invertIndex.size() : 0;
    }

    protected String normalizeWord(String word) {
	return word.toLowerCase();
    }

    private FileAction fileAction = new FileAction() {

	@Override
	public void proccessFile(File file, int fileID) {
	    FileReader fin = null;
	    try {
		fin = new FileReader(file);
		Parser.this.proccessFile(fin, invertIndex, fileID);
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fin != null)
		    try {
			fin.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
	    }
	}
    };

    public interface FileAction {
	public void proccessFile(File file, int fileID);
    }
    

}