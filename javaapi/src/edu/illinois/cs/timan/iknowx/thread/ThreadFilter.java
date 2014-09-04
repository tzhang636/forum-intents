package edu.illinois.cs.timan.iknowx.thread;

import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;

public class ThreadFilter {
	public static void filter(List<ForumThread> threads) {
		Iterator<ForumThread> iter = threads.iterator();
		while (iter.hasNext()) {
			ForumThread thread = iter.next();
			String label = thread.getLabel();
			if (/*!Constants.label2id.containsKey(label) ||*/ label.isEmpty()) {
				iter.remove();
			}
		}
	}
}