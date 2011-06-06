package com.camus.util.readability;


import java.util.*;

public class CamusTextUtil {
  private static final Set<String> STOP_WORDS;
  public static String PATTERN_PUNCTUATION = "[[\\d|\\p{Punct}]&&[^'\"]]*";
  public static String PATTERN_APOS = "'s|s'";
  public static String PATTERN_QUOTATIONS = "'|\"";

  static {
    String elements[] = {"a's", "able", "about", "above", "according", "accordingly", "across", "actually",
        "after", "afterwards", "again", "against", "ain't", "all", "allow", "allows", "almost", "alone",
        "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another",
        "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear",
        "appreciate", "appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking", "associated",
        "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming",
        "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best",
        "better", "between", "beyond", "both", "brief", "but", "by", "c", "c'mon", "c's", "came", "campaign", "can",
        "can't", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co",
        "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain",
        "containing", "contains", "corresponding", "could", "couldn't", "course", "currently",
        "definitely", "described", "despite", "did", "didn't", "different", "do", "does", "doesn't", "doing",
        "don't", "done", "down", "downwards", "during", "each", "edu", "eight", "either", "else",
        "elsewhere", "enough", "endorsed", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody",
        "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few",
        "fifth", "first", "financial", "five", "followed", "following", "follows", "for", "former", "formerly", "forth",
        "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go",
        "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadn't", "happens", "hardly",
        "has", "hasn't", "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here",
        "here's", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself",
        "his", "hither", "hopefully", "how", "howbeit", "however",  "i'd", "i'll", "i'm", "i've", "ie",
        "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates",
        "inner", "insofar", "instead", "into", "inward", "is", "isn't", "it", "it'd", "it'll", "it's", "its",
        "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately",
        "later", "latter", "latterly", "least", "less", "lest", "let", "let's", "like", "liked", "likely",
        "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean",
        "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself",
         "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never",
        "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally",
        "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok",
        "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise",
        "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular",
        "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably",
        "provides", "quite", "quote", "quarterly", "rather", "really", "reasonably", "regarding",
        "regardless", "regards", "relatively", "respectively", "right",  "said", "same", "saw", "say",
        "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen",
        "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she",
        "should", "shouldn't", "since", "six", "so", "some", "somebody", "somehow", "someone", "something",
        "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify",
        "specifying", "still", "sub", "such", "sup", "sure",  "t's", "take", "taken", "tell", "tends",
        "than", "thank", "thanks", "thanx", "that", "that's", "thats", "the", "their", "theirs", "them",
        "themselves", "then", "thence", "there", "there's", "thereafter", "thereby", "therefore", "therein",
        "theres", "thereupon", "these", "they", "they'd", "they'll", "they're", "they've", "think", "third",
        "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus",
        "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying",
        "twice", "two", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up",
        "upon", "us", "use", "used", "useful", "uses", "using", "usually", "uucp",  "value", "various",
        "very", "via", "viz", "vs", "want", "wants", "was", "wasn't", "way", "we", "we'd", "we'll",
        "we're", "we've", "welcome", "well", "went", "were", "weren't", "what", "what's", "whatever", "when",
        "whence", "whenever", "where", "where's", "whereafter", "whereas", "whereby", "wherein", "whereupon",
        "wherever", "whether", "which", "while", "whither", "who", "who's", "whoever", "whole", "whom",
        "whose", "why", "will", "willing", "wish", "with", "within", "without", "won't", "wonder", "would",
        "would", "wouldn't", "yes", "yet", "you", "you'd", "you'll", "you're", "you've", "your",
        "yours", "yourself", "yourselves", "zero", "official", "sharply", "criticized"};
    STOP_WORDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(elements)));
  }

  public static String  sanitizeWord(String str) {
    str = str.toLowerCase();
    str = str.replaceAll(PATTERN_PUNCTUATION, "");
    str = str.replaceAll(PATTERN_APOS, "");
    str = str.replaceAll(PATTERN_QUOTATIONS, "");
    return str;
  }
  
  public static WordStats getWordCountInfo(String content){
      
      WordStats ws = new WordStats();
      
      // count clean words
      List<String> cleanWords = new ArrayList<String>();
      String[] words = content.split(" ");
      String word = null;
      for (int i = 0; i < words.length; i++) {
	  word = sanitizeWord(words[i]);
	  if (!word.equals("")){
	      cleanWords.add(sanitizeWord(words[i]));
	  }
      }
      ws.setWordCount(cleanWords.size());
      
      // count paragraphs
      String[] paragraphsArray = content.split("\n\n");
      ws.setParagraphsCount(paragraphsArray.length);
      
      return ws;
  }


  public static WordStats getStopWordCount(String content) {

    WordStats ws = new WordStats();

    String[] words = content.split(" ");


    // sanitize each word in the array
    for (int i = 0; i < words.length; i++) {
      words[i] = sanitizeWord(words[i]);
    }

    // stem each word in the array if it is not null or a stop word
    List<String> stopWords = new ArrayList<String>();
    for (int i = 0; i < words.length; i++) {
      if (words[i] != null && words[i].length() > 0 && STOP_WORDS.contains(words[i]))
        stopWords.add(words[i]);
    }

    ws.setWordCount(words.length);
    ws.setStopWordCount(stopWords.size());
    ws.setStopWords(stopWords);
    return ws;
  }


}
