package com.camus.common;

import java.util.List;

public class LinkedStory {
	
	private String storyURL;
	private String storyContent;
	private String storyReadabilityContent;
	private String storyTitle;
	private String storyImageURL;
	private String storyDescription;
	private String storySiteName;
	private String storyCanonicalURL;
	private String storySiteType;
	private int storyImageWidth;
	private int storyImageHeight;
	private int storyWordsCount;
	private int storyParagraphsCount;
	private boolean storyImageIsLandscape;
	
	public boolean isStoryImageIsLandscape() {
	    return storyImageIsLandscape;
	}
	public void setStoryImageIsLandscape(boolean storyImageIsLandscape) {
	    this.storyImageIsLandscape = storyImageIsLandscape;
	}
	public int getStoryWordsCount() {
	    return storyWordsCount;
	}
	public void setStoryWordsCount(int storyWordsCount) {
	    this.storyWordsCount = storyWordsCount;
	}
	public int getStoryParagraphsCount() {
	    return storyParagraphsCount;
	}
	public void setStoryParagraphsCount(int storyParagraphsCount) {
	    this.storyParagraphsCount = storyParagraphsCount;
	}
	public int getStoryImageWidth() {
	    return storyImageWidth;
	}
	public void setStoryImageWidth(int storyImageWidth) {
	    this.storyImageWidth = storyImageWidth;
	}
	public int getStoryImageHeight() {
	    return storyImageHeight;
	}
	public void setStoryImageHeight(int storyImageHeight) {
	    this.storyImageHeight = storyImageHeight;
	}
	public int getStoryImageWordsCount() {
	    return storyWordsCount;
	}
	public void setStoryImageWordsCount(int storyImageWordsCount) {
	    this.storyWordsCount = storyImageWordsCount;
	}
	public String getStorySiteType() {
		return storySiteType;
	}
	public void setStorySiteType(String storySiteType) {
		this.storySiteType = storySiteType;
	}
	public String getStoryCanonicalURL() {
		return storyCanonicalURL;
	}
	public void setStoryCanonicalURL(String storyCanonicalURL) {
		this.storyCanonicalURL = storyCanonicalURL;
	}
	public String getStorySiteName() {
		return storySiteName;
	}
	public void setStorySiteName(String storySiteName) {
		this.storySiteName = storySiteName;
	}
	public String getStoryDescription() {
		return storyDescription;
	}
	public void setStoryDescription(String storyDescription) {
		this.storyDescription = storyDescription;
	}
	public String getStoryImageURL() {
		return storyImageURL;
	}
	public void setStoryImageURL(String storyImageURL) {
		this.storyImageURL = storyImageURL;
	}
	public String getStoryTitle() {
		return storyTitle;
	}
	public void setStoryTitle(String storyTitle) {
		this.storyTitle = storyTitle;
	}
	
	public String getStoryReadabilityContent() {
		return storyReadabilityContent;
	}
	public void setStoryReadabilityContent(String storyReadabilityContent) {
		this.storyReadabilityContent = storyReadabilityContent;
	}
	private List<StoryImage> imagesList;
	
	public String getStoryURL() {
		return storyURL;
	}
	public void setStoryURL(String storyURL) {
		this.storyURL = storyURL;
	}
	public String getStoryContent() {
		return storyContent;
	}
	public void setStoryContent(String storyContent) {
		this.storyContent = storyContent;
	}
	public List<StoryImage> getImagesList() {
		return imagesList;
	}
	public void setImagesList(List<StoryImage> imagesList) {
		this.imagesList = imagesList;
	}
	

}
