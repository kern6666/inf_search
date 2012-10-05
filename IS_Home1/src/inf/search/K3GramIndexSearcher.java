package inf.search;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;


public class K3GramIndexSearcher extends InverseIndexSearcher {
    private HashMap<String, TreeSet<String>> kgramIndex;

    
    @Override
    public void proccessDir(String dir) {
	super.proccessDir(dir);
	buildKGramIndex(getInverseIndex());
    }

    public void buildKGramIndex(HashMap<String, ?> invertIndex) {
	kgramIndex = new HashMap<String, TreeSet<String>>();
	StringBuilder sb = new StringBuilder(20);

	for (String word : invertIndex.keySet()) {
	    sb.setLength(0);
	    sb.append('$');
	    sb.append(word);
	    sb.append('$');
	    if (sb.length() < 3) {
		String kgram0 = sb.toString();
		TreeSet<String> wordsSet = kgramIndex.get(kgram0);
		if (wordsSet == null) {
		    wordsSet = new TreeSet<String>();
		    kgramIndex.put(kgram0, wordsSet);
		}
		wordsSet.add(word);
	    } else {
		for (int i = 0; i < sb.length() - 3; i++) {
		    String kgrami = sb.substring(i, i + 3);
		    TreeSet<String> wordsSet = kgramIndex.get(kgrami);
		    if (wordsSet == null) {
			wordsSet = new TreeSet<String>();
			kgramIndex.put(kgrami, wordsSet);
		    }
		    wordsSet.add(word);
		}
	    }
	}
    }

    @Override
    public TreeSet<String> searchInDocuments(String searchText) {
	String[] words = searchText.split("[\\W&&[^*]]+");
	if (words == null || words.length == 0)
	    return null;
	for (int i = 0; i < words.length; i++)
	    words[i] = normalizeWord(words[i]);
	printQuery(words);
	return searchWords(words);
    }

    private void printQuery(String[] words) {
	StringBuilder res = new StringBuilder();
	for (String w : words) {
	    if (!w.contains("*"))
		res.append(w);
	    else {
		String[] wildcardWords = getAllPossibleWords(w);
		res.append('[');
		for (String ww : wildcardWords) {
		    res.append(ww);
		    res.append(" ");
		}
		res.append(']');
	    }
	    res.append(" ");
	}
	System.out.println(res.toString());
    }

    private TreeSet<Integer> getWordsDocuments(String word) {
	HashMap<String, TreeSet<Integer>> inverseIndex = getInverseIndex();
	if (!word.contains("*")) {
	    return inverseIndex.get(word);
	}
	String[] words = getAllPossibleWords(word);
	TreeSet<Integer> res = new TreeSet<Integer>();
	for (String w : words) {
	    TreeSet<Integer> docSet = inverseIndex.get(w);
	    res.addAll(docSet);
	}
	return res;
    }

    @Override
    protected TreeSet<String> searchWords(String[] words) {
	TreeSet<String> resDocs = new TreeSet<String>();
	TreeSet<Integer> intersection = null;
	int i = 0;
	// 1HashMap<String, TreeSet<Integer>> inverseIndex = getInverseIndex();
	TreeSet<Integer> wordsDocs = getWordsDocuments(words[i++]);
	if (wordsDocs != null)
	    intersection = new TreeSet<Integer>(wordsDocs);
	else
	    return null;

	while (i < words.length) {
	    TreeSet<Integer> inverseList = getWordsDocuments(words[i++]);
	    if (inverseList == null || inverseList.size() == 0)
		return null;
	    if (inverseList != null)
		intersection = intersectInverseLists(intersection, inverseList);

	}
	for (Integer docNum : intersection)
	    if (docNum != null) {
		resDocs.add(getFilesNames().get(docNum));
	    }
	return resDocs;
    }

    public String[] getAllPossibleWords(String wildCardWord) {
	if (!wildCardWord.contains("*") || wildCardWord.length() == 0)
	    return new String[] { wildCardWord };

	String[] wordsPart = ("$" + wildCardWord + "$").split("\\*");
	wordsPart = getKGrams(wordsPart, 3);
	if (wordsPart[0].length() == 1) {

	}
	TreeSet<String> res = kgramIndex.get(wordsPart[0]);
	if (res != null)
	    res = new TreeSet<String>(res);

	for (int i = 1; i < wordsPart.length; i++) {
	    TreeSet<String> kgramWords = kgramIndex.get(wordsPart[i]);

	    if (kgramWords != null) {
		if (res != null)
		    res.retainAll(kgramWords);
		else
		    res = new TreeSet<String>(kgramWords);
	    }
	}

	if (res == null)
	    res = new TreeSet<String>(getInverseIndex().keySet());
	// Filtration
	wordsPart = wildCardWord.split("\\*");
	Iterator<String> sit = res.iterator();
	boolean isStartFromStar = wildCardWord.charAt(0) == '*';
	boolean isEndsFromStar = wildCardWord.charAt(wildCardWord.length() - 1) == '*';
	while(sit.hasNext()){
	    String word = sit.next();
	    int pos = 0;
	    int i = 0;
	    if (!isStartFromStar) {
		i = 1;
		pos = word.startsWith(wordsPart[0]) ? wordsPart[0].length()
			: -1;
	    }
	    for (; i < wordsPart.length && pos != -1; i++)
		if (wordsPart[i] != null && !wordsPart[i].equals("")) {		 
		    pos = word.indexOf(wordsPart[i], pos);
		    if(pos!=-1)
			pos += wordsPart[i].length();
		}
	    if (!isEndsFromStar) {
		i = wordsPart.length - 1;
		if (wordsPart[i] != null && !wordsPart[i].equals(""))
		    pos = word.endsWith(wordsPart[i]) ? pos : -1;
	    }
	    if (pos == -1)
		sit.remove();
	}
	String[] resArray = new String[res.size()];
	return res.toArray(resArray);
    }

    String[] getKGrams(String[] wordsPart, int k) {
	LinkedList<String> kgrams = new LinkedList<String>();
	for(int i = 0;i<wordsPart.length;i++)
	    if (wordsPart[i].length() >= k) {
		for (int j = 0; j <= wordsPart[i].length() - k; j++)
		    kgrams.add(wordsPart[i].substring(j, j + k));
	    }
	String[] res = new String[kgrams.size()];
	return kgrams.toArray(res);
    }

}
