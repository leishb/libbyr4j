package com.github.wks.libbyr4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class HttpFetcher {
	private String host = "bbs.byr.cn";

	private String defaultCharset = "GB18030";

	private boolean cache = false;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}

	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	private Map<String, String> cacheMap = new HashMap<String, String>();

	public String fetchPage(String url) throws IOException {
		if (cache) {
			String cachedContent = cacheMap.get(url);
			if (cachedContent != null) {
				return cachedContent;
			}
		}

		URL url2;

		try {
			url2 = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed URL", e);
		}

		String content;

		InputStream istream = url2.openStream();
		try {
			content = IOUtils.toString(istream, defaultCharset);
		} finally {
			istream.close();
		}

		if (cache) {
			cacheMap.put(url, content);
		}

		return content;
	}

}
