import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;


public class Main {
	
	
	public static void main(String ... args){
		String dir = null;
		if(args==null || args.length==0)
			dir = "txt";
		else
		 dir= args[0];
		 TextSearcher searcher = new TextSearcher();
		 long startTime =System.currentTimeMillis();
		 searcher.proccessDir(dir, TextSearcher.SearchStructure.IncidenceMatrix);
		 System.out.println("Time of dir proccessing: "+(System.currentTimeMillis() - startTime)/1000 +"s");
		 System.out.println("Number of procced documents: " + searcher.getFilesNum());
		 System.out.println("Number of proccessed words: "+searcher.getWordsCount());
		 BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		 String searchString = null;
		 System.out.println("Enter search string...");
		try {
			searchString = reader.readLine();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		 while(searchString==null || !searchString.trim().toLowerCase().equals("quit") ){
			 try {
					searchString = reader.readLine();
					startTime = System.currentTimeMillis();
					Collection<String> res = searcher.searchInDocuments(searchString);									
					System.out.println("Search time: "+(System.currentTimeMillis() - startTime)/1000f +"s");
					if(res!=null)
						printCollection(res);
					System.out.println("Enter search string...");
				} catch (IOException e) {			
					e.printStackTrace();
				}
		 }
		 
	}
	
	public static void printCollection(Collection<String> c){
		System.out.println("Seached files: "+ c.size());
		for(String s:c){
			System.out.print(s);
			System.out.print("\t");
		}
		System.out.println();
	}
}
