package com.github.wks.libbyr4j;

import java.util.Date;

/**
 * A row in a BoardPage.
 */
public class ThreadRow {
	private int threadId;
	private String title;
	private String url;
	private String author;
	private Date date;
	private int numReplies;

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getNumReplies() {
		return numReplies;
	}

	public void setNumReplies(int numReplies) {
		this.numReplies = numReplies;
	}

}
