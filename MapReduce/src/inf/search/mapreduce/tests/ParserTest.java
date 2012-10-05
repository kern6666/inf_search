package inf.search.mapreduce.tests;

import inf.search.mapreduce.Parser;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class ParserTest extends TestCase {
    private Parser parser;

    @Before
    public void prepare() {

    }

    @Test
    public void test() {
	parser = new Parser();
	parser.run(new String[] { "txt" }, null);
    }
}
