package edu.illinois.cs.timan.iknowx.svm;

import java.io.*;
import java.util.*;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVMTrainer {
	public static void train(String trainFile, String modelFile, double cost,
			double gamma) throws IOException {
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		// functional margin y*(w*x+b)
		param.gamma = gamma;
		param.coef0 = 0;
		// cost of misclassification
		// higher cost = less general classifier
		param.C = cost;
		param.nu = 0.5;
		param.p = 0.1;
		param.cache_size = 100;
		param.eps = 1e-3;
		param.shrinking = 1;
		param.probability = 1;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		
		svm_problem prob = new svm_problem();
		readProblem(trainFile, param, prob);
		svm_model model = svm.svm_train(prob, param);
		svm.svm_save_model(modelFile, model);
	}
	
	private static void readProblem(String trainFile, svm_parameter param,
			svm_problem prob) throws IOException {
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		BufferedReader trainFileReader = new BufferedReader(new FileReader(trainFile));
		String line;
		while ((line = trainFileReader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens() / 2;
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if (m > 0) {
				max_index = Math.max(max_index, x[m - 1].index);
			}
			vx.addElement(x);
		}

		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for (int i = 0; i < prob.l; i++) {
			prob.x[i] = vx.elementAt(i);
		}
		prob.y = new double[prob.l];
		for (int i = 0; i < prob.l; i++) {
			prob.y[i] = vy.elementAt(i);
		}
		if (param.gamma == 0 && max_index > 0) {
			param.gamma = 1.0 / max_index; // 1 / num features
		}
		
		trainFileReader.close();
	}

	private static double atof(String s) {
		return Double.parseDouble(s);
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}
}