package edu.illinois.cs.timan.iknowx.crowdflower;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import au.com.bytecode.opencsv.CSVReader;

/**
 * From gold-labeld data, reads labels.
 * @author hcho33
 *
 */
public class CrowdFlowerLabelReader 
{
	public final static String DOCID_IDX = "docid";
	public final static String LABEL_IDX = "choose_the_best_user_intent_for_the_post_gold";
	public final static String CONTENT_IDX = "content";
	
	public final static String MULT_LABEL_IDX = "check_all_that_apply_gold";

	
	public static Map<Integer, String> getLabels(String goldFilename) throws Exception
	{
		Map<Integer, String> labels = new HashMap<Integer, String>(); 
		CSVReader reader = new CSVReader(new FileReader( goldFilename ));
		String [] header = reader.readNext();
		Map<String, Integer> headerIdx = new HashMap<String, Integer>();
		for (int hidx = 0; hidx < header.length; hidx++)
		{
			headerIdx.put(header[hidx], hidx);
		}
		String [] line;
		int c = 0;
		while ((line = reader.readNext()) != null )
		{
			String docId = line[headerIdx.get(DOCID_IDX)];
			if (docId.isEmpty()) continue;
			String label = line[headerIdx.get(LABEL_IDX)];
			if (label.isEmpty())
			{
				//System.out.println("Empty label");
				continue;
			}
			String content = line[headerIdx.get(CONTENT_IDX)];
			if (content.isEmpty())
			{
				//System.out.println("Empty content");
				continue;
			}
			labels.put(Integer.parseInt(docId), label);
		}
		return labels;
	}
	
	public static Map<Integer, List<String>> getMultLabels(String goldFilename) throws Exception {
		Map<Integer, List<String>> labels = new HashMap<Integer, List<String>>(); 
		CSVReader reader = new CSVReader(new FileReader(goldFilename));
		String[] header = reader.readNext();
		Map<String, Integer> headerIdx = new HashMap<String, Integer>();
		for (int hidx = 0; hidx < header.length; hidx++) {
			headerIdx.put(header[hidx], hidx);
		}
		String [] line;
		int c = 0;
		while ((line = reader.readNext()) != null )
		{
			String docId = line[headerIdx.get(DOCID_IDX)];
			if (docId.isEmpty()) continue;
			String label = line[headerIdx.get(MULT_LABEL_IDX)];
			if (label.isEmpty())
			{
				//System.out.println("Empty label");
				continue;
			}
			String content = line[headerIdx.get(CONTENT_IDX)];
			if (content.isEmpty())
			{
				//System.out.println("Empty content");
				continue;
			}
			
			String[] multLabel = label.split("\\n");
			labels.put(Integer.parseInt(docId), new ArrayList<String>(Arrays.asList(multLabel)));
		}
		return labels;
	}
	
	/**
	 * Only pussies do error checking. Enumerates all gold file. Assumes
	 * it ends with .csv.
	 * 
	 * @param goldDir
	 * @return
	 * @throws Exception 
	 */
	public static Map<Integer, String> getDirLabels(String goldDir) throws Exception 
	{
		File dir = new File(goldDir);
		Map<Integer, String> labels = new HashMap<Integer, String>();
		for (File f : dir.listFiles())
		{
			if (f.getName().contains(".csv") && !f.getName().contains(".swp"))
			{
				labels.putAll( getLabels(f.getAbsolutePath()) );
			}
		}
		
		return labels;
	}
	
	public static Map<Integer, List<String>> getDirMultLabels(String goldDir) throws Exception 
	{
		File dir = new File(goldDir);
		Map<Integer, List<String>> labels = new HashMap<Integer, List<String>>();
		for (File f : dir.listFiles())
		{
			if (f.getName().contains(".csv") && !f.getName().contains(".swp"))
			{
				labels.putAll(getMultLabels(f.getAbsolutePath()));
			}
		}
		
		return labels;
	}
	
}
