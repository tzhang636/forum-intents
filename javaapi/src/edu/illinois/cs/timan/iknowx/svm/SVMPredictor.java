package edu.illinois.cs.timan.iknowx.svm;

import java.util.*;
import java.io.*;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SVMPredictor {
	public static List<Double> predict(String testFile, String modelFile)
			throws IOException {
		svm_model model = svm.svm_load_model(modelFile);
		List<Double> predictions = new ArrayList<Double>();
		BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = testFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			int m = st.countTokens() / 2;
			st.nextToken(); // label
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			double prediction = svm.svm_predict(model, x);
			predictions.add(prediction);
		}
		testFileReader.close();
		return predictions;
	}
	
	public static List<Double> probsPredict(String testFile, String modelFile)
			throws IOException {
		svm_model model = svm.svm_load_model(modelFile);
		List<Double> predictions = new ArrayList<Double>();
		BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = testFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			int m = st.countTokens() / 2;
			st.nextToken(); // label
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			double[] est = new double[3];
			double prediction = svm.svm_predict_probability(model, x, est);
			predictions.add(prediction);
		}
		testFileReader.close();
		return predictions;
	}
	
	public static List<double[]> getProbEsts(String testFile, String modelFile, int numClasses)
			throws IOException {
		svm_model model = svm.svm_load_model(modelFile);
		List<double[]> predictions = new ArrayList<double[]>();
		BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = testFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			int m = st.countTokens() / 2;
			st.nextToken(); // label
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			double[] est = new double[numClasses];
			svm.svm_predict_probability(model, x, est);
			predictions.add(est);
		}
		testFileReader.close();
		return predictions;
	}
	
	public static List<Double> getGolds(String testFile) throws IOException {
		List<Double> golds = new ArrayList<Double>();
		BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = testFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			double gold = atof(st.nextToken());
			golds.add(gold);
		}
		testFileReader.close();
		return golds;
	}

	public static double getAccuracy(String testFile, String modelFile)
			throws IOException {
		svm_model model = svm.svm_load_model(modelFile);
		int correct = 0;
		int total = 0;
		BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = testFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			double gold = atof(st.nextToken());
			int m = st.countTokens() / 2;
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			double prediction = svm.svm_predict(model, x);
			if (prediction == gold)
				++correct;
			++total;
		}
		testFileReader.close();
		return correct / ((double) total);
	}

	private static double atof(String s) {
		return Double.parseDouble(s);
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}
}