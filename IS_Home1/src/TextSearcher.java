import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextSearcher {

    private HashMap<String, Integer> wordsToNum;
    private SearchStructure searchStructure;
    private HashMap<String, TreeSet<Integer>> inverseIndex;
    private HashMap<Integer, HashMap<Integer, Set<Integer>>> twoWordInverseIndex;
    private HashMap<String, HashMap<Integer, Set<Long>>> inverseIndexWithPositions;
    private boolean[][] incidenceMatrix;
    private ArrayList<String> filesNames;

    public enum SearchStructure {
	IncidenceMatrix, InverseIndex, TwoWordsInverseIndex, InverseIndexWithPositions
    };

    public void proccessDir(String dir, SearchStructure searchStructure) {
	ArrayList<String> files = getTxtFilesFromDir(dir);
	this.filesNames = files;
	this.searchStructure = searchStructure;
	if (searchStructure == SearchStructure.IncidenceMatrix) {
	    this.wordsToNum = readAllWordsFromFiles(dir, files);
	    incidenceMatrix = buildIncidenceMatrix(dir, files, wordsToNum);
	}
	if (searchStructure == SearchStructure.InverseIndex) {
	    inverseIndex = buildInvertIndex(dir, files);
	}
	if (searchStructure == SearchStructure.TwoWordsInverseIndex) {
	    this.wordsToNum = readAllWordsFromFiles(dir, files);
	    twoWordInverseIndex = buildTwoWordInvertIndex(dir, files,
		    wordsToNum);
	}
	if (searchStructure == SearchStructure.InverseIndexWithPositions) {
	    inverseIndexWithPositions = buildInvertWithPositionIndex(dir, files);
	}
    }

    public TreeSet<String> searchInDocuments(String searchText) {
	String[] words = searchText.split("\\W+");
	if (words == null || words.length == 0)
	    return null;
	for (int i = 0; i < words.length; i++)
	    words[i] = normalizeWord(words[i]);
	TreeSet<String> resDocs = new TreeSet<String>();
	if (searchStructure == SearchStructure.IncidenceMatrix) {
	    boolean[] intersection = new boolean[filesNames.size()];
	    Arrays.fill(intersection, true);
	    boolean isEmpty = true;
	    for (String word : words) {
		Integer wordNum = wordsToNum.get(word);
		if (wordNum != null) {
		    isEmpty = false;
		    for (int i = 0; i < filesNames.size(); i++)
			intersection[i] &= incidenceMatrix[wordNum][i];
		} else
		    return null;
	    }
	    if (isEmpty)
		return null;
	    for (int i = 0; i < filesNames.size(); i++)
		if (intersection[i])
		    resDocs.add(filesNames.get(i));
	    return resDocs;

	} else if (searchStructure == SearchStructure.InverseIndex) {
	    TreeSet<Integer> intersection = null;
	    int i = 0;
	    intersection = new TreeSet<Integer>(inverseIndex.get(words[i++]));
	    if (intersection == null)
		return null;

	    intersection = new TreeSet<Integer>(intersection);

	    while (i < words.length) {
		TreeSet<Integer> inverseList = inverseIndex.get(words[i++]);
		if (inverseList == null)
		    return null;
		if (inverseList != null)
		    intersection = intersectInverseLists(intersection,
			    inverseList);

	    }
	    for (Integer docNum : intersection)
		if (docNum != null) {
		    resDocs.add(filesNames.get(docNum));
		}
	    return resDocs;
	} else if (searchStructure == SearchStructure.TwoWordsInverseIndex) {
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
		    resDocs.add(filesNames.get(i));
		}
	    return resDocs;
	} else if (searchStructure == SearchStructure.InverseIndexWithPositions) {
	    HashMap<Integer, Set<Long>> resDocInt = new HashMap<Integer, Set<Long>>();

	    String[] words1 = searchText.split("[\\W&&[^\\\\]]+");
	    if (words1 == null || words1.length == 0)
		return null;
	    for (int i = 0; i < words1.length; i++)
		if (words1[i].length() > 1 && words1[i].charAt(0) != '\\')
		    words1[i] = normalizeWord(words1[i]);

	    HashMap<Integer, Set<Long>> firstWordDocs = inverseIndexWithPositions
		    .get(words1[0]);
	    if (firstWordDocs == null)
		return null;
	    for (Integer doc : firstWordDocs.keySet()) {
		Set<Long> wordPos = firstWordDocs.get(doc);
		Set<Long> sentEnd = new TreeSet<Long>();
		for (Long start : wordPos) {
		    sentEnd.add(start + words1[0].length());
		}
		resDocInt.put(doc, sentEnd);
	    }

	    int maxDist = Integer.MAX_VALUE;

	    for (int i = 1; i < words1.length; i++) {

		if (words1[i].charAt(0) == '\\') {
		    try {
			maxDist = Integer.parseInt(words1[i].substring(1));
		    } catch (IndexOutOfBoundsException e) {
			return null;
		    } catch (NumberFormatException e) {
			return null;
		    }
		} else {

		    HashMap<Integer, Set<Long>> wordDocs = inverseIndexWithPositions
			    .get(words1[i]);
		    if (wordDocs == null)
			return null;

		    Set<Integer> sentDocs = new TreeSet<Integer>(
			    resDocInt.keySet());
		    for (Integer doc : sentDocs) {
			Set<Long> wordPos = wordDocs.get(doc);
			if (wordPos == null) {
			    resDocInt.remove(doc);
			    continue;
			}
			Set<Long> sentEnd = resDocInt.get(doc);

			sentEnd = intersectSentenceAndWord(sentEnd, wordPos,
				maxDist, words1[i].length());
			if (sentEnd == null || sentEnd.size() == 0) {
			    resDocInt.remove(doc);
			    continue;
			} else
			    resDocInt.put(doc, sentEnd);

		    }
		    maxDist = Integer.MAX_VALUE;
		}

	    }

	    for (Integer i : resDocInt.keySet())
		if (i != null) {
		    resDocs.add(filesNames.get(i));
		}
	    return resDocs;
	}

	return null;
    }

    public int getWordsCount() {
	if (searchStructure == null)
	    return 0;
	else if (searchStructure == SearchStructure.IncidenceMatrix)
	    return wordsToNum == null ? 0 : wordsToNum.size();
	else if (searchStructure == SearchStructure.InverseIndex)
	    return inverseIndex.size();
	return 0;
    }

    private TreeSet<Integer> intersectInverseLists(TreeSet<Integer> invList1,
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

    private static HashMap<Integer, HashMap<Integer, Set<Integer>>> buildTwoWordInvertIndex(
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

    private HashMap<String, HashMap<Integer, Set<Long>>> buildInvertWithPositionIndex(
	    String dir, ArrayList<String> files) {
	HashMap<String, HashMap<Integer, Set<Long>>> res = new HashMap<String, HashMap<Integer, Set<Long>>>();
	for (int i = 0; i < files.size(); i++) {
	    String file = files.get(i);
	    FileReader fin = null;
	    try {
		fin = new FileReader(new File(dir, file));
		proccessFileWordPosition(fin, res, i);
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

    private static HashMap<String, TreeSet<Integer>> buildInvertIndex(
	    String dir, ArrayList<String> files) {
	HashMap<String, TreeSet<Integer>> res = new HashMap<String, TreeSet<Integer>>();

	for (int i = 0; i < files.size(); i++) {
	    String file = files.get(i);
	    FileReader fin = null;
	    try {
		fin = new FileReader(new File(dir, file));
		proccessFile(fin, res, Integer.valueOf(i));
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

    private static void proccessFile(FileReader fin,
	    HashMap<String, TreeSet<Integer>> res, Integer fileNum)
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

    private static void proccessFile(FileReader fin,
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

    private static HashMap<String, Integer> readAllWordsFromFiles(String dir,
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

    private static boolean[][] buildIncidenceMatrix(String dir,
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

    private static void proccessFile(FileReader fin,
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

    private static void readWordsFromFile(BufferedReader fileReader,
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

    private static String normalizeWord(String word) {
	return word.toLowerCase();
    }

    public int getFilesNum() {
	// TODO Auto-generated method stub
	return filesNames == null ? 0 : filesNames.size();
    }

    private Integer startWord = 0;

    private String nextWord(String in, Integer wordStart, Matcher matcher) {
	if (!matcher.find())
	    return null;
	startWord = matcher.start();
	return in.substring(startWord, matcher.end());
    }

    private void proccessFileWordPosition(FileReader fin,
	    HashMap<String, HashMap<Integer, Set<Long>>> res, Integer fileNum)
	    throws IOException {
	BufferedReader breader = new BufferedReader(fin);
	String line;
	long curPos = 0;
	startWord = 0;
	while ((line = breader.readLine()) != null) {
	    Pattern pattern = Pattern.compile("[\\w]+");
	    Matcher matcher = pattern.matcher(line);
	    String word = null;
	    while ((word = nextWord(line, startWord, matcher)) != null) {
		word = normalizeWord(word);
		HashMap<Integer, Set<Long>> invList = res.get(word);
		if (invList == null) {
		    invList = new HashMap<Integer, Set<Long>>();
		    res.put(word, invList);
		}
		Set<Long> wordPos = invList.get(fileNum);
		if (wordPos == null) {
		    wordPos = new TreeSet<Long>();
		    invList.put(fileNum, wordPos);
		}
		wordPos.add(curPos + startWord);
	    }
	    curPos += line.length();
	}
    }

    private TreeSet<Long> intersectSentenceAndWord(Set<Long> sentceEndSet,
	    Set<Long> wordStartsSet, int maxDist, int wordLen) {
	TreeSet<Long> newSentenceEnd = new TreeSet<Long>();
	if (sentceEndSet.size() == 0 || wordStartsSet.size() == 0)
	    return newSentenceEnd;
	Iterator<Long> itWordSet = wordStartsSet.iterator();

	while (itWordSet.hasNext()) {
	    long wordStart = itWordSet.next();
	    Iterator<Long> itSentenceEnd = sentceEndSet.iterator();
	    while (itSentenceEnd.hasNext()) {
		long sentenceEnd = itSentenceEnd.next();
		if (Math.abs(wordStart - sentenceEnd) <= maxDist) {
		    newSentenceEnd.add(wordStart + wordLen);
		    break;
		}
	    }
	}
	return newSentenceEnd;
    }

}
