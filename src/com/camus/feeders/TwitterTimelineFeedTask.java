package com.camus.feeders;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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

import com.camus.common.LinkedSiteMeta;
import com.camus.util.readability.ContentExtractor;

import org.im4java.core.IdentifyCmd;

import com.ipeirotis.readability.BagOfReadabilityObjects;
import com.ipeirotis.readability.Readability;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import org.im4java.core.*;
import org.im4java.process.*;

public class TwitterTimelineFeedTask extends FeederTask {
    
    public static final String LINKED_SITE_CRAWLED  = "C";
    public static final String LINKED_SITE_READABILITY_ANALYSED  = "R";
    public static final String LINKED_SITE_IMAGE_ANALYSED  = "I";

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


    public void retrieveTweets() {
	try {
	    OAuthService service = new ServiceBuilder()
		    .provider(TwitterApi.class).apiKey(this.apiKey)
		    .apiSecret(this.apiSecret).build();

	    Token accessToken = new Token(accessTokenString,
		    accessTokenSecretString);

	    OAuthRequest request = new OAuthRequest(Verb.GET,
		    TwitterTimelineFeedTask.TWITTER_TIMELINE);
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    String xmlResponse = response.getBody();

	    logger.info(xmlResponse);
	    
	    Document domDocument = DocumentHelper.parseText(xmlResponse);
	    
	    Mongo mongo = new Mongo( "localhost" , 27017 );
	    DB camusDB = mongo.getDB("camus");
	    DBCollection feedItemsCollection = camusDB.getCollection("feedItems");
	    
	    List<Node> statusNodesList = (List<Node>) domDocument
		    .selectNodes("//statuses/status");
	    BasicDBObject dbObj = null;
	    BasicDBObject userDbObj = null;
	    String createDateString = null;

	    List<DBObject> feedsList = new ArrayList<DBObject>();
	    for (Node domNode : statusNodesList) {
		createDateString = domNode.selectSingleNode("created_at").getStringValue();
		dbObj =  new BasicDBObject();
		dbObj.put("user_id", 1);
		dbObj.put("orig_id", domNode.selectSingleNode("id").getStringValue());
		dbObj.put("created", new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH).parse(createDateString));
		dbObj.put("src", "twitter");
		dbObj.put("processed", false);
		
		userDbObj = new BasicDBObject();
		Node userNode = domNode.selectSingleNode("user");
		userDbObj.put("user_orig_id", userNode.selectSingleNode("id").getStringValue());
		userDbObj.put("user_name", userNode.selectSingleNode("screen_name").getStringValue());
		userDbObj.put("user_image_url", userNode.selectSingleNode("profile_image_url").getStringValue());
		userDbObj.put("followers", Integer.parseInt(userNode.selectSingleNode("followers_count").getStringValue()));
		dbObj.put("user", userDbObj);
		
		dbObj.put("msg", domNode.selectSingleNode("text").getStringValue());
		feedsList.add(dbObj);
	    }
	    
	    feedItemsCollection.insert(feedsList);
	    logger.info("done saving to mongodb");
	    
	} catch (Exception twitterException) {
	    twitterException.printStackTrace();
	    logger.error("Error in retrieveTweets " + twitterException);
	}
    }

    public void processTweets() {
	try {
	    Mongo mongo = new Mongo( "localhost" , 27017 );
	    DB camusDB = mongo.getDB("camus");
	    DBCollection feedItemsCollection = camusDB.getCollection("feedItems");
	    
	    BasicDBObject query = new BasicDBObject();
	    query.put("user_id", 1);
	    query.put("processed", false);
	    DBCursor cur = feedItemsCollection.find(query);
	    int tweetCounts = cur.size();
	    if (tweetCounts > 0){
		ExecutorService executor = Executors.newFixedThreadPool(tweetCounts);
		while (cur.hasNext()) {
		    Runnable worker = new TweetProcessingTask((BasicDBObject)cur.next());
		    executor.execute(worker);
		}
		executor.shutdown();
	    }

	} catch (Exception twitterException) {
	    logger.error("Error in processTweets " + twitterException);
	}

    }
}

class TweetProcessingTask implements Runnable {
    
    private static final Logger logger = Logger.getLogger(TweetProcessingTask.class);
    private final BasicDBObject tweet;
    
    public TweetProcessingTask(BasicDBObject tweet){
	this.tweet = tweet;
    }
    
    public void run() {
	HttpClient httpclient = new DefaultHttpClient();
	try {
	    Mongo mongo = new Mongo( "localhost" , 27017 );
	    DB camusDB = mongo.getDB("camus");
	    
	    logger.info("TweetProcessingTask run");
	    String message = (String)tweet.get("msg");
	    String tweetID = (String)tweet.get("orig_id");
	    String urlInFeed = FeedTaskUtil.extractURLInFeed(message);
	    if (urlInFeed != null){
		logger.info("urlInFeed: " + urlInFeed);

		HttpGet httpget = new HttpGet(urlInFeed);
		HttpResponse response = httpclient.execute(httpget);
		String[] acceptedHeaders = {"text/html", "text/html;charset=utf-8", "application/xhtml+xml;charset=utf-8", "text/html;charset=ISO-8859-1"};

		if (response.containsHeader("Content-type")) {
		    Header[] headers = response.getHeaders("Content-type");
		    Header contentTypeHeader = headers[0];
		    String contentTypeHeaderString = contentTypeHeader.getValue();
		    contentTypeHeaderString = contentTypeHeaderString.toLowerCase();
		    contentTypeHeaderString = contentTypeHeaderString.replaceAll(" ", "");
		    contentTypeHeaderString = contentTypeHeaderString.trim();

		    if (!ArrayUtils.contains(acceptedHeaders, contentTypeHeaderString)) {
			logger.info("rejecting content header : " + contentTypeHeader.getValue());
		    } else {
        		    HttpEntity entity = response.getEntity();
        		    String responseBody = EntityUtils.toString(entity);
        		    
        		    LinkedSiteMeta linkedSiteMeta = FeedTaskUtil.parseLinkedSiteHeader(responseBody);
        		    String readabilityText = new ContentExtractor().extractReadabilityContents(responseBody);
        		    
        		    DBCollection linkedSiteCollection = camusDB.getCollection("linkedSite");
        		    BasicDBObject dbObj =  new BasicDBObject();
        		    dbObj.put("url", urlInFeed);
        		    if (linkedSiteMeta.getCanonicalURL() == null){
        			linkedSiteMeta.setCanonicalURL(urlInFeed);
        		    }
        		    dbObj.put("canonical", linkedSiteMeta.getCanonicalURL());
        		    dbObj.put("desc", linkedSiteMeta.getDescription());
        		    dbObj.put("title", linkedSiteMeta.getTitleText());
        		    dbObj.put("name", linkedSiteMeta.getSiteName());
        		    dbObj.put("type", linkedSiteMeta.getSiteType());
        		    dbObj.put("image_url", linkedSiteMeta.getMetaImageURL());
//        		    dbObj.put("raw_contents", responseBody);
        		    
        		    List<String> statusList = new ArrayList<String>();
        		    statusList.add(TwitterTimelineFeedTask.LINKED_SITE_CRAWLED);
        		    
        		    IMOperation op = new IMOperation();
        		    op.addImage(linkedSiteMeta.getMetaImageURL());
        		    IdentifyCmd identify = new IdentifyCmd();
        		    ArrayListOutputConsumer output = new ArrayListOutputConsumer();
        		    identify.setOutputConsumer(output);
        		    identify.run(op);
        		    double imageBoost = 0;
        		    ArrayList<String> cmdOutput = output.getOutput();
        		    if (cmdOutput.size() > 0){
        			String[] identifyResults = cmdOutput.get(0).split(" ");
        			String[] dimensions = identifyResults[2].split("x");
        			int width = Integer.parseInt(dimensions[0]);
        			int height = Integer.parseInt(dimensions[1]);
        			boolean isLandscape = false;
        			if (width > height){
        			    // is landscape
        			    isLandscape = true;
        			}else{
        			    isLandscape = false;
        			}
        			dbObj.put("landscape", isLandscape);
        			
        			// use some arbitrary size to determine image is "good", need to take pixel into accounts 
        			if ((width>600) || (height > 500)){
        			    imageBoost = 1;
                		    dbObj.put("image_quality", 1);
        			}
        			
        		    }
        		    
        		    
        		    
        		    double readabilityMean = 0.0;
        		    if (readabilityText != null){
        			BasicDBObject readabilityScoresObj =  new BasicDBObject();
        			readabilityScoresObj.put("text", readabilityText);
        			
        			BagOfReadabilityObjects readabilityStats = new Readability(readabilityText).getMetrics();
        			/*
        			readabilityScoresObj.put("words", readabilityStats.getWords());
        			readabilityScoresObj.put("flesch", readabilityStats.getFleschReading());
        			readabilityScoresObj.put("flesch_kincaid", readabilityStats.getFleschKincaid());
        			readabilityScoresObj.put("ari", readabilityStats.getARI());
        			readabilityScoresObj.put("coleman_liau", readabilityStats.getColemanLiau());
        			readabilityScoresObj.put("gunning_fog", readabilityStats.getGunningFog());
        			readabilityScoresObj.put("smog_index", readabilityStats.getSMOGIndex());
        			*/
        			
        			double normalizedFlesch = (100 - Math.abs(readabilityStats.getFleschReading() - 50))/100;
        			double normalizedFleschKincaid = (16 - Math.abs(readabilityStats.getFleschReading() - 13))/16;
        			double normalizedARI = (16 - Math.abs(readabilityStats.getARI() - 13))/16;
        			double normalizedLiau = (16 - Math.abs(readabilityStats.getColemanLiau() - 13))/16;
        			double normalizedGunning = (16 - Math.abs(readabilityStats.getGunningFog() - 12))/16;
        			double normalizedSMOG = (16 - Math.abs(readabilityStats.getSMOGIndex() - 12))/16;
        			
        			readabilityMean = (normalizedFlesch + normalizedFleschKincaid + normalizedARI+ normalizedLiau + normalizedGunning + normalizedSMOG)/6;
        			readabilityScoresObj.put("mean", Double.valueOf(new DecimalFormat("#.####").format(readabilityMean)));
        			dbObj.put("readability", readabilityScoresObj);
        			
        			statusList.add(TwitterTimelineFeedTask.LINKED_SITE_READABILITY_ANALYSED);
        		    }
        		    
        		    // 3000 is very high - http://twitturly.com/ http://t.co/NTPVHVY
        		    double tweetCount = FeedTaskUtil.fetchTweetCount(linkedSiteMeta.getCanonicalURL());
        		    double normalizedTweet = tweetCount/2000;
        		    
        		    // 600 is very high. 400 is high, ?
        		    double facebookSharesCount = FeedTaskUtil.fetchFacebookShares(linkedSiteMeta.getCanonicalURL());
        		    double normalizedShares = facebookSharesCount/400;
        		    double meanSocial = (normalizedTweet+normalizedShares)/2;
        		    
        		    dbObj.put("social", meanSocial);
        		    
        		    double boostScore = meanSocial*0.4 + readabilityMean*0.3 + imageBoost*0.3;
        		    
        		    dbObj.put("boost", boostScore);        			
        		    
        		    
        		    dbObj.put("status", statusList);
        		    linkedSiteCollection.insert(dbObj);
		    }
		}
	    }
	    
	    // update tweet id, set processed to true
	    DBCollection feedItemsCollection = camusDB.getCollection("feedItems");
	    BasicDBObject setObj =  new BasicDBObject();
	    setObj.put("$set", new BasicDBObject("processed", true));
	    feedItemsCollection.update(this.tweet, setObj);
	    
	} catch (Exception e) {
	    logger.error("Error in run: " + e);
	} finally {
	    httpclient.getConnectionManager().shutdown();
	}

    }
}
