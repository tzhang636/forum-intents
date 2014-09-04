package edu.illinois.cs.timan.iknowx.datastructure;

public abstract class AbstractPostObject 
{
	/**
	 * Gets text from post.
	 * Order corresponds to n-th post, if any.
	 * 
	 * @param order
	 * @return
	 */
	public abstract String getText(int order);
	
	/**
	 * Gets size of the post 
	 * @return
	 */
	public abstract int getSize();
	
	/**
	 * Returns the corresponding ID for the object
	 * @return
	 */
	public abstract String getId();
	
	public abstract String getTitle();
}
