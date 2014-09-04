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
		label2id.put("Combination (&gt;2 of manage, cause, or adverse findings).", 4);
	}
	
	public static final String twoClassOut = "data/svm/two class/out";
	public static final String twoClassTrain = "data/svm/two class/train";
	public static final String twoClassTest = "data/svm/two class/test";
	public static final String twoClassModel = "data/svm/two class/train.model";

	public static final String fourClassOut = "data/svm/four class/out";
	public static final String fourClassOut2 = "data/svm/four class/out2";

	public static final String featOut = "data/svm/four class/feat.out";
	public static final String mixOut = "data/svm/four class/mix.out";
	public static final String forumOut = "data/svm/four class/forum.out";
	public static final String baselineMixOut = "data/svm/four class/baselineMix.out";
	public static final String baselineForumOut = "data/svm/four class/baselineForum.out";

	public static final String fourClassTrain = "data/svm/four class/train";
	public static final String fourClassTest = "data/svm/four class/test";
	public static final String fourClassModel = "data/svm/four class/train.model";
	
	public static final String allergyOut = "data/svm/analysis/allergy.out";
	public static final String bcOut = "data/svm/analysis/breast.cancer.out";
	public static final String depOut = "data/svm/analysis/depression.out";
	public static final String hdOut = "data/svm/analysis/heart.disease.out";
	
	public static final String train = "data/svm/analysis/train";
	public static final String test = "data/svm/analysis/test";
	public static final String model = "data/svm/analysis/train.model";


	public static final String liwcOut = "data/svm/liwc/out";
	public static final String liwcTrain = "data/svm/liwc/train";
	public static final String liwcTest = "data/svm/liwc/test";
	public static final String liwcModel = "data/svm/liwc/train.model";
	
	public static final String goldDir = "data/gold/";
	public static final String comboDir = "data/combo/";
	
	public static final String adamDir = "data/adam/";
	public static final String niteeshDir = "data/niteesh/";

	
	public static final String featDir = "edu.illinois.cs.timan.iknowx.features";
	//public static final String titleFeatDir = "edu.illinois.cs.timan.iknowx.titlefeatures";

	public static final String trainFile = "data/svm/train.txt";
	public static final String tuneFile = "data/svm/tune.txt";
	public static final String modelFile = "data/svm/train.model";
	
	public static final String trainNGramFile = "data/svm/trainNGram.txt";
	public static final String tuneNGramFile = "data/svm/tuneNGram.txt";
	public static final String modelNGramFile = "data/svm/trainNGram.model";
	
	public static final String testFile = "data/svm/test";
	
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
	
	public static final String jsonDir = "data/medhelp_jsons/";
}