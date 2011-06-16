/*
 Copyright (C) 2011 Red Soldier Limited. All rights reserved.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.camus.feeders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.camus.common.FacebookNewsFeedItem;
import com.camus.common.FeedStory;
import com.camus.common.LinkedStory;
import com.camus.common.Tweet;
import com.camus.util.readability.CamusTextUtil;
import com.camus.util.readability.WordStats;

import org.json.*;

public class FacebookNewsFeedTask extends FeederTask {

	private static final Logger logger = Logger
	        .getLogger(FacebookNewsFeedTask.class);

	private static final String FACEBOOK_NEWSFEED = "https://graph.facebook.com/me/home";
	private String accessTokenString = null;
	private String apiKey = null;
	private String apiSecret = null;

	public FacebookNewsFeedTask() {
		ResourceBundle rb = ResourceBundle.getBundle("camus");
		this.accessTokenString = rb.getString("facebook.user.token");
		this.apiKey = rb.getString("facebook.api.key");
		this.apiSecret = rb.getString("facebook.api.secret");
	}
	
	public List<FeedStory> retrieveFacebookNewsFeed() {
		List<FeedStory> feedsList = null;
		try {
			feedsList = new ArrayList<FeedStory>();
			feedsList = retrieveNewsFeed();
			feedsList = processNewsFeedConcurrent(feedsList);

		} catch (Exception facebookException) {
			logger
					.error("Error in retrieveFacebookNewsFeed "
							+ facebookException);
		}
		return feedsList;
	}
	

	protected List<FeedStory> retrieveNewsFeed() {
		try {
			OAuthService service = new ServiceBuilder().provider(
			        FacebookApi.class).apiKey(this.apiKey).apiSecret(
			        this.apiSecret).build();

			Token accessToken = new Token(accessTokenString, null);

			OAuthRequest request = new OAuthRequest(Verb.GET,
			        FacebookNewsFeedTask.FACEBOOK_NEWSFEED);
			service.signRequest(accessToken, request);
			Response response = request.send();
			String jsonResponse = response.getBody();

			JSONObject resp = new JSONObject(jsonResponse);
			JSONArray dataArray = resp.getJSONArray("data");
			int dataArrayLength = dataArray.length();
			JSONObject dataObject = null;
			JSONObject likesObject = null;
			JSONObject commentsObject = null;
			JSONObject fromObject = null;
			
			

			List<FeedStory> feedsList = new ArrayList<FeedStory>();
			FacebookNewsFeedItem feedStory = null;
			for (int i = 0; i < dataArrayLength; i++) {
				
				dataObject = dataArray.getJSONObject(i);
				
				
				if (dataObject.has("likes")) {
					likesObject = dataObject.getJSONObject("likes");
				}
				if (dataObject.has("comments")) {
					commentsObject = dataObject.getJSONObject("comments");
				}

				feedStory = new FacebookNewsFeedItem();
				feedStory.setSource("facebook");
				if (dataObject.has("message")) {
					feedStory.setStoryContent(dataObject.getString("message"));
				}
				if (dataObject.has("type")){
					feedStory.setType(dataObject.getString("type"));
				}
				
				fromObject = dataObject.getJSONObject("from");
				if (fromObject != null) {
					feedStory.setUserID(fromObject.getString("id"));
					feedStory.setUserName(fromObject.getString("name"));
				}
				if (likesObject != null) {
					feedStory.setLikesCount(likesObject.getInt("count"));
				}
				if (commentsObject != null) {
					feedStory.setCommentsCount(commentsObject.getInt("count"));
				}
				feedStory.setStoryPublishDate(dataObject
				        .getString("created_time"));

				if (feedStory.getType().equals("link")) {
					LinkedStory linkedStory = new LinkedStory();
					
					if (dataObject.has("link")) {
						linkedStory.setStoryURL(dataObject.getString("link"));
					}
					if (dataObject.has("picture")) {
						linkedStory.setStoryImageURL(dataObject.getString("picture"));
					}
					if (dataObject.has("name")) {
						linkedStory.setStoryTitle(dataObject.getString("name"));
					}
					if (dataObject.has("description")) {
						linkedStory.setStoryDescription(dataObject.getString("description"));						
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
					feedStory.setLinkedStory(linkedStory);
				}
				feedsList.add(feedStory);
			}

			return feedsList;

		} catch (Exception twitterException) {
			logger.error("Error in retrieveNewsFeed " + twitterException);
		}
		return null;

	}
	
	
	protected List<FeedStory> processNewsFeedConcurrent(List<FeedStory> feedsList) {
		try {

			Iterator<FeedStory> feedsListIterator = feedsList.iterator();

			ExecutorService threadPool = Executors.newFixedThreadPool(feedsList
					.size());
			CompletionService<FeedStory> pool = new ExecutorCompletionService<FeedStory>(
					threadPool);

			long startTime = System.currentTimeMillis();

			while (feedsListIterator.hasNext()) {
				
				pool.submit(new FacebookNewsFeedProcessingTask(feedsListIterator.next()));
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

		} catch (Exception faceException) {
			logger.error("Error in processNewsFeedConcurrent " + faceException);
		}
		return null;

	}
}


class FacebookNewsFeedProcessingTask implements Callable<FeedStory> {

	private static final Logger logger = Logger
			.getLogger(FacebookNewsFeedProcessingTask.class);

	private FeedStory feedStory;
	private boolean followLinks;

	public FacebookNewsFeedProcessingTask(FeedStory feedStory) {
		this.feedStory = feedStory;
		ResourceBundle rb = ResourceBundle.getBundle("camus");
		this.followLinks = Boolean.parseBoolean(rb.getString(
				"facebook.crawler.follow-links").trim());
	}

	public FeedStory call() throws Exception {

		if (this.followLinks == true) {
			String urlInFeed = null;
			if (feedStory.getLinkedStory() != null){
				urlInFeed = feedStory.getLinkedStory().getStoryURL();
			}
			
			if (urlInFeed != null) {
				
				if (feedStory.getLinkedStory() != null){
				
					LinkedStory linkedStory = FeederTask
						.crawlLinkedStory(urlInFeed, feedStory.getLinkedStory());
				feedStory.setLinkedStory(linkedStory);
				}else{
					LinkedStory linkedStory = FeederTask
					.crawlLinkedStory(urlInFeed);
			feedStory.setLinkedStory(linkedStory);
					
				}

				// remove url feed from story

			}
		}

		return feedStory;
	}
}
