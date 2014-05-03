package edu.illinois.cs.timan.iknowx.datastructure;


public class ExtractThreadId 
{
	public static String getId(String queryUrl)
	{
		String [] urlGet = queryUrl.split("\\?");
		String queryId = "";
        if (urlGet != null && urlGet.length == 2)
        {
        	String [] params = urlGet[1].split("&");
        	if (params != null && params.length > 1)
        	{
        		queryId = params[0].substring(2);
        		return queryId;
        	}
        }
        return "-1";
	}

}
