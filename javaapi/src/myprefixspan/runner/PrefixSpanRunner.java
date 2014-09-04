package myprefixspan.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import myprefixspan.PrefixSpan;
import myprefixspan.sequence.Item;
import myprefixspan.sequence.ItemSet;
import myprefixspan.sequence.Sequence;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.metamap.MetaMapMapping;
import edu.illinois.cs.timan.iknowx.processor.Processor;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class PrefixSpanRunner {
	private static List<ForumThread> threads;
	static {
		threads = ThreadLoader.loadThreads();
		ThreadFilter.filter(threads);
	}
	
	// sequence, list of elements, set of items
	private static Map<String, Integer> item2id;
	private static Map<Integer, String> id2term;
	static {
		item2id = new HashMap<String, Integer>();
		id2term = new HashMap<Integer, String>();
	}
	
	private static final double minSeen = .01;
	private static final int maxGap = 1;
	private static final int maxPattern = -1;
	
	// filter more sents
	// segment by commas?
	
	public static void encodeItems(String inputFile) throws Exception {
		PrintWriter writer = new PrintWriter(inputFile, "UTF-8");

		for (ForumThread thread : threads) {
			String tid = thread.getId();
			String post = thread.getFirstPost();
			Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(tid);
			List<String> sents = Processor.getQSents(post);
			sents = Processor.labelReplace(sents, mapping);
			sents = Processor.toLowerCase(sents);
			//sents = Processor.remPuncts(sents);
			List<List<String>> sentToks = Processor.tokenize(sents);
			List<List<String>> stemToks = Processor.listStem(sentToks);
			
			// encode item ids
			for (List<String> toks : stemToks) {
				if (toks.isEmpty()) {
					continue;
				}
				for (String tok : toks) {
					if (!item2id.containsKey(tok)) {
						int id = item2id.size();
						item2id.put(tok, id);
						id2term.put(id, tok);
					}
					int id = item2id.get(tok);
					writer.print(id + " ");
				}
				writer.print("#");
				writer.println();
			}
		}
		
		writer.close();
	}
	
	public static void runPrefixSpan(String inputFile, String outputFile) throws Exception {
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		PrefixSpan ps = new PrefixSpan(inputFile, minSeen);
		ps.setMaxGap(maxGap);
		ps.setMaxPattern(maxPattern);
		ps.prefixSpan();
		List<Sequence> seqs = ps.getFreqPattern();
		
		for (Sequence seq : seqs) {
			List<ItemSet> sets = seq.getItemSets();
			for (ItemSet set : sets) {
				List<Item> items = set.getItems();
				//writer.print("( ");
				for (Item item : items) {
					int id = item.getLabel();
					String term = id2term.get(id);
					writer.print(term + " ");
				}
				//writer.print(")");
			}
			writer.println();
		}
		
		writer.close();
	}

	
	public static void main(String[] args) throws Exception {
		/*PrintWriter writer = new PrintWriter("qsents.txt", "UTF-8");
		List<String> keywords = new ArrayList<String>();
		keywords.add("any");
		keywords.add("anyone");
		keywords.add("anybody");
		
		int i = 0, j = 0;
		for (ForumThread thread : threads) {
			j++;
			writer.println(thread.getId());
			//writer.println(thread.getFirstPost());
			//writer.println();
			String post = thread.getFirstPost();
			List<String> sents = Processor.getSents(post, keywords);
			if (!sents.isEmpty()) {
				i++;
			}
			else {
				writer.println(thread.getFirstPost());
			}
			
			for (String sent : sents) {
				writer.println(sent);
			}
			writer.println();
		}
		writer.close();
		System.out.println(i);
		System.out.println(j);*/
		
		/*PrintWriter writer = new PrintWriter("sents.txt", "UTF-8");
		for (ForumThread thread : threads) {
			String id = thread.getId();
			String post = thread.getFirstPost();
			Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(id);
			
			List<String> sents = Processor.getQSents(post);
			sents = Processor.labelReplace(sents, mapping);
			sents = Processor.toLowerCase(sents);
			
			writer.println(id);
			for (String sent : sents) {
				writer.println(sent);
			}
			writer.println();
			
		}
		writer.close();*/
		
		String inputFile = "prefix_span/input/input.txt";
		System.out.println("Encoding...");
		encodeItems(inputFile);
		System.out.println("Finished encoding...");
		
		String outputFile = "prefix_span/output/output.txt";
		System.out.println("Running prefix span...");
		runPrefixSpan(inputFile, outputFile);
		System.out.println("Finished prefix span...");
	}
}