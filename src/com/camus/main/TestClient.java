package com.camus.main;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import com.camus.feeders.TwitterTimelineFeedTask;

public class TestClient {

	private static final Logger logger = Logger.getLogger(TestClient.class);

	public static void main(String args[]) {
		logger.info("TestClient start");
		try {
			long startTime = System.currentTimeMillis();
			  TwitterTimelineFeedTask twitterFeedTask = new TwitterTimelineFeedTask();
			  twitterFeedTask.retrieveTweets();
			  twitterFeedTask.processTweets();

// deal with output and formatting again later			  
//			  StoryLayoutAnalyzer layoutAnalyzer = new StoryLayoutAnalyzer();
//			  Section section = layoutAnalyzer.analyzeStories(tweetsList);
/*
			FacebookNewsFeedTask facebookTask = new FacebookNewsFeedTask();
			List<FeedStory> fbNewsFeedsList = facebookTask.retrieveFacebookNewsFeed();
			StoryLayoutAnalyzer layoutAnalyzer = new StoryLayoutAnalyzer();
			Section section = layoutAnalyzer.analyzeStories(fbNewsFeedsList);
*/

//			Gson gson = new Gson();
//			String jsonToReturn = gson.toJson(section);

//			logger.info(jsonToReturn);

			long endTime = System.currentTimeMillis();
			double elapsedTime = endTime - startTime;
			logger.info("Secs taken: " + elapsedTime / 1000);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in testClient: " + e);
		}

	}
}
