package com.github.wks.libbyr4j;

import java.util.Date;

public class PostRow {
	private int postId;
	private String title;
	private String author;
	private String content;
	private Date date;
	private String sourceIpAddress;

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSourceIpAddress() {
		return sourceIpAddress;
	}

	public void setSourceIpAddress(String sourceIpAddress) {
		this.sourceIpAddress = sourceIpAddress;
	}

}
