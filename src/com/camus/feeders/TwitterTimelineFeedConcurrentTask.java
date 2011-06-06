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
import com.camus.common.FeedStory;
import com.camus.common.LinkedStory;

public class TwitterTimelineFeedConcurrentTask extends TwitterTimelineFeedTask {

    private static final Logger logger = Logger
	    .getLogger(TwitterTimelineFeedConcurrentTask.class);

    public List<FeedStory> retrieveTwitterArticles() {
	List<FeedStory> feedsList = null;
	try {
	    feedsList = new ArrayList<FeedStory>();
	    feedsList = retrieveTweets();
	    feedsList = processTweetsConcurrent(feedsList);

	} catch (Exception twitterException) {
	    logger.error("Error in retrieveTwitterArticles " + twitterException);
	}
	return feedsList;
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
	
	logger.debug("TweetProcessingTask twitter.crawler.follow: "+Boolean.parseBoolean(rb.getString("twitter.crawler.follow-links")));
	
	this.followLinks = Boolean.parseBoolean(rb.getString("twitter.crawler.follow-links").trim());
    }

    public FeedStory call() throws Exception {
	
	if (this.followLinks == true){
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
