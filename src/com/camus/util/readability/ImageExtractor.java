package com.camus.util.readability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import com.camus.common.LinkedStory;
import com.camus.common.StoryImage;
import com.camus.feeders.TwitterTimelineFeedTask;

public class ImageExtractor {

    private static final Logger logger = Logger
	    .getLogger(ImageExtractor.class);

    public String downloadImageToTempFile(String storyURL, String imageURL) {
	HttpClient httpclient = new DefaultHttpClient();
	String tempImageFilePath = null;
	try {
	    
	    logger.info("ImageExtractor imageURL: " + imageURL);
	    if (imageURL.indexOf("http://") > -1){
		imageURL = "http://"+imageURL;
	    }
	    
	    URLConnection connection = new URL(imageURL).openConnection();
	    String contentType = connection.getHeaderField("Content-Type");
		logger.info("Content-Type in downloadImageToTempFile: " + contentType);
	    
	    boolean isImage = contentType.startsWith("image/");
	    isImage = true;
	    
	    if (isImage){
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet(imageURL);
		HttpResponse response = httpclient.execute(httpget, localContext);
		String respStatus = response.getStatusLine().toString();
		if (!respStatus.contains("200")) {
		    // throw Exception
		    // return null;
		}
		
		/*

		if (response.containsHeader("Content-Type")) {
		    Header[] headers = response.getHeaders("Content-Type");
		    Header contentTypeHeader = headers[0];
		    contentType = contentTypeHeader.getValue();
		}
		if ((contentType != null) && (contentType.indexOf("/") > -1)) {
		    int slashPosition = contentType.indexOf("/") + 1;
		    contentType = contentType.substring(slashPosition,
			    contentType.length());
		} else {
		*/
		    // find from file name
		    int dotPosition = imageURL.lastIndexOf(".") + 1;
		    contentType = imageURL
		    .substring(dotPosition, imageURL.length());
//		}

		String fileName = ContentExtractor.md5(storyURL) + "." + contentType;
		logger.info("fileName contentType: " + contentType);

		ResourceBundle rb = ResourceBundle.getBundle("camus");
		tempImageFilePath = rb
		.getString("twitter.crawler.download-images-dir") + fileName;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		    InputStream instream = entity.getContent();
		    OutputStream outstream = new FileOutputStream(tempImageFilePath);
		    logger.info("saving story image file to " + tempImageFilePath);
		    try {
			IOUtils.copy(instream, outstream);
		    } catch (Exception e) {
			throw e;
		    } finally {
			instream.close();
			outstream.close();
		    }

		}
		logger.info("downloadImageToTempFile ended");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    logger.error("Error in downloadImageToTempFile: " + e);
	} finally {
	    httpclient.getConnectionManager().shutdown();
	}
	return tempImageFilePath;
    }

    public StoryImage crawlLinkedStoryImage(String imageURL) {
	StoryImage storyImage = null;
	try {
	    // grab image

	    // save to local, resized

	    // need width, height, resolution

	} catch (Exception e) {
	    logger.error("Error in crawlLinkedStoryImage: " + e);
	}
	return storyImage;
    }

}
