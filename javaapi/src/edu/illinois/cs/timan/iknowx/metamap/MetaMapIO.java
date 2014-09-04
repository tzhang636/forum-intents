package edu.illinois.cs.timan.iknowx.metamap;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import edu.illinois.cs.timan.iknowx.constants.Constants;
import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;
import edu.illinois.cs.timan.iknowx.io.SaveLoad;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapPost;
import edu.illinois.cs.timan.iknowx.thread.ThreadLoader;

public class MetaMapIO {
	public static MetaMapPost saveTitleToJson(ForumThread thread) throws Exception {
		Gson gson = new Gson();
		String filename = Constants.titleJsonDir + thread.getId() + ".json";
		MetaMapPost title = MetaMapParser.parseTitle(thread);
		String contents = gson.toJson(title);
  	System.out.println("saveTitlesToJson(): saving to " + filename + "...");
  	SaveLoad.saveFile(filename, contents);
  	return title;
	}
	
	public static MetaMapPost savePostsToJson(ForumThread thread) throws Exception {
		Gson gson = new Gson();
		String filename = Constants.postJsonDir + thread.getId() + ".json";
		MetaMapPost metaMapPost = MetaMapParser.parsePost(thread);
		String contents = gson.toJson(metaMapPost);
  	System.out.println("savePostsToJson(): saving to " + filename + "...");
  	SaveLoad.saveFile(filename, contents);
  	return metaMapPost;
	}
	
	public static MetaMapPost loadTitleFromJson(String threadId) throws Exception {
		String filename = Constants.titleJsonDir + threadId + ".json";
		//System.out.println("loadTitlesFromJson(): loading " + filename + "...");
		MetaMapPost title = MetaMapParser.loadTitle(filename);
		return title;
	}
	
	public static MetaMapPost loadPostFromJson(String threadId, String jsonDir) throws Exception {
		String filename = jsonDir + threadId + ".json";
		//System.out.println("loadPostsFromJson(): loading " + filename + "...");
		MetaMapPost metaMapPost = MetaMapParser.loadPost(filename);
		return metaMapPost;
	}
	
	public static void main(String[] args) throws Exception {
		List<ForumThread> threads = ThreadLoader.loadThreads();
		for (ForumThread thread : threads) {
			savePostsToJson(thread);
		}
	}
}