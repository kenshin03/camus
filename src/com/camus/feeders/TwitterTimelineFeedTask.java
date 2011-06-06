package com.camus.feeders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.model.Token;
import org.apache.log4j.Logger;
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
    
    public TwitterTimelineFeedTask(){
	 ResourceBundle rb = ResourceBundle.getBundle("camus"); 
	 this.accessTokenString = rb.getString("twitter.user.token");
	 this.accessTokenSecretString = rb.getString("twitter.user.secret");
	 this.apiKey = rb.getString("twitter.api.key");
	 this.apiSecret = rb.getString("twitter.api.secret");
    }
    
    protected List<FeedStory> retrieveTweets() {
	try {
	    OAuthService service = new ServiceBuilder()
		    .provider(TwitterApi.class).apiKey(this.apiKey)
		    .apiSecret(this.apiSecret)
		    .build();

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

}
