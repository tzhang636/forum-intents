package edu.illinois.cs.timan.iknowx.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.features.CombinedFeatures;
import edu.illinois.cs.timan.iknowx.features.Features;

public class ThreadGetter {
	private static final int threshold = 1;
	
	public static List<ForumThread> getTrainThreads(List<ForumThread> threads, List<Features> featuresList) throws Exception {
		List<ForumThread> trainThreads = new ArrayList<ForumThread>();
			
		for (ForumThread thread : threads) {
			Map<String, Integer> weightCounts = new HashMap<String, Integer>();
			String threadLabel = thread.getLabel();
			
			// get weight counts for each set of features
			for (Features features : featuresList) {
				Map<Integer, Double> weights = features.getWeights(thread);
				int weightCount = 0;
				for (Double weight : weights.values()) {
					if (weight > 0.0) {
						weightCount++;
					}
				}
				weightCounts.put(features.getName(), weightCount);
			}
			
			
			List<String> labels = new ArrayList<String>();
			for (String label : weightCounts.keySet()) {
				int weightCount = weightCounts.get(label);
				if (weightCount > 0) {
					labels.add(label);
				}
			}
			for (String label : labels) {
				if (label.equals(threadLabel)) {
					trainThreads.add(thread);
					break;
				}
			}
			
			
			// get labels with max num of non-zero weights
			/*int maxCount = 0;
			List<String> labels = new ArrayList<String>();
			for (String label : weightCounts.keySet()) {
				int weightCount = weightCounts.get(label);
				if (weightCount > maxCount) {
					maxCount = weightCount;
					labels.clear();
					labels.add(label);
				}
				else if (weightCount == maxCount) {
					labels.add(label);
				}
			}
			
			// do not use threads that match none of the features
			if (maxCount == 0) {
				continue;
			}
			
			// use as training thread if one of the labels matches the gold label
			for (String label : labels) {
				if (label.equals(threadLabel)) {
					trainThreads.add(thread);
					break;
				}
			}*/
			
		}
		
		return trainThreads;
	}
	
	public static List<ForumThread> getTestThreads(List<ForumThread> threads, List<Features> featuresList) throws Exception {
		List<ForumThread> testThreads = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			Map<Integer, Double> weights = new HashMap<Integer, Double>();
			for (Features features : featuresList) {
				weights.putAll(features.getWeights(thread));
			}
			int numWeights = 0;
			for (Double weight : weights.values()) {
				if (weight > 0.0) {
					numWeights++;
				}
			}
			if (numWeights >= threshold) {
				testThreads.add(thread);
			}
		}
		return testThreads;
	}
	
	public static void main(String[] args) throws Exception {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		Collections.shuffle(threads, new Random(System.nanoTime()));
		
		List<ForumThread> trainCandidates = threads.subList(0, (int) (0.9 * threads.size()));
		List<ForumThread> testCandidates = threads.subList((int) (0.9*threads.size()), threads.size());
		
		System.out.println(trainCandidates.size());
		System.out.println(testCandidates.size());
		
		List<Features> featuresList = CombinedFeatures.initFeatures();
		List<ForumThread> trainThreads = ThreadGetter.getTrainThreads(trainCandidates, featuresList);
		List<ForumThread> testThreads = ThreadGetter.getTestThreads(testCandidates, featuresList);
		
		System.out.println(trainThreads.size());
		System.out.println(testThreads.size());
	}
}