package com.camus.layout;

public class LayoutTemplates {
    
    public static final String COVER_TEMPLATE_A = "H1=(T1|T2|T3)";
    public static final String TWO_COLUMNS_TEMPLATE_A = "(H1=T1=T2)|(T3=T4=H2)";
    
    public static final String[] TWEETS_FEATURE_LAYOUT_A = {COVER_TEMPLATE_A, TWO_COLUMNS_TEMPLATE_A};
    
    public static String[] fetchRandomTweetsLayoutFeature(){
	return TWEETS_FEATURE_LAYOUT_A;
    }

}
