package com.capgemini.cwin16.alexa.twitter;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;


public class TwitterSpeechletHandler extends SpeechletRequestStreamHandler{

  private static final Set<String> supportedApplicationIds = new HashSet<String>();
      static {
          /*
           * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
           * Alexa Skill and put the relevant Application Ids in this Set.
           */
          supportedApplicationIds.add("amzn1.ask.skill.ee9ba7cb-634a-4d11-b3d4-f6993a117d44");
      }

      public TwitterSpeechletHandler() {
          super(new TwitterSpeechlet(), supportedApplicationIds);
      }

}
