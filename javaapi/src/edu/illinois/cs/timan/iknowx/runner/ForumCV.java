package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.NGramFeatures;
import edu.illinois.cs.timan.iknowx.features.PatternFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class ForumCV extends CV {

	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		
		int numClasses = 4;
		int k = 8;
		List<List<ForumThread>> folds = getFoldsByForum(threads);
		
		PrintWriter pw = new PrintWriter(Constants.forumOut);

		Map<String, Double> pPreds = new HashMap<String, Double>();
		Map<String, Double> pGolds = new HashMap<String, Double>();
		
		for (int fold = 0; fold < folds.size(); ++fold) {
			List<ForumThread> trainSet = getPatternTrainSet(folds, fold);
			List<ForumThread> testSet = folds.get(fold);
			
			List<PatternFeatures> pFeatures = CombinedFeatures.initPatternFeatures(trainSet, k);
			CombinedFeatures.saveFeatures(trainSet, pFeatures, Constants.fourClassTrain + fold, Constants.postJsonDir);
			List<String> testTids = CombinedFeatures.saveFeatures(testSet, pFeatures, Constants.fourClassTest + fold, Constants.postJsonDir);

			SVMTrainer.train(Constants.fourClassTrain + fold, Constants.fourClassModel, 1000, 0);
			List<Double> testPreds = SVMPredictor.predict(Constants.fourClassTest + fold, Constants.fourClassModel);
			List<Double> testGolds = SVMPredictor.getGolds(Constants.fourClassTest + fold);
			
			for (int i = 0; i < testTids.size(); ++i) {
				pPreds.put(testTids.get(i), testPreds.get(i));
				pGolds.put(testTids.get(i), testGolds.get(i));
			}
		}
		
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		m.put(0, 0);
		m.put(1, 0);
		m.put(2, 0);
		m.put(4, 0);
		for (ForumThread thread : threads) {
			String tid = thread.getId();
			int label = Constants.label2id.get(thread.getLabel());
			if (m.containsKey(label) && pPreds.containsKey(tid)) {
				m.put(label, m.get(label)+1);
			}
		}
		for (int label : m.keySet()) {
			System.out.println(label + "\t" + m.get(label));
		}

		//List<ForumThread> remThreads = getRemThreads(threads, pPreds.keySet());
		//List<List<ForumThread>> remFolds = getFoldsByForum(remThreads);
		/*Map<String, Double> wPreds = new HashMap<String, Double>();
		Map<String, Double> wGolds = new HashMap<String, Double>();

		for (int fold = 0; fold < folds.size(); ++fold) {
			List<ForumThread> trainSet = getTrainSet(folds, fold); // train on 4 classes (all classes except combo)
			List<ForumThread> testSet = getRemThreads(folds.get(fold), pPreds.keySet()); // get unclassified threads from pattern classifier
			
			List<NGramFeatures> nFeatures = CombinedFeatures.initNGramFeatures(trainSet);
			CombinedFeatures.saveFeatures(trainSet, nFeatures, Constants.fourClassTrain + fold, Constants.postJsonDir);
			List<String> testTids = CombinedFeatures.saveFeatures(testSet, nFeatures, Constants.fourClassTest + fold, Constants.postJsonDir);

			SVMTrainer.train(Constants.fourClassTrain + fold, Constants.fourClassModel, 1000, 0);
			List<Double> testPreds = SVMPredictor.predict(Constants.fourClassTest + fold, Constants.fourClassModel);
			List<Double> testGolds = SVMPredictor.getGolds(Constants.fourClassTest + fold);
			
			for (int i = 0; i < testTids.size(); ++i) {
				wPreds.put(testTids.get(i), testPreds.get(i));
				wGolds.put(testTids.get(i), testGolds.get(i));
			}
		}

		int pCorrect = getNumCorrect(threads, pPreds, pGolds);
		int[][] pMatrix = getMatrix(threads, pPreds, pGolds, numClasses);
		double[] pPrecision = getPrecision(pMatrix, numClasses);
		double[] pRecall = getRecall(pMatrix, numClasses);
		double[] pF1 = getF1(pPrecision, pRecall, numClasses);

		pw.println("Pattern Correct " + pCorrect);
		pw.println("Pattern Total " + pGolds.size());
		printMatrix(pMatrix, pw);
		print(pPrecision, pw, "Pattern Precision: ");
		print(pRecall, pw, "Pattern Recall: ");
		print(pF1, pw, "Pattern F1: ");
		
		int wCorrect = getNumCorrect(threads, wPreds, wGolds);
		int[][] wMatrix = getMatrix(threads, wPreds, wGolds, numClasses);
		double[] wPrecision = getPrecision(wMatrix, numClasses);
		double[] wRecall = getRecall(wMatrix, numClasses);
		double[] wF1 = getF1(wPrecision, wRecall, numClasses);		
		
		pw.println("Word Correct " + wCorrect);
		pw.println("Word Total " + wGolds.size());
		printMatrix(wMatrix, pw);
		print(wPrecision, pw, "Word Precision: ");
		print(wRecall, pw, "Word Recall: ");
		print(wF1, pw, "Word F1: ");
		
		int correct = pCorrect + wCorrect;
		int[][] matrix = mergeMatrix(pMatrix, wMatrix);
		double[] precision = getPrecision(matrix, numClasses);
		double[] recall = getRecall(matrix, numClasses);
		double[] f1 = getF1(precision, recall, numClasses);
		
		pw.println("Correct " + correct);
		pw.println("Total " + (pGolds.size() + wGolds.size()));
		printMatrix(matrix, pw);
		print(precision, pw, "Precision: ");
		print(recall, pw, "Recall: ");
		print(f1, pw, "F1: ");

		pw.close();*/
	}
	
}