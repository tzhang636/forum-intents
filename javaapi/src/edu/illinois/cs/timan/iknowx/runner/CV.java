package edu.illinois.cs.timan.iknowx.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;

public abstract class CV {
	
	public static List<List<ForumThread>> getFolds(List<ForumThread> threads, int numFolds) {
		List<List<ForumThread>> folds = new ArrayList<List<ForumThread>>();
		double totalSize = threads.size();
		int foldSize = (int) (totalSize / numFolds);
		for (int i = 0; i < numFolds; ++i) {
			if (i == numFolds - 1) { // last fold
				folds.add(threads.subList(i * foldSize, (int) totalSize));
			}
			else {
				folds.add(threads.subList(i * foldSize, (i + 1) * foldSize));
			}
		}
		return folds;
	}
	
	public static List<List<ForumThread>> getFoldsByForum(List<ForumThread> threads) {
		Map<String, List<ForumThread>> folds = new HashMap<String, List<ForumThread>>();
		for (ForumThread thread : threads) {
			String forumId = thread.getSubForumId();
			if (!folds.containsKey(forumId)) {
				folds.put(forumId, new ArrayList<ForumThread>());
			}
			List<ForumThread> fold = folds.get(forumId);
			fold.add(thread);
			folds.put(forumId, fold);
		}
		List<List<ForumThread>> ret = new ArrayList<List<ForumThread>>(folds.values());
		return ret;
	}
	
	/**
	 * Gets all non-combo, non-story threads from all except one fold.
	 * @param folds
	 * @param fold
	 * @return
	 */
	public static List<ForumThread> getPatternTrainSet(List<List<ForumThread>> folds, int fold) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		int numFolds = folds.size();
		for (int i = 0; i < numFolds; ++i) {
			if (i == fold) {
				continue;
			}
			List<ForumThread> currFold = folds.get(i);
			for (ForumThread thread : currFold) {
				int label = Constants.label2id.get(thread.getLabel());
				if (label != 3 && label != 4) { // if not story or combo
					trainSet.add(thread);
				}
			}
		}
		return trainSet;
	}
	
	public static List<ForumThread> getTrainSet(List<List<ForumThread>> folds, int fold) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		int numFolds = folds.size();
		for (int i = 0; i < numFolds; ++i) {
			if (i == fold) {
				continue;
			}
			List<ForumThread> currFold = folds.get(i);
			for (ForumThread thread : currFold) {
				int label = Constants.label2id.get(thread.getLabel());
				if (label != 4) {
					trainSet.add(thread);
				}
			}
		}
		return trainSet;
	}
	
	public static List<ForumThread> getTrainSet(List<List<ForumThread>> folds, int fold, int max) {
		List<ForumThread> trainSet = new ArrayList<ForumThread>();
		int numFolds = folds.size();
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (int i = 0; i < numFolds; ++i) {
			if (i == fold) {
				continue;
			}
			List<ForumThread> currFold = folds.get(i);
			for (ForumThread thread : currFold) {
				int label = Constants.label2id.get(thread.getLabel());
				if (label == 4) { // combo
					continue;
				}
				if (!counts.containsKey(label)) {
					counts.put(label, 0);
				}
				int count = counts.get(label);
				if (count < max) {
					trainSet.add(thread);
					counts.put(label, count+1);
				}
			}
		}
		return trainSet;
	}
	
	public static List<ForumThread> getRemThreads(List<ForumThread> threads, Set<String> tids) {
		List<ForumThread> remThreads = new ArrayList<ForumThread>();
		for (ForumThread thread : threads) {
			if (!tids.contains(thread.getId())) {
				remThreads.add(thread);
			}
		}
		return remThreads;
	}
	
	public static int[][] mergeMatrix(int[][] pMatrix, int[][] wMatrix) {
		int[][] matrix = new int[pMatrix.length][pMatrix.length];
		for (int i = 0; i < pMatrix.length; ++i) {
			for (int j = 0; j < wMatrix.length; ++j) {
				matrix[i][j] = pMatrix[i][j] + wMatrix[i][j];
			}
		}
		return matrix;
	}

	/**
	 * Gets the labels of the thread with tid.
	 * @param tid
	 * @param threads
	 * @return
	 */
	public static List<String> getLabels(String tid, List<ForumThread> threads) {
		for (ForumThread thread : threads) {
			if (thread.getId().equals(tid)) {
				return thread.getLabels();
			}
		}
		return null;
	}
	
	public static int getNumCorrect(List<ForumThread> threads, Map<String, Double> preds, Map<String, Double> golds) {
		int numCorrect = 0;
		for (String tid : preds.keySet()){
			int pred = preds.get(tid).intValue();
			int gold = golds.get(tid).intValue();
			
			if (gold == 4) { // combo
				List<String> labels = getLabels(tid, threads);
				for (String label : labels) {
					gold = Constants.label2id.get(label);
					if (pred == gold) { // one of the combo labels predicted correctly
						++numCorrect;
						break;
					}
				}
			}
			else { // not a combo thread
				if (pred == gold) {
					++numCorrect;
				}
			}
		}
		return numCorrect;
	}
	
	public static int[] getMcNemar(List<ForumThread> threads, Map<String, Double> h, Map<String, Double> w, Map<String, Double> g) {
		int numPos = 0, numNeg = 0, numPosNeg = 0, numNegPos = 0;
		
		for (String tid : h.keySet()) {
			int hp = h.get(tid).intValue();
			int wp = w.get(tid).intValue();
			int gp = g.get(tid).intValue();
			
			if (gp == 4) { // combo
				List<String> labels = getLabels(tid, threads);
				boolean hc = false, wc = false;
				for (String label : labels) {
					gp = Constants.label2id.get(label);
					if (hp == gp) {
						hc = true;
					}
					if (wp == gp) {
						wc = true;
					}
				}
				if (hc && wc) {
					++numPos;
				}
				if (!hc && !wc) {
					++numNeg;
				}
				if (hc && !wc) {
					++numPosNeg;
				}
				if (!hc && wc) {
					++numNegPos;
				}
			}
			
			if (hp == gp && wp == gp) {
				++numPos;
			}
			if (hp != gp && wp != gp) {
				++numNeg;
			}
			if (hp == gp && wp != gp) {
				++numPosNeg;
			}
			if (hp != gp && wp == gp) {
				++numNegPos;
			}
		}
		
		return new int[] {numPos, numNeg, numPosNeg, numNegPos};
	}
	
	public static int[][] getMatrix(List<ForumThread> threads, Map<String, Double> preds, Map<String, Double> golds, int size) {
		int[][] matrix = new int[size][size];
		for (String tid : preds.keySet()){
			int pred = preds.get(tid).intValue();
			int gold = golds.get(tid).intValue();
			
			if (gold == 4) { // combo
				List<String> labels = getLabels(tid, threads);
				boolean m = false, c = false, a = false, correct = false;
				for (String label : labels) {
					gold = Constants.label2id.get(label);
					if (gold == 0) {
						m = true;
					}
					if (gold == 1) {
						c = true;
					}
					if (gold == 2) {
						a = true;
					}
					if (pred == gold) { // one of combo labels predicted correctly
						correct = true;
						break;
					}
				}
				if (correct) {
					++matrix[gold][pred];
				}
				else {
					if (m) {
						++matrix[0][pred];
					}
					else if (c) {
						++matrix[1][pred];
					}
					else if (a) {
						++matrix[2][pred];
					}
				}
			}
			else { // not a combo thread
				++matrix[gold][pred];
			}
		}
		return matrix;
	}
	
	public static double[] getPrecision(int[][] matrix, int numClasses) {
		double[] precision = new double[numClasses+1];
		int c = 0;
		int tot = 0;
		for (int i = 0; i<numClasses; ++i) {
			double t = 0;
			for (int j = 0; j<numClasses; ++j) {
				t += matrix[j][i];
			}
			precision[i] = matrix[i][i] / t;
			c += matrix[i][i];
			tot += t;
		}
		precision[numClasses] = (double) c / tot;
		return precision;
	}
	
	public static double[] getRecall(int[][] matrix, int numClasses) {
		double[] recall = new double[numClasses+1];
		int c = 0;
		int tot = 0;
		for (int i = 0; i<numClasses; ++i) {
			double t = 0;
			for (int j = 0; j<numClasses; ++j) {
				t += matrix[i][j];
			}
			recall[i] = matrix[i][i] / t;
			c += matrix[i][i];
			tot += t;
		}
		recall[numClasses] = (double) c / tot;
		return recall;
	}
	
	public static double[] getF1(double[] precision, double[] recall, int numClasses) {
		double[] f1 = new double[numClasses+1];
		for (int i = 0; i<numClasses+1; ++i) {
			f1[i] = (2*precision[i]*recall[i]) / (precision[i]+recall[i]);
		}
		return f1;
	}
	
	public static void printMatrix(int[][] matrix, PrintWriter pw) {
		for (int x = 0; x < matrix.length; ++x) {
			for (int y = 0; y < matrix[0].length; ++y) {
				pw.print(matrix[x][y] + "\t");
			}
			pw.println();
		}
		pw.println();
	}
	
	public static void print(double[] arr, PrintWriter pw, String str) {
		pw.println(str);
		for (int i = 0; i<arr.length; ++i) {
			pw.println(arr[i]);
		}
		pw.println();
	}
	
}