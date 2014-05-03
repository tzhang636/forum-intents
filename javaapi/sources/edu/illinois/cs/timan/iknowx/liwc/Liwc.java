package edu.illinois.cs.timan.iknowx.liwc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class Liwc {
	
	public static void createInput(List<ForumThread> threads) throws Exception {
		for (ForumThread thread : threads) {
			String threadId = thread.getId();
			PrintWriter pw = new PrintWriter("data/liwc/input/" + threadId);
			pw.println(thread.getFirstPost());
			pw.close();
		}
	}
	
	public static Map<String, Map<String, Double>> parseOutput() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("data/liwc/output/results"));
    StringBuilder sb = new StringBuilder();
    Map<String, Map<String, Double>> output = new HashMap<String, Map<String, Double>>();
    
    String line = br.readLine(); // header line
    String[] features = line.split("\\t");
    
    line = br.readLine(); // read next line
    while (line != null) {
  		String[] values = line.split("\\t");  		
  		Map<String, Double> storedValues = new HashMap<String, Double>();
  		for (int i = 1; i < values.length; ++i) {
  			storedValues.put(features[i], Double.parseDouble(values[i]));
  		}
  		output.put(values[0], storedValues);
      line = br.readLine();
    }
    br.close();
    return output;
	}
	
	public static Map<Integer, Map<String, Double>> average(List<ForumThread> threads, Map<String, Map<String, Double>> output) {
		Map<Integer, Map<String, Double>> res = new HashMap<Integer, Map<String, Double>>();
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (ForumThread thread : threads) {
			String tid = thread.getId();
			int label = Constants.label2id.get(thread.getLabel());
			if (!res.containsKey(label)) {
				res.put(label, new HashMap<String, Double>());
			}
			Map<String, Double> sum = res.get(label);
			Map<String, Double> values = output.get(tid);
			for (String feature : values.keySet()) {
				double value = values.get(feature);
				if (!sum.containsKey(feature)) {
					sum.put(feature, 0.0);
				}
				double s = sum.get(feature);
				s += value;
				sum.put(feature, s);
			}
			
			if (!counts.containsKey(label)) {
				counts.put(label, 0);
			}
			counts.put(label, counts.get(label) + 1);
		}
		
		for (int label : res.keySet()) {
			int count = counts.get(label);
			Map<String, Double> values = res.get(label);
			for (String feature : values.keySet()) {
				double val = values.get(feature);
				val /= count;
				values.put(feature, val);
			}
		}
		
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
		
		createInput(threads);
		Map<String, Map<String, Double>> output = parseOutput();
		
		Map<Integer, Map<String, Double>> avg = average(threads, output);
		Map<String, Double> p = avg.get(0);
		System.out.print("Label\t");
		for (String feature : p.keySet()) {
			System.out.print(feature + "\t");
		}
		System.out.println();
		
		for (int label : avg.keySet()) {
			System.out.print(label + "\t");
			Map<String, Double> vals = avg.get(label);
			for (String feature : vals.keySet()) {
				double val = vals.get(feature);
				System.out.print(val + "\t");
			}
			System.out.println();
		}
		
		int count = 0;
		double words = 0;
		for (ForumThread thread : threads) {
			int label = Constants.label2id.get(thread.getLabel());
			if (label == 0) {
				count++;
				words += thread.getFirstPost().split("\\s+").length;
			}
		}
		System.out.println(words/count);
		
		/*for (String tid : output.keySet()) {
			System.out.print(tid + "\t");
			Map<String, Double> values = output.get(tid);
			for (String feature : values.keySet()) {
				double val = values.get(feature);
				System.out.print(feature + ": " + val + "\t");
			}
			System.out.println();
		}*/
	}
}