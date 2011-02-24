package com.github.wks.libbyr4j;

import java.io.IOException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class Byr {
	private HttpFetcher httpFetcher = new HttpFetcher();
	
	private HtmlCleaner htmlCleaner = new HtmlCleaner();
	
	private TagNode toTree(String content) {
		return htmlCleaner.clean(content);
	}
	
	private TagNode fetchTree(String urlPattern, String... args) throws IOException {
		String url = String.format(urlPattern, args);
		String content = httpFetcher.fetchPage(url);
		TagNode rootNode = toTree(content);
		return rootNode;
	}
	
	public String fetchBoard(String boardName) throws IOException, XPatherException {
		TagNode tree = fetchTree("http://bbs.byr.cn/board/%s", boardName);
		
		return (String)(tree.evaluateXPath("//title/text()")[0]);
	}
	
}
