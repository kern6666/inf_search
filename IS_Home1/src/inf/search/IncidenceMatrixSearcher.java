package inf.search;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class IncidenceMatrixSearcher extends AbstractTextSearcher {

    private HashMap<String, Integer> wordsToNum;
    private boolean[][] incidenceMatrix;

    protected HashMap<String, Integer> getWordsID() {
	return wordsToNum;
    }

    @Override
    public void proccessDir(String dir) {
	super.proccessDir(dir);

	this.wordsToNum = readAllWordsFromFiles(dir, getFilesNames());
	incidenceMatrix = buildIncidenceMatrix(dir, getFilesNames(), wordsToNum);

    }

    private boolean[][] buildIncidenceMatrix(String dir,
	    ArrayList<String> files, HashMap<String, Integer> wordToNum) {

	int i;
	boolean[][] incidenceMatrix = new boolean[wordToNum.size()][files
		.size()];
	for (i = 0; i < files.size(); i++) {
	    String file = files.get(i);
	    FileReader fin = null;
	    try {
		fin = new FileReader(new File(dir, file));
		proccessFile(fin, incidenceMatrix, wordToNum,
			Integer.valueOf(i));
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
	return incidenceMatrix;
    }

    private void proccessFile(FileReader fin,
	    boolean[][] incidenceMatrix, HashMap<String, Integer> wordToNum,
	    Integer fileNum) throws IOException {
	BufferedReader breader = new BufferedReader(fin);
	String line;
	while ((line = breader.readLine()) != null) {
	    String[] words = line.split("\\W+");
	    for (String word : words) {
		word = normalizeWord(word);
		incidenceMatrix[wordToNum.get(word)][fileNum] = true;
	    }
	}
    }

    public TreeSet<String> searchInDocuments(String searchText) {
	String[] words = searchText.split("\\W+");
	if (words == null || words.length == 0)
	    return null;
	for (int i = 0; i < words.length; i++)
	    words[i] = normalizeWord(words[i]);
	TreeSet<String> resDocs = new TreeSet<String>();

	boolean[] intersection = new boolean[getFilesNames().size()];
	    Arrays.fill(intersection, true);
	    boolean isEmpty = true;
	    for (String word : words) {
		Integer wordNum = wordsToNum.get(word);
		if (wordNum != null) {
		    isEmpty = false;
		for (int i = 0; i < getFilesNames().size(); i++)
			intersection[i] &= incidenceMatrix[wordNum][i];
		} else
		    return null;
	    }
	    if (isEmpty)
		return null;
	for (int i = 0; i < getFilesNames().size(); i++)
		if (intersection[i])
		resDocs.add(getFilesNames().get(i));
	    return resDocs;

    }

    public int getWordsCount() {
	return wordsToNum == null ? 0 : wordsToNum.size();
    }

}
