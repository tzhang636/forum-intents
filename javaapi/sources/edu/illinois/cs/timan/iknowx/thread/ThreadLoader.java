package edu.illinois.cs.timan.iknowx.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.crowdflower.CrowdFlowerLabelReader;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.io.ForumThreadReader;

public class ThreadLoader {
	private static Map<Integer, String> readLabels() {
		Map<Integer, String> labels = null;
		try {
			labels = CrowdFlowerLabelReader.getDirLabels(Constants.goldDir);
		} catch (Exception e1) {
			System.out.println("loadLabels()");
			e1.printStackTrace();
		}
		return labels;
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

	private static List<ForumThread> filterThreads(Map<Integer, String> labels,
			List<ForumThread> threads) {
		List<ForumThread> filteredThreads = new ArrayList<ForumThread>();
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
			filteredThreads.add(thread);
		}
		return filteredThreads;
	}

	public static List<ForumThread> loadThreads() {
		Map<Integer, String> labels = readLabels();
		List<ForumThread> threads = readThreads();
		return filterThreads(labels, threads);
	}
}