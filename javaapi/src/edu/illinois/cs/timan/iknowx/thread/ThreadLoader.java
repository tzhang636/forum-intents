package edu.illinois.cs.timan.iknowx.thread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.crowdflower.CrowdFlowerLabelReader;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.io.ForumThreadReader;

public class ThreadLoader {
	
	private static String clean(String str) throws Exception {
		return str.replaceAll("\\P{InBasic_Latin}", " ");
	}
	
	private static String convertAscii(String str) throws Exception {
		String s = Normalizer.normalize(str, Normalizer.Form.NFKD);
    String regex = "[\\p{InCombiningDiacriticalMarks}]+";
    return new String(s.replaceAll(regex, "").getBytes("ascii"), "ascii");
	}
	
	public static List<ForumThread> load(String path) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(path));
    List<ForumThread> threads = new ArrayList<ForumThread>();
    String lastTid = "";
    
    try {
      String line = br.readLine();
      while (line != null) {      	
        String[] tokens = line.split("\\t");
        String tid = convertAscii(clean(tokens[0]));
        String firstPost = convertAscii(clean(tokens[5])); // tokens[5].replaceAll("\\u00C2", "");
      	if (!lastTid.equals(tid)) { // new thread
          ForumThread thread = new ForumThread(); // add first post
          thread.setLabel("How should I manage or treat symptoms or conditions X?"); // placeholder
          thread.setTid(tid);
          thread.setFirstPost(firstPost);
          threads.add(thread);
      	}
      	lastTid = tid;
        line = br.readLine();
      }
    } finally {
      br.close();
    }

    return threads;
	}
	
	public static Map<Integer, String> readLabels(String dir) {
		Map<Integer, String> labels = null;
		try {
			labels = CrowdFlowerLabelReader.getDirLabels(dir);
		} catch (Exception e1) {
			System.out.println("readLabels()");
			e1.printStackTrace();
		}
		return labels;
	}
	
	private static Map<Integer, List<String>> readMultLabels() {
		Map<Integer, List<String>> multLabels = null;
		try {
			multLabels = CrowdFlowerLabelReader.getDirMultLabels(Constants.comboDir);
		} catch (Exception e1) {
			System.out.println("readMultLabels()");
			e1.printStackTrace();
		}
		return multLabels;
	}

	private static List<ForumThread> readThreads() {
		ForumThreadReader ftReader = new ForumThreadReader();
		List<ForumThread> threads = new ArrayList<ForumThread>();
		for (String xmlDir : Constants.xmlDirs) {
			try {
				ftReader.parseXml(xmlDir);
			} catch (IOException e) {
				System.out.println("loadThreads()");
				e.printStackTrace();
			}
		}
		threads.addAll(ftReader.getForumThreads());
		return threads;
	}

	private static List<ForumThread> setLabels(Map<Integer, String> labels,
			Map<Integer, List<String>> multLabels, List<ForumThread> threads) {
		List<ForumThread> labeledThreads = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			String sThreadId = thread.getThreadId();
			if (sThreadId.isEmpty()) {
				continue;
			}
			int threadId = Integer.parseInt(sThreadId);
			if (labels != null && !labels.containsKey(threadId)) {
				continue;
			}
			thread.setLabel(labels.get(threadId));
			List<String> mLabels = multLabels.get(threadId);
			if (mLabels != null) {
				thread.setLabels(mLabels);
			}
			labeledThreads.add(thread);
		}
		return labeledThreads;
	}
	
	public static List<ForumThread> setLabel(Map<Integer, String> labels, List<ForumThread> threads) {
		List<ForumThread> labeledThreads = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			String sThreadId = thread.getThreadId();
			if (sThreadId.isEmpty()) {
				continue;
			}
			int threadId = Integer.parseInt(sThreadId);
			if (labels != null && !labels.containsKey(threadId)) {
				continue;
			}
			thread.setLabel(labels.get(threadId));
			labeledThreads.add(thread);
		}
		return labeledThreads;
	}

	public static List<ForumThread> loadThreads(String dir) {
		Map<Integer, String> labels = readLabels(dir);
		Map<Integer, List<String>> multLabels = readMultLabels();
		List<ForumThread> threads = readThreads();
		System.out.println("Number of threads: " + threads.size());
		return setLabels(labels, multLabels, threads);
		//return setLabel(labels, threads);
	}
	
}