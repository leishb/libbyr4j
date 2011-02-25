package com.github.wks.libbyr4j;

import java.util.*;

/**
 * A page representing a board.
 * <p>
 * Note that boards may be paginated.
 */
public class BoardPage {
	private List<String> boardMasters = new ArrayList<String>();
	private int maxPageNum;
	private List<ThreadRow> threads = new ArrayList<ThreadRow>();

	public List<String> getBoardMasters() {
		return boardMasters;
	}

	public void setBoardMasters(List<String> boardMasters) {
		this.boardMasters = boardMasters;
	}

	public int getMaxPageNum() {
		return maxPageNum;
	}

	public void setMaxPageNum(int maxPageNum) {
		this.maxPageNum = maxPageNum;
	}

	public List<ThreadRow> getThreads() {
		return threads;
	}

	public void setThreads(List<ThreadRow> threads) {
		this.threads = threads;
	}
}
