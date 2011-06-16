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

package com.camus.util.readability;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;
import org.apache.log4j.Logger;

public class ContentExtractor {

    private static final Logger logger = Logger
	    .getLogger(ContentExtractor.class);
    
    /**
     * Return a string of 32 lower case hex characters.
     * 
     * @param input
     * @return a string of 32 hex characters
     */
    public static String md5(String input)
    {
        String hexHash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] output = md.digest();
            hexHash = bytesToLowerCaseHex(output);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        return hexHash;
    }

    private static String bytesToLowerCaseHex(byte[] data)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
    

    /**
     * returns a list of nodes we want to search on like paragraphs and tables
     * 
     * @return
     */
    private ArrayList<Element> getNodesToCheck(Document doc) {
	ArrayList<Element> nodesToCheck = new ArrayList<Element>();

	Elements items = doc.getElementsByTag("article");
	for (Element item : items) {
	    nodesToCheck.add(item);
	}
	Elements items1 = doc.getElementsByTag("p");
	for (Element item : items1) {
	    nodesToCheck.add(item);
	}
	Elements items2 = doc.getElementsByTag("pre");
	for (Element item : items2) {
	    nodesToCheck.add(item);
	}
	Elements items3 = doc.getElementsByTag("td");
	for (Element item : items3) {
	    nodesToCheck.add(item);
	}
	return nodesToCheck;

    }

    public String extractReadabilityContents(String inputHTML) {
	String responseHTML = null;
	try {
	    Document jsoupDoc = Jsoup.parse(inputHTML);
	    Element articleNode = this
		    .calculateBestNodeBasedOnClustering(jsoupDoc);
	    if (articleNode != null) {
		articleNode = this.getFormattedElement(articleNode);

		StringBuilder sb = new StringBuilder();
		Elements nodes = articleNode.getAllElements();
		for (Element e : nodes) {
		    if (e.tagName().equals("p")) {
			// String text =
			// StringEscapeUtils.escapeHtml(e.text()).trim();
			String text = e.text().trim();
			sb.append(text);
			sb.append("\n\n");
		    }
		}
		responseHTML = sb.toString();
	    }
	    return responseHTML;

	} catch (Exception e) {
	    logger.error("Error in extractReadabilityContents: " + e);
	    return null;
	}
    }

    public Element getFormattedElement(Element topNode) {

	topNode = removeNodesWithNegativeScores(topNode);

	topNode = convertLinksToText(topNode);

	topNode = replaceTagsWithText(topNode);

	topNode = removeParagraphsWithFewWords(topNode);

	return topNode;

    }

    /**
     * cleans up and converts any nodes that should be considered text into text
     */
    private Element convertLinksToText(Element topNode) {
	logger.debug("Turning links to text");
	Elements links = topNode.getElementsByTag("a");
	for (Element item : links) {
	    if (item.getElementsByTag("img").size() == 0) {
		TextNode tn = new TextNode(item.text(), topNode.baseUri());
		item.replaceWith(tn);
	    }
	}
	return topNode;
    }

    /**
     * if there are elements inside our top node that have a negative gravity
     * score, let's give em the boot
     */
    private Element removeNodesWithNegativeScores(Element topNode) {
	Elements gravityItems = topNode.select("*[gravityScore]");
	for (Element item : gravityItems) {
	    int score = Integer.parseInt(item.attr("gravityScore"));
	    if (score < 1) {
		item.remove();
	    }
	}
	return topNode;
    }

    /**
     * replace common tags with just text so we don't have any crazy formatting
     * issues so replace <br>
     * , <i>, <strong>, etc.... with whatever text is inside them
     */
    private Element replaceTagsWithText(Element topNode) {

	Elements strongs = topNode.getElementsByTag("strong");
	for (Element item : strongs) {
	    TextNode tn = new TextNode(item.text(), topNode.baseUri());
	    item.replaceWith(tn);
	}

	Elements bolds = topNode.getElementsByTag("b");
	for (Element item : bolds) {
	    TextNode tn = new TextNode(item.text(), topNode.baseUri());
	    item.replaceWith(tn);
	}

	Elements italics = topNode.getElementsByTag("i");
	for (Element item : italics) {
	    TextNode tn = new TextNode(item.text(), topNode.baseUri());
	    item.replaceWith(tn);
	}
	return topNode;
    }

    /**
     * remove paragraphs that have less than x number of words, would indicate
     * that it's some sort of link
     */
    private Element removeParagraphsWithFewWords(Element topNode) {
	if (logger.isDebugEnabled()) {
	    logger.debug("removeParagraphsWithFewWords starting...");
	}

	Elements allNodes = topNode.getAllElements();
	for (Element el : allNodes) {

	    try {
		// get stop words that appear in each node

		WordStats stopWords = CamusTextUtil.getStopWordCount(el.text());

		if (stopWords.getStopWordCount() < 5
			&& el.getElementsByTag("object").size() == 0
			&& el.getElementsByTag("embed").size() == 0) {
		    el.remove();
		}
	    } catch (IllegalArgumentException e) {
		logger.error(e.getMessage());
	    }
	    // }
	}
	return topNode;
    }

    /**
     * we're going to start looking for where the clusters of paragraphs are.
     * We'll score a cluster based on the number of stopwords and the number of
     * consecutive paragraphs together, which should form the cluster of text
     * that this node is around also store on how high up the paragraphs are,
     * comments are usually at the bottom and should get a lower score
     * 
     * @return
     */
    private Element calculateBestNodeBasedOnClustering(Document doc) {
	Element topNode = null;

	// grab all the paragraph elements on the page to start to inspect the
	// likely hood of them being good peeps
	ArrayList<Element> nodesToCheck = getNodesToCheck(doc);

	double startingBoost = 1.0;
	int cnt = 0;
	int i = 0;

	// holds all the parents of the nodes we're checking
	Set<Element> parentNodes = new HashSet<Element>();

	ArrayList<Element> nodesWithText = new ArrayList<Element>();

	for (Element node : nodesToCheck) {

	    String nodeText = node.text();
	    WordStats wordStats = CamusTextUtil.getStopWordCount(nodeText);
	    boolean highLinkDensity = isHighLinkDensity(node);

	    if (wordStats.getStopWordCount() > 2 && !highLinkDensity) {

		nodesWithText.add(node);
	    }

	}

	int numberOfNodes = nodesWithText.size();
	int negativeScoring = 0; // we shouldn't give more negatives than
	// positives
	// we want to give the last 20% of nodes negative scores in case they're
	// comments
	double bottomNodesForNegativeScore = (float) numberOfNodes * 0.25;

	logger.debug("About to inspect num of nodes with text: "
		+ numberOfNodes);

	for (Element node : nodesWithText) {

	    // add parents and grandparents to scoring
	    // only add boost to the middle paragraphs, top and bottom is
	    // usually jankz city
	    // so basically what we're doing is giving boost scores to
	    // paragraphs that appear higher up in the dom
	    // and giving lower, even negative scores to those who appear lower
	    // which could be commenty stuff

	    float boostScore = 0;

	    if (isOkToBoost(node, i)) {
		if (cnt >= 0) {
		    boostScore = (float) ((1.0 / startingBoost) * 50);
		    startingBoost++;
		}
	    }

	    // check for negative node values
	    if (numberOfNodes > 15) {
		if ((numberOfNodes - i) <= bottomNodesForNegativeScore) {
		    float booster = (float) bottomNodesForNegativeScore
			    - (float) (numberOfNodes - i);
		    boostScore = -(float) Math.pow(booster, (float) 2);

		    // we don't want to score too highly on the negative side.
		    float negscore = Math.abs(boostScore) + negativeScoring;
		    if (negscore > 40) {
			boostScore = 5;
		    }
		}
	    }

	    logger.debug("Location Boost Score: " + boostScore
		    + " on interation: " + i + "' id='" + node.parent().id()
		    + "' class='" + node.parent().attr("class"));
	    String nodeText = node.text();
	    WordStats wordStats = CamusTextUtil.getStopWordCount(nodeText);
	    int upscore = (int) (wordStats.getStopWordCount() + boostScore);
	    updateScore(node.parent(), upscore);
	    updateScore(node.parent().parent(), upscore / 2);
	    updateNodeCount(node.parent(), 1);
	    updateNodeCount(node.parent().parent(), 1);

	    if (!parentNodes.contains(node.parent())) {
		parentNodes.add(node.parent());
	    }

	    if (!parentNodes.contains(node.parent().parent())) {
		parentNodes.add(node.parent().parent());
	    }

	    cnt++;
	    i++;
	}

	// now let's find the parent node who scored the highest

	int topNodeScore = 0;
	for (Element e : parentNodes) {

	    logger.debug("ParentNode: score='" + e.attr("gravityScore")
		    + "' nodeCount='" + e.attr("gravityNodes") + "' id='"
		    + e.id() + "' class='" + e.attr("class") + "' ");
	    // int score = Integer.parseInt(e.attr("gravityScore")) *
	    // Integer.parseInt(e.attr("gravityNodes"));
	    int score = getScore(e);
	    if (score > topNodeScore) {
		topNode = e;
		topNodeScore = score;
	    }

	    if (topNode == null) {
		topNode = e;
	    }
	}
	if (topNode == null) {
	    logger.info("ARTICLE NOT ABLE TO BE EXTRACTED!, WE HAZ FAILED YOU LORD VADAR!!");
	} else {
	    String logText = "";
	    String targetText = "";
	    Element topPara = topNode.getElementsByTag("p").first();
	    if (topPara == null) {
		topNode.text();
	    } else {
		topPara.text();
	    }

	    if (targetText.length() >= 51) {
		logText = targetText.substring(0, 50);
	    } else {
		logText = targetText;
	    }
	    logger.debug("TOPNODE TEXT: " + logText.trim());
	    logger.debug("Our TOPNODE: score='" + topNode.attr("gravityScore")
		    + "' nodeCount='" + topNode.attr("gravityNodes") + "' id='"
		    + topNode.id() + "' class='" + topNode.attr("class") + "' ");
	}

	return topNode;

    }

    /**
     * adds a score to the gravityScore Attribute we put on divs we'll get the
     * current score then add the score we're passing in to the current
     * 
     * @param node
     * @param addToScore
     *            - the score to add to the node
     */
    private void updateScore(Element node, int addToScore) {
	int currentScore;
	try {
	    currentScore = Integer.parseInt(node.attr("gravityScore"));
	} catch (NumberFormatException e) {
	    currentScore = 0;
	}
	int newScore = currentScore + addToScore;
	node.attr("gravityScore", Integer.toString(newScore));

    }

    /**
     * stores how many decent nodes are under a parent node
     * 
     * @param node
     * @param addToCount
     */
    private void updateNodeCount(Element node, int addToCount) {
	int currentScore;
	try {
	    currentScore = Integer.parseInt(node.attr("gravityNodes"));
	} catch (NumberFormatException e) {
	    currentScore = 0;
	}
	int newScore = currentScore + addToCount;
	node.attr("gravityNodes", Integer.toString(newScore));

    }

    /**
     * returns the gravityScore as an integer from this node
     * 
     * @param node
     * @return
     */
    private int getScore(Element node) {
	try {
	    return Integer.parseInt(node.attr("gravityScore"));
	} catch (NumberFormatException e) {
	    return 0;
	} catch (NullPointerException e) {
	    return 0;
	}
    }

    /**
     * alot of times the first paragraph might be the caption under an image so
     * we'll want to make sure if we're going to boost a parent node that it
     * should be connected to other paragraphs, at least for the first n
     * paragraphs so we'll want to make sure that the next sibling is a
     * paragraph and has at least some substatial weight to it
     * 
     * @param node
     * @param i
     * @return
     */
    private boolean isOkToBoost(Element node, int i) {

	int stepsAway = 0;

	Element sibling = node.nextElementSibling();
	while (sibling != null) {

	    if (sibling.tagName().equals("p")) {
		if (stepsAway >= 3) {
		    logger.debug("Next paragraph is too far away, not boosting");
		    return false;
		}

		String paraText = sibling.text();
		String html = sibling.outerHtml();
		WordStats wordStats = CamusTextUtil.getStopWordCount(paraText);
		if (wordStats.getStopWordCount() > 5) {
		    logger.debug("We're gonna boost this node, seems contenty");
		    return true;
		}

	    }

	    // increase how far away the next paragraph is from this node
	    stepsAway++;

	    sibling = sibling.nextElementSibling();
	}

	return false;
    }

    /**
     * checks the density of links within a node, is there not much text and
     * most of it contains linky shit? if so it's no good
     * 
     * @param e
     * @return
     */
    private static boolean isHighLinkDensity(Element e) {

	Elements links = e.getElementsByTag("a");
	String txt = e.text().trim();

	if (links.size() == 0) {
	    return false;
	}

	float score = 0;
	String text = e.text();
	String[] words = text.split(" ");
	float numberOfWords = words.length;

	// let's loop through all the links and calculate the number of words
	// that make up the links
	StringBuilder sb = new StringBuilder();
	for (Element link : links) {
	    sb.append(link.text());
	}
	String linkText = sb.toString();
	String[] linkWords = linkText.split(" ");
	float numberOfLinkWords = linkWords.length;

	float numberOfLinks = links.size();

	float linkDivisor = (float) (numberOfLinkWords / numberOfWords);
	score = (float) linkDivisor * numberOfLinks;

	String logText = "";
	if (e.text().length() >= 51) {
	    logText = e.text().substring(0, 50);
	} else {
	    logText = e.text();
	}

	logger.debug("Calulated link density score as: " + score
		+ " for node: " + logText);
	if (score > 1) {
	    return true;
	}

	return false;
    }

    /**
     * remove any divs that looks like non-content, clusters of links, or paras
     * with no gusto
     * 
     * @param node
     * @return
     */
    private Element cleanupNode(Element node) {
	if (logger.isDebugEnabled()) {
	    logger.debug("Starting cleanup Node");
	}

	node = addSiblings(node);

	Elements nodes = node.children();
	for (Element e : nodes) {
	    if (e.tagName().equals("p")) {
		continue;
	    }
	    if (logger.isDebugEnabled()) {
		logger.debug("CLEANUP  NODE: " + e.id() + " class: "
			+ e.attr("class"));
	    }
	    boolean highLinkDensity = isHighLinkDensity(e);
	    if (highLinkDensity) {
		if (logger.isDebugEnabled()) {
		    logger.debug("REMOVING  NODE FOR LINK DENSITY: " + e.id()
			    + " class: " + e.attr("class"));
		}
		e.remove();
		continue;
	    }
	    // now check for word density
	    // grab all the paragraphs in the children and remove ones that are
	    // too small to matter
	    Elements subParagraphs = e.getElementsByTag("p");

	    for (Element p : subParagraphs) {
		if (p.text().length() < 25) {
		    p.remove();
		}
	    }

	    // now that we've removed shorty paragraphs let's make sure to
	    // exclude any first paragraphs that don't have paras as
	    // their next siblings to avoid getting img bylines
	    // first let's remove any element that now doesn't have any p tags
	    // at all
	    Elements subParagraphs2 = e.getElementsByTag("p");
	    if (subParagraphs2.size() == 0 && !e.tagName().equals("td")) {
		if (logger.isDebugEnabled()) {
		    logger.debug("Removing node because it doesn't have any paragraphs");
		}
		e.remove();
		continue;
	    }

	    // if this node has a decent enough gravityScore we should keep it
	    // as well, might be content
	    int topNodeScore = getScore(node);
	    int currentNodeScore = getScore(e);
	    float thresholdScore = (float) (topNodeScore * .08);
	    if (logger.isDebugEnabled()) {
		logger.debug("topNodeScore: " + topNodeScore
			+ " currentNodeScore: " + currentNodeScore
			+ " threshold: " + thresholdScore);
	    }
	    if (currentNodeScore < thresholdScore) {
		if (!e.tagName().equals("td")) {
		    if (logger.isDebugEnabled()) {
			logger.debug("Removing node due to low threshold score");
		    }
		    e.remove();
		} else {
		    if (logger.isDebugEnabled()) {
			logger.debug("Not removing TD node");
		    }
		}

		continue;
	    }

	}

	return node;

    }

    /**
     * adds any siblings that may have a decent score to this node
     * 
     * @param node
     * @return
     */
    private Element addSiblings(Element node) {
	if (logger.isDebugEnabled()) {
	    logger.debug("Starting to add siblings");
	}
	int baselineScoreForSiblingParagraphs = getBaselineScoreForSiblings(node);

	Element currentSibling = node.previousElementSibling();
	while (currentSibling != null) {

	    if (currentSibling.tagName().equals("p")) {

		node.child(0).before(currentSibling.outerHtml());
		currentSibling = currentSibling.previousElementSibling();
		continue;
	    }

	    // check for a paraph embedded in a containing element
	    int insertedSiblings = 0;
	    Elements potentialParagraphs = currentSibling.getElementsByTag("p");
	    if (potentialParagraphs.first() == null) {
		currentSibling = currentSibling.previousElementSibling();
		continue;
	    }
	    for (Element firstParagraph : potentialParagraphs) {
		WordStats wordStats = CamusTextUtil.getStopWordCount(firstParagraph
			.text());

		int paragraphScore = wordStats.getStopWordCount();

		if ((float) (baselineScoreForSiblingParagraphs * .30) < paragraphScore) {
		    if (logger.isDebugEnabled()) {
			logger.debug("This node looks like a good sibling, adding it");
		    }
		    node.child(insertedSiblings).before(
			    "<p>" + firstParagraph.text() + "<p>");
		    insertedSiblings++;
		}

	    }

	    currentSibling = currentSibling.previousElementSibling();
	}
	return node;

    }

    /**
     * we could have long articles that have tons of paragraphs so if we tried
     * to calculate the base score against the total text score of those
     * paragraphs it would be unfair. So we need to normalize the score based on
     * the average scoring of the paragraphs within the top node. For example if
     * our total score of 10 paragraphs was 1000 but each had an average value
     * of 100 then 100 should be our base.
     * 
     * @param topNode
     * @return
     */
    private int getBaselineScoreForSiblings(Element topNode) {

	int base = 100000;

	int numberOfParagraphs = 0;
	int scoreOfParagraphs = 0;

	Elements nodesToCheck = topNode.getElementsByTag("p");

	for (Element node : nodesToCheck) {

	    String nodeText = node.text();
	    WordStats wordStats = CamusTextUtil.getStopWordCount(nodeText);
	    boolean highLinkDensity = isHighLinkDensity(node);

	    if (wordStats.getStopWordCount() > 2 && !highLinkDensity) {

		numberOfParagraphs++;
		scoreOfParagraphs += wordStats.getStopWordCount();
	    }

	}

	if (numberOfParagraphs > 0) {
	    base = scoreOfParagraphs / numberOfParagraphs;
	    if (logger.isDebugEnabled()) {
		logger.debug("The base score for siblings to beat is: " + base
			+ " NumOfParas: " + numberOfParagraphs
			+ " scoreOfAll: " + scoreOfParagraphs);
	    }
	}

	return base;

    }

}
