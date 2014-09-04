package edu.illinois.cs.timan.iknowx.thread;

public class Thread {
	
	private String tid;
	private String firstPost;
	
	public Thread(String tid, String firstPost) {
		this.tid = tid;
		this.firstPost = firstPost;
	}
	
	public String getTid() {
		return tid;
	}

	public String getFirstPost() {
		return firstPost;
	}
	
}