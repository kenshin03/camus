package com.camus.common;

public class Tweet {
	private String storyID;
	private String userName;
	private String userID;
	private String userThumbnail;
	private String storyPublishDate;
	private String storyContent;
	private String source;
	public String getSource() {
	    return source;
	}
	public void setSource(String source) {
	    this.source = source;
	}

	public String getStoryID() {
		return storyID;
	}
	public void setStoryID(String storyID) {
		this.storyID = storyID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserThumbnail() {
		return userThumbnail;
	}
	public void setUserThumbnail(String userThumbnail) {
		this.userThumbnail = userThumbnail;
	}
	public String getStoryPublishDate() {
		return storyPublishDate;
	}
	public void setStoryPublishDate(String storyPublishDate) {
		this.storyPublishDate = storyPublishDate;
	}
	public String getStoryContent() {
		return storyContent;
	}
	public void setStoryContent(String storyContent) {
		this.storyContent = storyContent;
	}
	
}
