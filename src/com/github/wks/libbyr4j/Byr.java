package com.github.wks.libbyr4j;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
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

	static final XPath MAX_PAGE_NUM = mkXPath("(//ol[@class='page-main'])[1]/li//text()");

	private static int selectMaxPageNum(Document doc) throws JaxenException {
		int mpn = -1;
		for (Object obj : MAX_PAGE_NUM.selectNodes(doc)) {
			try {
				int pn = Integer.parseInt(toStr(obj));
				if (pn > mpn) {
					mpn = pn;
				}
			} catch (NumberFormatException e) {
				// IGNORE "<<" and ">>"
			}
		}
		return mpn;
	}

	static final XPath BOARD_PAGE__BOARD_MASTERS = mkXPath("//div[@class='b-head corner']/div/a/text()");
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

			boardPage.setMaxPageNum(selectMaxPageNum(doc));

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

	static final XPath THREAD_PAGE__POST_ROWS = mkXPath("//table[@class='article']");
	static final XPath POST_ROW__AUTHOR = mkXPath(".//span[@class='u-name']/a/text()");
	static final XPath POST_ROW__REPLY_URL = mkXPath(".//li[@class='a-reply']/a/@href");
	static final XPath POST_ROW__CONTENT = mkXPath(".//td/p/node()");

	static final char NBSP = '\u00a0';

	static final Pattern POST_ROW__TITLE = Pattern.compile("标\\s+题:(.*)\n");
	static final Pattern POST_ROW__DATE = Pattern
			.compile("\\w+\\s+(\\w+\\s+\\d+\\s+\\d+:\\d+:\\d+\\s+\\d+)");
	static final SimpleDateFormat POST_ROW__DATE_FORMAT = new SimpleDateFormat(
			"MMM dd HH:mm:ss yyyy", Locale.US);

	static final Pattern POST_ROW__SOURCE_IPADDR = Pattern
			.compile("※\\s+来源.*\\[FROM:\\s+([0-9\\.\\*\\:]+)\\]");

	private static Date grepPostRowDate(String header) {
		String dateText = grepFirstGroup(POST_ROW__DATE, header);
		try {
			return POST_ROW__DATE_FORMAT.parse(dateText);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static String removeNbsp(String str) {
		return str.replace(NBSP, ' ');
	}

	public ThreadPage getThread(String boardName, int threadId)
			throws ByrException {
		return getThread(boardName, threadId, 1);
	}

	public ThreadPage getThread(String boardName, int threadId, int pageNum) {
		try {
			Document doc = fetchDom("http://bbs.byr.cn/article/%s/%d?p=%d",
					boardName, threadId, pageNum);

			ThreadPage threadPage = new ThreadPage();

			threadPage.setMaxPageNum(selectMaxPageNum(doc));

			for (Object pr : THREAD_PAGE__POST_ROWS.selectNodes(doc)) {
				PostRow postRow = new PostRow();

				String replyUrl = POST_ROW__REPLY_URL.stringValueOf(doc);
				int slash = replyUrl.lastIndexOf('/');
				int postId = Integer.parseInt(replyUrl.substring(slash + 1));

				StringBuilder sb = new StringBuilder();
				for (Object obj : POST_ROW__CONTENT.selectNodes(pr)) {
					boolean isBr = false;
					if (obj instanceof Element) {
						Element elem = ((Element) obj);
						if (elem.getNodeName().equalsIgnoreCase("br")) {
							isBr = true;
						}
					}

					if (isBr) {
						sb.append("\n");
					} else {
						sb.append(removeNbsp(((Node) obj).getTextContent())
								.trim());
					}
				}

				String bigContent = sb.toString();

				// split at an empty line;
				int headerSplitter = bigContent.indexOf("\n\n") + 1;

				String header = bigContent.substring(0, headerSplitter);

				int footerSplitter = bigContent.length();

				for (int i = bigContent.length() - 1; i >= headerSplitter; i--) {
					if (bigContent.charAt(i) == '※') {
						if (bigContent.charAt(i - 1) == '\n') {
							footerSplitter = i;
						} else {
							break;
						}
					} else if (bigContent.charAt(i) == '\n') {
						break;
					}
				}

				String footer = bigContent.substring(footerSplitter);

				String smallContent = bigContent.substring(headerSplitter,
						footerSplitter).trim();

				String title = grepFirstGroup(POST_ROW__TITLE, header).trim();
				Date date = grepPostRowDate(header);
				String sourceIpAddress = grepFirstGroup(
						POST_ROW__SOURCE_IPADDR, footer);

				postRow.setPostId(postId);
				postRow.setTitle(title);
				postRow.setAuthor(POST_ROW__AUTHOR.stringValueOf(pr));
				postRow.setContent(smallContent);
				postRow.setDate(date);
				postRow.setSourceIpAddress(sourceIpAddress);

				threadPage.getPosts().add(postRow);
			}

			return threadPage;
		} catch (Exception e) {
			throw new ByrException("Error getting thread:" + threadId
					+ " from board " + boardName, e);
		}
	}

	private static final XPath USER_PAGE__NICK_NAME = mkXPath("//dl[@class='u-info']/dd[1]");
	private static final XPath USER_PAGE__GENDER = mkXPath("//dl[@class='u-info']/dd[2]");
	private static final XPath USER_PAGE__ZODIAC = mkXPath("//dl[@class='u-info']/dd[3]");
	private static final XPath USER_PAGE__QQ = mkXPath("//dl[@class='u-info']/dd[4]");
	private static final XPath USER_PAGE__MSN = mkXPath("//dl[@class='u-info']/dd[5]");
	private static final XPath USER_PAGE__HOME_PAGE = mkXPath("//dl[@class='u-info']/dd[6]");

	private static final XPath USER_PAGE__DETAIL_INFO = mkXPath("//dl[@class='u-info u-detail']/*/text()");

	private String weizhiable(String str) {
		if (str.equals("未知")) {
			return null;
		} else {
			return str;
		}
	}

	private static final Pattern INTEGER_PATTERN = Pattern.compile("(\\d+)");

	private static int grepInt(String str) {
		Matcher m = INTEGER_PATTERN.matcher(str);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		} else {
			return -1;
		}
	}

	private static Date parseIsoDataFormat(String dateString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

	private static boolean parseOnline(String str) {
		return !str.equals("目前不在线上");
	}

	public UserPage getUser(String userName) {
		try {
			Document doc = fetchDom("http://bbs.byr.cn/user/query/%s", userName);

			UserPage userPage = new UserPage();

			userPage.setUserName(userName);
			userPage.setNickName(weizhiable(USER_PAGE__NICK_NAME
					.stringValueOf(doc)));
			userPage.setGender(weizhiable(USER_PAGE__GENDER.stringValueOf(doc)));
			userPage.setZodiac(weizhiable(USER_PAGE__ZODIAC.stringValueOf(doc)));
			userPage.setQq(weizhiable(USER_PAGE__QQ.stringValueOf(doc)));
			userPage.setMsn(weizhiable(USER_PAGE__MSN.stringValueOf(doc)));
			userPage.setHomePage(weizhiable(USER_PAGE__HOME_PAGE
					.stringValueOf(doc)));

			@SuppressWarnings("unchecked")
			List<Object> details = USER_PAGE__DETAIL_INFO.selectNodes(doc);
			for (int i = 0; i < details.size()-1; i += 2) {
				String fieldName = ((Text)details.get(i)).getData();
				String fieldValue = ((Text)details.get(i+1)).getData();
				
				if(fieldName.equals("论坛等级：")) {
					userPage.setLevel(fieldValue);
				} else if (fieldName.equals("帖子总数：")) {
					userPage.setTotalPosts(grepInt(fieldValue));
				} else if (fieldName.equals("积分：")) {
					userPage.setScore(Integer.parseInt(fieldValue));
				} else if (fieldName.equals("生命力：")) {
					userPage.setLife(Integer.parseInt(fieldValue));
				} else if (fieldName.equals("上次登录：")) {
					userPage.setLastLogin(parseIsoDataFormat(fieldValue));
				} else if (fieldName.equals("最后访问IP：")) {
					userPage.setLastIpAddress(fieldValue);
				} else if (fieldName.equals("当前状态：")) {
					userPage.setOnlineNow(parseOnline(fieldValue));
				}		
			}

			return userPage;
		} catch (Exception e) {
			throw new ByrException("Error getting user:" + userName, e);
		}
	}
}
