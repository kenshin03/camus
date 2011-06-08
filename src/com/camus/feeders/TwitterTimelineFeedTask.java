package com.camus.feeders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.camus.common.FeedStory;
import com.camus.common.LinkedStory;
import com.camus.common.Tweet;

public class TwitterTimelineFeedTask extends FeederTask {

	private static final Logger logger = Logger
			.getLogger(TwitterTimelineFeedTask.class);
	
	// private final String twitterRequestTokenString = "6985360";
	private static final String TWITTER_TIMELINE = "http://api.twitter.com/1/statuses/home_timeline.xml";
	private String accessTokenString = null;
	private String accessTokenSecretString = null;
	private String apiKey = null;
	private String apiSecret = null;

	public TwitterTimelineFeedTask() {
		ResourceBundle rb = ResourceBundle.getBundle("camus");
		this.accessTokenString = rb.getString("twitter.user.token");
		this.accessTokenSecretString = rb.getString("twitter.user.secret");
		this.apiKey = rb.getString("twitter.api.key");
		this.apiSecret = rb.getString("twitter.api.secret");
	}
	

	public List<FeedStory> retrieveTwitterArticles() {
		List<FeedStory> feedsList = null;
		try {
			feedsList = new ArrayList<FeedStory>();
			feedsList = retrieveTweets();
			feedsList = processTweetsConcurrent(feedsList);

		} catch (Exception twitterException) {
			logger
					.error("Error in retrieveTwitterArticles "
							+ twitterException);
		}
		return feedsList;
	}


	protected List<FeedStory> retrieveTweets() {
		try {
			OAuthService service = new ServiceBuilder().provider(
					TwitterApi.class).apiKey(this.apiKey).apiSecret(
					this.apiSecret).build();

			Token accessToken = new Token(accessTokenString,
					accessTokenSecretString);

			OAuthRequest request = new OAuthRequest(Verb.GET,
					TwitterTimelineFeedTask.TWITTER_TIMELINE);
			service.signRequest(accessToken, request);
			Response response = request.send();
			String xmlResponse = response.getBody();

			logger.debug(xmlResponse);
			Document domDocument = DocumentHelper.parseText(xmlResponse);
			// this.getDOMDocument(xmlResponse);
			List<Node> statusNodesList = (List<Node>) domDocument
					.selectNodes("//statuses/status");
			String tweetText = "";
			String userName = "";
			String userID = "";
			String userThumbnail = "";
			Tweet tweet = null;
			String createdDate = null;

			List<FeedStory> feedsList = new ArrayList<FeedStory>();
			for (Node domNode : statusNodesList) {
				tweetText = domNode.selectSingleNode("text").getStringValue();
				createdDate = domNode.selectSingleNode("created_at")
						.getStringValue();
				Node userNode = domNode.selectSingleNode("user");
				userID = userNode.selectSingleNode("id").getStringValue();
				userName = userNode.selectSingleNode("screen_name")
						.getStringValue();
				userThumbnail = userNode.selectSingleNode("profile_image_url")
						.getStringValue();

				tweet = new Tweet();
				tweet.setUserID(userID);
				tweet.setUserName(userName);
				tweet.setUserThumbnail(userThumbnail);
				tweet.setStoryContent(tweetText);
				tweet.setStoryPublishDate(createdDate);
				tweet.setSource("twitter");

				feedsList.add(tweet);
			}

			return feedsList;

		} catch (Exception twitterException) {
			logger.error("Error in retrieveTweets " + twitterException);
		}
		return null;

	}

	protected List<FeedStory> processTweetsConcurrent(List<FeedStory> feedsList) {
		try {

			logger.info("processTweetsConcurrent start!!!!! threads: "
					+ feedsList.size());

			Iterator<FeedStory> feedsListIterator = feedsList.iterator();

			ExecutorService threadPool = Executors.newFixedThreadPool(feedsList
					.size());
			CompletionService<FeedStory> pool = new ExecutorCompletionService<FeedStory>(
					threadPool);

			long startTime = System.currentTimeMillis();

			while (feedsListIterator.hasNext()) {
				pool.submit(new TweetProcessingTask(feedsListIterator.next()));
			}
			long endTime = System.currentTimeMillis();
			double elapsedTime = endTime - startTime;

			List<FeedStory> returnedList = new ArrayList<FeedStory>();
			FeedStory feedStory = null;
			for (int i = 0; i < feedsList.size(); i++) {
				feedStory = pool.take().get();
				returnedList.add(feedStory);
			}
			threadPool.shutdown();

			return feedsList;

		} catch (Exception twitterException) {
			logger.error("Error in processTweets " + twitterException);
		}
		return null;

	}
}

class TweetProcessingTask implements Callable<FeedStory> {

	private static final Logger logger = Logger
			.getLogger(TweetProcessingTask.class);

	private FeedStory feedStory;
	private boolean followLinks;

	public TweetProcessingTask(FeedStory feedStory) {
		this.feedStory = feedStory;
		ResourceBundle rb = ResourceBundle.getBundle("camus");

		logger.debug("TweetProcessingTask twitter.crawler.follow: "
				+ Boolean.parseBoolean(rb
						.getString("twitter.crawler.follow-links")));

		this.followLinks = Boolean.parseBoolean(rb.getString(
				"twitter.crawler.follow-links").trim());
	}

	public FeedStory call() throws Exception {

		if (this.followLinks == true) {
			String urlInFeed = FeederTask.extractURLInFeed(feedStory
					.getStoryContent());
			if (urlInFeed != null) {
				LinkedStory linkedStory = FeederTask
						.crawlLinkedStory(urlInFeed);
				feedStory.setLinkedStory(linkedStory);

				// remove url feed from story

			}
		}

		return feedStory;
	}
}
