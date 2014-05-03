package edu.illinois.cs.timan.iknowx.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.timan.iknowx.constants.Constants;

public class NegWordsLoader {
	public static List<String> loadNegWords() throws Exception {
		List<String> negWords = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(Constants.negWordsFile));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			negWords.add(line);
		}
		br.close();
		return negWords;
	}
}