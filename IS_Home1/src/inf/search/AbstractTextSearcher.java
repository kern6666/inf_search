package inf.search;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public abstract class AbstractTextSearcher {

    private ArrayList<String> filesNames;

    public ArrayList<String> getFilesNames() {
	return filesNames;
    }

    public void proccessDir(String dir) {
	ArrayList<String> files = getTxtFilesFromDir(dir);
	filesNames = files;
    }

    abstract public TreeSet<String> searchInDocuments(String searchText);

    abstract public int getWordsCount();

    protected HashMap<String, Integer> readAllWordsFromFiles(String dir,
	    ArrayList<String> files) {
	TreeSet<String> allWords = new TreeSet<String>();
	for (int i = 0; i < files.size(); i++) {
	    String file = files.get(i);
	    FileReader fin = null;
	    try {
		fin = new FileReader(new File(dir, file));
		readWordsFromFile(new BufferedReader(fin), allWords);
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

	HashMap<String, Integer> wordToNum = new HashMap<String, Integer>();
	int i = 0;
	for (String w : allWords)
	    wordToNum.put(w, i++);
	return wordToNum;
    }

    private void readWordsFromFile(BufferedReader fileReader,
	    TreeSet<String> words) throws IOException {
	String line;
	while ((line = fileReader.readLine()) != null) {
	    String[] lineWords = line.split("\\W+");
	    for (String word : lineWords) {
		word = normalizeWord(word);
		words.add(word);
	    }
	}
    }

    private static ArrayList<String> getTxtFilesFromDir(String dir) {
	File dirFile = new File(dir);
	ArrayList<String> res = new ArrayList<String>();
	FilenameFilter filter = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		if (name.endsWith(".txt"))
		    return true;
		else
		    return false;
	    }
	};
	if (dirFile.isDirectory())
	    for (File f : dirFile.listFiles(filter)) {
		res.add(f.getName());
	    }
	return res;
    }

    protected String normalizeWord(String word) {
	return word.toLowerCase();
    }

    public int getFilesNum() {
	// TODO Auto-generated method stub
	return filesNames == null ? 0 : filesNames.size();
    }

}
