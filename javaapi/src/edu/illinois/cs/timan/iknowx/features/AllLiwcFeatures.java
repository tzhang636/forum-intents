package edu.illinois.cs.timan.iknowx.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.liwc.Liwc;

public class AllLiwcFeatures extends LiwcFeatures {

	Map<String, Map<String, Double>> output;
	List<String> features = new ArrayList<String>();
	
	protected AllLiwcFeatures(int baseOffset) {
		super(baseOffset);
	}

	@Override
	public void init(List<ForumThread> threads) throws Exception {
		Liwc.createInput(threads);
		output = Liwc.parseOutput();
		String tid = threads.get(0).getId();
		Map<String, Double> vals = output.get(tid);
		for (String feature : vals.keySet()) {
			//System.out.println(feature);
			features.add(feature);
		}
		endIdx += output.get(tid).size();
	}

	@Override
	public Map<Integer, Double> getWeights(ForumThread thread) throws Exception {
		String tid = thread.getId();
		Map<String, Double> vals = output.get(tid);
		Map<Integer, Double> weights = new HashMap<Integer, Double>();
		int idx = startIdx;
		for (String feature : features) {
			double weight = vals.get(feature);
			weights.put(idx, weight);
			idx++;
		}
		return weights;
	}
}