package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.LiwcFeatures;
import edu.illinois.cs.timan.iknowx.features.PatternFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class LiwcCV extends CV {
	
	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		
		int numClasses = 2;
		int numFolds = 5;
		List<List<ForumThread>> folds = getFolds(threads, numFolds);
		
		PrintWriter pw = new PrintWriter(Constants.liwcOut);

		Map<String, Double> preds = new HashMap<String, Double>();
		Map<String, Double> golds = new HashMap<String, Double>();
		
		for (int fold = 0; fold < folds.size(); ++fold) {
			List<ForumThread> trainSet = getTrainSet(folds, fold, 250);
			List<ForumThread> testSet = folds.get(fold);
			
			List<LiwcFeatures> liwcFeatures = CombinedFeatures.initLiwcFeatures(trainSet);
			CombinedFeatures.saveFeatures(trainSet, liwcFeatures, Constants.liwcTrain + fold);
			List<String> testTids = CombinedFeatures.saveFeatures(testSet, liwcFeatures, Constants.liwcTest + fold);

			SVMTrainer.train(Constants.liwcTrain + fold, Constants.liwcModel, 1000, 0);
			List<Double> testPreds = SVMPredictor.predict(Constants.liwcTest + fold, Constants.liwcModel);
			List<Double> testGolds = SVMPredictor.getGolds(Constants.liwcTest + fold);
			
			for (int i = 0; i < testTids.size(); ++i) {
				preds.put(testTids.get(i), testPreds.get(i));
				golds.put(testTids.get(i), testGolds.get(i));
			}
		}
		
		int numCorrect = getNumCorrect(threads, preds, golds);
		int[][] matrix = getMatrix(threads, preds, golds, numClasses);
		double[] precision = getPrecision(matrix, numClasses);
		double[] recall = getRecall(matrix, numClasses);
		double[] f1 = getF1(precision, recall, numClasses);

		pw.println("Number Correct " + numCorrect);
		pw.println("Number Total " + golds.size());
		pw.println("Accuracy: " + (double) numCorrect / golds.size());
		for (int x = 0; x < matrix.length; ++x) {
			for (int y = 0; y < matrix[0].length; ++y) {
				pw.print(matrix[x][y] + "\t");
			}
			pw.println();
		}
		pw.println();
		
		pw.println("Precision: ");
		for (int i = 0; i<numClasses; ++i) {
			pw.println(precision[i]);
		}
		pw.println();
		
		pw.println("Recall: ");
		for (int i = 0; i<numClasses; ++i) {
			pw.println(recall[i]);
		}
		pw.println();
		
		pw.println("F1: ");
		for (int i = 0; i<numClasses; ++i) {
			pw.println(f1[i]);
		}
		pw.println();
					
		pw.close();
		
	}
	
}