package edu.illinois.cs.timan.iknowx.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;

import edu.illinois.cs.timan.iknowx.datastructure.ForumThread;

/**
 * Reads forum thread
 * @author hcho33
 *
 */
public class ForumThreadReader 
{
	private Digester digester = new Digester();
	private static List<ForumThread> forumThreads = new ArrayList<ForumThread>();

	public ForumThreadReader()
	{
		initThreadPopulator();
	}
	
	public List<ForumThread> getForumThreads()
	{
		return forumThreads;
	}
	
	/**
	 * Parses all healthboard format xml files in directory.
	 * @param xmlFileDir
	 * @throws IOException
	 */
	public void parseXml(String xmlFileDir) throws IOException {
		int numThreadsIndexed = 0;
		int numThreadsSkipped = 0;
		
		File inputDirectory = new File(xmlFileDir);
		Iterator<File> iter = FileUtils.iterateFiles(inputDirectory, new String[]{"xml"}, true);
		while(iter.hasNext()) 
		{
			File file = (File) iter.next();
			Reader reader = null;
			FileInputStream inputStream = null;
			try 
			{
				inputStream = new FileInputStream(file);
				reader = new InputStreamReader(inputStream, "UTF-8");
				InputSource is = new InputSource(reader);
				is.setEncoding("UTF-8");
				digester.parse(is);

				numThreadsIndexed += 1;
//				System.out.println("Parsed : " + file);
			}
			catch (Exception e) 
			{
//				System.out.println("Couldn't parse xml " + file);
//				e.printStackTrace();
				numThreadsSkipped += 1;
			}
			if (inputStream != null)
				inputStream.close();
			if (reader != null)
				reader.close();
		}
		//System.out.println("Num skipped:" + numThreadsSkipped);
	}
	
	public void initThreadPopulator()
	{
        // instantiate Digester and disable XML validation
        digester.setValidating(false);
        // Instantiate ForumIndexer and Thread class upon seeing <THREAD> tag
        digester.addObjectCreate("THREAD", ForumThreadReader.class );
        digester.addObjectCreate("THREAD", ForumThread.class );

        // Set different properties of Thread instance using specified methods
        digester.addCallMethod("THREAD/URL",            "setUrl", 0);
        digester.addCallMethod("THREAD/SUBFORUMID",     "setSubForumId", 0);
        digester.addCallMethod("THREAD/POSTS",          "setNumPosts", 0);

        // Set new methods for Custom Queries
        digester.addCallMethod("THREAD/POST/TEXT/PE",       "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED",      "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER",       "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q",        "addBkg", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PMH",      "addBkg", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PD",       "addBkg", 0);

        digester.addCallMethod("THREAD/POST/TEXT/PE/ER",    "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PE/ER",    "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PE/MED",   "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PE/MED",   "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PE/Q",     "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/PE/Q",     "addBkg", 0);

        digester.addCallMethod("THREAD/POST/TEXT/MED/PE",   "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED/PE",   "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED/ER",   "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED/ER",   "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED/Q",    "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/MED/Q",    "addBkg", 0);

        digester.addCallMethod("THREAD/POST/TEXT/ER/PE",    "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER/PE",    "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER/MED",   "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER/MED",   "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER/Q",     "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/ER/Q",     "addBkg", 0);

        digester.addCallMethod("THREAD/POST/TEXT/Q/PE",     "addPe", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q/PE",     "addBkg", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q/MED",    "addMed", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q/MED",    "addBkg", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q/ER",     "addEr", 0);
        digester.addCallMethod("THREAD/POST/TEXT/Q/ER",     "addBkg", 0);
        
        digester.addCallMethod("THREAD/POST/MEDTERM", "addMedTerm", 0);
        digester.addCallMethod("THREAD/POST", "startPost", 0);

        digester.addCallMethod("THREAD/POST/TEXT",      "addPost", 0);
        digester.addCallMethod("THREAD/POST/TITLE",     "addTitle", 0);
        

        // Call 'addThread' method when the THREAD pattern is seen
        digester.addSetNext("THREAD", "addThread");
	}
	

    /**
     * Adds threads to the list.
     */
    public void addThread(ForumThread thread) 
    {
    	forumThreads.add(thread);
//    	System.out.println(thread.getTitle());
//    	System.out.println(thread.getFirstPost());
    }
}
