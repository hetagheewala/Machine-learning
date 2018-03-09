import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NBClassifier {

	HashMap<String,Integer> trainingDocs;
	int[] trainingLables;
	int numClasses;
	int[] classCounts;
	String[] classStrings;
	int[] classTokenCounts;
	HashMap<String,Double>[] condProb;
	HashSet<String> vocabulary;
	private BufferedReader reader;
	
	/**
	 * Build a Naive Bayes classifier using a training document set
	 * @param trainDataFolder the training document folder
	 */
	
	public NBClassifier(String trainDataFolder)
	{
		numClasses = 2;
		trainingDocs = new HashMap<String,Integer>();
		classCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<String>();
		
		preprocess(trainDataFolder);
		
		for(int i=0;i<numClasses;i++) {
			classStrings [i ]= "";
			condProb[i] = new HashMap<String,Double>();
		}
		
		Set<String> keys = trainingDocs.keySet();
		for(String key:keys) {
			classCounts[trainingDocs.get(key)]++;
			String allLines = new String();
		
			try {
				if(trainingDocs.get(key) == 0) {
					reader = new BufferedReader(new FileReader(trainDataFolder + "/neg/" + key));
				}else {
					reader = new BufferedReader(new FileReader(trainDataFolder + "/pos/" + key));
				}
				String line = null;
				
				while((line = reader.readLine()) != null) {
					allLines += line.toLowerCase();
				}
			
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			classStrings[trainingDocs.get(key)] += (allLines + " ");
		}
		
		for(int i=0;i<numClasses;i++) {
			String[] tokens = classStrings[i].split("[ .,?!:;$%#/&*+()\\-\\^\"]+");
			classTokenCounts[i] = tokens.length;
				
			for(String token:tokens) {
				vocabulary.add(token);
				
				if(condProb[i].containsKey(token)){
					double count = condProb[i].get(token);
					condProb[i].put(token, count+1); 
				}
				else
					condProb[i].put(token, 1.0); 
			}
		}
		
		for(int i=0;i<numClasses;i++){
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vSize = vocabulary.size();
			while(iterator.hasNext())
			{
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				count = (count+1)/(classTokenCounts[i]+vSize); 
				condProb[i].put(token, count);
			}
		}
	}
	
	/**
	 * Load the training documents
	 * @param trainDataFolder
	 */
	
	public void preprocess(String trainDataFolder)
	{
		File trainFolder = new File(trainDataFolder);
		
		File[] trainFileList = trainFolder.listFiles();
		for(File trainFile : trainFileList) {
			if(trainFile.isDirectory()) {
				if(trainFile.getName().equals("pos")) {
					
					String path = trainFile.getAbsolutePath();
					File posFolder = new File(path);
					File[] posTrainFiles = posFolder.listFiles();
				
					for(int i=0;i< posTrainFiles.length;i++) {
						trainingDocs.put(posTrainFiles[i].getName(), 1);
					}	
				}else if(trainFile.getName().equals("neg")) {
					String path = trainFile.getAbsolutePath();
					File negFolder = new File(path);
					File[] negTrainFiles = negFolder.listFiles();
				
					for(int i=0;i< negTrainFiles.length;i++) {
						trainingDocs.put(negTrainFiles[i].getName(), 0);
					}	
				}
			}	
		}
	}
	
	/**
	 * Classify a test doc
	 * @param doc test doc
	 * @return class label
	 */
	
	public int classify(String doc){
		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];
		for(int i=0;i<score.length;i++){
			score[i] = Math.log(classCounts[i]*1.0/trainingDocs.size());
		}
		
		try {
			reader = new BufferedReader(new FileReader(doc));
			String allLines = new String();
			String line = null;
					
			while((line = reader.readLine()) != null) {
				allLines += line.toLowerCase();
			}
			
			String[] tokens = allLines.split("[ .,?!:;$%#/&*+()\\-\\^\"]+");
			
			for(int i=0;i<numClasses;i++){
				for(String token: tokens){
					if(condProb[i].containsKey(token))
						score[i] += Math.log(condProb[i].get(token));
					else
						score[i] += Math.log(1.0/(classTokenCounts[i]+vSize)); 
				}
			}
		
			double maxScore = score[0];
			for(int i=0;i<score.length;i++){
				if(score[i]>maxScore)
					label = i;
			}
			
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		return label;
	}
	
	
	/**
	 *  Classify a set of testing documents and report the accuracy
	 * @param testDataFolder fold that contains the testing documents
	 * @return classification accuracy
	 */
	
	public double classifyAll(String testDataFolder)
	{
		int countCorrect = 0;
		int countTotal = 0;
		double accuracy = 0.000;
		
		File testFolder = new File(testDataFolder);
		
		File[] testFileList = testFolder.listFiles();
		for(File testFile : testFileList) {
			if(testFile.isDirectory()) {
				if(testFile.getName().equals("pos")) {
					
					String path = testFile.getAbsolutePath();
					File posFolder = new File(path);
					File[] posTestFiles = posFolder.listFiles();
				
					for(int i=0;i< posTestFiles.length;i++) {
						int label = classify("test/pos/"+posTestFiles[i].getName());
						
						if(label == 1)
							countCorrect += 1;
						countTotal += 1;
					}	
				}else if(testFile.getName().equals("neg")) {
					
					String path = testFile.getAbsolutePath();
					File posFolder = new File(path);
					File[] posTrainFiles = posFolder.listFiles();
				
					for(int i=0;i< posTrainFiles.length;i++) {
						int label = classify("test/neg/"+posTrainFiles[i].getName());
						
						if(label == 0)
							countCorrect += 1;
						countTotal += 1;
					}
				}
			}
		}
	
		System.out.println("Correctly classified " + countCorrect + " out of " + countTotal);
		accuracy = (countCorrect * 1.000) / (countTotal * 1.000);
		return accuracy;
	}
	
	
	public static void main(String[] args)
	{		
		NBClassifier nbc = new NBClassifier("train");
		
		//here 0 = neg and 1 = pos class
		
		System.out.println("Assigned class to test/neg/cv900_10800.txt is " + nbc.classify("test/neg/cv900_10800.txt") + "\n"); 
		System.out.println("Assigned class to test/pos/cv927_10681.txt is " + nbc.classify("test/pos/cv927_10681.txt") + "\n");
		
		System.out.println("Accuracy: " + nbc.classifyAll("test"));
	}
}
