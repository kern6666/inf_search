package test;

import inf.search.K3GramIndexSearcher;

import org.junit.Before;
import org.junit.Test;

public class KGramsTest {
    private K3GramIndexSearcher searcher;

    @Before
    public void prepare() {
	searcher = new K3GramIndexSearcher();
	searcher.proccessDir("txt");
    }

    @Test
    public void test() {

	for (String w : searcher.getAllPossibleWords("*ed*ing*")) {
	    System.out.println(w);
	}

    }


}
