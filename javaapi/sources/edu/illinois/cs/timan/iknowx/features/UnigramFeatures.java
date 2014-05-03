package edu.illinois.cs.timan.iknowx.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.processor.Processor;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class UnigramFeatures extends NGramFeatures {
	private Map<String, Integer> tok2id;
	private Map<Integer, Integer> threadCounts;

	public UnigramFeatures(int baseOffset) {
		super(baseOffset);
		tok2id = new HashMap<String, Integer>();
		threadCounts = new HashMap<Integer, Integer>();
	}
	
	@Override
	public void init(List<ForumThread> threads) throws Exception {
		this.numThreads = threads.size();
		
		// init tok2id
		for (ForumThread thread : threads) {
			String post = thread.getFirstPost();
			List<String> toks = Processor.tokenize(post);
			List<String> ltoks = Processor.toLowerCase(toks);
			
			for (String ltok : ltoks) {
				if (!tok2id.containsKey(ltok)) {
					tok2id.put(ltok, endIdx);
					endIdx++;
				}
			}
		}
		
		// init thread counts
		for (ForumThread thread : threads) {
			String post = thread.getFirstPost();
			List<String> toks = Processor.tokenize(post);
			List<String> ltoks = Processor.toLowerCase(toks);
			
			for (String ltok : ltoks) {
				int id = tok2id.get(ltok);
				if (!threadCounts.containsKey(id)) {
					threadCounts.put(id, 0);
				}
				threadCounts.put(id, threadCounts.get(id) + 1);
			}
		}
	}

	@Override
	public Map<Integer, Double> getWeights(ForumThread thread) throws Exception {
		Map<Integer, Double> weights = new HashMap<Integer, Double>();
		Map<Integer, Integer> tokCounts = getTokCounts(thread);
		
		String post = thread.getFirstPost();
		List<String> toks = Processor.tokenize(post);
		List<String> ltoks = Processor.toLowerCase(toks);
		
		for (String ltok : ltoks) {
			if (!tok2id.containsKey(ltok)) {
				continue;
			}
			
			Integer id = tok2id.get(ltok);
			Integer count = tokCounts.get(id);
			double tf = Math.log(count + 1.0);
			
			double df;
			if (!threadCounts.containsKey(id)) {
				df = numThreads;
			} 
			else {
				df = threadCounts.get(id);
			}
			
			double weight = tf * Math.log(numThreads / df);
			if (weight == 0.0) {
				continue;
			}
			weights.put(id, weight);
		}
		
		return weights;
	}
	
	public Map<Integer, Integer> getTokCounts(ForumThread thread) {
		Map<Integer, Integer> tokCounts = new HashMap<Integer, Integer>();
		
		String post = thread.getFirstPost();
		List<String> toks = Processor.tokenize(post);
		List<String> ltoks = Processor.toLowerCase(toks);
		
		for (String ltok : ltoks) {
			if (!tok2id.containsKey(ltok)) {
				continue;
			}
			int id = tok2id.get(ltok);
			if (!tokCounts.containsKey(id)) {
				tokCounts.put(id, 0);
			}
			tokCounts.put(id, tokCounts.get(id) + 1);		
		}
		
		return tokCounts;
	}
	
	public static void main(String[] args) throws Exception {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		NGramFeatures uf = new UnigramFeatures(0);
		uf.init(threads);
		for (ForumThread thread : threads) {
			Map<Integer, Double> weights = uf.getWeights(thread);
			System.out.println(weights.size());
		}
	}
}