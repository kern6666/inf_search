package inf.search;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class InverseIndexSearcher extends AbstractTextSearcher {

    private HashMap<String, TreeSet<Integer>> inverseIndex;

    @Override
    public void proccessDir(String dir) {
	super.proccessDir(dir);
	inverseIndex = buildInvertIndex(dir, getFilesNames());
    }

    protected HashMap<String, TreeSet<Integer>> getInverseIndex() {
	return inverseIndex;
    }

    @Override
    public TreeSet<String> searchInDocuments(String searchText) {
	String[] words = searchText.split("\\W+");
	if (words == null || words.length == 0)
	    return null;
	for (int i = 0; i < words.length; i++)
	    words[i] = normalizeWord(words[i]);
	TreeSet<String> resDocs = searchWords(words);
	return resDocs;
    }

    protected TreeSet<String> searchWords(String[] words) {
	TreeSet<String> resDocs = new TreeSet<String>();
	TreeSet<Integer> intersection = null;
	    int i = 0;
	intersection = inverseIndex.get(words[i++]);
	if (intersection != null)
	    intersection = new TreeSet<Integer>(intersection);
	else
	    return null;

	while (i < words.length) {
	    TreeSet<Integer> inverseList = inverseIndex.get(words[i++]);
	    if (inverseList == null)
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

    private HashMap<String, TreeSet<Integer>> buildInvertIndex(
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

    private void proccessFile(FileReader fin,
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

    @Override
    public int getWordsCount() {
	return inverseIndex!=null? inverseIndex.size():0;
    }

}
