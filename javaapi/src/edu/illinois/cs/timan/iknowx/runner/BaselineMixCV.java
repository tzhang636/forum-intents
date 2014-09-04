package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.NGramFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class BaselineMixCV extends CV {

	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads(Constants.goldDir);
		ThreadFilter.filter(threads);
		PrintWriter pw = new PrintWriter(Constants.baselineMixOut);
		int numClasses = 4;
		int numFolds = 5;
		
		List<List<ForumThread>> folds = getFolds(threads, numFolds);
		
		Map<String, Double> wPreds = new HashMap<String, Double>();
		Map<String, Double> wGolds = new HashMap<String, Double>();

		for (int fold = 0; fold < folds.size(); ++fold) {
			List<ForumThread> trainSet = getTrainSet(folds, fold);
			List<ForumThread> testSet = folds.get(fold);
			
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

		pw.close();
	}
	
}