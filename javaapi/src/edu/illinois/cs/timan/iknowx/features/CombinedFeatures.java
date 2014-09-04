package edu.illinois.cs.timan.iknowx.features;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.io.SaveLoad;

public class CombinedFeatures {

	public static List<LiwcFeatures> initLiwcFeatures(List<ForumThread> threads) {
		Reflections refs = new Reflections(Constants.featDir);
		Set<Class<? extends LiwcFeatures>> fclasses = refs
				.getSubTypesOf(LiwcFeatures.class);
		List<LiwcFeatures> featuresList = new ArrayList<LiwcFeatures>();
		int baseOffset = 0;
		for (Class<? extends LiwcFeatures> fclass : fclasses) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set<Constructor> cons = ReflectionUtils.getConstructors(fclass);
			for (Constructor<? extends LiwcFeatures> con : cons) {
				try {
					System.out.println(con.toGenericString());
					LiwcFeatures liwcFeatures = con.newInstance(baseOffset);
					liwcFeatures.init(threads);
					featuresList.add(liwcFeatures);
					baseOffset += liwcFeatures.getSize();
				} catch (Exception e) {
					System.err.println("CombinedFeatures.initLiwcFeatures()");
					e.printStackTrace();
				}
			}
		}
		return featuresList;
	}
	
	public static List<PatternFeatures> initPatternFeatures(List<ForumThread> threads, int k) {
		Reflections refs = new Reflections(Constants.featDir);
		Set<Class<? extends PatternFeatures>> fclasses = refs
				.getSubTypesOf(PatternFeatures.class);
		List<PatternFeatures> featuresList = new ArrayList<PatternFeatures>();
		int baseOffset = 0;
		for (Class<? extends PatternFeatures> fclass : fclasses) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set<Constructor> cons = ReflectionUtils.getConstructors(fclass);
			for (Constructor<? extends PatternFeatures> con : cons) {
				try {
					System.out.println(con.toGenericString());
					PatternFeatures patternFeatures = con.newInstance(baseOffset, k);
					patternFeatures.init(threads);
					featuresList.add(patternFeatures);
					baseOffset += patternFeatures.getSize();
				} catch (Exception e) {
					System.err.println("CombinedFeatures.initPatternFeatures()");
					e.printStackTrace();
				}
			}
		}
		return featuresList;
	}
	
	public static List<NGramFeatures> initNGramFeatures(List<ForumThread> threads) {
		Reflections refs = new Reflections(Constants.featDir);
		Set<Class<? extends NGramFeatures>> fclasses = refs
				.getSubTypesOf(NGramFeatures.class);
		List<NGramFeatures> featuresList = new ArrayList<NGramFeatures>();
		int baseOffset = 0;
		for (Class<? extends NGramFeatures> fclass : fclasses) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set<Constructor> cons = ReflectionUtils.getConstructors(fclass);
			for (Constructor<? extends NGramFeatures> con : cons) {
				try {
					System.out.println(con.toGenericString());
					NGramFeatures features = con.newInstance(baseOffset);
					features.init(threads);
					featuresList.add(features);
					baseOffset += features.getSize();
				} catch (Exception e) {
					System.err.println("CombinedFeatures.initNGramFeatures()");
					e.printStackTrace();
				}
			}
		}
		return featuresList;
	}

	public static List<String> saveFeatures(List<ForumThread> threads,
			List<? extends Features> featuresList, String filename, String jsonDir) throws Exception {
		List<String> tids = new ArrayList<String>();
		StringBuffer fileContent = new StringBuffer();
		Map<Integer, List<ForumThread>> sortedThreads = new HashMap<Integer, List<ForumThread>>();
		
		// separate threads by label
		for (ForumThread thread : threads) {
			int intLabel = Constants.label2id.get(thread.getLabel());
			if (!sortedThreads.containsKey(intLabel)) {
				sortedThreads.put(intLabel, new ArrayList<ForumThread>());
			}
			List<ForumThread> labelThreads = sortedThreads.get(intLabel);
			labelThreads.add(thread);
			sortedThreads.put(intLabel, labelThreads);
		}
		
		// for every label
		for (int intLabel : sortedThreads.keySet()) {
			// get list of threads with this label
			List<ForumThread> lThreads = sortedThreads.get(intLabel);
			
			// for every thread with this label
			for (ForumThread thread : lThreads) {
				// get all weights
				Map<Integer, Double> weights = new HashMap<Integer, Double>();
				for (Features features : featuresList) {
					weights.putAll(features.getWeights(thread, jsonDir));
				}
				
				// skip if matches no weights
				if (weights.isEmpty()) {
					continue;
				}
				
				// append to file
				List<Integer> sortedIds = getSortedIds(weights);
				String line = getLine(intLabel, weights, sortedIds);
				fileContent.append(line);
				
				// store thread id
				tids.add(thread.getId());
			}
		}
		
		SaveLoad.saveFile(filename, fileContent.toString());
		return tids;
	}
	
	/*
	List<Integer> intLabels = new ArrayList<Integer>();
	List<Map<Integer, Double>> weightsList = new ArrayList<Map<Integer, Double>>();
	intLabels.add(intLabel);
	weightsList.add(weights);
	*/
	
	/*double minWeight = getMinWeight(weightsList);
	double maxWeight = getMaxWeight(weightsList);
	
	for (int i = 0; i < weightsList.size(); ++i) {
		Map<Integer, Double> weights = weightsList.get(i);
		int intLabel = intLabels.get(i);
		
		scaleWeights(weights, minWeight, maxWeight);
		List<Integer> sortedIds = getSortedIds(weights);
		String line = getLine(intLabel, weights, sortedIds);
		content.append(line);
	}*/
	
	private static double getMinWeight(List<Map<Integer, Double>> weightsList) {
		double minWeight = Double.MAX_VALUE;
		for (Map<Integer, Double> weights : weightsList) {
			for (Double weight : weights.values()) {
				if (weight < minWeight) {
					minWeight = weight;
				}
			}
		}
		return minWeight;
	}

	private static double getMaxWeight(List<Map<Integer, Double>> weightsList) {
		double maxWeight = Double.MIN_VALUE;
		for (Map<Integer, Double> weights : weightsList) {
			for (Double weight : weights.values()) {
				if (weight > maxWeight) {
					maxWeight = weight;
				}
			}
		}
		return maxWeight;
	}
	
	private static void scaleWeights(Map<Integer, Double> weights, double minWeight, double maxWeight) {
		double diff = maxWeight - minWeight;
		for (int id : weights.keySet()) {
			double val = weights.get(id);
			double newVal = (val - minWeight) / diff;
			weights.put(id, newVal);
		}
	}
	
	private static List<Integer> getSortedIds(Map<Integer, Double> weights) {
		List<Integer> sortedIds = new ArrayList<Integer>();
		sortedIds.addAll(weights.keySet());
		Collections.sort(sortedIds);
		return sortedIds;
	}

	private static String getLine(int intLabel, Map<Integer, Double> weights,
			List<Integer> sortedIds) {
		StringBuffer line = new StringBuffer();
		line.append(intLabel + " ");
		for (int id : sortedIds) {
			line.append(id + ":" + weights.get(id) + " ");
		}
		line.append("\n");
		return line.toString();
	}
}