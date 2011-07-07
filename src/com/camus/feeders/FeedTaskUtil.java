package com.camus.feeders;

import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.camus.common.LinkedSiteMeta;

public class FeedTaskUtil {

    private static final Logger logger = Logger.getLogger(FeederTask.class);
    
    public static int fetchTweetCount(String url){
	try{
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://urls.api.twitter.com/1/urls/count.json?url="+url);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		String responseBody = EntityUtils.toString(entity);
		JSONObject dataObject = new JSONObject(responseBody);
		if (dataObject.has("count")) {
		    return dataObject.getInt("count");
		}else{
		    return 0;
		}
	    
	}catch(Exception e){
	    logger.error("Exception in fetching tweet count for " + url + ": "+e);
	    return -1;
	}
    }
    
    public static int fetchFacebookShares(String url){
	try{
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("https://graph.facebook.com/?id="+URLEncoder.encode(url));
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		String responseBody = EntityUtils.toString(entity);
		JSONObject dataObject = new JSONObject(responseBody);
		if (dataObject.has("shares")) {
		    return dataObject.getInt("shares");
		}else{
		    return 0;
		}
	    
	}catch(Exception e){
	    logger.error("Exception in fetching shares count for " + url + ": "+e);
	    return -1;
	}
    }
    
    

    public static String extractURLInFeed(String feedStory) {
	String foundURL = null;
	try {
	    Pattern pattern = Pattern
		    .compile("((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)");
	    Matcher matcher = pattern.matcher(feedStory);

	    while (matcher.find()) {
		foundURL = matcher.group();
		break;
	    }

	} catch (Exception e) {
	    logger.error("Error in extractURLInFeed: " + e);
	}
	return foundURL;
    }

    /**
     * based on a delimiter in the title take the longest piece or do some
     * custom logic based on the site
     * 
     * @param title
     * @param delimeter
     * @return
     */
    public static String doTitleSplits(String title, String delimeter) {

	String largeText = "";
	int largetTextLen = 0;

	String[] titlePieces = title.split(delimeter);

	// take the largest split
	for (String p : titlePieces) {
	    if (p.length() > largetTextLen) {
		largeText = p;
		largetTextLen = p.length();
	    }
	}

	largeText = largeText.replace("&raquo;", "");
	largeText = largeText.replace("»", "");

	return largeText.trim();

    }

    public static LinkedSiteMeta parseLinkedSiteHeader(String responseBody) {

	LinkedSiteMeta linkedSiteMeta = new LinkedSiteMeta();

	// parse header and meta tags
	Pattern titleTagPattern = Pattern.compile("\\<title>(.*)\\</title>",
	        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	Matcher matcher = titleTagPattern.matcher(responseBody);
	String titleText = null;
	if (matcher.find()) {
	    titleText = matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	    if (titleText != null) {
		boolean usedDelimeter = false;
		if (titleText.contains("|")) {
		    titleText = FeedTaskUtil.doTitleSplits(titleText, "\\|");
		    usedDelimeter = true;
		}
		if (titleText.contains("-") && !usedDelimeter) {
		    titleText = FeedTaskUtil.doTitleSplits(titleText, " - ");
		    usedDelimeter = true;
		}
		if (titleText.contains("»") && !usedDelimeter) {
		    titleText = FeedTaskUtil.doTitleSplits(titleText, "»");
		    usedDelimeter = true;
		}
		if (titleText.contains(":") && !usedDelimeter) {
		    titleText = FeedTaskUtil.doTitleSplits(titleText, ":");
		    usedDelimeter = true;
		}
		linkedSiteMeta.setTitleText(titleText);
		logger.debug("title: " + titleText);
	    }
	}

	String ogMetaImageURL = null;
	Pattern ogImageTagPattern = Pattern.compile(
	        "\\<meta property=\"og:image\" content=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	matcher = ogImageTagPattern.matcher(responseBody);
	if (matcher.find()) {
	    ogMetaImageURL = matcher.group(1).replaceAll("[\\s\\<>]+", " ")
		    .trim();
	    if (ogMetaImageURL != null) {
		linkedSiteMeta.setMetaImageURL(ogMetaImageURL);
		logger.debug("ogImageURL: " + ogMetaImageURL);
	    }
	}

	String storyDescription = null;
	Pattern storyDescriptionTagPattern = Pattern.compile(
	        "\\<meta name=\"description\" content=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	matcher = storyDescriptionTagPattern.matcher(responseBody);
	if (matcher.find()) {
	    storyDescription = matcher.group(1).replaceAll("[\\s\\<>]+", " ")
		    .trim();
	    if (storyDescription != null) {
		linkedSiteMeta.setDescription(storyDescription);
		logger.debug("storyDescription: " + storyDescription);
	    }
	}

	Pattern ogStoryDescriptionTagPattern = Pattern.compile(
	        "\\<meta name=\"og:description\" content=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	matcher = ogStoryDescriptionTagPattern.matcher(responseBody);
	if (matcher.find()) {
	    storyDescription = matcher.group(1).replaceAll("[\\s\\<>]+", " ")
		    .trim();
	    if (storyDescription != null) {
		linkedSiteMeta.setDescription(storyDescription);
		logger.debug("storyDescription: " + storyDescription);
	    }
	}

	String siteName = null;
	Pattern siteNameTagPattern = Pattern.compile(
	        "\\<meta property=\"og:site_name\" content=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE);
	matcher = siteNameTagPattern.matcher(responseBody);
	if (matcher.find()) {
	    siteName = matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	    if (siteName != null) {
		linkedSiteMeta.setSiteName(siteName);
		logger.debug("siteName: " + siteName);
	    }
	}

	String canonicalURL = null;
	Pattern canonicalURLPattern = Pattern.compile(
	        "\\<link rel=\"canonical\" href=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE);
	matcher = canonicalURLPattern.matcher(responseBody);
	if (matcher.find()) {
	    canonicalURL = matcher.group(1).replaceAll("[\\s\\<>]+", " ")
		    .trim();
	    if (canonicalURL != null) {
		linkedSiteMeta.setCanonicalURL(canonicalURL);
		logger.debug("canonicalURL: " + canonicalURL);
	    }
	}else{
	    // try 
		Pattern canonicalURL2Pattern = Pattern.compile("\\<meta property=\"og:url\" content=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
		matcher = canonicalURL2Pattern.matcher(responseBody);
		if (matcher.find()) {
		    canonicalURL = matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
		}
		    if (canonicalURL != null) {
			linkedSiteMeta.setCanonicalURL(canonicalURL);
			logger.debug("canonicalURL: " + canonicalURL);
		    }
	}

	String siteType = null;
	Pattern siteTypeTagPattern = Pattern.compile(
	        "\\<meta property=\"og:type\" content=\"(.*?)\"",
	        Pattern.CASE_INSENSITIVE);
	matcher = siteTypeTagPattern.matcher(responseBody);
	if (matcher.find()) {
	    siteType = matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
	    if (siteName != null) {
		linkedSiteMeta.setSiteType(siteType);
		logger.debug("siteType: " + siteType);
	    }
	}

	return linkedSiteMeta;
    }

}
