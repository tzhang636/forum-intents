package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import libsvm.svm;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.Features;
import edu.illinois.cs.timan.iknowx.features.LiwcFeatures;
import edu.illinois.cs.timan.iknowx.features.NGramFeatures;
import edu.illinois.cs.timan.iknowx.features.PatternFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadGetter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class CrossValidate {
	public static List<List<ForumThread>> getFolds(List<ForumThread> threads, int numFolds) {
		List<List<ForumThread>> folds = new ArrayList<List<ForumThread>>();
		double totalSize = threads.size();
		int foldSize = (int) (totalSize / numFolds);
		for (int i = 0; i < numFolds; ++i) {
			if (i == numFolds - 1) { // last fold
				folds.add(threads.subList(i * foldSize, (int) totalSize));
			}
			else {
				folds.add(threads.subList(i * foldSize, (i + 1) * foldSize));
			}
		}
		return folds;
	}

	public static List<ForumThread> getTrainSet(List<List<ForumThread>> folds, int fold) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		int numFolds = folds.size();
		for (int i = 0; i < numFolds; ++i) {
			if (i == fold) {
				continue;
			}
			trainSet.addAll(folds.get(i));
		}
		return trainSet;
	}
	
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
	
	public static List<ForumThread> getTrain(List<List<ForumThread>> folds,
			int tuneFold, int testFold) {
		List<ForumThread> train = new ArrayList<ForumThread>();
		int numFolds = folds.size();
		for (int i = 0; i < numFolds; ++i) {
			if (i == tuneFold || i == testFold) {
				continue;
			}
			train.addAll(folds.get(i));
		}
		return train;
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
	
	public static List<List<ForumThread>> getFoldsByForum(List<ForumThread> threads) {
		Map<String, List<ForumThread>> folds = new HashMap<String, List<ForumThread>>();
		for (ForumThread thread : threads) {
			String forumId = thread.getSubForumId();
			if (!folds.containsKey(forumId)) {
				folds.put(forumId, new ArrayList<ForumThread>());
			}
			List<ForumThread> fold = folds.get(forumId);
			fold.add(thread);
			folds.put(forumId, fold);
		}
		List<List<ForumThread>> ret = new ArrayList<List<ForumThread>>(folds.values());
		return ret;
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
		// get threads
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		
		// create folds
		int numFolds = 5;
		List<List<ForumThread>> folds = getFolds(threads, numFolds);
		
		// pattern predictions and golds
		Map<String, Double> liwcPreds = new HashMap<String, Double>();
		Map<String, Double> liwcGolds = new HashMap<String, Double>();
		
		// 5-fold CV
		for (int fold = 0; fold < folds.size(); /*numFolds;*/ ++fold) {
			// train set is only threads from 3 classes for patterns
			List<ForumThread> trainSet = getTrainSet(folds, fold); /*get3Class(getTrainSet(folds, fold));*/ 
			List<ForumThread> testSet = folds.get(fold); /*folds.get(fold);*/
			
			// save pattern features to file
			//List<LiwcFeatures> liwcFeatures = CombinedFeatures.initLiwcFeatures(trainSet);
			List<NGramFeatures> nGramFeatures = CombinedFeatures.initNGramFeatures(trainSet);
			List<Features> features = new ArrayList<Features>();
			//features.addAll(liwcFeatures);
			features.addAll(nGramFeatures);
			CombinedFeatures.saveFeatures(trainSet, features, Constants.trainFile);
			List<String> testSetTids = CombinedFeatures.saveFeatures(testSet, features, Constants.testFile + fold);

			// train and predict
			SVMTrainer.train(Constants.trainFile, Constants.modelFile, 1000, 0);
			List<Double> testPreds = SVMPredictor.predict(Constants.testFile + fold, Constants.modelFile);
			List<Double> testGolds = SVMPredictor.getGolds(Constants.testFile + fold);
			
			// store predictions and golds
			for (int i = 0; i < testSetTids.size(); ++i) {
				liwcPreds.put(testSetTids.get(i), testPreds.get(i));
				liwcGolds.put(testSetTids.get(i), testGolds.get(i));
			}
		}
		
		int numClasses = 4;
		int pc = 0, pt = 0;
		int[][] pMatrix = new int[numClasses][numClasses];
		for (String tid : liwcPreds.keySet()) {
			int pred = liwcPreds.get(tid).intValue();
			int gold = liwcGolds.get(tid).intValue();
			if (pred == gold) {
				pc++;
			}
			pt++;
			pMatrix[gold][pred]++;
		}
		
		System.out.println("num correct predictions: " + pc);
		System.out.println("num total predicted: " + pt);
		for (int i = 0; i<numClasses; ++i) {
			for (int j = 0; j<numClasses; ++j) {
				System.out.print(pMatrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
		
		// get best alpha
		/*double bestAlpha = 0;
		int bestCorrect = 0;
		int total = folds.get(3).size();
		//for (double alpha = 0.1; alpha <= 0.9; alpha += 0.02) { // tune alpha
			
			double alpha = 0.64;
			List<ForumThread> train = getTrain(folds, 3, 4); // use folds 0-2 to train
			List<ForumThread> patternTrain = getPatternTrain(train);
			List<ForumThread> tune = folds.get(3); // use fold 3 to tune
			
			// pattern
			List<Features> featuresList = CombinedFeatures.initFeatures(patternTrain);
			CombinedFeatures.saveFeatureFile(patternTrain, featuresList, Constants.trainFile);
			List<String> tids = CombinedFeatures.saveFeatureFile(tune, featuresList, Constants.tuneFile);

			SVMTrainer.train(Constants.trainFile, Constants.modelFile, 1000, 0);
			List<Double> tuneGolds = SVMPredictor.getGolds(Constants.tuneFile);
			List<double[]> tuneProbs = SVMPredictor.getProbEsts(Constants.tuneFile,Constants.modelFile, numClasses - 1);
			
			// store probabilities
			Map<String, double[]> probs = new HashMap<String, double[]>(); // pattern probabilities
			for (int i = 0; i < tids.size(); ++i) {
				probs.put(tids.get(i), tuneProbs.get(i));
			}
			
			// n-gram
			List<NGramFeatures> nGramFeaturesList = CombinedFeatures.initNGramFeatures(train);
			CombinedFeatures.saveNGramFeatureFile(train, nGramFeaturesList, Constants.trainNGramFile);
			List<String> nGramTids = CombinedFeatures.saveNGramFeatureFile(tune, nGramFeaturesList, Constants.tuneNGramFile);
			
			SVMTrainer.train(Constants.trainNGramFile, Constants.modelNGramFile, 1000, 0);
			List<Double> nGramTuneGolds = SVMPredictor.getGolds(Constants.tuneNGramFile);
			List<double[]> nGramTuneProbs = SVMPredictor.getProbEsts(Constants.tuneNGramFile,Constants.modelNGramFile, numClasses);
			
			// store probabilities
			Map<String, double[]> nGramProbs = new HashMap<String, double[]>(); // n-gram probabilities
			Map<String, Double> golds = new HashMap<String, Double>(); // gold labels
			for (int i = 0; i < nGramTids.size(); ++i) {
				nGramProbs.put(nGramTids.get(i), nGramTuneProbs.get(i));
				golds.put(nGramTids.get(i), nGramTuneGolds.get(i));
			}
			
			// combine probabilities
			Map<String, double[]> combinedProbs = new HashMap<String, double[]>(); // combined probabilities
			for (String tid : nGramProbs.keySet()) {
				
				double[] nGramProb = nGramProbs.get(tid);
				double[] prob = null;
				if (probs.containsKey(tid)) {
					prob = probs.get(tid);
				}
				
				double[] ngp = new double[4];
				double[] p = new double[3];
				for (int i = 0; i < nGramProb.length; ++i) {
					ngp[i] = nGramProb[i] * alpha;
				}
				if (prob != null) {
					for (int i = 0; i < prob.length; ++i) {
						p[i] = prob[i] * (1 - alpha);
					}
				}
				
				double[] combined = new double[numClasses];
				for (int i = 0; i < numClasses; ++i) {
					if (p == null || i == numClasses - 1) {
						combined[i] = ngp[i];
					}
					else {
						combined[i] = ngp[i] + p[i];
					}
				}
				combinedProbs.put(tid, combined);
				
			}
			
			PrintWriter pw = new PrintWriter("out2.txt");
			for (String tid : combinedProbs.keySet()) {
				double[] comScores = combinedProbs.get(tid);
				double[] nGramScores = nGramProbs.get(tid);
				double[] patScores = null;
				if (probs.containsKey(tid)) {
					patScores = probs.get(tid);
				}
				
				int comPred = getPrediction(comScores);
				int nGramPred = getPrediction(nGramScores);
				int patPred = -1;
				if (patScores != null) {
					patPred = getPrediction(patScores);
				}
				
				int gold = golds.get(tid).intValue();
				
				if ((patPred != gold && nGramPred != gold) && comPred == gold) {
					pw.println(gold);
					if (patScores != null) {
						pw.println(patPred + "\t" + patScores[0] + "\t" + patScores[1] + "\t" + patScores[2]);
					}
					else {
						pw.println("----------------------------------------------------------------");
					}
					pw.println(nGramPred + "\t" + nGramScores[0] + "\t" + nGramScores[1] + "\t" + nGramScores[2] + "\t" + nGramScores[3]);
					pw.println(comPred + "\t" + comScores[0] + "\t" + comScores[1] + "\t" + comScores[2] + "\t" + comScores[3]);
					pw.println();
				}
				
			}
			pw.close();
			
			int correct = 0;
			for (String tid : combinedProbs.keySet()) {
				double[] scores = combinedProbs.get(tid);
				int gold = golds.get(tid).intValue();
				int prediction = getPrediction(scores);
				if (gold == prediction) {
					correct++;
				}
			}
			System.out.println(alpha);
			System.out.println(correct);
			System.out.println(total);
			
			/*if (correct > bestCorrect) {
				bestAlpha = alpha;
				bestCorrect = correct;
			}*/
			
		//}*/
		
		/*System.out.println(bestAlpha);
		System.out.println(bestCorrect);
		System.out.println(total);*/
		
		
		// pattern
		
		/*PrintWriter pw = new PrintWriter("unmatched.txt");
		List<Features> featuresList = CombinedFeatures.initFeatures(threads);
		List<ForumThread> unmatched = CombinedFeatures.saveFeatureFile(threads, featuresList, "file.txt");
		for (ForumThread thread : unmatched) {
			pw.println(thread.getLabel());
			pw.println(thread.getFirstPost());
			pw.println();
		}
		pw.close();
		System.out.println(unmatched.size());*/
		
		/*Map<String, Double> golds = new HashMap<String, Double>();
		Map<String, Double> predictions = new HashMap<String, Double>();
		Map<String, double[]> probs = new HashMap<String, double[]>();

		for (int fold = 0; fold < numFolds; ++fold) {
			List<ForumThread> train = getPatternTrain(getTrain(folds, fold));
			List<ForumThread> test = folds.get(fold);
			
			List<Features> featuresList = CombinedFeatures.initFeatures(train);
			List<String> trainTids = CombinedFeatures.saveFeatureFile(train, featuresList, Constants.trainFile);
			List<String> testTids = CombinedFeatures.saveFeatureFile(test, featuresList, Constants.testFile);

			SVMTrainer.train(Constants.trainFile, Constants.modelFile, 1000, 0);			

			List<Double> testGolds = SVMPredictor.getGolds(Constants.testFile);
			List<Double> testPredictions = SVMPredictor.predict(Constants.testFile,Constants.modelFile);
			List<double[]> testProbs = SVMPredictor.getProbEsts(Constants.testFile, Constants.modelFile, numClasses - 1);

			for (int i = 0; i < testTids.size(); ++i) {
				golds.put(testTids.get(i), testGolds.get(i));
				predictions.put(testTids.get(i), testPredictions.get(i));
				probs.put(testTids.get(i), testProbs.get(i));
			}
		}
		
		// n-gram
		Map<String, Double> NGolds = new HashMap<String, Double>();
		Map<String, Double> NPredictions = new HashMap<String, Double>();
		Map<String, double[]> NProbs = new HashMap<String, double[]>();
		for (int fold = 0; fold < numFolds; ++fold) {
			List<ForumThread> train = getTrain(folds, fold);
			List<ForumThread> test = folds.get(fold);
			
			List<NGramFeatures> featuresList = CombinedFeatures.initNGramFeatures(train);
			List<String> trainTids = CombinedFeatures.saveNGramFeatureFile(train, featuresList, Constants.trainFile);
			List<String> testTids = CombinedFeatures.saveNGramFeatureFile(test, featuresList, Constants.testFile);

			SVMTrainer.train(Constants.trainFile, Constants.modelFile, 1000, 0);			

			List<Double> testGolds = SVMPredictor.getGolds(Constants.testFile);
			List<Double> testPredictions = SVMPredictor.predict(Constants.testFile,Constants.modelFile);
			List<double[]> testProbs = SVMPredictor.getProbEsts(Constants.testFile, Constants.modelFile, numClasses);

			for (int i = 0; i < testTids.size(); ++i) {
				NGolds.put(testTids.get(i), testGolds.get(i));
				NPredictions.put(testTids.get(i), testPredictions.get(i));
				NProbs.put(testTids.get(i), testProbs.get(i));
			}
		}		
		
		int c = 0; int t = 0;
		int[][] matrix = new int[numClasses][numClasses];
		
		for (String tid : NGolds.keySet()) {
			
			// n-gram results
			int nGold = NGolds.get(tid).intValue();
			//int nPrediction = NPredictions.get(tid).intValue();
			double[] nProb = NProbs.get(tid);
			double[] nTemp = new double[nProb.length];
			for (int i = 0; i<nProb.length; ++i) {
				nTemp[i] = nProb[i];
			}
			int nPrediction = getPrediction(nTemp);
			nTemp[nPrediction] = 0;
			int snPrediction = getPrediction(nTemp);
			
			// pattern results
			Integer pGold = null;
			Integer pPrediction = null;
			Integer spPrediction = null;
			double[] pProb = null;
			double[] pTemp = null;
			
			if (golds.containsKey(tid)) {
				pGold = golds.get(tid).intValue();
				//pPrediction = predictions.get(tid).intValue();
				pProb = probs.get(tid);
				
				pTemp = new double[pProb.length];
				for (int i = 0; i<pProb.length; ++i) {
					pTemp[i] = pProb[i];
				}
				pPrediction = getPrediction(pTemp);
				pTemp[pPrediction] = 0;
				spPrediction = getPrediction(pTemp);
			}

			int g = nGold, p;
			if (pGold == null) { // no pattern features
				p = nPrediction;
			}
			else { // both pattern and unigram features
				if (nPrediction == 3) { // unigram predict story telling/others
					p = nPrediction;
				}
				else {
					double pRatio = pProb[pPrediction] - pProb[spPrediction];
					double nRatio = nProb[nPrediction] - nProb[snPrediction];
					if (pRatio >= nRatio) {
						p = pPrediction;
					}
					else {
						p = nPrediction;
					}
				}
			}
			
			if (g == p) {
				c++;
			}
			t++;
			matrix[g][p]++;
			
		}
		
		System.out.println(c + " " + t);
		for (int i = 0; i<numClasses; ++i) {
			for (int j = 0; j<numClasses; ++j) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}*/
		
		/*System.out.print(nPrediction + "\t" + nGold + "\t");
		for (double p : nProb) {
			System.out.print(p + "\t");
		}
		System.out.println();
		
		System.out.print(pPrediction + "\t" + pGold + "\t");
		if (pProb != null) {
			for (double p : pProb) {
				System.out.print(p + "\t");
			}
		}
		System.out.println();
		System.out.println();*/
		
		//Collections.shuffle(threads, new Random(System.nanoTime()));
		
		/*for (int j = 0; j < 10; ++j) {
			Collections.shuffle(threads, new Random(System.nanoTime()));
			
			List<Double> predictions = new ArrayList<Double>();
			List<Double> golds = new ArrayList<Double>();
	
			List<ForumThread> trainCandidates = threads.subList(0, (int) (0.9 * threads.size()));
			List<ForumThread> testCandidates = threads.subList((int) (0.9*threads.size()), threads.size());
			
			List<Features> featuresList = CombinedFeatures.initFeatures();
			List<ForumThread> trainThreads = ThreadGetter.getTrainThreads(trainCandidates, featuresList);
			List<ForumThread> testThreads = ThreadGetter.getTestThreads(testCandidates, featuresList);
			
			System.out.println(trainThreads.size());
			System.out.println(testThreads.size());
			
			CombinedFeatures.saveFile(trainThreads, featuresList, Constants.trainFile);
			CombinedFeatures.saveFile(testThreads, featuresList, Constants.testFile);
						
			SVMTrainer.train(Constants.trainFile, Constants.modelFile, 1000, 0);						
			predictions.addAll(SVMPredictor.predict(Constants.testFile,Constants.modelFile));
			golds.addAll(SVMPredictor.getGolds(Constants.testFile));
			
			System.out.println("predictions size: " + predictions.size());
			
			int c = 0; int t = 0;
			for (int i = 0; i < predictions.size(); ++i) {
				int gold = golds.get(i).intValue();
				int prediction = predictions.get(i).intValue();
				if (gold == prediction) {
					correct++;
					c++;
				}
				t++;
				total++;
			}
			System.out.println(c + " " + t);
		}
		System.out.println(correct + " " + total);*/
		
		
		/*
		int[][] matrix = new int[3][3];
		int correct = 0, total = 0;
		for (int i = 0; i < predictions.size(); ++i) {
			int gold = golds.get(i).intValue();
			int prediction = predictions.get(i).intValue();
			if (gold == prediction) {
				correct++;
			}
			total++;
			matrix[gold][prediction]++;
		}
		System.out.println(correct + " " + total);
		
		PrintWriter p = new PrintWriter("results.txt", "UTF-8");
		for (int i = 0; i < matrix.length; ++i) {
			for (int j = 0; j < matrix[0].length; ++j) {
				p.print(matrix[i][j] + "\t");
			}
			p.println();
		}
		p.println();
		p.println("numTrainThreads: " + trainThreads.size());
		p.println("numTestThreads: " + testThreads.size());
		p.println();
		for (int i = 0; i < testThreads.size(); ++i) {
			ForumThread thread = testThreads.get(i);
			Map<Integer, Double> weights = weightsList.get(i);
			p.println(thread.getThreadId());
			p.println(golds.get(i).intValue() + " " + predictions.get(i).intValue());
			p.println(weights.toString());
			p.println(thread.getFirstPost());
			p.println();
		}
		p.close();*/
		
		/*
		double bestCost = -1;
		double bestGamma = -1;
		int bestMaxFeatures = -1;
		double bestAccuracy = 0.0;
		List<Double> bestPredictions = null;
		List<Double> bestGolds = null;
		
		// grid search
		for (double cost = 1000; cost <= 1000; cost += 100) {
			//double cost = Math.pow(2, costExp);
			//for (double gammaExp = -15; gammaExp <= -5; gammaExp += 2) {
				//double gamma = Math.pow(2, gammaExp);
				for (int maxFeatures = 30; maxFeatures <= 30; ++maxFeatures) {
					// cross validate
					System.out.println("Cross validating with cost " + cost 
							+ " maxFeatures " + maxFeatures);
					List<Double> predictions = new ArrayList<Double>();
					List<Double> golds = new ArrayList<Double>();
					for (int fold = 0; fold < folds.size(); ++fold) {
						List<ForumThread> train = CrossValidate.getTrain(folds, fold);
						List<ForumThread> test = CrossValidate.getTest(folds, fold);

						List<DocFeatures> featuresList = CombinedDocFeatures.initFeatures(train);
						CombinedDocFeatures.saveFile(train, featuresList, Constants.trainFile, maxFeatures);
						CombinedDocFeatures.saveFile(test, featuresList, Constants.testFile, maxFeatures);

						SVMTrainer.train(Constants.trainFile, Constants.modelFile, cost, 0);						
						predictions.addAll(SVMPredictor.predict(Constants.testFile,Constants.modelFile));
						golds.addAll(SVMPredictor.getGolds(Constants.testFile));
					}
					double correct = 0;
					int total = 0;
					double accuracy = 0.0;
					for (int i = 0; i < predictions.size(); ++i) {
						double prediction = predictions.get(i);
						double gold = golds.get(i);
						if (prediction == gold) {
							++correct;
						}
						++total;
					}
					accuracy = correct / total;
					System.out.println("Cross validation accuracy: " + accuracy);
					System.out.println();
					if (accuracy > bestAccuracy) { // save best parameters
						bestCost = cost;
						//bestGamma = gamma;
						bestMaxFeatures = maxFeatures;
						bestAccuracy = accuracy;
						bestPredictions = predictions;
						bestGolds = golds;
					}
				//}
			}
		}

		System.out.println("bestCost: " + bestCost);
		System.out.println("bestGamma: " + bestGamma);
		System.out.println("bestMaxFeatures: " + bestMaxFeatures);
		System.out.println("bestAccuracy: " + bestAccuracy);
		
		// print classifications
		PrintWriter pw = new PrintWriter(Constants.classifications, "UTF-8");
		for (int i = 0; i < bestPredictions.size(); ++i) {
			pw.println(bestGolds.get(i) + " " + bestPredictions.get(i));
			pw.println(firstPosts.get(i));
			pw.println();
		}
		pw.close();*/
	}
}