import java.util.ArrayList;

public class PositionalIndex {

	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocId>> docLists;
	
	//Task 1 : Constructor
	public PositionalIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocId>>();
		
		ArrayList<DocId> docList;
		
		for(int i=0;i<myDocs.length;i++) {
			String[] tokens = myDocs[i].split(" ");
			String token;
			
			for(int j=0;j<tokens.length;j++) {
				token = tokens[j];
				
				if(!termList.contains(token)) {
					termList.add(token);
					docList = new ArrayList<DocId>();
					DocId doid = new DocId(i,j);
					docList.add(doid);
					docLists.add(docList);
				}
				else {
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					int k=0;
					boolean match = false;
					
					for(DocId doid:docList) {
						if(doid.docId==i) {
							doid.insertPosition(j);
							docList.set(k, doid);
							match = true;
							break;
						}
						k++;
					}
					
					if(!match) {
						DocId doid = new DocId(i,j);
						docList.add(doid);
					}
				}
			}
		}
	}
	
	public String toString()
	{
		String matrixString = new String();
		ArrayList<DocId> docList;
		for(int i=0;i<termList.size();i++){
				matrixString += String.format("%-15s", termList.get(i));
				docList = docLists.get(i);
				for(int j=0;j<docList.size();j++)
				{
					matrixString += docList.get(j)+ "\t";
				}
				matrixString += "\n";
			}
		return matrixString;
	}
	
	//Task 2 : intersect method
	public ArrayList<DocId> intersect(ArrayList<DocId> l1, ArrayList<DocId> l2)
	{
		ArrayList<DocId> mergedList = new ArrayList<DocId>();
		int id1=0, id2 = 0;
	
		while(id1<l1.size() && id2<l2.size()) {
			if(l1.get(id1).docId == l2.get(id2).docId) {
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
			
				boolean newDoc = true;
				int pid1=0, pid2=0;
				
				while(pid1<pp1.size()) {
					while(pid2 < pp2.size()) {
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2)) <= 1) {
							for(DocId tdoid:mergedList) {
								if(tdoid.docId == l1.get(id1).docId) {
									tdoid.insertPosition(pp2.get(pid2));
									mergedList.set(l1.get(id1).docId, tdoid);
									newDoc = false;
								}
							}
							if(newDoc) {
								DocId ntdoid = new DocId(l1.get(id1).docId,pp2.get(pid2));
								mergedList.add(ntdoid);
							}
						}
						else if(pp2.get(pid2) > pp1.get(pid1))
							break;
						pid2++;
					}
					pid1++;
				}
				id1++;
				id2++;
			}
			else if (l1.get(id1).docId < l2.get(id2).docId) 
				id1++;
			else
				id2++;
		}
		return mergedList;
	}
	
	//Phrase query for multiple words
	public ArrayList<DocId> phraseQuery(String[] query)
	{
		ArrayList<DocId> result = new ArrayList<DocId>();
		ArrayList<DocId> result1 = new ArrayList<DocId>();
		
		if(termList.contains(query[0])) {
			result = docLists.get(termList.indexOf(query[0]));
			
			for(int i=1;i<query.length;i++) {
				
				if(termList.contains(query[i])) {
					result1 = docLists.get(termList.indexOf(query[i]));
					
					if(result != null && result1 != null) {
						result = intersect(result, result1);
					}
					else
						return null;
				}else 
					return null;
			}	
		} else
			return null;
		
		return result;
	}

	//Function for query output 
	public void convertPhraseQuery(String query, String[] docs) {
	
		String lowerStr = query.toLowerCase();
		String[] queryArr = lowerStr.split(" ");
		ArrayList<DocId> result = phraseQuery(queryArr);
		
		System.out.println("\nPhrase query '" + query + "' result:\n");
	
		if(result.size() != 0) {
			
			for (int i=0;i<result.size();i++) {
				System.out.println(docs[result.get(i).docId]);
			}
		}
		else
			System.out.println("No Match !!");
		
		System.out.println("----------------------------------------------------");
	}
	
	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
						"home sales rise in july",
						"increase in home sales in july",
						"july new home sales rise"};

		PositionalIndex pi = new PositionalIndex(docs);
		System.out.println("----------------------------------------------------");
		System.out.println("Positional Index\n");
		System.out.print(pi);
		System.out.println("----------------------------------------------------");
		
		//TASK4: design and test phrase queries with 2-5 terms
	
		String searchtwoterms = "forecasts july";
		pi.convertPhraseQuery(searchtwoterms,docs);
		
		String searchthreeterms = "new home sales";
		pi.convertPhraseQuery(searchthreeterms,docs);
		
		String searchfourterms = "rise in july sales";
		pi.convertPhraseQuery(searchfourterms,docs);
		
		String searchfiveterms = "new home sales top forecasts";
		pi.convertPhraseQuery(searchfiveterms,docs);
	}
}


class DocId{
	int docId;
	ArrayList<Integer> positionList;

	public DocId(int did)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
	}
	public DocId(int did, int position)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
		positionList.add(new Integer(position));
	}
	
	public void insertPosition(int position)
	{
		positionList.add(new Integer(position));
	}
	
	public String toString()
	{
		String docIdString = ""+docId + ":<";
		for(Integer pos:positionList)
			docIdString += pos + ",";
		docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
		return docIdString;		
	}
}

