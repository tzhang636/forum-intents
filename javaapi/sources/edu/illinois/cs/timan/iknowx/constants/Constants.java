package edu.illinois.cs.timan.iknowx.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
	public static Map<String, Integer> label2id;
	static {
		label2id = new HashMap<String, Integer>();
		label2id.put("How should I manage or treat symptoms or conditions X?", 0);
		label2id.put("What is the cause of symptoms, conditions, or test findings X?", 1);
		label2id.put("Can drugs or treatments X cause adverse finding Y?", 2);
		label2id.put("Story telling, news, sharing or asking about experience, or soliciting support.", 3);
		label2id.put("Others.", 3);
		//label2id.put("Combination (&gt;2 of manage, cause, or adverse findings).", 4);
	}
	
	public static final String featDir = "edu.illinois.cs.timan.iknowx.features";
	//public static final String titleFeatDir = "edu.illinois.cs.timan.iknowx.titlefeatures";

	public static final String trainFile = "data/svm/train.txt";
	public static final String tuneFile = "data/svm/tune.txt";
	public static final String modelFile = "data/svm/train.model";
	
	public static final String trainNGramFile = "data/svm/trainNGram.txt";
	public static final String tuneNGramFile = "data/svm/tuneNGram.txt";
	public static final String modelNGramFile = "data/svm/trainNGram.model";
	
	public static final String testFile = "data/svm/test";
	
	public static final String goldDir = "data/gold/";
	public static List<String> xmlDirs = new ArrayList<String>();
	static {
		xmlDirs.add("data/allergy/");
		xmlDirs.add("data/breast cancer/");
		xmlDirs.add("data/depression/");
		xmlDirs.add("data/heart disorder/");
	}
	
	public static final String semTypesFile = "data/semantic types/SemTypes.txt";
	public static final String semGroupsFile = "data/semantic types/SemGroups.txt";
	public static final String qWordsFile = "data/question words/qWords.txt";
	public static final String negWordsFile = "data/sentiments/negWords.txt";
	
	public static final String postJsonDir = "data/json/posts/";
	public static final String titleJsonDir = "data/json/titles/";
}