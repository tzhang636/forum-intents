package edu.illinois.cs.timan.iknowx.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.timan.iknowx.constants.Constants;

public class QWordsLoader {
	public static List<String> loadQWords() throws Exception {
		List<String> qWords = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(Constants.qWordsFile));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			qWords.add(line);
		}
		br.close();
		return qWords;
	}
}