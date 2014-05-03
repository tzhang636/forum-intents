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

public class CauseFeatures extends PatternFeatures {
	public CauseFeatures(int baseOffset) {
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
		List<String> sents = Processor.getQSents(thread.getFirstPost()); // get questions
		sents = Processor.labelSents(sents, mapping); // label using metamap
		List<List<String>> toks = Processor.tokenize(sents); // tokenize post
		toks = Processor.listToLowerCase(toks); // change every token to lower case
		
		List<List<String>> stemToks = Processor.listStem(toks); // stem every token
		
		List<List<String>> tags = Processor.tag(sents); // POS tag all sentences
		
		featIdx = getIsFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getHaveFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getWhatFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getIfFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getSoundFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getCouldFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getWhyFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getDontKnowFeatures(toks, stemToks, tags, featIdx, weights);

		endIdx = featIdx;

		/*for (int i = startIdx; i < endIdx; ++i) {
			if (weights.get(i) == null) {
				weights.put(i, 0.0);
			}
		}*/
		return weights;
	}
	
	private int getDontKnowFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cDontKnow = false, cIf = false;
			
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
					if (subStemToks.get(0).equals("don't") &&
							subToks.get(1).equals("know")) {
						cDontKnow = true;
					}
				}
				if (cDontKnow && tok.equals("if")) {
					weights.put(idx, 1.0);
					cIf = true;
				}
				idx++;
				if (cIf && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getWhyFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cCould = false, cCause = false, cBe = false, cBeCause = false, cHave = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx == 0 && tok.equals("why")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.equals("why")) {
					//weights.put(idx, 1.0);
				}
				//idx++;
				
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
			boolean cCould = false, cCouldCause = false, cCouldBe = false, cCouldBeCause = false, cCouldHave = false;
			boolean cCouldCaus = false, cCouldBe2 = false, cCouldBeCaus = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tok.equals("could") || tok.equals("can")) {
					cCould = true;
				}
				if (cCould && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cCouldCaus = false;
				}
				idx++;
				if (cCouldCaus && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("could") || subToks.get(0).equals("can")) &&
							(subStemToks.get(1).equals("caus"))) {
						weights.put(idx, 1.0);
						cCouldCause = true;
					}
				}
				idx++;
				if (cCouldCause && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cCould && tok.equals("be")) {
					cCouldBe2 = true;
				}
				if (cCouldBe2 && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("could") || subToks.get(0).equals("can")) &&
							(subToks.get(1).equals("be"))) {
						cCouldBe = true;
					}
				}
				if (cCouldBe && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cCouldBe2 && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cCouldBeCaus = true;
				}
				idx++;
				if (cCouldBeCaus && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cCouldBe && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cCouldBeCause = true;
				}
				idx++;
				if (cCouldBeCause && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cCould && tok.equals("have")) {
					weights.put(idx, 1.0);
					cCouldHave = true;
				}
				idx++;
				if (cCouldHave && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getSoundFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cSound = false, cDiso = false, cSoundLike = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (stemTok.equals("sound")) {
					cSound = true;
				}
				if (cSound && tok.equals("related")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cSound && tok.contains("<diso>")) {
					cDiso = true;
				}
				if (cDiso && tok.equals("related")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subStemToks.get(0).equals("sound") &&
							subToks.get(1).equals("like")) {
						weights.put(idx, 1.0);
						cSoundLike = true;
					}
				}
				idx++;
				if (cSoundLike && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getIfFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cIf = false, cDiso = false, cHas = false;
			boolean cIfPrpHas = false, cIfPrpHas2 = false;
			boolean cIfItIs = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tok.equals("if")) {
					cIf = true;
				}
				if (cIf && tok.equals("related")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("if")) &&
							(subToks.get(1).equals("it") || subToks.get(1).equals("this") || subToks.get(1).equals("that")) &&
							(subToks.get(2).equals("is"))){
						cIfItIs = true;
					}
					if ((subToks.get(0).equals("if")) &&
							(subToks.get(1).equals("they") || subToks.get(1).equals("these")) &&
							(subToks.get(2).equals("are"))){
						cIfItIs = true;
					}
				}
				if (cIfItIs && tok.equals("normal")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIf && tok.contains("<diso>")) {
					cDiso = true;
				}
				if (cDiso && tok.equals("related")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("if")) &&
							(subTags.get(1).equals("PRP")) &&
							(subToks.get(2).equals("has") || subToks.get(2).equals("have") || subToks.get(2).equals("had"))){
						//weights.put(idx, 1.0);
						cIfPrpHas2 = true;
					}
				}
				//idx++;
				if (cIfPrpHas2 && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIf && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subTags.get(0).equals("PRP") &&
							(subToks.get(1).equals("has") || subToks.get(1).equals("have") || subToks.get(1).equals("had"))) {
						//weights.put(idx, 1.0);
						cIfPrpHas = true;
					}
				}
				//idx++;
				if (cIfPrpHas && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getWhatFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cWhat = false, cWhatCould = false, cWhatCouldCaus = false, cWhatCaus = false, cWhatCouldCause = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tok.equals("what")) {
					cWhat = true;
				}
				if (cWhat && tok.equals("wrong")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cWhat && tok.equals("mean")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cWhat && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cWhatCaus = true;
				}
				idx++;
				if (cWhatCaus && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("what")) &&
							(subToks.get(1).equals("could") || subToks.get(1).equals("can"))) {
						cWhatCould = true;
					}
				}
				if (cWhatCould && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cWhatCouldCaus = true;
				}
				idx++;
				if (cWhatCouldCaus && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("what")) &&
							(subToks.get(1).equals("could") || subToks.get(1).equals("can")) &&
							(subStemToks.get(2).equals("caus"))) {
						weights.put(idx, 1.0);
						cWhatCouldCause = true;
					}
				}
				idx++;
				if (cWhatCouldCause && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if ((subToks.get(0).equals("what")) &&
							(subToks.get(1).equals("could") || subToks.get(1).equals("can")) &&
							(subTags.get(2).equals("DT")) &&
							(subToks.get(3).equals("be"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+4);
					if ((subToks.get(0).equals("what")) &&
							(subToks.get(1).equals("could") || subToks.get(1).equals("can")) &&
							(subTags.get(2).equals("PRP")) &&
							(subToks.get(3).equals("be"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (cWhatCould && tok.equals("be")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("what")) && 
							(subToks.get(1).equals("it") || subToks.get(1).equals("this") || subToks.get(1).equals("that")) &&
							subToks.get(2).equals("is")) {
						weights.put(idx, 1.0);
					}
					if ((subToks.get(0).equals("what")) && 
							(subToks.get(1).equals("they") || subToks.get(1).equals("these")) &&
							subToks.get(2).equals("are")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("what")) && 
							(subToks.get(1).equals("is")) &&
							(subToks.get(2).equals("it") || subToks.get(2).equals("this") || subToks.get(2).equals("that"))) {
						weights.put(idx, 1.0);
					}
					if ((subToks.get(0).equals("what")) && 
							(subToks.get(1).equals("are")) &&
							(subToks.get(2).equals("they") || subToks.get(2).equals("these"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (cWhat && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if ((subToks.get(0).equals("it") || subToks.get(0).equals("this") || subToks.get(0).equals("that")) &&
							subToks.get(1).equals("is")) {
						weights.put(idx, 1.0);
					}
					if ((subToks.get(0).equals("they") || subToks.get(0).equals("these")) &&
							subToks.get(1).equals("are")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (cWhat && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subToks.get(0).equals("is") &&
							(subToks.get(1).equals("it") || subToks.get(1).equals("this") || subToks.get(1).equals("that"))) {
						weights.put(idx, 1.0);
					}
					if (subToks.get(0).equals("are") &&
							(subToks.get(1).equals("they") || subToks.get(1).equals("these"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getHaveFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			
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
					if ((subToks.get(0).equals("do") || subToks.get(0).equals("does")) && 
							(subTags.get(1).equals("PRP")) &&
							(subToks.get(2).equals("have"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+3);
					if ((subToks.get(0).equals("could") || subToks.get(0).equals("can")) && 
							(subTags.get(1).equals("PRP")) &&
							(subToks.get(2).equals("have"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				
			}
		}
		return idx;
	}

	private int getIsFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cIsIt = false, cAreThey = false;
			boolean cIsItDiso = false, cIsItCause = false;
			boolean cIsItPossible = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx == 0 && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subToks.get(0).equals("is") &&
							(subToks.get(1).equals("it") || subToks.get(1).equals("this") || subToks.get(1).equals("that"))) {
						cIsIt = true;
					}
				}
				if (tokIdx == 0 && tokIdx+2 <= sentToks.size()) {
					List<String> subToks = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemToks = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTags = sentTags.subList(tokIdx, tokIdx+2);
					if (subToks.get(0).equals("are") &&
							(subToks.get(1).equals("they") || subToks.get(1).equals("these"))) {
						cAreThey = true;
					}
				}
				/*if (cIsIt || cAreThey) {
					weights.put(idx, 1.0);
				}
				idx++;*/
				if ((cIsIt || cAreThey) && tok.equals("because")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if ((cIsIt || cAreThey) && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
					cIsItDiso = true;
				}
				idx++;
				if ((cIsIt || cAreThey) && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
					cIsItCause = true;
				}
				idx++;
				if (cIsItCause && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIsItCause && stemTok.equals("caus")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if ((cIsIt || cAreThey) && stemTok.equals("possibl")) {
					weights.put(idx, 1.0);
					cIsItPossible = true;
				}
				idx++;
				if (cIsItPossible && tok.contains("<diso>")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIsItDiso && stemTok.equals("possibl")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if ((cIsIt || cAreThey) && tok.equals("normal")) {
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
		CauseFeatures causeFeatures = new CauseFeatures(0);
		
		PrintWriter p = new PrintWriter("output/cause.txt", "UTF-8");
		for (ForumThread thread : threads) {
			Map<Integer, Double> weights = causeFeatures.getWeights(thread);
			p.println(thread.getThreadId());
			p.println(weights.toString());
			
			Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(thread.getId());
			List<String> sents = Processor.getQSents(thread.getFirstPost()); // get questions
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