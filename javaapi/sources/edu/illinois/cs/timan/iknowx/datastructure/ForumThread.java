package edu.illinois.cs.timan.iknowx.datastructure;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Represents forum threads.
 * Note: This class has potentials to be extremely buggy...
 * 
 * @author hcho33
 *
 */
public class ForumThread extends AbstractPostObject
{

    private String url;
    private String subForumId;
    private String numPosts;
    private ArrayList<String> allPosts;
    private ArrayList<Integer> ages;
    private ArrayList<String> ageGroups;
    private ArrayList<String> titles;
    private ArrayList<String> medTerms;
    private ArrayList<String> authors;
    private String label = "";

    // Use Hashmaps to keep track of the post number
    private HashMap<Integer, String> postPe;
    private HashMap<Integer, String> postMed;
    private HashMap<Integer, String> postEr;
    private HashMap<Integer, String> postBkg;
    private HashMap<Integer, String> postMedTerms;

    private int currentPost;
    private boolean preaddedPost = false;
    
    public ForumThread() {
        allPosts = new ArrayList<String>();
        titles = new ArrayList<String>();
        ages = new ArrayList<Integer>();
        ageGroups = new ArrayList<String>();
        authors = new ArrayList<String>();
        
        medTerms = new ArrayList<String>();
        postPe = new HashMap<Integer, String>();
        postMed = new HashMap<Integer, String>();
        postEr = new HashMap<Integer, String>();
        postBkg = new HashMap<Integer, String>();
        postMedTerms = new HashMap<Integer, String>();
        
        currentPost = 0;
    }

    public void startPost(String post)
    {
    	currentPost++;
    }
    
    public void addMedTerm(String medTerm)
    {
    	prePopulate();
    	medTerms.add(medTerm);
        if (postMedTerms.containsKey(currentPost)) {
            String currMedTerm = postPe.get(currentPost);
            postMedTerms.remove(currentPost);
            postMedTerms.put(currentPost, currMedTerm + " " + medTerm);
        }
        else {
        	postMedTerms.put(currentPost, medTerm);
        }
    }
    
    public ArrayList<String> getMedTerms()
    {
    	return medTerms;
    }
    
    public String getMedTerm(Integer key) {
        if (postMedTerms.containsKey(key)) {
            //System.out.println("PE: " + postPe.get(key));
            return postMedTerms.get(key);
        }
        return "";
    }
    
    public String getMedTermString()
    {
    	String medTermString = "";
    	for (String medTerm : medTerms)
    	{
    		medTermString += medTerm + "\n";
    	}
    	return medTermString;
    }
    
    public void addAge(String age)
    {
    	try
    	{
    		int currAge = Integer.parseInt(age);
			
    		if (currAge <= 0)
    		{
    			for (int i = 0; i < 10; i++)
    			{
    				this.ages.add(i * 10);
    			}
    		} 
    		else
    			this.ages.add(currAge);
    	} 
    	catch (Exception e)
    	{
    		this.ages.add(-1);
    	}
    }
    
    public ArrayList<Integer> getAges()
    {
    	return ages;
    }
    
    public void setLabel(String label)
    {
    	this.label = label;
    }
    
    public String getLabel()
    {
    	return label;
    }
    
    public String getAgeString()
    {
        String ageString = "";
        for (Integer age : ages)
        {
        	ageString += age + "\n";
        }
        return ageString;
    }
    
    public void addAgeGroup(String ageGroup)
    {
    	int currAge = Integer.parseInt(ageGroup);
    	this.ageGroups.add(ageGroup);
    	
		if (currAge <= 0)
		{
			for (int i = 0; i < 10; i++)
			{
				this.ageGroups.add("" + i * 10);
			}
		} 
		else
			this.ageGroups.add(ageGroup);
			
    }
    
    public String getAgeGroupString()
    {
    	 String ageGroupString = "";
         for (String age : ageGroups)
         {
        	 ageGroupString += age + "\n";
         }
         return ageGroupString;
    }
    public ArrayList<String> getAgeGroup()
    {
    	return ageGroups;
    }

    public void setUrl(String newUrl) {
        url = newUrl;
    }
    public String getUrl() {
        return url;
    }

    public void setSubForumId(String newId) {
        subForumId = newId;
    }
    public String getSubForumId() {
        return subForumId;
    }

    public void setNumPosts(String newNumPosts) {
        numPosts = newNumPosts;
    }
    public String getNumPosts() {
        return numPosts;
    }

    public String getFirstPost() {
        if (allPosts.size() > 0) {
            //System.out.println("First Post: " + allPosts.get(0));
            return allPosts.get(0);
        }
        return null;
    }
    
    public void addAuthor(String author)
    {
    	authors.add(author);
    }
    
    public String getFirstAuthor()
    {
    	return authors.get(0);
    }
    
    public String getNthAuthor(int nth)
    {
//    	if (authors.size() == allPosts.size())
//    		System.out.println(this.getThreadId() + "," + authors.size() +  " , " + allPosts.size());
    	return (authors.size() > nth) ? authors.get(nth) : "";
    }

    public void addPost(String post) 
    {
    	prePopulate();
    	//if (preaddedPost)
    	{
    		String oldPost = allPosts.get(currentPost);
    		allPosts.set(currentPost, oldPost + " " + post);
    		return;
    	}
        //allPosts.add(post);
        //currentPost++;
    }

    public String getAllPostString()
    {
        String posts = "";
        for (String post : allPosts) {
            posts = posts + post + "\n";
        }
        return posts;
    }
    
    public ArrayList<String> getAllPosts() {
        return allPosts;
    }

    public void addTitle(String title) {
        titles.add(title);
    }

    // Return the first title found
    @Override
    public String getTitle() {
        if (titles.size() > 0) {
            return titles.get(0);
        }
        return null;
    }

    public void addPe(String pe) 
    {
    	prePopulate();
        String currentPostString = allPosts.get(currentPost);

        if (postPe.containsKey(currentPost)) {
            String currPe = postPe.get(currentPost);
            postPe.remove(currentPost);
            postPe.put(currentPost, currPe + " " + pe);
            
            if (!currentPostString.contains(pe))
            	allPosts.set(currentPost, currentPostString + " " + pe);
        }
        else {
            postPe.put(currentPost, pe);
            if (!currentPostString.contains(pe))
            	allPosts.set(currentPost, currentPostString + " " + pe);
        }
    }

    public void addMed(String med) {
    	
    	prePopulate();
        String currentPostString = allPosts.get(currentPost);

        if (postMed.containsKey(currentPost)) {
            String currMed = postMed.get(currentPost);
            postMed.remove(currentPost);
            postMed.put(currentPost, currMed + " " + med);
            
            if (!currentPostString.contains(med))
            	allPosts.set(currentPost, currentPostString + " " + med);
        }
        else {
            postMed.put(currentPost, med);
            if (!currentPostString.contains(med))
            	allPosts.set(currentPost, currentPostString + " " + med);

        }
    }

    public void addEr(String er) {
    	prePopulate();
        String currentPostString = allPosts.get(currentPost);

        if (postEr.containsKey(currentPost)) {
            String currEr = postEr.get(currentPost);
            postEr.remove(currentPost);
            postEr.put(currentPost, currEr + " " + er);
            
            if (!currentPostString.contains(er))
            	allPosts.set(currentPost, currentPostString + " " + er);
        }
        else {
            postEr.put(currentPost, er);
            if (!currentPostString.contains(er))
            	allPosts.set(currentPost, currentPostString + " " + er);

        }
    }

    public void addBkg(String bkg)
    {
    	prePopulate();
        String currentPostString = allPosts.get(currentPost);

        if (postBkg.containsKey(currentPost)) {
            String currBkg = postBkg.get(currentPost);
            postBkg.remove(currentPost);
            postBkg.put(currentPost, currBkg + " " + bkg);
            
            if (!currentPostString.contains(bkg))
            	allPosts.set(currentPost, currentPostString + " " + bkg);
        }
        else {
            postBkg.put(currentPost, bkg);
            if (!currentPostString.contains(bkg))
            	allPosts.set(currentPost, currentPostString + " " + bkg);

        }
    }

    public String getPe(Integer key) {
        if (postPe.containsKey(key)) {
            //System.out.println("PE: " + postPe.get(key));
            return postPe.get(key);
        }
        return "";
    }

    public String getMed(Integer key) {
        if (postMed.containsKey(key)) {
            //System.out.println("MED: " + postMed.get(key));
            return postMed.get(key);
        }
        return "";
    }

    public String getEr(Integer key) {
        if (postEr.containsKey(key)) {
            //System.out.println("ER: " + postEr.get(key));
            return postEr.get(key);
        }
        return "";
    }

    public String getBkg(Integer key) {
        if (postBkg.containsKey(key)) {
            //System.out.println("BKG: " + postBkg.get(key));
            return postBkg.get(key);
        }
        return "";
    }
    
    public void prePopulate()
    {
    	if (allPosts.size() >= Integer.parseInt(numPosts)) return;
    	for (int i = 0; i < Integer.parseInt(numPosts); i++)
    	{
    		allPosts.add("");
    	}
    }

	public String getThreadId() 
	{
		return ExtractThreadId.getId(url);

	}

	@Override
	public String getText(int order) 
	{
		// TODO Auto-generated method stub
		return allPosts.get(order);
	}

	@Override
	public String getId() 
	{
		return this.getThreadId();
	}
	
	public int getSize()
	{
		return this.allPosts.size();
	}
}