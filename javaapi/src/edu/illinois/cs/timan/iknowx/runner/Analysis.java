package edu.illinois.cs.timan.iknowx.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.PatternFeatures;
import edu.illinois.cs.timan.iknowx.svm.SVMPredictor;
import edu.illinois.cs.timan.iknowx.svm.SVMTrainer;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class Analysis {
	
	public static List<ForumThread> getTrainSet(List<ForumThread> threads) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			int label = Constants.label2id.get(thread.getLabel());
			if (label == 0 || label == 1 || label == 2) {
				trainSet.add(thread);
			}
		}
		return trainSet;
	}
	
	public static void writeFiles(final File folder) throws Exception {
    for (final File fileEntry : folder.listFiles()) {
      BufferedReader br = new BufferedReader(new FileReader(fileEntry));
    	String filename = fileEntry.getName().substring(14);
      PrintWriter pw = new PrintWriter("data/medhelp_allergies/allergies/" + filename);
      try {
          String line = br.readLine();
          System.out.println("Writing " + filename + "...");
          pw.print(line);
      } finally {
          br.close();
          pw.close();
      }
    }
	}
	
	/*public static void main(String[] args) throws Exception {
		final File folder = new File("data/medhelp_allergies/allergiesJsons/");
		writeFiles(folder);
	}*/
	
	public static void main(String[] args) throws Exception {
		List<ForumThread> trainSet = ThreadLoader.loadThreads();
		ThreadFilter.filter(trainSet);
		trainSet = getTrainSet(trainSet);
		int k = 8;
		
		// train using only 3 classes
		List<PatternFeatures> pFeatures = CombinedFeatures.initPatternFeatures(trainSet, k);
		CombinedFeatures.saveFeatures(trainSet, pFeatures, Constants.train, Constants.postJsonDir);
		SVMTrainer.train(Constants.train, Constants.model, 1000, 0);

		// run on allergies
		System.out.println("Running classifier on allergies...");
		List<ForumThread> testSet = ThreadLoader.load("data/medhelp/allergy.csv");
		List<String> tids = CombinedFeatures.saveFeatures(testSet, pFeatures, Constants.test, Constants.jsonDir);
		List<Double> preds = SVMPredictor.predict(Constants.test, Constants.model);
		
		Map<Double, Integer> counts = new HashMap<Double, Integer>();
		int total = 0;
		for (double pred : preds) {
			if (!counts.containsKey(pred)) {
				counts.put(pred, 0);
			}
			counts.put(pred, counts.get(pred)+1);
			total++;
		}
		
		PrintWriter pw = new PrintWriter(Constants.allergyOut);
		for (int i = 0; i < tids.size(); ++i) {
			String tid = tids.get(i);
			double pred = preds.get(i);
			pw.println(tid + '\t' + pred);
		}
		pw.println();
		
		pw.println("total " + '\t' + total);
		for (double intent : counts.keySet()) {
			int count = counts.get(intent);
			pw.println(intent + "\t" + count);
		}
		pw.close();
		
		
		System.out.println("Running classifier on breast cancer...");
		testSet = ThreadLoader.load("data/medhelp/breast_cancer.csv");
		tids = CombinedFeatures.saveFeatures(testSet, pFeatures, Constants.test, Constants.jsonDir);
		preds = SVMPredictor.predict(Constants.test, Constants.model);
		
		counts = new HashMap<Double, Integer>();
		total = 0;
		for (double pred : preds) {
			if (!counts.containsKey(pred)) {
				counts.put(pred, 0);
			}
			counts.put(pred, counts.get(pred)+1);
			total++;
		}
		
		pw = new PrintWriter(Constants.bcOut);
		for (int i = 0; i < tids.size(); ++i) {
			String tid = tids.get(i);
			double pred = preds.get(i);
			pw.println(tid + '\t' + pred);
		}
		pw.println();
		
		pw.println("total " + '\t' + total);
		for (double intent : counts.keySet()) {
			int count = counts.get(intent);
			pw.println(intent + "\t" + count);
		}
		pw.close();
		
		
		
		System.out.println("Running classifier on depression...");
		testSet = ThreadLoader.load("data/medhelp/depression.csv");
		tids = CombinedFeatures.saveFeatures(testSet, pFeatures, Constants.test, Constants.jsonDir);
		preds = SVMPredictor.predict(Constants.test, Constants.model);
		
		counts = new HashMap<Double, Integer>();
		total = 0;
		for (double pred : preds) {
			if (!counts.containsKey(pred)) {
				counts.put(pred, 0);
			}
			counts.put(pred, counts.get(pred)+1);
			total++;
		}
		
		pw = new PrintWriter(Constants.depOut);
		for (int i = 0; i < tids.size(); ++i) {
			String tid = tids.get(i);
			double pred = preds.get(i);
			pw.println(tid + '\t' + pred);
		}
		pw.println();
		
		pw.println("total " + '\t' + total);
		for (double intent : counts.keySet()) {
			int count = counts.get(intent);
			pw.println(intent + "\t" + count);
		}
		pw.close();
		
		
		
		System.out.println("Running classifier on heart disease...");
		testSet = ThreadLoader.load("data/medhelp/heart_disease.csv");
		tids = CombinedFeatures.saveFeatures(testSet, pFeatures, Constants.test, Constants.jsonDir);
		preds = SVMPredictor.predict(Constants.test, Constants.model);
		
		counts = new HashMap<Double, Integer>();
		total = 0;
		for (double pred : preds) {
			if (!counts.containsKey(pred)) {
				counts.put(pred, 0);
			}
			counts.put(pred, counts.get(pred)+1);
			total++;
		}
		
		pw = new PrintWriter(Constants.hdOut);
		for (int i = 0; i < tids.size(); ++i) {
			String tid = tids.get(i);
			double pred = preds.get(i);
			pw.println(tid + '\t' + pred);
		}
		pw.println();
		
		pw.println("total " + '\t' + total);
		for (double intent : counts.keySet()) {
			int count = counts.get(intent);
			pw.println(intent + "\t" + count);
		}
		pw.close();
		
	}
	
}