package com.github.wks.libbyr4j;

import java.util.*;

public class ThreadPage {
	private int maxPageNum;

	private List<PostRow> posts = new ArrayList<PostRow>();

	public int getMaxPageNum() {
		return maxPageNum;
	}

	public void setMaxPageNum(int maxPageNum) {
		this.maxPageNum = maxPageNum;
	}

	public List<PostRow> getPosts() {
		return posts;
	}

	public void setPosts(List<PostRow> posts) {
		this.posts = posts;
	}

}
