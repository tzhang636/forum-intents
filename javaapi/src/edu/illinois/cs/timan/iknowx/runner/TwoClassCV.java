package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.NGramFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class TwoClassCV extends CV {
	
	public static List<ForumThread> getThreads() {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		return threads;
	}
	
	public static List<ForumThread> randomize(List<ForumThread> threads) {
		List<ForumThread> shuffledThreads = new ArrayList<ForumThread>(threads);
		long seed = System.nanoTime();
		Collections.shuffle(shuffledThreads, new Random(seed));
		return shuffledThreads;
	}
	
	/**
	 * Get equal split of training data from both classes (200 each).
	 * Use rest of data for test set.
	 * @param threads
	 * @return
	 */
	public static List<List<ForumThread>> getSets(List<ForumThread> threads) {
		List<ForumThread> train = new ArrayList<ForumThread>();
		List<ForumThread> test = new ArrayList<ForumThread>();
		List<List<ForumThread>> ret = new ArrayList<List<ForumThread>>();
		
		int numFirst = 0, numSecond = 0;
		int total = 100;
		for (ForumThread thread : threads) {
			int label = Constants.label2id.get(thread.getLabel());
			if (label == 0 && numFirst < total) {
				train.add(thread);
				++numFirst;
			}
			else if (label == 1 && numSecond < total) {
				train.add(thread);
				++numSecond;
			}
			else {
				test.add(thread);
			}
		}
		
		ret.add(train);
		ret.add(test);
		return ret;
	}
	
	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = getThreads();

		PrintWriter pw = new PrintWriter(Constants.twoClassOut);
		for (int i = 0; i < 10; ++i) {
			List<ForumThread> shuffledThreads = randomize(threads);
			List<List<ForumThread>> sets = getSets(shuffledThreads);
			List<ForumThread> train = sets.get(0);
			List<ForumThread> test = sets.get(1);
			
			List<NGramFeatures> nFeatures = CombinedFeatures.initNGramFeatures(train);
			CombinedFeatures.saveFeatures(train, nFeatures, Constants.twoClassTrain + i);
			CombinedFeatures.saveFeatures(test, nFeatures, Constants.twoClassTest + i);
			
			SVMTrainer.train(Constants.twoClassTrain + i, Constants.twoClassModel, 1000, 0);
			List<Double> preds = SVMPredictor.predict(Constants.twoClassTest + i, Constants.twoClassModel);
			List<Double> golds = SVMPredictor.getGolds(Constants.twoClassTest + i);
			
			int numCorrect = getNumCorrect(preds, golds);
			int[][] matrix = getMatrix(preds, golds, 2);
			
			// print out stats
			pw.println("Run " + i);
			pw.println("Number Correct " + numCorrect);
			pw.println("Number Total " + golds.size());
			for (int j = 0; j < matrix.length; ++j) {
				for (int k = 0; k < matrix[0].length; ++k) {
					pw.print(matrix[j][k] + "\t");
				}
				pw.println();
			}
			pw.println();
			
		}
		pw.close();
		
	}
}
