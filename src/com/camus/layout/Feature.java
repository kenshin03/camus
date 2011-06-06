package com.camus.layout;

public class Feature {
    private String story;
    private String date;
    private String title;
    private String imageURL;
    private boolean imageOnTop;
    private boolean imageOnLeft;
    private String source;
    private String authorName;
    private String authorImageURL;
    
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public String getAuthorImageURL() {
        return authorImageURL;
    }
    public void setAuthorImageURL(String authorImageURL) {
        this.authorImageURL = authorImageURL;
    }
    
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getStory() {
        return story;
    }
    public void setStory(String story) {
        this.story = story;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public boolean isImageOnTop() {
        return imageOnTop;
    }
    public void setImageOnTop(boolean imageOnTop) {
        this.imageOnTop = imageOnTop;
    }
    public boolean isImageOnLeft() {
        return imageOnLeft;
    }
    public void setImageOnLeft(boolean imageOnLeft) {
        this.imageOnLeft = imageOnLeft;
    }

}
