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
import com.camus.feeders.FacebookNewsFeedTask;
import com.camus.feeders.TwitterTimelineFeedTask;
import com.camus.layout.Section;
import com.camus.layout.StoryLayoutAnalyzer;
import com.google.gson.Gson;

public class TestClient {

	private static final Logger logger = Logger.getLogger(TestClient.class);

	public static void main(String args[]) {
		logger.info("TestClient start");
		try {
			long startTime = System.currentTimeMillis();
			TwitterTimelineFeedTask twitterFeedTask = new TwitterTimelineFeedTask(); 
			List<FeedStory> tweetsList =
			 twitterFeedTask.retrieveTwitterArticles();
			 StoryLayoutAnalyzer layoutAnalyzer = new StoryLayoutAnalyzer();
			 Section section = layoutAnalyzer.analyzeStories(tweetsList);
			
			/*

			FacebookNewsFeedTask facebookTask = new FacebookNewsFeedTask();
			List<FeedStory> fbNewsFeedsList = facebookTask.retrieveFacebookNewsFeed();
			StoryLayoutAnalyzer layoutAnalyzer = new StoryLayoutAnalyzer();
			Section section = layoutAnalyzer.analyzeStories(fbNewsFeedsList);
			*/

			Gson gson = new Gson();
			String jsonToReturn = gson.toJson(section);

			logger.info(jsonToReturn);

			long endTime = System.currentTimeMillis();
			double elapsedTime = endTime - startTime;
			logger.info("Secs taken: " + elapsedTime / 1000);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in testClient: " + e);
		}

	}
}
