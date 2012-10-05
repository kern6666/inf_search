package inf.search;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class TwoWordsInverseIndexSearcher extends AbstractTextSearcher {

    private HashMap<String, Integer> wordsToNum;
    private HashMap<Integer, HashMap<Integer, Set<Integer>>> twoWordInverseIndex;



    @Override
    public void proccessDir(String dir) {
	super.proccessDir(dir);
	this.wordsToNum = readAllWordsFromFiles(dir, getFilesNames());
	twoWordInverseIndex = buildTwoWordInvertIndex(dir, getFilesNames(),
		    wordsToNum);
    }

    public TreeSet<String> searchInDocuments(String searchText) {
	String[] words = searchText.split("\\W+");
	if (words == null || words.length == 0)
	    return null;
	for (int i = 0; i < words.length; i++)
	    words[i] = normalizeWord(words[i]);
	TreeSet<String> resDocs = new TreeSet<String>();
	TreeSet<Integer> resDocsInt = new TreeSet<Integer>();

	String word = words[0];
	Integer wordNum = wordsToNum.get(word);
	if (wordNum == null)
	    return null;
	HashMap<Integer, Set<Integer>> docToAfterWords = twoWordInverseIndex
		.get(wordNum);
	if (docToAfterWords == null)
	    return null;
	resDocsInt.addAll(docToAfterWords.keySet());

	if (words.length > 1) {

	    for (int i = 0; i < words.length - 1; i++) {
		word = words[i];
		Integer curWordNum = wordsToNum.get(word);
		word = words[i + 1];
		Integer nextWordNum = wordsToNum.get(word);
		if (curWordNum == null || nextWordNum == null)
		    return null;
		HashMap<Integer, Set<Integer>> nextWordsSet = twoWordInverseIndex
			.get(curWordNum);
		Iterator<Integer> it = resDocsInt.iterator();
		while (it.hasNext()) {
		    Integer docNum = it.next();
		    Set<Integer> wordsAfterCurInCurDoc = nextWordsSet
			    .get(docNum);
		    if (wordsAfterCurInCurDoc == null)
			it.remove();
		    if (!wordsAfterCurInCurDoc.contains(nextWordNum))
			it.remove();
		}

	    }
	}
	for (Integer i : resDocsInt)
	    if (i != null) {
		resDocs.add(getFilesNames().get(i));
	    }
	return resDocs;

    }

    private HashMap<Integer, HashMap<Integer, Set<Integer>>> buildTwoWordInvertIndex(
	    String dir, ArrayList<String> files,
	    HashMap<String, Integer> wordToNum) {
	HashMap<Integer, HashMap<Integer, Set<Integer>>> res = new HashMap<Integer, HashMap<Integer, Set<Integer>>>();
	for (int i = 0; i < files.size(); i++) {
	    String file = files.get(i);
	    FileReader fin = null;
	    try {
		fin = new FileReader(new File(dir, file));
		proccessFile(fin, res, Integer.valueOf(i), wordToNum);
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
	return res;
    }

    private void proccessFile(FileReader fin,
	    HashMap<Integer, HashMap<Integer, Set<Integer>>> res,
	    Integer fileNum, HashMap<String, Integer> wordToNum)
	    throws IOException {
	BufferedReader breader = new BufferedReader(fin);
	String line;
	Integer prevWordNum = -1;
	while ((line = breader.readLine()) != null) {
	    String[] words = line.split("\\W+");

	    for (int i = 0; i < words.length; i++) {
		String word = normalizeWord(words[i]);
		Integer wordNum = wordToNum.get(word);
		if (prevWordNum == -1) {
		    prevWordNum = wordNum;
		    continue;
		}
		if (!res.containsKey(prevWordNum))
		    res.put(prevWordNum, new HashMap<Integer, Set<Integer>>());
		HashMap<Integer, Set<Integer>> invList = res.get(prevWordNum);
		Set<Integer> wordsAfterInFile;
		if (invList.containsKey(fileNum)) {
		    wordsAfterInFile = invList.get(fileNum);
		} else {
		    wordsAfterInFile = new TreeSet<Integer>();
		    invList.put(fileNum, wordsAfterInFile);
		}
		wordsAfterInFile.add(wordNum);
		prevWordNum = wordNum;
	    }

	}
    }

    @Override
    public int getWordsCount() {

	return wordsToNum == null ? 0 : wordsToNum.size();
    }
  



}
