package edu.illinois.cs.timan.iknowx.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.loader.NegWordsLoader;

public abstract class PatternFeatures extends Features {
	protected static List<String> modAux;
	static {
		modAux = new ArrayList<String>();
		modAux.add("can");
		modAux.add("could");
		modAux.add("may");
		modAux.add("might");
		modAux.add("will");
		modAux.add("would");
		modAux.add("shall");
		modAux.add("should");
		modAux.add("ought");
	}
	
	protected static List<String> negWords;
	static {
		try {
			negWords = NegWordsLoader.loadNegWords();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected int numThreads;
	protected int startIdx;
	protected int endIdx;

	protected PatternFeatures(int baseOffset) {
		this.startIdx = baseOffset;
		this.endIdx = baseOffset;
	}
	
	public abstract void init(List<ForumThread> threads) throws Exception;

	public abstract Map<Integer, Double> getWeights(ForumThread thread) throws Exception;

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