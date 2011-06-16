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

	public Section analyzeStories(List<FeedStory> feedStoryList) {

		// if image width > 500 or words count > 100, set as feature story
		// to do: doc clustering, image resolution check, social signal ranking

		List<ShortMessage> shortMessagesList = new ArrayList<ShortMessage>();
		List<Feature> featuredStoriesList = new ArrayList<Feature>();
		List<Filler> fillerStoriesList = new ArrayList<Filler>();

		Iterator<FeedStory> feedStoryListIterator = feedStoryList.iterator();
		FeedStory feedStory = null;
		LinkedStory linkedStory = null;

		Section section = new Section();

		while (feedStoryListIterator.hasNext()) {
			feedStory = feedStoryListIterator.next();

			if (feedStory.getLinkedStory() == null) {
				// no links in tweet, lower priority
				ShortMessage shortMessage = new ShortMessage();
				shortMessage.setDate(feedStory.getStoryPublishDate());
				shortMessage.setMessage(feedStory.getStoryContent());
				shortMessage.setSource(feedStory.getSource());
				shortMessage.setAuthorName(feedStory.getUserName());
				shortMessage.setAuthorImageURL(feedStory.getUserThumbnail());
				shortMessagesList.add(shortMessage);

			} else {
				linkedStory = feedStory.getLinkedStory();
				if ((linkedStory.getStoryImageWidth() > 500)
						|| (linkedStory.getStoryWordsCount() >= 100)) {

					Feature featureStory = new Feature();
					featureStory.setDate(feedStory.getStoryPublishDate());
					featureStory.setTitle(linkedStory.getStoryTitle());
					featureStory.setStory(linkedStory
							.getStoryReadabilityContent());
					featureStory.setSource(feedStory.getSource());
					featureStory.setAuthorName(feedStory.getUserName());
					featureStory
							.setAuthorImageURL(feedStory.getUserThumbnail());

					if (linkedStory.getStoryImageURL() != null) {
						featureStory
								.setImageURL(linkedStory.getStoryImageURL());
						if (linkedStory.getStoryImageWidth() > linkedStory
								.getStoryImageHeight()) {
							featureStory.setLandscape(true);
						}
					}
					featuredStoriesList.add(featureStory);
				} else {

					Filler filler = new Filler();
					filler.setDate(feedStory.getStoryPublishDate());
					filler.setTitle(linkedStory.getStoryTitle());
					if (linkedStory.getStoryReadabilityContent() != null) {
						filler.setStory(linkedStory
								.getStoryReadabilityContent());
					} else {
						filler.setStory(feedStory.getStoryContent());
					}

					filler.setSource(feedStory.getSource());
					filler.setAuthorName(feedStory.getUserName());
					filler.setAuthorImageURL(feedStory.getUserThumbnail());
					logger
							.info("StoryLayoutAnalyzer filler linkedStory.getStoryImageURL(): "
									+ linkedStory.getStoryImageURL());

					if (linkedStory.getStoryImageURL() != null) {
						filler.setImageURL(linkedStory.getStoryImageURL());
						if (linkedStory.getStoryImageWidth() > linkedStory
								.getStoryImageHeight()) {
							filler.setLandscape(true);
						}
					}
					fillerStoriesList.add(filler);
				}
			}
		}
		section.setShortMessages(shortMessagesList);
		section.setFeaturedStories(featuredStoriesList);
		section.setFillerStories(fillerStoriesList);
		
		/*

		// assemble templates
		String[] twitterPagesLayout = LayoutTemplates
				.fetchRandomTweetsLayoutFeature();
		String layoutString = "";
		for (int i = 0; i < twitterPagesLayout.length; i++) {
			layoutString = layoutString + twitterPagesLayout[i];
			if (i < twitterPagesLayout.length - 1) {
				layoutString = layoutString + ",";
			}
		}
		section.setLayoutTemplate(layoutString);
		*/

		return section;
	}

}
