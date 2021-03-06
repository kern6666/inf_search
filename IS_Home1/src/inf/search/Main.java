package inf.search;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.TreeSet;

public class Main {

    public static void main(String... args) {
	try {
	    String dir = null;
	    TextSearcher.SearchStructure searchtype = TextSearcher.SearchStructure.InverseIndexWithPositions;
	    if (args != null)
		for (String arg : args) {
		    if ("-m".equals(arg)) {
			searchtype = TextSearcher.SearchStructure.IncidenceMatrix;
		    } else {
			dir = arg;
			break;
		    }
		}

	    if (dir == null)
		dir = "txt";

	    // TextSearcher searcher = new TextSearcher();
	    AbstractTextSearcher searcher = new K3GramIndexSearcher();
	    long startTime = System.currentTimeMillis();
	    // searcher.proccessDir(dir, searchtype);
	    searcher.proccessDir(dir);
	    System.out.println("Time of dir proccessing: "
		    + (System.currentTimeMillis() - startTime) / 1000 + "s");
	    System.out.println("Number of procced documents: "
		    + searcher.getFilesNum());
	    System.out.println("Number of words in dictionary: "
		    + searcher.getWordsCount());
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    System.in));
	    String searchString = null;
	    System.out.println("Enter search string...");
	    try {
		searchString = reader.readLine();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    while (searchString == null
		    || !searchString.trim().toLowerCase().equals("quit")) {
		try {

		    startTime = System.currentTimeMillis();
		    TreeSet<String> res = searcher
			    .searchInDocuments(searchString);
		    System.out.println("Search time: "
			    + (System.currentTimeMillis() - startTime) / 1000f
			    + "s");
		    printCollection(res);
		    System.out.println("Enter search string...");
		    searchString = reader.readLine();
		} catch (IOException e) {
		    // e.printStackTrace();
		}
	    }
	    try {
		reader.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	} finally {
	}
	// catch (Throwable e) {
	// printUsage();
	// }

    }

    private static void printUsage() {
	System.out.println("search [-m] [<texts directory>]");
	System.out
		.println("-m Uses incidence matrix. Default uses inverse index");
	System.out.println("Print 'quit' to exit.");
    }

    public static void printCollection(Collection<String> c) {
	if (c == null) {
	    System.out.println("Nothing find.");
	    return;
	}
	System.out.println("Seached files: " + c.size());
	for (String s : c) {
	    System.out.print(s);
	    System.out.print("\t");
	}
	System.out.println();
    }
}
