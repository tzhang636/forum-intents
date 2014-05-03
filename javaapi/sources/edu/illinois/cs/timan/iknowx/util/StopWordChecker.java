package edu.illinois.cs.timan.iknowx.util;

import java.util.*;
import java.io.*;

public class StopWordChecker {
	
	private HashSet<String> stopWord = new HashSet<String>();
	private String stopWordFileName = "data/stopwords.txt";
	
	public StopWordChecker()
	{
		readWords();
	}
	
	private void readWords()
	{
		{
			try
			{
				// Open the file that is the first 
				// command line parameter
				FileInputStream fstream = new FileInputStream(stopWordFileName);
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)   {
					// Print the content on the console
					stopWord.add(strLine.trim());
				}
				//Close the input stream
				in.close();
			}catch (Exception e)
			{//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
	}
	
	public boolean isStopWord(String word)
	{
		return stopWord.contains(word.toLowerCase());
	}
}
