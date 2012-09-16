import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class TextSearcher {

	private HashMap<String,Integer> wordsToNum;
	private SearchStructure searchStructure ;
	private HashMap<String, TreeSet<Integer>> inverseIndex;
	private boolean[][] incidenceMatrix;
	private ArrayList<String> filesNames;
	
	
	public enum SearchStructure{IncidenceMatrix,InverseIndex};
	
	public void proccessDir(String dir,SearchStructure searchStructure){
		ArrayList<String> files= getTxtFilesFromDir(dir);
		this.filesNames = files;
		this.searchStructure = searchStructure;
		if(searchStructure == SearchStructure.IncidenceMatrix){
			this.wordsToNum = readAllWordsFromFiles(dir,files);			 
			incidenceMatrix = buildIncidenceMatrix(dir, files, wordsToNum);			
		}
		if(searchStructure == SearchStructure.InverseIndex){						 
			inverseIndex = buildInvertIndex(dir, files);			
		}
	}
	
	public Set<String> searchInDocuments(String searchText){
		String[] words = searchText.split("\\W");
		if(words == null || words.length==0)
			return null;
		for(int i =0;i<words.length;i++)
			words[i] = normalizeWord(words[i]);
		HashSet<String> resDocs = new HashSet<String>();
		if(searchStructure == SearchStructure.IncidenceMatrix){
			boolean[] intersection = new boolean[filesNames.size()]; 
			Arrays.fill(intersection, true);
			for(String word:words){
				Integer wordNum = wordsToNum.get(word);
				if(wordNum!=null){
					for(int i =0;i<filesNames.size();i++)
						intersection[i]&=incidenceMatrix[wordNum][i];
				}				
			}
			for(int i =0;i<filesNames.size();i++)
				if(intersection[i])
					resDocs.add(filesNames.get(i));
			return resDocs; 
			
		}else if(searchStructure == SearchStructure.InverseIndex){
			TreeSet<Integer> intersection = null;
			int i =0;
			while(i<words.length && (intersection = inverseIndex.get(words[i++])) == null );
			if(intersection == null)
				return null;
			intersection = new TreeSet<Integer>(intersection);
			
			while(i<words.length){
				TreeSet<Integer> inverseList = inverseIndex.get(words[i++]) ;
				if(inverseList!=null)
					intersection = intersectInverseLists(intersection,inverseList);
			}
			for(Integer docNum:intersection)
			if(docNum!=null){
				resDocs.add(filesNames.get(docNum));
			}
			return resDocs;	
			
		}
		return null;
	}
	
	public int getWordsCount(){
		if(searchStructure == null)
			return 0;
		else if(searchStructure==SearchStructure.IncidenceMatrix)
			return wordsToNum == null?0:wordsToNum.size();
		else if(searchStructure == SearchStructure.InverseIndex)
			return inverseIndex.size();
		return 0;
	}
	
	private TreeSet<Integer> intersectInverseLists(
			TreeSet<Integer> invList1, TreeSet<Integer> invList2) {
		TreeSet<Integer> res = new TreeSet<Integer>();
		Iterator<Integer>it1 =  invList1.iterator();
		Iterator<Integer>it2 =  invList2.iterator();
		int i1 ;
		int i2 ;
		if(it1.hasNext() && it2.hasNext()){
			i1 = it1.next();
			i2 = it1.next();
		}else return res;
		do{			
			if(i1==i2){
				res.add(i1);
				i1 = it1.next();
				i2 = it1.next();
			}else if(i1>i2){
				i2 = it2.next();
			}else
				i1 = it1.next();
		}while(it1.hasNext() && it2.hasNext());
		if(i1==i2)
			res.add(i1);
		return res;
	}

	private static HashMap<String, TreeSet<Integer>> buildInvertIndex(String dir,ArrayList<String> files){
		HashMap<String,TreeSet<Integer>> res = new HashMap<String, TreeSet<Integer>>();
		
		for( int i=0;i<files.size();i++){
			String file = files.get(i);
			FileReader fin = null;
			try{
			 fin= new FileReader(new File(dir,file));
			 proccessFile(fin,res,Integer.valueOf(i));
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
				if(fin!=null)
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
			HashMap<String, TreeSet<Integer>> res, Integer fileNum) throws IOException {
		BufferedReader breader = new BufferedReader(fin);
		String line;
		while((line=breader.readLine())!=null){
			String[] words = line.split("\\W");
			for(String word:words){
				word = normalizeWord(word);
				if(!res.containsKey(word))
					res.put(word, new TreeSet<Integer>());
				TreeSet<Integer> invList = res.get(word);
				invList.add(fileNum);
			}
		}				
	}
	private static HashMap<String,Integer> readAllWordsFromFiles(String dir,ArrayList<String> files){
		TreeSet<String> allWords =new TreeSet<String>();			
		for( int i=0;i<files.size();i++){
			String file = files.get(i);
			FileReader fin = null;
			try{
			 fin= new FileReader(new File(dir,file));
			 readWordsFromFile(new BufferedReader(fin),allWords);
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
				if(fin!=null)
					try {
						fin.close();
					} catch (IOException e) {						
						e.printStackTrace();
					}
			}
		}
		
		HashMap<String,Integer> wordToNum = new HashMap<String, Integer>();
		int i =0;
		for(String w:allWords)
			wordToNum.put(w, i++);
		return wordToNum;
	}
	private static boolean[][] buildIncidenceMatrix(String dir,ArrayList<String> files,HashMap<String,Integer> wordToNum){
			
		int i;		
		boolean[][] incidenceMatrix = new boolean[wordToNum.size()][files.size()];
		for( i=0;i<files.size();i++){
			String file = files.get(i);
			FileReader fin = null;
			try{
			 fin= new FileReader(new File(dir,file));
			 proccessFile(fin,incidenceMatrix,wordToNum,Integer.valueOf(i));
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
				if(fin!=null)
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
			boolean[][] incidenceMatrix,HashMap<String,Integer> wordToNum, Integer fileNum) throws IOException {
		BufferedReader breader = new BufferedReader(fin);
		String line;
		while((line=breader.readLine())!=null){
			String[] words = line.split("\\W");
			for(String word:words){
				word = normalizeWord(word);
				incidenceMatrix[wordToNum.get(word)][fileNum] = true;
			}
		}				
	}


	private static ArrayList<String> getTxtFilesFromDir(String dir){
		File dirFile = new File(dir);
		ArrayList<String> res = new ArrayList<String>();
		FilenameFilter filter = new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(".txt"))
				return true;
				else return false;
			}
		};
		if(dirFile.isDirectory())
		for(File f:dirFile.listFiles(filter)){
			res.add(f.getName());
		}
		return res;
	}
	
	private static void readWordsFromFile(BufferedReader fileReader,TreeSet<String> words) throws IOException{
		String line;
		while((line=fileReader.readLine())!=null){
			String[] lineWords = line.split("\\W");
			for(String word:lineWords){
				word = normalizeWord(word);
				words.add(word);
			}
		}
	}
	
	private static String normalizeWord(String word){
		return word.toLowerCase();
	}

	public int getFilesNum() {
		// TODO Auto-generated method stub
		return filesNames == null?0:filesNames.size();
	}
	
}