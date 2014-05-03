package edu.illinois.cs.timan.iknowx.features;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.metamap.MetaMapMapping;
import edu.illinois.cs.timan.iknowx.processor.Processor;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class SideEffectFeatures extends PatternFeatures {	
	public SideEffectFeatures(int baseOffset) {
		super(baseOffset);
	}
	
	@Override
	public void init(List<ForumThread> threads) throws Exception {
		ThreadFilter.filter(threads);
		getWeights(threads.get(0));	
	}

	@Override
	public Map<Integer, Double> getWeights(ForumThread thread) throws Exception {
		Map<Integer, Double> weights = new HashMap<Integer, Double>();
		int featIdx = startIdx;
		
		Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(thread.getId());
		List<String> sents = Processor.getSents(thread.getFirstPost()); // get post sentences
		sents = Processor.labelSents(sents, mapping); // label using metamap
		List<List<String>> toks = Processor.tokenize(sents); // tokenize post
		toks = Processor.listToLowerCase(toks); // change every token to lower case
		
		List<List<String>> stemToks = Processor.listStem(toks); // stem every token
		
		List<List<String>> tags = Processor.tag(sents); // POS tag all sentences
		
		featIdx = getReactionFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getSideEffectFeatures(toks, stemToks, tags, featIdx, weights);

		endIdx = featIdx;

		/*for (int i = startIdx; i < endIdx; ++i) {
			if (weights.get(i) == null) {
				weights.put(i, 0.0);
			}
		}*/
		return weights;
	}
	
	private int getReactionFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cReactionIN = false;
			boolean cChem = false;
			boolean isQSent = Processor.isQSent(sentToks);

			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("reaction") &&
							subTags.get(1).equals("IN")) {
						weights.put(idx, 1.0);
						cReactionIN = true;
					}
				}
				idx++;
				if (cReactionIN && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cReactionIN && (tok.contains("<chem>") || tok.contains("<proc>")) && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("<chem>") || subToks.get(1).equals("<proc>")) &&
							subStemToks.get(1).equals("reaction")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tok.contains("<chem>") || tok.contains("<proc>")) {
					cChem = true;
				}
				if (cChem && tok.equals("reaction")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getSideEffectFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cSEIn = false, cSEInChem = false;
			boolean cChem = false;
			boolean cSE = false;
			boolean isQSent = Processor.isQSent(sentToks);
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect") &&
							subTags.get(2).equals("IN")) {
						weights.put(idx, 1.0);
						cSEIn = true;
					}
				}
				idx++;
				if (cSEIn && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
					cSEInChem = true;
				}
				idx++;
				if (cSEInChem && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.contains("<chem>") || tok.contains("<proc>")) {
					cChem = true;
				}
				if (cChem && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							subStemToks.get(1).equals("side") &&
							subStemToks.get(2).equals("effect")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect")) {
						weights.put(idx, 1.0);
						cSE = true;
					}
				}
				idx++;
				if (cSE && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cSE && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads();
		//ThreadFilter.filter(threads);
		SideEffectFeatures sideEffectFeatures = new SideEffectFeatures(0);
		
		PrintWriter p = new PrintWriter("output/side-effects.txt", "UTF-8");
		for (ForumThread thread : threads) {
			Map<Integer, Double> weights = sideEffectFeatures.getWeights(thread);
			p.println(thread.getThreadId());
			p.println(weights.toString());
			
			Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(thread.getId());
			List<String> sents = Processor.getSents(thread.getFirstPost()); // get questions
			sents = Processor.labelSents(sents, mapping); // label using metamap
			for (String sent : sents) {
				p.println("-" + sent);
			}
			
			p.println("---------------------------------------------------------");
			p.println(thread.getFirstPost());
			p.println();
		}
		p.close();
	}
}