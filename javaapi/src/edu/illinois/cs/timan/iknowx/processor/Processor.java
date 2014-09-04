package edu.illinois.cs.timan.iknowx.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.illinois.cs.timan.iknowx.loader.QWordsLoader;
import edu.illinois.cs.timan.iknowx.util.ApacheNLPWrapper;
import edu.illinois.cs.timan.iknowx.util.PorterStemmer;
import edu.illinois.cs.timan.iknowx.util.StopWordChecker;

public class Processor {
	protected static List<String> qWords;
	static {
		try {
			qWords = QWordsLoader.loadQWords();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final PorterStemmer ps = new PorterStemmer();
	private static final StopWordChecker swc = new StopWordChecker();
	private static final ApacheNLPWrapper wrapper = new ApacheNLPWrapper();
	
	public static List<List<String>> listToLowerCase(List<List<String>> tokList) {
		List<List<String>> res = new ArrayList<List<String>>();
		for (List<String> toks : tokList) {
			res.add(toLowerCase(toks));
		}
		return res;
	}
	
	public static List<String> toLowerCase(List<String> toks) {
		List<String> res = new ArrayList<String>();
		for (String tok : toks) {
			res.add(toLowerCase(tok));
		}
		return res;
	}
	
	public static String toLowerCase(String str) {
		return str.toLowerCase();
	}
	
	public static List<List<String>> listStem(List<List<String>> tokList) {
		List<List<String>> res = new ArrayList<List<String>>();
		for (List<String> toks : tokList) {
			res.add(stem(toks));
		}
		return res;
	}
	
	public static List<String> remStopWords(List<String> toks) {
		List<String> res = new ArrayList<String>();
		Iterator<String> toksIter = toks.iterator();
		while (toksIter.hasNext()) {
			String tok = toksIter.next();
			if (!swc.isStopWord(tok)) {
				res.add(tok);
			}
		}
		return res;
	}
	
	public static List<String> stem(List<String> toks) {
		List<String> res = new ArrayList<String>();
		for (String tok : toks) {
			res.add(stem(tok));
		}
		return res;
	}
	
	public static String stem(String str) {
		ps.add(str.toCharArray(), str.length());
		ps.stem();
		return ps.toString();
	}
	
	public static List<List<String>> tokenize(List<String> strList) {
		List<List<String>> res = new ArrayList<List<String>>();
		for (String str : strList) {
			res.add(tokenize(str));
		}
		return res;
	}
	
	public static List<String> tokenize(String str) {
		List<String> res = new ArrayList<String>();
		res.addAll(Arrays.asList(wrapper.tokenize(str)));
		return res;
	}
	
	public static List<List<String>> tag(List<String> strList) {
		List<List<String>> res = new ArrayList<List<String>>();
		for (String str : strList) {
			List<String> tags = tag(str);
			res.add(tags);
		}
		return res;
	}
	
	public static List<String> tag(String str) {
		List<String> res = new ArrayList<String>();
		res.addAll(Arrays.asList(wrapper.tagPOS(wrapper.tokenize(str))));
		return res;
	}
	
	public static List<String> getSents(String str) {
		List<String> res = new ArrayList<String>();
		res.addAll(Arrays.asList(wrapper.getSentences(str)));
		return res;
	}
	
	public static List<String> getQSents(String str) {
		List<String> res = new ArrayList<String>();
		List<String> origSents = getSents(str);
		for (int i = 0; i < origSents.size(); ++i) {
			String origSent = origSents.get(i);
			List<String> toks = tokenize(origSent);
			for (String tok : toks) {
				String lTok = toLowerCase(tok);
				if (qWords.contains(lTok)) {
					res.add(origSent);
					break;
				}
			}
		}
		return res;
	}
	
	public static boolean isQSent(List<String> sentToks) {
		for (String tok : sentToks) {
			String lTok = toLowerCase(tok);
			if (qWords.contains(lTok)) {
				return true;
			}
		}
		return false;
	}
	
	public static List<String> getSents(String str, List<String> keywords) {
		List<String> res = new ArrayList<String>();
		List<String> origSents = getSents(str);
		for (int i = 0; i < origSents.size(); ++i) {
			String origSent = origSents.get(i);
			List<String> toks = tokenize(origSent);
			for (String tok : toks) {
				String lTok = toLowerCase(tok);
				if (keywords.contains(lTok)) {
					res.add(origSent);
					break;
				}
			}
		}
		return res;
	}
	
	public static List<String> labelSents(List<String> sents, Map<String, String> mapping) throws Exception {
		List<String> res = new ArrayList<String>();
		for (String sent : sents) {
			for (String phrase : mapping.keySet()) {
				sent = sent.replace(phrase, "<" + mapping.get(phrase) + "> " + phrase + " <" + mapping.get(phrase) + ">");
			}
			res.add(sent);
		}
		return res;
	}
	
	public static List<String> labelReplace(List<String> sents, Map<String, String> mapping) throws Exception {
		List<String> res = new ArrayList<String>();
		for (String sent : sents) {
			for (String phrase : mapping.keySet()) {
				sent = sent.replace(phrase, "<" + mapping.get(phrase) + ">");
			}
			res.add(sent);
		}
		return res;
	}
	
	public static String labelReplace(String sent, Map<String, String> mapping) throws Exception {
		String res = new String(sent);
		for (String phrase : mapping.keySet()) {
			res = sent.replace(phrase, "" + mapping.get(phrase) + "");
		}
		return res;
	}
	
	public static String remPuncts(String str) {
		return str.replaceAll("\\p{Punct}+", "");
	}
}