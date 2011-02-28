package com.github.wks.libbyr4j;

import java.util.Date;

public class UserPage {
	private String userName;
	private String nickName;
	private String gender;
	private String zodiac;
	private String qq;
	private String msn;
	private String homePage;
	private String level;
	private int totalPosts;
	private int score;
	private int life;
	private Date lastLogin;
	private String lastIpAddress;
	private boolean onlineNow;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getZodiac() {
		return zodiac;
	}

	public void setZodiac(String zodiac) {
		this.zodiac = zodiac;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getMsn() {
		return msn;
	}

	public void setMsn(String msn) {
		this.msn = msn;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getTotalPosts() {
		return totalPosts;
	}

	public void setTotalPosts(int totalPosts) {
		this.totalPosts = totalPosts;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getLastIpAddress() {
		return lastIpAddress;
	}

	public void setLastIpAddress(String lastIpAddress) {
		this.lastIpAddress = lastIpAddress;
	}

	public boolean isOnlineNow() {
		return onlineNow;
	}

	public void setOnlineNow(boolean onlineNow) {
		this.onlineNow = onlineNow;
	}

}
