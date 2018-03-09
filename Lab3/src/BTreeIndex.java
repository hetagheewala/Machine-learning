import java.util.*;

public class BTreeIndex {
	String[] myDocs;
	BinaryTree termList;
	BTNode root;
	
	/**
	 * Construct binary search tree to store the term dictionary 
	 * @param docs List of input strings
	 * 
	 */
	public BTreeIndex(String[] docs)
	{
		myDocs = docs;
		termList = new BinaryTree();
		
		ArrayList<String> allTokens = new ArrayList<>();
		String[] tempTokens;
		
		//Define root node for balanced tree
		for(int i=0;i<myDocs.length;i++) {
			tempTokens = myDocs[i].split(" "); 
			
			for(int j=0;j<tempTokens.length;j++) {
				if(!allTokens.contains(tempTokens[j])){
					allTokens.add(tempTokens[j]);
				}
			}
		}
		
		Collections.sort(allTokens);
		
		int start = 0;
		int end = allTokens.size() -1;
		int mid = (start+end)/2 ;
		
		root = new BTNode(allTokens.get(mid), null);
		
		//Create tree
		for(int t=0;t<allTokens.size();t++) {
			BTNode iNode = new BTNode(allTokens.get(t),null);
			termList.add(root, iNode);
		}
		
		System.out.println("Building a tree with root term : " + root.term + "\n");
		
		for(int i=0;i<myDocs.length;i++) {
			String[] tokens = myDocs[i].split(" ");
			
			for(String token:tokens) {
				ArrayList<Integer> docList = new ArrayList<>();
				docList.add(new Integer(i));
				BTNode node = new BTNode(token, docList);
				termList.add(root, node);
			}
		}
		
		termList.printInOrder(root);
		termList.wildCardSearch(root, "f*");
	}
	
	/**
	 * Single keyword search
	 * @param query the query string
	 * @return doclists that contain the term
	 */
	
	public ArrayList<Integer> search(String query)
	{
			BTNode node = termList.search(root, query);
			if(node==null)
				return null;
			return node.docLists;
	}
	
	/**
	 * conjunctive query search
	 * @param query the set of query terms
	 * @return doclists that contain all the query terms
	 */

	public ArrayList<Integer> search(String[] query)
	{	
		ArrayList<Integer> result = search(query[0]);
		
		int termId = 1;
		while(termId<query.length)
		{
			ArrayList<Integer> result1 = search(query[termId]);
			result = merge(result,result1);
			termId++;
		}		
		return result;
	}

	
	/**
	 * 
	 * @param wildcard the wildcard query, e.g., ho (so that home can be located)
	 * @return a list of ids of documents that contain terms matching the wild card
	 */
	
	public ArrayList<Integer> wildCardSearch(String wildcard)
	{
		ArrayList<Integer> docs = new ArrayList<Integer>();
		termList.matches = new ArrayList<BTNode>();
		ArrayList<BTNode> matches = termList.wildCardSearch(root, wildcard);
	
		for(int i=0; i<matches.size();i++) {
			for(int j=0; j<matches.get(i).docLists.size();j++) {
				if(!docs.contains(matches.get(i).docLists.get(j))) {
					docs.add(new Integer(matches.get(i).docLists.get(j)));
				}
			}
		}

		return docs;
	}
	
	
	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			if(l1.get(id1).intValue()==l2.get(id2).intValue()){
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			}
			else if(l1.get(id1)<l2.get(id2))
				id1++;
			else
				id2++;
		}
		return mergedList;
	}
	
	
	/**
	 * Test cases
	 * @param args commandline input
	 */
	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
						 "home sales rise in july",
						 "increase in home sales in july",
						 "july new home sales rise"
						};
		
		BTreeIndex btn = new BTreeIndex(docs);
		
		
		ArrayList<Integer> searchforecasts = btn.search("forecasts");
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for query (case1 - Single word): forecasts \n");
		
		if(searchforecasts != null && !searchforecasts.isEmpty()) {
			for(Integer i : searchforecasts)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		
		String[] strsalesin = {"sales","in"};
		
		ArrayList<Integer> searchsalesin = btn.search(strsalesin);
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for query (case2 - multi word): sales in \n");
		
		if(searchsalesin != null && !searchsalesin.isEmpty()) {
			for(Integer i : searchsalesin)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		String[] strsalesinforecasts = {"sales","in","forecasts"};
		
		ArrayList<Integer> searchsalesinforecasts = btn.search(strsalesinforecasts);
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for query (case3 - multi word): sales in forecasts  \n");
		
		if(searchsalesinforecasts != null && !searchsalesinforecasts.isEmpty()) {
			for(Integer i : searchsalesinforecasts)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for wildcard query (case1): ho \n");
		
		ArrayList<Integer> wildcardho = btn.wildCardSearch("ho");
		
		if(wildcardho != null && !wildcardho.isEmpty()) {
			for(Integer i : wildcardho)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for wildcard query(case2): inc \n");
		
		ArrayList<Integer> wildcardinc = btn.wildCardSearch("inc");
		
		if(wildcardinc != null && !wildcardinc.isEmpty()) {
			for(Integer i : wildcardinc)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for wildcard query(case3): jul \n");
		
		ArrayList<Integer> wildcardjul = btn.wildCardSearch("jul");
		
		if(wildcardjul != null && !wildcardjul.isEmpty()) {
			for(Integer i : wildcardjul)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		
		System.out.println("\n=================================================\n");
		System.out.println("Documents for wildcard query(case4): d \n");
		
		ArrayList<Integer> wildcardd = btn.wildCardSearch("d");
		
		if(wildcardd != null && !wildcardd.isEmpty()) {
			for(Integer i : wildcardd)
				System.out.println(docs[i]);
		}else 
				System.out.println("No Match Found!!");	
		
		
	}
}