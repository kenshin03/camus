package com.camus.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import com.camus.common.FeedStory;
import com.camus.common.LinkedStory;
import com.camus.feeders.TwitterTimelineFeedTask;

public class StoryLayoutAnalyzer {
    
    private static final Logger logger = Logger
    .getLogger(StoryLayoutAnalyzer.class);
    
    
    public Section analyzeStories(List<FeedStory> feedStoryList){
    
	// if image width > 500 or words count > 100, set as feature story
	// to do: doc clustering, image resolution check, social signal ranking
	
	List<ShortMessage> shortMessagesList = new ArrayList<ShortMessage>();
	List<Feature> featuredStoriesList = new ArrayList<Feature>();
	List<Filler> fillerStoriesList = new ArrayList<Filler>();
	
	Iterator<FeedStory> feedStoryListIterator = feedStoryList.iterator();
	FeedStory feedStory = null;
	LinkedStory linkedStory = null;
	
	Section section = new Section();
	
	while (feedStoryListIterator.hasNext()){
	    feedStory = feedStoryListIterator.next();
	    
	    if (feedStory.getLinkedStory() == null){
		// no links in tweet, lower priority
		ShortMessage shortMessage = new ShortMessage();
		shortMessage.setDate(feedStory.getStoryPublishDate());
		shortMessage.setMessage(feedStory.getStoryContent());
		shortMessage.setSource(feedStory.getSource());
		shortMessage.setAuthorName(feedStory.getUserName());
		shortMessage.setAuthorImageURL(feedStory.getUserThumbnail());
		shortMessagesList.add(shortMessage);
		
	    }else{
		linkedStory = feedStory.getLinkedStory();
		if ((linkedStory.getStoryImageWidth() > 500) || (linkedStory.getStoryWordsCount() >= 100)){
		    
		    Feature featureStory = new Feature();
		    featureStory.setDate(feedStory.getStoryPublishDate());
		    featureStory.setTitle(linkedStory.getStoryTitle());
		    featureStory.setStory(linkedStory.getStoryReadabilityContent());
		    featureStory.setSource(feedStory.getSource());
		    featureStory.setAuthorName(feedStory.getUserName());
		    featureStory.setAuthorImageURL(feedStory.getUserThumbnail());
		    logger.info("StoryLayoutAnalyzer feature linkedStory.getStoryImageURL(): " + linkedStory.getStoryImageURL());
		    
    		    if (linkedStory.getStoryImageURL() != null){
    			featureStory.setImageURL(linkedStory.getStoryImageURL());
    			if (linkedStory.getStoryImageWidth() > linkedStory.getStoryImageHeight()){
    			    featureStory.setImageOnTop(true);
    			}
    		    }
		    featuredStoriesList.add(featureStory);
		}else{
		    
		    Filler filler = new Filler();
		    filler.setDate(feedStory.getStoryPublishDate());
		    filler.setTitle(linkedStory.getStoryTitle());
		    if (linkedStory.getStoryReadabilityContent() != null){
			filler.setStory(linkedStory.getStoryReadabilityContent());
		    }else{
			filler.setStory(feedStory.getStoryContent());
		    }
		    
		    filler.setSource(feedStory.getSource());
		    filler.setAuthorName(feedStory.getUserName());
		    filler.setAuthorImageURL(feedStory.getUserThumbnail());
		    logger.info("StoryLayoutAnalyzer filler linkedStory.getStoryImageURL(): " + linkedStory.getStoryImageURL());
		    
    		    if (linkedStory.getStoryImageURL() != null){
    			filler.setImageURL(linkedStory.getStoryImageURL());
    			if (linkedStory.getStoryImageWidth() > linkedStory.getStoryImageHeight()){
    			    filler.setImageOnTop(true);
    			}
    		    }
		    fillerStoriesList.add(filler);
		}
	    }
	}
	section.setShortMessages(shortMessagesList);
	section.setFeaturedStories(featuredStoriesList);
	section.setFillerStories(fillerStoriesList);
	
	// assemble templates
	String[] twitterPagesLayout = LayoutTemplates.fetchRandomTweetsLayoutFeature();
	String layoutString = "";
	for (int i=0;i<twitterPagesLayout.length;i++){
	    layoutString = layoutString + twitterPagesLayout[i];
	    if (i < twitterPagesLayout.length-1){
		layoutString = layoutString+",";
	    }
	}
	section.setLayoutTemplate(layoutString);
	
	return section;
    }
    

}
