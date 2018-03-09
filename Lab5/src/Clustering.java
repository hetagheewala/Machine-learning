import java.util.*;

/**
 * Document clustering
 */

public class Clustering {
		
	String[] myDocs;
	ArrayList<Doc> docLists;
	HashMap<String, Integer> termList;
	ArrayList<Doc>[] clusterLists;
	double[] docLength;
	int numClusters;
	
	/**
	 * Constructor for attribute initialization
	 * @param numC number of clusters
	 */
	
	public Clustering(int numC)
	{
		numClusters = numC;
		clusterLists = new ArrayList[numClusters];
		for(int i=0;i<numClusters;i++)
			clusterLists[i] = new ArrayList<Doc>(); 	
	}
	
	/**
	 * Load the documents to build the vector representations
	 * @param docs
	 */
	
	public void preprocess(String[] docs){
		myDocs = docs;
		termList = new HashMap<String,Integer>();
		docLists = new ArrayList<Doc>();
		
		int termId = 0;
		
		System.out.println("\n************* DocID: <TermID => Term Frequecy> *************\n");
		
		for(int i=0; i<myDocs.length;i++) {
			String[] tokens = myDocs[i].split(" ");
			String token;
			Doc doc = new Doc(i);
			
			for(int j=0;j<tokens.length;j++) {
				token = tokens[j];
				
				if(!termList.containsKey(token)) {
					termList.put(token,termId);
					doc.termMap.put(termId, 1.0);
					termId++;
				}
				else {
					int tId = termList.get(token);
					if(doc.termMap.get(tId) != null) {
						Double tf = doc.termMap.get(tId); 
						doc.termMap.put(tId, tf+1);
					}
					else {
						doc.termMap.put(tId, 1.0);
					}
				}
			}
			System.out.println(doc);
			docLists.add(doc);
		}
		
		
		//Calculate document vector
		
		int N = myDocs.length;
		docLength = new double[N];
		
		for(int i=0;i<docLists.size();i++) {
			Doc doc = docLists.get(i);
			double[] termVector = new double[termList.size()]; 
			
			for(int k : doc.termMap.keySet()) {
				double tf = 1 + Math.log(doc.termMap.get(k));
				docLength[doc.docId] +=Math.pow(tf, 2);
				doc.termMap.put(k,tf);
				docLists.set(i,doc);
				termVector[k] = tf;
			}
			doc.termVector = termVector;
						
			//update the doc lengths
			for(int j=0;j<N;j++) {
				docLength[j] = Math.sqrt(docLength[j]); 
			}
		}
	}
	
	/**
	 * Cluster the documents
	 * For kmeans clustering, use the first and the ninth documents as the initial centroids
	 */
	
	public void cluster(){
		double[] centroid1 = docLists.get(0).termVector;
		double[] centroid2 = docLists.get(8).termVector;
		int iterator = 1;
		
		System.out.println("\n\n");
		
		while(true) {
			
			for(int i=0;i<numClusters;i++)
				clusterLists[i] = new ArrayList<Doc>(); 
			
			double[] similarityC1 = new double[termList.size()];
			double[] similarityC2 = new double[termList.size()];
			
			for(int i=0;i<docLists.size();i++) {
				similarityC1[i] = cosineSimilarity(centroid1, docLists.get(i).termVector)/docLength[i];
				//System.out.println("C1 & Doc " + i +":"+similarityC1[i]);
				
				similarityC2[i] = cosineSimilarity(centroid2, docLists.get(i).termVector)/docLength[i];
				//System.out.println("C2 & Doc " + i +":"+similarityC2[i]);
				
				if(similarityC1[i] < similarityC2[i]) 
					clusterLists[1].add(docLists.get(i));
				else
					clusterLists[0].add(docLists.get(i));
			}
			
			System.out.println("************* Iteratortion: " + iterator + " *************");
			
			System.out.println("Cluster 0");
			for(int j=0;j<clusterLists[0].size();j++) {
				System.out.println(clusterLists[0].get(j).docId);
			}
			
			System.out.println("Cluster 1");
			for(int j=0;j<clusterLists[1].size();j++) {
				System.out.println(clusterLists[1].get(j).docId);
			}
			
			double[] oldc1 = new double[termList.size()];
			double[] oldc2 = new double[termList.size()];
			
			oldc1 = centroid1;
			oldc2 = centroid2;
			 
			centroid1 = calculateCentroid(clusterLists[0]);
			centroid2 = calculateCentroid(clusterLists[1]);
			
			iterator++;
			
			//Stop condition
			if(Arrays.equals(centroid1, oldc1) && Arrays.equals(centroid2, oldc2))
				break;
		}
	}	    
	
	public double[] calculateCentroid(ArrayList<Doc> cluster) {
		double[] centroid = new double[termList.size()];
		
		for(int i=0;i<cluster.size();i++) {
			for(int j=0;j<termList.size();j++) {
				centroid[j] +=cluster.get(i).termVector[j]/cluster.size();
			}
		}
	
		return centroid;
	}
	
	public double cosineSimilarity(double[] centroid, double[] doc) {
		double dotProduct = 0.0;
  
	    for (int i=0;i<centroid.length;i++)
	    	dotProduct += (centroid[i] * doc[i]);
	    
	    return  dotProduct;
	}
	
	public static void main(String[] args){
		String[] docs = {"hot chocolate cocoa beans",
				 "cocoa ghana africa",
				 "beans harvest ghana",
				 "cocoa butter",
				 "butter truffles",
				 "sweet chocolate can",
				 "brazil sweet sugar can",
				 "suger can brazil",
				 "sweet cake icing",
				 "cake black forest"
				};
		
		Clustering c = new Clustering(2);
		
		c.preprocess(docs);

		c.cluster();
		
		/*
		 * Expected result:
		 * Cluster: 0
			0	1	2	3	4	
		   Cluster: 1
			5	6	7	8	9	
		 */
	}
}

/**
 * Document class for the vector representation of a document
 */

class Doc{
    int docId;
    HashMap<Integer, Double> termMap;
    double[] termVector;
    
    public Doc(int id) {
        docId = id;
        termMap = new HashMap<Integer, Double>();
    }
    
    public void setTermVector(double[] vector) {
    	termVector = vector;
    }
    
    public String toString() {
    	String docIdString = "" + docId + ":<";
    	
    	for (Map.Entry<Integer, Double> entry : termMap.entrySet()) 
    	    docIdString += entry.getKey()+"=>"+entry.getValue() + ",";
    	docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
		
		return docIdString;		
	}
}
