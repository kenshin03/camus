Background
==============

*Camus* is the server component for a flipboard like social reader I'm working on, called *Cassius*.

More details on http://www.corgitoergosum.net/cassius-project-flipboard-clone/

New in revision:
  * tweets and extracted articles now need to be stored in a running local instance of mongodb. Once I move and test these scripts on AWS, I'll make the settings configuable




Third party libraries:

   * Apache Commons - for network 
      * commons-io-2.0.1.jar
      * commons-lang-2.6.jar
      * httpcomponents-client-4.1.1      
   * GSON - for parsing and JSON http://code.google.com/p/google-gson/
      * gson-1.7.1.jar      
   * JSOUP - for parsing HTML  http://jsoup.org/
      * jsoup-1.5.2.jar      
   * Log4J - logging
      * log4j-1.2.16.jar            
   * scribe - for openauth to the various facebook and twitter APIs - https://github.com/facebook/scribe/wiki
      * scribe-1.1.3.jar
   * Dom4J - xml parsing
      * dom4j
   * mongodb java driver - for storing documents and tweets
      * mongo-2.6.3.jar
   * lingpipe - required by ipeirotis related classes for analyzing readability of english text
      * lingpipe-4.1.0.jar
