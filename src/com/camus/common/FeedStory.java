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

package com.camus.common;

public class FeedStory {
	
	private String storyID;
	private String userName;
	private String userID;
	private String userThumbnail;
	private String storyPublishDate;
	private String storyContent;
	private String source;
	public String getSource() {
	    return source;
	}
	public void setSource(String source) {
	    this.source = source;
	}

	private LinkedStory linkedStory;
	
	public LinkedStory getLinkedStory() {
		return linkedStory;
	}
	public void setLinkedStory(LinkedStory linkedStory) {
		this.linkedStory = linkedStory;
	}
	public String getStoryID() {
		return storyID;
	}
	public void setStoryID(String storyID) {
		this.storyID = storyID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserThumbnail() {
		return userThumbnail;
	}
	public void setUserThumbnail(String userThumbnail) {
		this.userThumbnail = userThumbnail;
	}
	public String getStoryPublishDate() {
		return storyPublishDate;
	}
	public void setStoryPublishDate(String storyPublishDate) {
		this.storyPublishDate = storyPublishDate;
	}
	public String getStoryContent() {
		return storyContent;
	}
	public void setStoryContent(String storyContent) {
		this.storyContent = storyContent;
	}
	
	public String toString(){
	    return this.storyContent.substring(0, 10);
	}

}
