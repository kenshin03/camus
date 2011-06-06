package com.camus.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;

import com.camus.common.FeedStory;
import com.camus.feeders.TwitterTimelineFeedConcurrentTask;
import com.camus.layout.LayoutTemplates;
import com.camus.layout.Section;
import com.camus.layout.StoryLayoutAnalyzer;
import com.google.gson.Gson;

public class TestClient {

	private static final Logger logger = Logger.getLogger(TestClient.class);
	
	public static void main(String args[]) {
	    logger.info("TestClient start");
	    try{
		long startTime = System.currentTimeMillis();  

		TwitterTimelineFeedConcurrentTask twitterFeedTask = new TwitterTimelineFeedConcurrentTask();
		List<FeedStory> tweetsList = twitterFeedTask.retrieveTwitterArticles();
		
		StoryLayoutAnalyzer layoutAnalyzer = new StoryLayoutAnalyzer();
		Section section = layoutAnalyzer.analyzeStories(tweetsList);
		
		Gson gson = new Gson();
		String jsonToReturn = gson.toJson(section);  
		
		logger.info(jsonToReturn);
		
		long endTime = System.currentTimeMillis();  
		double elapsedTime = endTime - startTime;
		logger.info("Secs taken: " + elapsedTime/1000);
		
		
		
	    }catch(Exception e){
		e.printStackTrace();
		logger.error("Error in testClient: " + e);
	    }
		
	}
}
