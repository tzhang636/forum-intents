package edu.illinois.cs.timan.iknowx.metamap;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.loader.SemTypesLoader;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapEV;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapObject;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapPCM;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapPost;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapResult;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapUtterance;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;

public class MetaMapMapping {
	private static MetaMapApi api = new MetaMapApiImpl();
	static {
    List<String> theOptions = new ArrayList<String>();
    theOptions.add("-y"); // wsd
    theOptions.add("-A"); // strict model
    //theOptions.add("-J"); // only semantic types
    //theOptions.add("-K"); // ignore stop phrases
    api.setOptions(theOptions);
	}
	
	private static List<String> badConcepts = new ArrayList<String>();
	static {
		badConcepts.add("I-");
		badConcepts.add("I NOS");
	}
	
	private static List<String> diso;
	static {
		diso = new ArrayList<String>();
		diso.add("dsyn");
		diso.add("patf");
		diso.add("sosy");
		diso.add("neop");
		diso.add("acab");
		diso.add("cgab");
		diso.add("mobd");
	}
	
	private static List<String> chem;
	static {
		chem = new ArrayList<String>();
		chem.add("strd");
		chem.add("phsu");
		chem.add("antb");
		chem.add("clnd");
	}
	
	private static List<String> proc;
	static {
		proc = new ArrayList<String>();
		proc.add("hlca");
		proc.add("topp");
		proc.add("diap");
	}
	
	private static Map<String, String> abbv2Full;
	private static Map<String, String> abbv2Code;
	private static Map<String, String> code2GroupAbbv;
	static {
		try {
			abbv2Full = SemTypesLoader.loadAbbv2Full();
			abbv2Code = SemTypesLoader.loadAbbv2Code();
			code2GroupAbbv = SemTypesLoader.loadCode2GroupAbbv();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isConcept(String concept) {
		if (badConcepts.contains(concept)) {
			return false;
		}
		return true;
	}
	
	private static String getGroup(String[] semTypes) {
		for (String semType : semTypes) {
			if (!diso.contains(semType) && 
					!chem.contains(semType) && 
					!proc.contains(semType)) {
				continue;
			}
			return code2GroupAbbv.get(abbv2Code.get(semType));
		}
		return new String();
	}
	
	public static Map<String, String> getTitleMapping(String threadId) throws Exception {
		MetaMapPost mTitle = MetaMapIO.loadTitleFromJson(threadId);
		Map<String, String> mapping = new HashMap<String, String>();
		for (MetaMapObject mObj : mTitle.metamapObjects) {
			for (MetaMapResult mRes : mObj.resultList) {
				for (MetaMapUtterance mUtt : mRes.utterances) {
					for (MetaMapPCM mPCM : mUtt.pcms) {
						String phrase = mPCM.phrase;
						for (MetaMapEV mapEV : mPCM.evs) {
							String concept = mapEV.conceptName;
							String[] semTypes = mapEV.semTypes;
							String group = getGroup(semTypes);
							if (isConcept(concept) && !group.isEmpty()) {
								mapping.put(phrase, group);
							}
						}
					}
				}
			}
		}
		return mapping;
	}
	
	public static Map<String, String> getFirstPostMapping(String threadId) throws Exception {
		MetaMapPost mFirstPost = MetaMapIO.loadPostFromJson(threadId);
		Map<String, String> mapping = new HashMap<String, String>();
		for (MetaMapObject mObj : mFirstPost.metamapObjects) {
			for (MetaMapResult mRes : mObj.resultList) {
				for (MetaMapUtterance mUtt : mRes.utterances) {
					for (MetaMapPCM mPCM : mUtt.pcms) {
						String phrase = mPCM.phrase;
						for (MetaMapEV mapEV : mPCM.evs) {
							String concept = mapEV.conceptName;
							String[] semTypes = mapEV.semTypes;
							String group = getGroup(semTypes);
							if (isConcept(concept) && !group.isEmpty()) {
								mapping.put(phrase, group);
							}
						}
					}
				}
			}
		}
		return mapping;
	}

	public static void main(String[] args) throws Exception {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		//ThreadFilter.filter(threads);
    PrintWriter p = new PrintWriter("mapping.txt", "UTF-8");
		for (ForumThread thread : threads) {
			Map<String, String> mapping = getTitleMapping(thread.getId());
			p.println(mapping.toString());
			p.println(thread.getTitle());
			p.println();
		}
		p.close();
	}
}