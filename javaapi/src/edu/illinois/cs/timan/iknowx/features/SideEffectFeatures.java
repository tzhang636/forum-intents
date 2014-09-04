package edu.illinois.cs.timan.iknowx.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.processor.Processor;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;

public class SideEffectFeatures extends PatternFeatures {	
	public SideEffectFeatures(int baseOffset, int k) {
		super(baseOffset, k);
	}
	
	@Override
	public void init(List<ForumThread> threads) throws Exception {
		ThreadFilter.filter(threads);
		getWeights(threads.get(0), Constants.postJsonDir);	
	}

	@Override
	public Map<Integer, Double> getWeights(ForumThread thread, String jsonDir) throws Exception {
		List<List<String>> toks = tokQSents(thread.getId(), thread.getFirstPost(), jsonDir);
		List<List<String>> stemToks = Processor.listStem(toks);
		List<List<String>> tags = tokQSents(thread.getId(), thread.getFirstPost(), jsonDir);
		
		if (toks.isEmpty()) {
			toks = tokSents(thread.getId(), thread.getFirstPost(), jsonDir);
			stemToks = Processor.listStem(toks);
			tags = tagSents(thread.getId(), thread.getFirstPost(), jsonDir);
		}
		
		Map<Integer, Double> weights = new HashMap<Integer, Double>();
		int featIdx = startIdx;
		featIdx = getReactionFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getSideEffectFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getSafeFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getWithdrawalFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getDiscontinueFeatures(toks, stemToks, tags, featIdx, weights);
		//featIdx = getDisoFeatures(toks, stemToks, tags, featIdx, weights);
		//featIdx = getAllergicFeatures(toks, stemToks, tags, featIdx, weights);
		//featIdx = getMakeFeatures(toks, stemToks, tags, featIdx, weights);
		//featIdx = getProblemFeatures(toks, stemToks, tags, featIdx, weights);
		//featIdx = getCouldFeatures(toks, stemToks, tags, featIdx, weights);
		
		endIdx = featIdx;
		
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
				
				if ((k == 4 || k == 7 || k == 8 || k == 1) && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("reaction") &&
							(subTags.get(1).equals("IN") || subTags.get(1).equals("TO"))) {
						weights.put(idx, 1.0);
						cReactionIN = true;
					}
				}
				idx++;
				if ((k == 6 || k == 8 || k == 3) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if (subStemToks.get(0).equals("reaction") &&
							(subTags.get(1).equals("IN") || subTags.get(1).equals("TO")) &&
							(subToks.get(2).contains("<chem>") || subToks.get(2).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if ((k == 6 || k == 8 || k == 3) && cReactionIN && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				/*if (cReactionIN && (tok.contains("<chem>") || tok.contains("<proc>")) && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;*/
				if ((k == 5 || k == 7 || k == 8 || k == 2) && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							subStemToks.get(1).equals("reaction")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tok.contains("<chem>") || tok.contains("<proc>")) {
					cChem = true;
				}
				if ((k == 5 || k == 7 || k == 8 || k == 2) && cChem && tok.equals("reaction")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if ((k == 4 || k == 7 || k == 8 || k == 1) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if (subToks.get(0).equals("have") &&
							(subTags.get(1).equals("DT")) &&
							(subStemToks.get(2).equals("reaction")) && 
							isQSent) {
						weights.put(idx, 1.0);
					}
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
				
				if ((k == 4 || k == 7 || k == 8 || k == 1) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect") &&
							(subTags.get(2).equals("IN") || subTags.get(2).equals("TO"))) {
						weights.put(idx, 1.0);
						cSEIn = true;
					}
				}
				idx++;
				if ((k == 6 || k == 8 || k == 3) && tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect") &&
							(subTags.get(2).equals("IN") || subTags.get(2).equals("TO")) && 
							(subToks.get(3).contains("<chem>") || subToks.get(3).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if ((k == 6 || k == 8 || k == 3) && cSEIn && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
					cSEInChem = true;
				}
				idx++;
				/*if (cSEInChem && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;*/
				if (tok.contains("<chem>") || tok.contains("<proc>")) {
					cChem = true;
				}
				if ((k == 5 || k == 7 || k == 8 || k == 2) && tokIdx+3 <= sentToks.size()) {
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
				if ((k == 5 || k == 7 || k == 8 || k == 2) && cChem && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("side") &&
							subStemToks.get(1).equals("effect")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (((k >= 4 && k <= 8) || k == 0) && tokIdx+2 <= sentToks.size()) {
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
				/*if (cSE && isQSent) {
					weights.put(idx, 1.0);
				}
				idx++;*/
				if ((k == 5 || k == 7 || k == 8 || k == 2) && cSE && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getAllergicFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean a = false;
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
					if (subToks.get(0).equals("allergic") &&
							subTags.get(1).equals("TO")) {
						weights.put(idx, 1.0);
						a = true;
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if (subToks.get(0).equals("allergic") &&
							subTags.get(1).equals("TO") &&
							(subToks.get(2).contains("<chem>") || subToks.get(2).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (a && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getMakeFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean mpf = false, mpd = false, cmpf = false, cmp = false;
			boolean isQSent = Processor.isQSent(sentToks);
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if ((subStemToks.get(0).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(1).equals("PRP") || subTags.get(1).equals("PRP$")) &&
							(subToks.get(2).equals("feel")) && 
							(negWords.contains(subToks.get(3)))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subStemToks.get(0).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(1).equals("PRP") || subTags.get(1).equals("PRP$")) &&
							(subToks.get(2).equals("feel"))) {
						mpf = true;
					}
				}
				if (mpf && negWords.contains(tok)) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subStemToks.get(0).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(1).equals("PRP") || subTags.get(1).equals("PRP$")) &&
							(subToks.get(2).contains("<diso>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subStemToks.get(0).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(1).equals("PRP") || subTags.get(1).equals("PRP$"))) {
						mpd = true;
					}
				}
				if (mpd && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+5 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+5);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+5);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+5);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							(subStemToks.get(1).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(2).equals("PRP") || subTags.get(2).equals("PRP$")) &&
							(subToks.get(3).equals("feel")) && 
							(negWords.contains(subToks.get(4)))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							(subStemToks.get(1).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(2).equals("PRP") || subTags.get(2).equals("PRP$")) &&
							(subToks.get(3).equals("feel"))) {
						cmpf = true;
					}
				}
				if (cmpf && negWords.contains(tok)) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							(subStemToks.get(1).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(2).equals("PRP") || subTags.get(2).equals("PRP$")) &&
							(subToks.get(3).contains("<diso>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							(subStemToks.get(1).equals("make") || subStemToks.get(1).equals("made")) &&
							(subTags.get(2).equals("PRP") || subTags.get(2).equals("PRP$"))) {
						cmp = true;
					}
				}
				if (cmp && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getProblemFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean p = false, pi = false, prt = false, c = false, cc = false;
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
					if ((subStemToks.get(0).equals("problem")) &&
							(subTags.get(1).equals("IN")) &&
							(subToks.get(2).contains("<chem>") || subToks.get(2).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subStemToks.get(0).equals("problem")) &&
							(subTags.get(1).equals("IN"))) {
						pi = true;
					}
				}
				if (pi && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (stemTok.equals("problem")) {
					p = true;
				}
				if (p && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("related")) &&
							(subTags.get(1).equals("TO")) &&
							(subToks.get(2).contains("<chem>") || subToks.get(2).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (p && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("related")) &&
							(subTags.get(1).equals("TO"))) {
						prt = true;
					}
				}
				if (prt && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.contains("<chem>") || tok.contains("<proc>")) {
					c = true;
				}
				if (c && stemTok.equals("problem")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (c && stemTok.equals("caus")) {
					cc = true;
				}
				if (cc && stemTok.equals("problem")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getCouldFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean c = false, cc = false, ccc = false, ccb = false, ccbc = false;
			boolean co = false, cob = false, cobc = false, cobci = false;
			boolean d = false, dco = false, dcob = false, dcobc = false, dcobci = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if ((tok.contains("<chem>") || tok.contains("<proc>"))) {
					c = true;
				}
				if (c && (tok.equals("could") || tok.equals("can"))) {
					cc = true;
				}
				if (cc && stemTok.equals("caus")) {
					ccc = true;
					weights.put(idx, 1.0);
				}
				idx++;
				if (ccc && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cc && tok.equals("be")) {
					ccb = true;
				}
				if (ccb && stemTok.equals("caus")) {
					ccbc = true;
					weights.put(idx, 1.0);
				}
				idx++;
				if (ccbc && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
				if ((tok.equals("could") || tok.equals("can"))) {
					co = true;
				}
				if (co && tok.equals("be")) {
					cob = true;
				}
				if (cob && stemTok.equals("caus")) {
					cobc = true;
				}
				if (cobc && tag.equals("IN")) {
					cobci = true;
				}
				if (cobci && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
				if (tok.contains("<diso>")) {
					d = true;
				}
				if (d && (tok.equals("could") || tok.equals("can"))) {
					dco = true;
				}
				if (dco && tok.equals("be")) {
					dcob = true;
				}
				if (dcob && stemTok.equals("caus")) {
					dcobc = true;
				}
				if (dcobc && tag.equals("IN")) {
					dcobci = true;
				}
				if (dcobci && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getSafeFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean isQSent = Processor.isQSent(sentToks);
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if ((k == 5 || k == 7 || k == 8 || k == 2) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("is") || subToks.get(0).equals("are")) &&
							(subToks.get(1).contains("<chem>") || subToks.get(1).contains("<proc>")) &&
							(subToks.get(2).equals("safe")) && 
							isQSent) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if ((k == 5 || k == 7 || k == 8 || k == 2) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).contains("<chem>") || subToks.get(0).contains("<proc>")) &&
							(subToks.get(1).equals("is") || subToks.get(1).equals("are")) &&
							(subToks.get(2).contains("safe"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getDisoFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean di = false;
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
					if ((subToks.get(0).contains("<diso>")) &&
							(subTags.get(1).equals("IN")) &&
							(subToks.get(2).contains("<chem>") || subToks.get(2).contains("<proc>"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).contains("<diso>")) &&
							(subTags.get(1).equals("IN"))) {
						di = true;
					}
				}
				if (di && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getWithdrawalFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean isQSent = Processor.isQSent(sentToks);
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (((k >= 4 && k <= 8) || k == 0) && tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subStemToks.get(0).equals("withdraw")) &&
							(subStemToks.get(1).equals("side")) &&
							(subStemToks.get(2).equals("effect"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (((k >= 4 && k <= 8) || k == 0) && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subStemToks.get(0).equals("withdraw")) &&
							(subStemToks.get(1).equals("symptom"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (((k >= 4 && k <= 8) || k == 0) && stemTok.equals("withdraw")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getDiscontinueFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean d = false;
			boolean isQSent = Processor.isQSent(sentToks);
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (stemTok.equals("discontinu")) {
					d = true;
				}
				if ((k == 5 || k == 7 || k == 8 || k == 2) && d && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}

}