package edu.illinois.cs.timan.iknowx.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.loader.NegWordsLoader;
import edu.illinois.cs.timan.iknowx.metamap.MetaMapMapping;
import edu.illinois.cs.timan.iknowx.processor.Processor;

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
	protected int k;

	protected PatternFeatures(int baseOffset, int k) {
		this.startIdx = baseOffset;
		this.endIdx = baseOffset;
		this.k = k;
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
	
	public List<List<String>> tokQSents(String tid, String post, String jsonDir) throws Exception {
		List<String> sents = Processor.getQSents(post);
		Map<String, String> mapping = MetaMapMapping.getMapping(tid, jsonDir);
		sents = Processor.labelSents(sents, mapping);
		return Processor.listToLowerCase(Processor.tokenize(sents));
	}
	
	public List<List<String>> tagQSents(String tid, String post, String jsonDir) throws Exception {
		List<String> sents = Processor.getQSents(post);
		Map<String, String> mapping = MetaMapMapping.getMapping(tid, jsonDir);
		sents = Processor.labelSents(sents, mapping);
		return Processor.tag(sents);
	}
	
	public List<List<String>> tokSents(String tid, String post, String jsonDir) throws Exception {
		List<String> sents = Processor.getSents(post);
		Map<String, String> mapping = MetaMapMapping.getMapping(tid, jsonDir);
		sents = Processor.labelSents(sents, mapping);
		return Processor.listToLowerCase(Processor.tokenize(sents));
	}
	
	public List<List<String>> tagSents(String tid, String post, String jsonDir) throws Exception {
		List<String> sents = Processor.getSents(post);
		Map<String, String> mapping = MetaMapMapping.getMapping(tid, jsonDir);
		sents = Processor.labelSents(sents, mapping);
		return Processor.tag(sents);
	}
}