package edu.illinois.cs.timan.iknowx.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.thread.ThreadFilter;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class Agreement {
	
	public static Map<String, Integer> label2id;
	static {
label2id = new HashMap<String, Integer>();
label2id.put("What is the drug of choice for condition x?", 0);
label2id.put("What is the cause of symptom x?", 1);
label2id.put("What test is indicated in situation x?", 2);
label2id.put("What is the dose of drug x?", 3);
label2id.put("How should I treat condition x (not limited to drug treatment)?", 4);
label2id.put("How should I manage condition x (not specifying diagnostic or therapeutic)?", 5);
label2id.put("What is the cause of physical finding x?", 6);
label2id.put("What is the cause of test finding x?", 7);
label2id.put("Can drug x cause (adverse) finding y?", 8);
label2id.put("Could this patient have condition x?", 9);
label2id.put("Story telling, news, sharing or asking about experience, soliciting support, or others.", 10);
	}
	
	// 0, 3, 4, 5
	// 1, 6, 7, 9
	// 8
	
	// 0-3, 0, 0
	// 0-4, 0, 0
	// 0-5, 0, 0
	// 3-4, 0, 
	// 3-5,
	// 4-5,
	// 1-6, 
	// 1-7,
	// 1-8,
	// 1-9,
	// 6-7, 
	// 6-9,
	// 7-9,
	
	
	public static void main(String[] args) throws Exception {
		
		Map<Integer, String> alabels = ThreadLoader.readLabels(Constants.adamDir);
		Map<Integer, String> nlabels = ThreadLoader.readLabels(Constants.niteeshDir);
		for (int tid : alabels.keySet()) {
			String[] aLabel = alabels.get(tid).split("\\r?\\n");
			String[] nLabel = nlabels.get(tid).split("\\r?\\n");
			if (aLabel.length > 1) {
				alabels.put(tid, aLabel[0]);
			}
			if (nLabel.length > 1) {
				nlabels.put(tid, nLabel[0]);
			}
			System.out.println(tid + "\t" + alabels.get(tid) + "\t" + nlabels.get(tid));
			System.out.println();
		}
		
		List<ForumThread> aThreads = new ArrayList<ForumThread>();	
		List<ForumThread> nThreads = new ArrayList<ForumThread>();
		for (int tid : alabels.keySet()) {
			String alabel = alabels.get(tid);
			String nlabel = nlabels.get(tid);
			ForumThread aThread = new ForumThread();
			ForumThread nThread = new ForumThread();
			aThread.setTid(Integer.toString(tid));
			nThread.setTid(Integer.toString(tid));
			aThread.setLabel(alabel);
			nThread.setLabel(nlabel);
			aThreads.add(aThread);
			nThreads.add(nThread);
		}
		
		int[][] matrix = new int[11][11];
		for (int i = 0; i < aThreads.size(); ++i) {
			ForumThread adam = aThreads.get(i);
			ForumThread niteesh = nThreads.get(i);
			int adamL = label2id.get(adam.getLabel());
			int niteeshL = label2id.get(niteesh.getLabel());
			matrix[adamL][niteeshL]++;
		}
				
		for (int i = 0; i < matrix.length; ++i) {
			for (int j = 0; j < matrix[0].length; ++j) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		
		// 0-3, 0, 0
		// 0-4, 0, 0
		// 0-5, 0, 0
		// 3-4, 0, 
		// 3-5,
		// 4-5,
		// 1-6, 
		// 1-7,
		// 1-8,
		// 1-9,
		// 6-7, 
		// 6-9,
		// 7-9,
		
		System.out.println(matrix[3][4]);
		System.out.println(matrix[4][3]);
		System.out.println(matrix[3][5]);
		System.out.println(matrix[5][3]);
		System.out.println(matrix[4][5]);
		System.out.println(matrix[5][4]);
		System.out.println(matrix[1][6]);
		System.out.println(matrix[6][1]);
		System.out.println(matrix[1][7]);
		System.out.println(matrix[7][1]);
		System.out.println(matrix[1][8]);
		System.out.println(matrix[8][1]);		
		System.out.println(matrix[1][9]);
		System.out.println(matrix[9][1]);
		System.out.println(matrix[6][7]);
		System.out.println(matrix[7][6]);
		System.out.println(matrix[6][9]);
		System.out.println(matrix[9][6]);
		System.out.println(matrix[7][9]);
		System.out.println(matrix[9][7]);
	}
	
}