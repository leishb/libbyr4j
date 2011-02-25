package com.github.wks.libbyr4j;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.*;

import org.jaxen.*;
import org.jaxen.dom.*;
import org.w3c.dom.*;

import com.github.wks.libbyr4j.http.HttpFetcher;

public class Byr {
	private HttpFetcher httpFetcher = new HttpFetcher();

	private CleanerProperties cleanerProperties = new CleanerProperties();
	private HtmlCleaner htmlCleaner = new HtmlCleaner(cleanerProperties);

	private DomSerializer domSerializer = new DomSerializer(cleanerProperties);

	private Document fetchDom(String urlPattern, Object... args)
			throws IOException, ParserConfigurationException {
		String url = String.format(urlPattern, args);
		String content = httpFetcher.fetchPage(url);
		Document rootNode = toDom(content);
		return rootNode;
	}

	private Document toDom(String content) throws ParserConfigurationException {
		return domSerializer.createDOM(htmlCleaner.clean(content));
	}

	private static DOMXPath mkXPath(String xpath) {
		try {
			return new DOMXPath(xpath);
		} catch (JaxenException e) {
			throw new IllegalArgumentException("Bad xpath expression", e);
		}
	}

	private static String toStr(Object node) {
		return ((Text) node).getData();
	}

	private static String grepFirstGroup(Pattern pattern, String target) {
		Matcher matcher = pattern.matcher(target);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	static final XPath SERVER_TIME = mkXPath("//div[@id='time']/text()");
	static final Pattern SERVER_TIME_PATTERN = Pattern
			.compile("(\\d+-\\d+-\\d+ \\d+:\\d+)");
	static final SimpleDateFormat SERVER_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	private static Date selectServerTime(Object doc) throws JaxenException {
		String serverTimeNodeStr = SERVER_TIME.stringValueOf(doc);
		String serverTimeStr = grepFirstGroup(SERVER_TIME_PATTERN,
				serverTimeNodeStr);
		try {
			return SERVER_TIME_FORMAT.parse(serverTimeStr);
		} catch (ParseException e) {
			return null;
		}
	}

	static final Pattern ABBREVIATABLE_DATE = Pattern
			.compile("(\\d+-\\d+-\\d+|\\d+:\\d+:\\d+)");
	static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private static Date grepAbbreviatableTime(String str, Date serverTime)
			throws JaxenException {
		String abbrDate = grepFirstGroup(ABBREVIATABLE_DATE, str);
		try {
			return dateFormat.parse(abbrDate);
		} catch (ParseException e) {
			try {
				Date timePortion = timeFormat.parse(abbrDate);

				Calendar now = new GregorianCalendar();
				now.setTime(timePortion);

				Calendar serverTimeCal = new GregorianCalendar();
				serverTimeCal.setTime(serverTime);

				now.set(GregorianCalendar.YEAR,
						serverTimeCal.get(GregorianCalendar.YEAR));
				now.set(GregorianCalendar.MONTH,
						serverTimeCal.get(GregorianCalendar.MONTH));
				now.set(GregorianCalendar.DAY_OF_MONTH,
						serverTimeCal.get(GregorianCalendar.DAY_OF_MONTH));

				return now.getTime();
			} catch (ParseException e2) {
				return null;
			}
		}
	}

	static final XPath BOARD_PAGE__BOARD_MASTERS = mkXPath("//div[@class='b-head corner']/div/a/text()");
	static final XPath BOARD_PAGE__MAX_PAGE_NUM = mkXPath("(//ol[@class='page-main'])[1]/li[last()-1]//text()");
	static final XPath BOARD_PAGE__THREAD_ROWS = mkXPath("//table[@class='board-list tiz']//tr");

	static final XPath THREAD_ROW__TITLE = mkXPath("td[@class='title_9']/a/text()");
	static final XPath THREAD_ROW__AUTHOR = mkXPath("td[@class='title_10']/a/text()");
	static final XPath THREAD_ROW__NUM_REPLIES = mkXPath("td[@class='title_11 middle']/text()");
	static final XPath THREAD_ROW__URL = mkXPath("td[@class='title_9']/a/@href");
	static final XPath THREAD_ROW__DATE = mkXPath("td[@class='title_10']/text()");

	static final Pattern THREAD_ROW__URL__THREAD_ID = Pattern
			.compile("/article/\\w+/(\\d+)");

	public BoardPage getBoard(String boardName) throws ByrException {
		return getBoard(boardName, 1);
	}

	public BoardPage getBoard(String boardName, int pageNum)
			throws ByrException {
		try {
			Document doc = fetchDom("http://bbs.byr.cn/board/%s?p=%d",
					boardName, pageNum);

			BoardPage boardPage = new BoardPage();

			for (Object bm : BOARD_PAGE__BOARD_MASTERS.selectNodes(doc)) {
				boardPage.getBoardMasters().add(toStr(bm));
			}

			boardPage.setMaxPageNum(BOARD_PAGE__MAX_PAGE_NUM.numberValueOf(doc)
					.intValue());
			
			Date serverTime = selectServerTime(doc);

			for (Object tr : BOARD_PAGE__THREAD_ROWS.selectNodes(doc)) {
				ThreadRow threadRow = new ThreadRow();

				String url = THREAD_ROW__URL.stringValueOf(tr);
				int threadId = Integer.parseInt(grepFirstGroup(
						THREAD_ROW__URL__THREAD_ID, url));

				String dateNodeStr = THREAD_ROW__DATE.stringValueOf(tr);
				Date date = grepAbbreviatableTime(dateNodeStr, serverTime);

				threadRow.setThreadId(threadId);
				threadRow.setTitle(THREAD_ROW__TITLE.stringValueOf(tr));
				threadRow.setAuthor(THREAD_ROW__AUTHOR.stringValueOf(tr));
				threadRow.setDate(date);
				threadRow.setNumReplies(THREAD_ROW__NUM_REPLIES.numberValueOf(
						tr).intValue());

				boardPage.getThreads().add(threadRow);
			}

			return boardPage;
		} catch (Exception e) {
			throw new ByrException("Error getting board:" + boardName, e);
		}
	}
	
}
