package edu.illinois.cs.timan.iknowx.features;

import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;

public abstract class NGramFeatures extends Features {
	protected int numThreads;
	protected int startIdx;
	protected int endIdx;

	protected NGramFeatures(int baseOffset) {
		this.startIdx = baseOffset;
		this.endIdx = baseOffset;
	}
	
	public abstract void init(List<ForumThread> threads) throws Exception;

	//public abstract Map<Integer, Double> getWeights(ForumThread thread) throws Exception;

	public int getSize() throws Exception {
		return endIdx - startIdx;
	}
	
	public int getStartIdx() {
		return startIdx;
	}
	
	public int getEndIdx() {
		return endIdx;
	}
}