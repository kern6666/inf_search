package inf.search.mapreduce;

import java.io.File;

public class Utils {
    public static File getFreeName(String dir, int parserID, String file_name) {
	File f;
	if (dir == null)
	    f = new File(file_name + parserID);
	else
	    f = new File(dir, file_name + parserID);
	if (!f.exists())
	    return f;
	String tmp = file_name + parserID + "_";
	int i = 1;
	while (f.exists()) {
	    if (dir == null)
		f = new File(tmp + i);
	    else
		f = new File(dir, tmp + i);
	    i++;
	}
	return f;
    }

    public static void deleteDir(String path) {
	File dir = new File(path);
	if (dir.exists())
	    for (File f : dir.listFiles()) {
		if (f.isDirectory()) {
		    deleteDir(f.getAbsolutePath());
		}else
		    f.delete();
	    }
	dir.delete();
    }
}
