package com.camus.layout;

import java.util.List;

public class Section {
    private String layoutTemplate;
    private List<Feature> featuredStories;
    private List<Filler> fillerStories;
    private List<ShortMessage> shortMessages;
    public String getLayoutTemplate() {
        return layoutTemplate;
    }
    public void setLayoutTemplate(String layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
    }
    public List getFeaturedStories() {
        return featuredStories;
    }
    public void setFeaturedStories(List featuredStories) {
        this.featuredStories = featuredStories;
    }
    public List getFillerStories() {
        return fillerStories;
    }
    public void setFillerStories(List fillerStories) {
        this.fillerStories = fillerStories;
    }
    public List getShortMessages() {
        return shortMessages;
    }
    public void setShortMessages(List shortMessages) {
        this.shortMessages = shortMessages;
    }
    
}
