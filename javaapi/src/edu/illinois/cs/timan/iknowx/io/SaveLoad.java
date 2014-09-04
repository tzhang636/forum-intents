package edu.illinois.cs.timan.iknowx.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class SaveLoad {
	public static void saveFile(String filename, String saveContent)
	{
		try{
			// Create file 
			File file = new File(filename);
			if (file.exists()) file.delete();
//			file.getParentFile().mkdirs();
			FileWriter fstream = new FileWriter(filename, file.exists()); //don't append to end
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(saveContent);			
//			System.out.println("saving : " + saveContent);
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error in save : " + e.getMessage());
		}
	}
	
	public static String loadFile(String loadFile)
	{
		String content = "";
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(loadFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				content += strLine + "\n";
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return content;
	}
}
