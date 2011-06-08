package com.camus.feeders;

import java.io.File;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.camus.common.LinkedStory;
import com.camus.util.readability.CamusTextUtil;
import com.camus.util.readability.ContentExtractor;
import com.camus.util.readability.ImageExtractor;
import com.camus.util.readability.SimpleImageInfo;
import com.camus.util.readability.WordStats;

public abstract class FeederTask {

	private static final Logger logger = Logger.getLogger(FeederTask.class);

	protected Document getDOMDocument(String xmlFileName) {
		Document document = null;
		SAXReader reader = new SAXReader();
		try {
			document = reader.read(xmlFileName);
		} catch (DocumentException e) {
			logger.error("Error in getDOMDocument: " + e);
			e.printStackTrace();
		}
		return document;
	}

	public static String retrieveRealURL(String inputURL) {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
					Boolean.FALSE);
			HttpHead httpget = new HttpHead(inputURL);
			HttpResponse response = httpclient.execute(httpget);
			if (response.containsHeader("Location")) {
				Header[] headers = response.getHeaders("Location");
				Header locationHeader = headers[0];
				inputURL = locationHeader.getValue();
			}

		} catch (Exception e) {
			logger.error("Error in retrieveRealURL: " + e);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return inputURL;
	}

	public static LinkedStory parseCrawledLinkedStory(String responseBody,
			LinkedStory linkedStory) {

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
					titleText = doTitleSplits(titleText, "\\|");
					usedDelimeter = true;
				}

				if (titleText.contains("-") && !usedDelimeter) {
					titleText = doTitleSplits(titleText, " - ");
					usedDelimeter = true;
				}
				if (titleText.contains("»") && !usedDelimeter) {
					titleText = doTitleSplits(titleText, "»");
					usedDelimeter = true;
				}

				if (titleText.contains(":") && !usedDelimeter) {
					titleText = doTitleSplits(titleText, ":");
					usedDelimeter = true;
				}
				linkedStory.setStoryTitle(titleText);
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
				linkedStory.setStoryImageURL(ogMetaImageURL);
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
				linkedStory.setStoryDescription(storyDescription);
				logger.debug("storyDescription: " + storyDescription);
			}
		}

		if (linkedStory.getStoryDescription() == null) {
			Pattern ogStoryDescriptionTagPattern = Pattern.compile(
					"\\<meta name=\"og:description\" content=\"(.*?)\"",
					Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			matcher = ogStoryDescriptionTagPattern.matcher(responseBody);
			if (matcher.find()) {
				storyDescription = matcher.group(1).replaceAll("[\\s\\<>]+",
						" ").trim();
				if (storyDescription != null) {
					linkedStory.setStoryDescription(storyDescription);
					logger.debug("storyDescription: " + storyDescription);
				}
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
				linkedStory.setStorySiteName(siteName);
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
				linkedStory.setStoryCanonicalURL(canonicalURL);
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
				linkedStory.setStorySiteType(siteType);
				logger.debug("siteType: " + siteType);
			}
		}

		linkedStory.setStoryContent(responseBody);

		try {
			ContentExtractor contentExtractor = new ContentExtractor();
			String readabilityContents = contentExtractor
					.extractReadabilityContents(responseBody);
			if (readabilityContents != null) {
				linkedStory.setStoryReadabilityContent(readabilityContents);
			}
			WordStats descriptionWordStats = null;
			if (linkedStory.getStoryReadabilityContent() != null) {
				descriptionWordStats = CamusTextUtil
						.getWordCountInfo(linkedStory
								.getStoryReadabilityContent());
			} else if (linkedStory.getStoryDescription() != null) {
				descriptionWordStats = CamusTextUtil
						.getWordCountInfo(linkedStory.getStoryDescription());
			}
			if (descriptionWordStats != null) {
				linkedStory.setStoryParagraphsCount(descriptionWordStats
						.getParagraphsCount());
				linkedStory.setStoryWordsCount(descriptionWordStats
						.getWordCount());
			}

			if ((linkedStory.getStoryImageURL() != null)
					&& (!linkedStory.getStoryImageURL().equals(""))) {

				ResourceBundle rb = ResourceBundle.getBundle("camus");
				boolean downloadImages = Boolean
						.parseBoolean(rb
								.getString("twitter.crawler.download-followed-links-images"));
				if (downloadImages) {
					ImageExtractor imageExtractor = new ImageExtractor();

					String storyURL = null;
					if (linkedStory.getStoryCanonicalURL() != null) {
						storyURL = linkedStory.getStoryCanonicalURL();
					} else {
						storyURL = linkedStory.getStoryURL();
					}

					logger
							.info("parseCrawledLinkedStory linkedStory.getStoryImageURL(): "
									+ linkedStory.getStoryImageURL());

					if ((linkedStory.getStoryImageURL() != null)
							&& (storyURL != null)) {
						String tempFileName = imageExtractor
								.downloadImageToTempFile(storyURL, linkedStory
										.getStoryImageURL());

						/*
						 * SimpleImageInfo simpleImageInfo = new
						 * SimpleImageInfo( new File(tempFileName)); linkedStory
						 * .setStoryImageHeight(simpleImageInfo.getHeight());
						 * linkedStory
						 * .setStoryImageWidth(simpleImageInfo.getWidth());
						 * 
						 * if (linkedStory.getStoryImageWidth() > linkedStory
						 * .getStoryImageHeight()) {
						 * linkedStory.setStoryImageIsLandscape(true); } else {
						 * linkedStory.setStoryImageIsLandscape(false); }
						 */
						linkedStory.setStoryImageURL(tempFileName);
						logger
								.info("parseCrawledLinkedStory setting tempFileName to setStoryImageURL: "
										+ linkedStory.getStoryImageURL());
					}

				} else {
					linkedStory
							.setStoryImageURL(linkedStory.getStoryImageURL());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in retrieving readability or image data: " + e);
		}

		return linkedStory;
	}

	
	public static LinkedStory crawlLinkedStory(String storyURL) {
		return FeederTask.crawlLinkedStory(storyURL, null);
	}
	
	public static LinkedStory crawlLinkedStory(String storyURL, LinkedStory inputLinkedStory) {
		LinkedStory linkedStory = null;
		if (inputLinkedStory != null){
			linkedStory = inputLinkedStory;
		}else{
			linkedStory = new LinkedStory();
		}
		
		linkedStory.setStoryURL(storyURL);
		HttpClient httpclient = new DefaultHttpClient();
		try {
			logger.debug("crawlLinkedStory:" + storyURL);

			HttpGet httpget = new HttpGet(storyURL);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			HttpResponse response = httpclient.execute(httpget);

			if (response.containsHeader("Content-type")) {
				Header[] headers = response.getHeaders("Content-type");
				Header contentTypeHeader = headers[0];
				String contentTypeHeaderString = contentTypeHeader.getValue();
				contentTypeHeaderString = contentTypeHeaderString.toLowerCase();
				contentTypeHeaderString = contentTypeHeaderString.replaceAll(" ", "");
				
				
				if ((!contentTypeHeaderString
						.equals("text/html;charset=utf-8"))
						&& (!contentTypeHeaderString
								.equals("application/xhtml+xml;charset=utf-8"))) {
					
					logger.info("rejecting content header : "
							+ contentTypeHeader.getValue());
					return null;
				} else {
					HttpEntity entity = response.getEntity();
					String responseBody = EntityUtils.toString(entity);

					linkedStory = parseCrawledLinkedStory(responseBody,
							linkedStory);
				}
			}
			return linkedStory;
		} catch (Exception e) {
			logger.error("Error in crawlLinkedStory: " + e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return null;
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

}
