package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.PatternFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class FeaturesCV extends CV {
	
	public static List<ForumThread> getTrainSet(List<ForumThread> threads, List<ForumThread> fold) {
		// construct map of thread ids to threads in fold
		Map<String, ForumThread> id2Thread = new HashMap<String, ForumThread>();
		for (ForumThread thread : fold) {
			id2Thread.put(thread.getId(), thread);
		}
		// construct training set consisting all threads not found in fold
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			String tid = thread.getId();
			if (!id2Thread.containsKey(tid)) {
				trainSet.add(thread);
			}
		}
		return trainSet;
	}
	
	public static List<ForumThread> getTrainSet(List<ForumThread> threads, List<ForumThread> fold, String forumId) {
		// construct map of thread ids to threads in fold
		Map<String, ForumThread> id2Thread = new HashMap<String, ForumThread>();
		for (ForumThread thread : fold) {
			id2Thread.put(thread.getId(), thread);
		}
		// construct training set consisting all threads not found in fold
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			String tForumId = thread.getSubForumId();
			String tid = thread.getId();
			if (!id2Thread.containsKey(tid) && !tForumId.equals(forumId)) {
				trainSet.add(thread);
			}
		}
		return trainSet;
	}
	
	public static boolean isMax(double p, double[] probEst) {
		for (double prob : probEst) {
			if (p < prob) {
				return false;
			}
		}
		return true;
	}
	
	public static int getPrediction(double[] probs) {
		int prediction = -1;
		double bestProb = -1;
		for (int i = 0; i < probs.length; ++i) {
			double prob = probs[i];
			if (prob > bestProb) {
				bestProb = prob;
				prediction = i;
			}
		}
		return prediction;
	}
	
	public static List<ForumThread> get3Class(List<ForumThread> threads) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			int intLabel = Constants.label2id.get(thread.getLabel());
			if (intLabel != 3) {
				trainSet.add(thread);
			}
		}
		return trainSet;
	}
	
	public static List<ForumThread> getNThreads(List<ForumThread> threads, Set<String> matchedTids) {
		List<ForumThread> nThreads = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			String tid = thread.getThreadId();
			if (!matchedTids.contains(tid)) {
				nThreads.add(thread);
			}
		}
		return nThreads;
	}
	
	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		Map<String, Map<String, Integer>> comboCounts = new HashMap<String, Map<String, Integer>>();
		Map<Integer, Character> m = new HashMap<Integer, Character>();
		m.put(0, 'M');
		m.put(1, 'C');
		m.put(2, 'A');
		
		for (ForumThread thread : threads) {
			String forumId = thread.getSubForumId();
			if (!comboCounts.containsKey(forumId)) {
				comboCounts.put(forumId, new HashMap<String, Integer>());
			}
			Map<String, Integer> map = comboCounts.get(forumId);
			int label = Constants.label2id.get(thread.getLabel());
			if (label == 4) {
				if (!map.containsKey("MC")) {
					map.put("MC", 0);
				}
				if (!map.containsKey("MA")) {
					map.put("MA", 0);
				}
				if (!map.containsKey("CA")) {
					map.put("CA", 0);
				}
				if (!map.containsKey("MCA")) {
					map.put("MCA", 0);
				}
				if (!map.containsKey("M")) {
					map.put("M", 0);
				}
				if (!map.containsKey("C")) {
					map.put("C", 0);
				}
				if (!map.containsKey("A")) {
					map.put("A", 0);
				}
				List<String> labels = thread.getLabels();
				String fLabel = "";
				for (String l : labels) {
					int l1 = Constants.label2id.get(l);
					fLabel += m.get(l1);
				}
				map.put(fLabel, map.get(fLabel)+1);
				comboCounts.put(forumId, map);
			}
		}
		for (String forumId : comboCounts.keySet()) {
			Map<String, Integer> counts = comboCounts.get(forumId);
			System.out.println(forumId);
			for (String label : counts.keySet()) {
				int count = counts.get(label);
				System.out.println(label + '\t' + count);
			}
			System.out.println();
		}
		
		
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for (ForumThread thread : threads) {
			String label = thread.getLabel();
			
			if (!counts.containsKey(label)) {
				counts.put(label, 0);
			}
			counts.put(label, counts.get(label)+1);
		}
		for (String label : counts.keySet()) {
			int count = counts.get(label);
			System.out.println(label + '\t' + count);
		}
		
		int numClasses = 4;
		int numFolds = 5;
		List<List<ForumThread>> folds = getFolds(threads, numFolds);
		PrintWriter pw = new PrintWriter(Constants.featOut);

		for (int k = 8; k <= 8; ++k) {
			if (k == 1 || k == 2 || k == 3) {
				continue;
			}
			System.out.println("K = " + k);

			Map<String, Double> preds = new HashMap<String, Double>();
			Map<String, Double> golds = new HashMap<String, Double>();
			
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
					preds.put(testTids.get(i), testPreds.get(i));
					golds.put(testTids.get(i), testGolds.get(i));
				}
			}
			
			for (String tid : preds.keySet()) {
				double gold = golds.get(tid);
				if (gold == 4) { // combo
					System.out.println(tid);
					System.out.println("Pred: " + preds.get(tid));
				}
			}
			
			/*int numCorrect = getNumCorrect(threads, preds, golds);
			int[][] matrix = getMatrix(threads, preds, golds, numClasses);
			double[] precision = getPrecision(matrix, numClasses);
			double[] recall = getRecall(matrix, numClasses);
			double[] f1 = getF1(precision, recall, numClasses);

			pw.println("K = " + k);
			pw.println("Number Correct " + numCorrect);
			pw.println("Number Total " + golds.size());
			
			printMatrix(matrix, pw);
			print(precision, pw, "Precision: ");
			print(recall, pw, "Recall: ");
			print(f1, pw, "F1: ");*/
						
		}
		pw.close();
		
	}
}