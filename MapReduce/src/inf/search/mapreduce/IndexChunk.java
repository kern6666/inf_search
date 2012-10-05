package inf.search.mapreduce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class IndexChunk implements Serializable {

    private static final long serialVersionUID = 7729506254083517192L;

    public final ArrayList<String> filesNames;
    public final SortedMap<String, TreeSet<Integer>> invertIndex;

    public IndexChunk(ArrayList<String> filesNames,
	    SortedMap<String, TreeSet<Integer>> invertIndex) {
	this.filesNames = filesNames;
	this.invertIndex = invertIndex;
    }

    public void saveToFile(String path) {
	ObjectOutputStream outputStream = null;
	try {
	    outputStream = new ObjectOutputStream(new FileOutputStream(path));
	    outputStream.writeObject(this);

	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (outputStream != null)
		try {
		    outputStream.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
    }

    public static IndexChunk loadFromFile(String file) {
	ObjectInputStream outputStream = null;
	try {
	    outputStream = new ObjectInputStream(new FileInputStream(file));
	    IndexChunk chunk = (IndexChunk) outputStream.readObject();
	    return chunk;
	} catch (ClassNotFoundException | IOException e) {
	    e.printStackTrace();
	} finally {
	    if (outputStream != null)
		try {
		    outputStream.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	return null;
    }

    public static String saveIndexIntoChunks(String dir,
	    TreeMap<String, TreeSet<Integer>> invertIndex, int parserID,
	    ArrayList<String> filesNames) {
	int coffset = ((int) 'a');
	int D = ((int) 'z') - ((int) 'a');
	int d = D / CHUNK_NUMBER;
	int chunkSizes = 0;

	File resDir = Utils.getFreeName(dir, parserID, DIR_NAME);
	resDir.mkdirs();
	for (int i = 0; i < CHUNK_NUMBER; i++) {
	    char start = (char) (coffset + d * i);
	    char end = (char) (coffset + d * (i + 1));
	    String endKey;
	    String startKey;
	    if (i + 1 == CHUNK_NUMBER) {
		endKey = invertIndex.lastKey();
	    } else
		endKey = invertIndex.lowerKey(String.valueOf(end));
	    if (i == 0)
		startKey = invertIndex.firstKey();
	    else
		startKey = invertIndex.ceilingKey(String.valueOf(start));

	    SortedMap<String, TreeSet<Integer>> chunk = invertIndex.subMap(
		    startKey, true, endKey, true);
	    chunk = new TreeMap<String, TreeSet<Integer>>(chunk);
	    chunkSizes += chunk.size();
	    IndexChunk indexChunk = new IndexChunk(filesNames, chunk);
	    saveChunk(indexChunk, resDir.getAbsolutePath(), i);
	    // indexChunk.saveToFile(new File(resDir, String.valueOf(i))
	    // .getAbsolutePath());
	}

	System.out.println(chunkSizes + ", Index Size: " + invertIndex.size());
	return resDir.getAbsolutePath();
    }

    public static void saveChunk(IndexChunk chunk, String chunkDir, int chunkNum) {
	chunk.saveToFile(new File(chunkDir, String.valueOf(chunkNum))
		.getAbsolutePath());
    }

    public static IndexChunk loadChunk(String chunksDir, int chunkNum) {
	return loadFromFile(new File(chunksDir, String.valueOf(chunkNum))
		.getAbsolutePath());
    }

    private static final int CHUNK_NUMBER = 5;
    private static final String DIR_NAME = "parser";

    public static int getChunkNumber() {
	return CHUNK_NUMBER;
    }



    public void merge(IndexChunk ic) {
	int offset = filesNames.size();
	filesNames.addAll(ic.filesNames);
	for(String s:ic.invertIndex.keySet()){
	    TreeSet<Integer> docs = ic.invertIndex.get(s);
	    TreeSet<Integer> newDocs = new TreeSet<Integer>();
	    for (Integer i : docs)
		newDocs.add(i + offset);
	    docs = invertIndex.get(s);
	    if (s == null)
		invertIndex.put(s, newDocs);
	    else
		docs.addAll(newDocs);
	}
    }

    public void saveToTXT(String file) {
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file));
	    for (int i = 0; i < filesNames.size(); i++) {
		writer.write(String.valueOf(i));
		writer.write(':');
		writer.write(filesNames.get(i));
		writer.write('\n');
	    }
	    writer.write('\n');
	    for (String s : invertIndex.keySet()) {
		TreeSet<Integer> docs = invertIndex.get(s);
		writer.write(s);
		writer.write(':');
		writer.write(docs.toString());
		writer.write('\n');
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (writer != null)
		try {
		    writer.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
    }
}
