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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseIndexWithPositionsSearcher extends AbstractTextSearcher {

    private HashMap<String, HashMap<Integer, Set<Long>>> inverseIndexWithPositions;


    @Override
    public void proccessDir(String dir) {
	super.proccessDir(dir);

	inverseIndexWithPositions = buildInvertWithPositionIndex(dir,
		getFilesNames());

    }

    public TreeSet<String> searchInDocuments(String searchText) {

	TreeSet<String> resDocs = new TreeSet<String>();

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
		resDocs.add(getFilesNames().get(i));
		}
	    return resDocs;

    }

    @Override
    public int getWordsCount() {

	return inverseIndexWithPositions == null ? 0
		: inverseIndexWithPositions.size();

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
