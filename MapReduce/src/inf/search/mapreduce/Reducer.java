package inf.search.mapreduce;

import java.io.File;

public class Reducer {

    public String reduce(String dir,String firstDir, String secondDir) {
	File resDir = Utils.getFreeName(dir, 0, "reducer");
	resDir.mkdirs();
	for (int i = 0; i < IndexChunk.getChunkNumber(); i++) {
	    IndexChunk ic1 = IndexChunk.loadChunk(firstDir, i);
	    IndexChunk ic2 = IndexChunk.loadChunk(firstDir, i);
	    ic1.merge(ic2);
	    IndexChunk.saveChunk(ic1, resDir.getAbsolutePath(), i);
	}
	return resDir.getAbsolutePath();
    }

}
