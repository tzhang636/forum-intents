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

public class ManageFeatures extends PatternFeatures {
	public ManageFeatures(int baseOffset) {
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
		
		featIdx = getHowFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getWhatFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getHelpFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getWorkFeatures(toks, stemToks, tags, featIdx, weights);
		featIdx = getTryFeatures(toks, stemToks, tags, featIdx, weights);
		
		endIdx = featIdx;

		/*for (int i = startIdx; i < endIdx; ++i) {
			if (weights.get(i) == null) {
				weights.put(i, 0.0);
			}
		}*/
		return weights;
	}
	
	private int getTryFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;
		
		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cIf = false, cTry = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tok.equals("if")) {
					cIf = true;
				}
				if (cIf && (stemTok.equals("try") || stemTok.equals("tri"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (modAux.contains(subTokList.get(0)) &&
							subTokList.get(1).equals("try")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (stemTok.equals("try") || stemTok.equals("tri")) {
					cTry = true;
				}
				if (cTry && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getWorkFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;

		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cDo = false, cDoChem = false;
			boolean cHas = false, cHasChem = false;
			boolean cIf = false, cIfHas = false, cIfHasChem = false, cIfChem = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx == 0 && (tok.equals("do") || tok.equals("does"))) {
					cDo = true;
				}
				if (cDo && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cDo && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cDoChem = true;
				}
				if (cDoChem && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tokIdx == 0 && (tok.equals("has") || tok.equals("have") || tok.equals("had"))) {
					cHas = true;
				}
				if (cHas && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cHas && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cHasChem = true;
				}
				if (cHasChem && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.equals("if")) {
					cIf = true;
				}
				if (cIf && (tok.equals("has") || tok.equals("have") || tok.equals("had"))) {
					cIfHas = true;
				}
				if (cIfHas && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIfHas && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cIfHasChem = true;
				}
				if (cIfHasChem && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIf && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIf && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cIfChem = true;
				}
				if (cIfChem && stemTok.equals("work")) {
					weights.put(idx, 1.0);
				}
				idx++;
				
			}
		}
		return idx;
	}

	private int getHelpFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;

		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cWho = false;
			boolean cDo = false, cDoChem = false;
			boolean cIs = false, cIsChem = false;
			boolean cMD = false, cMDChem = false;
			boolean cIf = false, cIfChem = false;
			
			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tok.equals("who")) {
					cWho = true;
				}
				
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+3);
					if (subTokList.get(0).equals("who") &&
							modAux.contains(subTokList.get(1)) &&
							subTokList.get(2).equals("help")) {
						weights.put(idx, 1.0); // ...who can/could/may/might/will/would help...
					}
				}
				idx++;
				if (cWho && tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (modAux.contains(subTokList.get(0)) &&
							subTokList.get(1).equals("help")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				
				if (tokIdx == 0 && (tok.equals("do") || tok.equals("does"))) {
					cDo = true;
				}
				if (cDo && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cDo && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cDoChem = true;
				}
				if (cDoChem && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.equals("is") || tok.equals("are")) {
					cIs = true;
				}
				if (cIs && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIs && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cIsChem = true;
				}
				if (cIsChem && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (modAux.contains(tag)) {
					cMD = true;
				}
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (modAux.contains(subTokList.get(0)) &&
							subTokList.get(1).equals("help")) {
						weights.put(idx, 1.0); // ...can/could/may/might/will/would help...
					}
				}
				idx++;
				if (cMD && stemTok.equals("help")) {
					weights.put(idx, 1.0); // ...can/could/may/might/will/would...help...
				}
				idx++;
				if (cMD && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cMDChem = true;
				}
				if (cMDChem && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (tok.equals("if")) {
					cIf = true;
				}
				if (cIf && stemTok.equals("help")) {
					weights.put(idx, 1.0);
				}
				idx++;
				if (cIf && (tok.contains("<chem>") || tok.contains("<proc>"))) {
					cIfChem = true;
				}
				if (cIfChem && stemTok.equals("help")) {
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
			boolean cWhat = false, cWhatDo = false;

			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+3);
					if (subTokList.get(0).equals("what") &&
							subTokList.get(1).equals("to") &&
							subTagList.get(2).equals("VB")) {
						//weights.put(idx, 1.0); // ...what to vb...
					}
					if (subTokList.get(0).equals("what") &&
							subTokList.get(1).equals("to") &&
							subTokList.get(2).equals("do")) {
						weights.put(idx+1, 1.0); // ...what to do...
					}
				}
				idx += 1;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTokList.get(0).equals("what") &&
							modAux.contains(subTokList.get(1))) {
						//weights.put(idx, 1.0); // ...what can/could/may/might/will/would...
					}
				}
				//idx++;
				
				if (tok.equals("what")) {
					cWhat = true;
				}
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTokList.get(0).equals("what") &&
							(subTokList.get(1).equals("do") || subTokList.get(1).equals("does"))) {
						//weights.put(idx, 1.0); // ...what do/does...
						cWhatDo = true;
					}
				}
				//idx++;
				if (cWhatDo && tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTagList.get(0).equals("PRP") &&
							subTokList.get(1).equals("do")) {
						weights.put(idx, 1.0); // ...what do/does...PRP do...
					}
				}
				idx++;
				
				if (tokIdx+4 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+4);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+4);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+4);
					if (subTokList.get(0).equals("what") && 
							modAux.contains(subTokList.get(1)) &&
							subTagList.get(2).equals("PRP") &&
							subTokList.get(3).equals("do")) {
							weights.put(idx, 1.0); // ...what can/could/may/might/will/would PRP do...
					}
				}
				idx++;
				
				if (cWhat && tokIdx+3 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+3);
					if (modAux.contains(subTokList.get(0)) &&
							subTagList.get(1).equals("PRP") &&
							subTokList.get(2).equals("do")) {
							weights.put(idx, 1.0); // ...what...can/could/may/might/will/would PRP do...
					}
				}
				idx++;
				
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+3);
					if (subTokList.get(0).equals("what") &&
							modAux.contains(subTokList.get(1)) &&
							subTokList.get(2).equals("help")) {
						weights.put(idx, 1.0); // ...what can/could/may/might/will/would help...
					}
				}
				idx++;
				if (cWhat && tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (modAux.contains(subTokList.get(0)) &&
							subTokList.get(1).equals("help")) {
						weights.put(idx, 1.0); // ...what...can/could/may/might/will/would help...
					}
				}
				idx++;
				
			}
		}
		return idx;
	}
	
	private int getHowFeatures(List<List<String>> toks, List<List<String>> stemToks, List<List<String>> tags, 
			int featIdx, Map<Integer, Double> weights) {
		int idx = featIdx;

		// for every sentence
		for (int sentIdx = 0; sentIdx < toks.size(); ++sentIdx) {
			List<String> sentToks = toks.get(sentIdx);
			List<String> sentStemToks = stemToks.get(sentIdx);
			List<String> sentTags = tags.get(sentIdx);
			boolean cHow = false;

			// for every token in sentence
			for (int tokIdx = 0; tokIdx < sentToks.size(); ++tokIdx) {
				idx = featIdx;
				String tok = sentToks.get(tokIdx);
				String stemTok = sentStemToks.get(tokIdx);
				String tag = sentTags.get(tokIdx);
				
				if (tokIdx+3 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+3);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+3);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+3);
					if (subTokList.get(0).equals("how") &&
							subTokList.get(1).equals("to") &&
							subTagList.get(2).equals("VB")) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTokList.get(0).equals("how") &&
							modAux.contains(subTokList.get(1))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tok.equals("how")) {
					cHow = true;
				}
				if (cHow && modAux.contains(tok)) {
					//weights.put(idx, 1.0);
				}
				//idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTokList.get(0).equals("how") &&
							(subTokList.get(1).equals("do") || subTokList.get(1).equals("does"))) {
						weights.put(idx, 1.0);
					}
				}
				idx++;
				if (tokIdx+2 <= sentToks.size()) {
					List<String> subTokList = sentToks.subList(tokIdx, tokIdx+2);
					List<String> subStemList = sentStemToks.subList(tokIdx, tokIdx+2);
					List<String> subTagList = sentTags.subList(tokIdx, tokIdx+2);
					if (subTokList.get(0).equals("how") &&
							(subTokList.get(1).equals("is") || subTokList.get(1).equals("are"))) {
						//weights.put(idx, 1.0);
					}
				}
				//idx++;
				
			}
		}
		return idx;
	}

	public static void main(String[] args) throws Exception {		
		List<ForumThread> threads = ThreadLoader.loadThreads();
		//ThreadFilter.filter(threads);
		ManageFeatures manageFeatures = new ManageFeatures(0);
		
		PrintWriter p = new PrintWriter("output/manage.txt", "UTF-8");
		for (ForumThread thread : threads) {
			Map<Integer, Double> weights = manageFeatures.getWeights(thread);
			p.println(thread.getThreadId());
			p.println(weights.toString());
			
			Map<String, String> mapping = MetaMapMapping.getFirstPostMapping(thread.getId());
			List<String> sents = Processor.getQSents(thread.getFirstPost()); // get questions
			sents = Processor.labelSents(sents, mapping); // label using metamap
			List<List<String>> toks = Processor.tokenize(sents); // tokenize post
			toks = Processor.listToLowerCase(toks); // change every token to lower case
			
			List<List<String>> stemToks = Processor.listStem(toks); // stem every token
			
			List<List<String>> tags = Processor.tag(sents); // POS tag all sentences
			
			/*for (int i = 0; i<toks.size(); ++i) {
				List<String> sentToks = toks.get(i);
				List<String> sentTags = tags.get(i);
				p.print("-");
				for (int j = 0; j<sentToks.size(); ++j) {
					String sentTok = sentToks.get(j);
					String sentTag = sentTags.get(j);
					p.print(sentTok + "-" + sentTag + ", ");
				}
				p.println();
			}*/
			
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