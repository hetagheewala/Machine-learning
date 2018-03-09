import java.util.*;
import java.io.*;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

public class Parser {

	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<String> stopwordList;
	String[] tokens = null;
	ArrayList<ArrayList<Integer>> docLists;
	
	//Constructor    
	public Parser(String stopwordsFile,String fileFolder) {
		
		//Generates stop words arrayList  and sort
		stopwordList = stopwordParser(stopwordsFile);
		Collections.sort(stopwordList);
		
		//Gets files from given folder
		File folder = new File(fileFolder);
		File[] files = folder.listFiles();
		myDocs = new String[files.length];
						
		System.out.println("Documents in folder:");
		for(int i=0;i<files.length;i++) {
			myDocs[i] = files[i].getName();
			System.out.println("DocId: "+ i + " DocName: " + myDocs[i]);	
		}
		System.out.println("\nToken Matrix: \n");
				
		//Extract tokens from each document
		tokens = null;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList;
				
		try {
			for(int i=0; i<myDocs.length;i++) {
				BufferedReader reader = new BufferedReader(new FileReader(fileFolder + "/" + myDocs[i]));
				String allLines = new String();
				String line = null;
						
				while((line = reader.readLine()) != null) {
					allLines += line.toLowerCase();
				}
				tokens = allLines.split("[ .,?!:;$%#/&*+()\\-\\^\"]+");
						
				for(String token:tokens) {
					if(!stopwordList.contains(token)) {
					
						//Stemming tokens
						Stemmer st = new Stemmer();
						st.add(token.toCharArray(),token.length());
						st.stem();
						token = st.toString();
						
						if(!termList.contains(token)) {
							termList.add(token);
							docList = new ArrayList<Integer>();
							docList.add(new Integer(i));
							docLists.add(docList);
						}else {
							int index = termList.indexOf(token);
							docList = docLists.get(index);
							if(!docList.contains(new Integer(i))) {
								docList.add(new Integer(i));
								docLists.set(index,docList);
							}
						}
					}
				}
			}	
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	//Stopword parser
	public ArrayList<String> stopwordParser(String filename) {
			
		ArrayList<String> parsedStopwords = new ArrayList<String>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename)); 
			String word = null;
			
			while((word = reader.readLine()) != null) {
				parsedStopwords.add(word.toLowerCase());
			}
	
		}catch(Exception ioe) {
			ioe.printStackTrace();
		}
		
		System.out.println("Stopword list: \n" + parsedStopwords + "\n");
		return parsedStopwords;
	}
	
	//Stemmer for query terms
	public String termStemmer(String term) {
		Stemmer st = new Stemmer();
		st.add(term.toCharArray(),term.length());
		st.stem();
		return st.toString();
	}
	
	//Single word
	public ArrayList<Integer> singleWordSearch(String query){
		query = termStemmer(query.toLowerCase());
		int index = termList.indexOf(query);
		
		if(index < 0)
			return null;
		
		return docLists.get(index);
	}
	
	//Two words with AND
	public ArrayList<Integer> twoANDWordsSearch(String queries){
		String[] query = queries.split(" ");		
		ArrayList<Integer> result = singleWordSearch(query[0]);
		
		if(result != null) {
			ArrayList<Integer> result1 = singleWordSearch(query[1]);
			if(result1 != null)
				result = mergeANDList(result, result1);
			else
				return null;
		}
		else
			return null;
		
		return result;
	}
	
	//Two words with OR
	public ArrayList<Integer> twoORWordsSearch(String queries){
		String[] query = queries.split(" ");
		ArrayList<Integer> result = singleWordSearch(query[0]);

		if(result != null) {
			ArrayList<Integer> result1 = singleWordSearch(query[1]);
			if(result1 != null)
				result = mergeORList(result, result1);
			else
				return result;
		}
		else {
			ArrayList<Integer> result1 = singleWordSearch(query[1]);
			if(result1 != null)
				return result1;
			else
				return null;
		}
		
		return result;
	}
	
	//Three or more words
	public ArrayList<Integer> moreWordsSearch(String queries){
		String[] query = queries.split(" ");
		
		//hashmap to store query terms and postings 
		Map<String, ArrayList<Integer>> sortList = new HashMap<String, ArrayList<Integer>>();
		int id = 0;
		while(id < query.length) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			list = singleWordSearch(query[id]);
			if(list != null) {
				sortList.put(query[id], list);
				id++;
			}else
				return null;
		}
		
		//hashmap sorting based on posting length
		Map<String, List<Integer>> sorted = sortList.entrySet().stream()
        .sorted(comparingInt(e->e.getValue().size()))
        .collect(toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a,b) -> {throw new AssertionError();},
                LinkedHashMap::new
        ));
		
		//creating string to display term merge order based on posting list length
	    ArrayList<Integer> result = new ArrayList<Integer>(); 
	    String termOrder = new String();
	    int i=1;
	    
	    System.out.println("\nTermList posting order:: \n");
	    for (String key : sorted.keySet()) {
			termOrder = termOrder + "\n" + i + ". " + key + "\n";
			i++;
		}
		System.out.println(termOrder);
		
		//merging postings based on sorted first
		for (List<Integer> value : sorted.values()) {
			if(result.isEmpty())
				result.addAll(value);
			else {
				ArrayList<Integer> result1 = new ArrayList<Integer>();
				result1.addAll(value);	
				result = mergeANDList(result,result1);
				}
		}
		return result;	
	}
	
	//Merge for AND
	public ArrayList<Integer> mergeANDList(ArrayList<Integer> l1, ArrayList<Integer> l2){
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2 = 0;

		while(id1 < l1.size() && id2 < l2.size()) {			
			if(l1.get(id1).intValue() == l2.get(id2)) {
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			} else if(l1.get(id1) < l2.get(id2))
				id1++;
			else
				id2++;	
		}
		return mergedList;
	}
	
	//Merge for OR
	public ArrayList<Integer> mergeORList(ArrayList<Integer> l1, ArrayList<Integer> l2){
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		
		mergedList.addAll(l1);
		
		int id = 0;
		while(id < l2.size()) {
			if(!l1.contains(l2.get(id))) {
				mergedList.add(l2.get(id));
			}
			id++;
		}
		
		return mergedList;
	}
	
	//String Matrix 
	public String toString() {
		String matrixString = new String();
		ArrayList<Integer> docList;
		
		for(int i=0;i<termList.size();i++) {
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			
			for(int j=0;j<docList.size();j++) 
				matrixString +=docList.get(j) + "\t";
			
			matrixString += "\n";
		}
		return matrixString;
	}
	
	//Test for single word
	public void testSingleWord() {
		System.out.println("Single word query cases::\n");
		
		//Case 1
		ArrayList<Integer> singlewordCase1 = singleWordSearch("Drink");
				
		System.out.println("Document name for single word query (case1): Drink");
				
		if(singlewordCase1 != null) {
			for(Integer i : singlewordCase1)
				System.out.println(myDocs[i]);
		}else
			System.out.println("No Match Found!!");
				
		//Case 2
		ArrayList<Integer> singlewordCase2 = singleWordSearch("fact");
		
		System.out.println("Document name for single word query (case2): fact");
				
		if(singlewordCase2 != null) {
			for(Integer i : singlewordCase2)
				System.out.println(myDocs[i]);
		}else
			System.out.println("No Match Found!!");
	}
	
	//Test for two words - AND
	public void testTwoWordsWithAND() {
		System.out.println("\nTwo words AND query cases::\n");
		
		//Case 1
		ArrayList<Integer> twowordANDCase1 = twoANDWordsSearch("watch Movie");
		
		System.out.println("Document name for two words AND query (case1): watch Movie");
		
		if(twowordANDCase1 != null) {
			for(Integer i : twowordANDCase1)
				System.out.println(myDocs[i]);
		}else 
			System.out.println("No Match Found!!");
		
		//Case 2
		ArrayList<Integer> twowordANDCase2 = twoANDWordsSearch("Information Technology");
		
		System.out.println("Document name for two words AND query (case2): Information Technology");
		
		if(twowordANDCase2 != null) {
			for(Integer i : twowordANDCase2)
				System.out.println(myDocs[i]);
		}else 
			System.out.println("No Match Found!!");
	}
	
	//Test for two words  - OR
	public void testTwoWordsWithOR() {
		System.out.println("\nTwo words OR query cases::\n");
		
		//Case 1
		ArrayList<Integer> twowordORCase1 = twoORWordsSearch("watch movie");
				
		System.out.println("Document name for two words OR query (case1): watch Movie");
				
		if(twowordORCase1 != null) {
			for(Integer i : twowordORCase1)
				System.out.println(myDocs[i]);
		}else 
			System.out.println("No Match Found!!");
		
		//case 2
		ArrayList<Integer> twowordORCase2 = twoORWordsSearch("Information Technology");
		
		System.out.println("Document name for two words OR query (case2): Information Technology");
				
		if(twowordORCase2 != null) {
			for(Integer i : twowordORCase2)
				System.out.println(myDocs[i]);
		}else 
			System.out.println("No Match Found!!");
	}
	
	//Test for three or more - AND
	public void testThreeOrMore() {
		System.out.println("\nThree or more words AND query cases::");
		
		//Case 1
		ArrayList<Integer> morewordCase1 = moreWordsSearch("weird things happen");
					
		System.out.println("Document name for three or more words AND query (case1): weird things happen");
					
		if(morewordCase1 != null) {
			for(Integer i : morewordCase1)
				System.out.println(myDocs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		//Case 2
		ArrayList<Integer> morewordCase2 = moreWordsSearch("Rochester Institute Technology");
		
		System.out.println("Document name for three or more words AND query (case2): Rochester Institute Technology");
					
		if(morewordCase2 != null) {
			for(Integer i : morewordCase2)
				System.out.println(myDocs[i]);
		}else 
				System.out.println("No Match Found!!");	
	}
	
	//Main method
	public static void main(String[] args) {	
		Parser p = new Parser("stopwords.txt", "Lab1_Data");
		System.out.println(p);
		
		p.testSingleWord();
		p.testTwoWordsWithAND();
		p.testTwoWordsWithOR();
		p.testThreeOrMore();
	}
}
