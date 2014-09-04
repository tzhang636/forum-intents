package edu.illinois.cs.timan.iknowx.features;

import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;

public abstract class Features {
	
	public abstract Map<Integer, Double> getWeights(ForumThread thread, String jsonDir) throws Exception;
}