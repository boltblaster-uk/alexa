package com.capgemini.cwin16.alexa.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import twitter4j.*;

public class TwitterSpeechlet implements Speechlet{
  private static final Logger log =
        LoggerFactory.getLogger(TwitterSpeechlet.class);

  private static final String SCREENNAME_SLOT = "screenname";

  private static final String HASHTAG_SLOT = "hashtag";




  @Override
  public void onSessionStarted(final SessionStartedRequest request,
        final Session session) throws SpeechletException {
      log.info("onSessionStarted requestId={}, sessionId={}",
              request.getRequestId(),
              session.getSessionId());

  }

  @Override
  public SpeechletResponse onLaunch(final LaunchRequest request,
          final Session session) throws SpeechletException {
      log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
              session.getSessionId());
      return getWelcomeResponse();
  }

  @Override
  public SpeechletResponse onIntent(final IntentRequest request, final Session session)
          throws SpeechletException {
      log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
              session.getSessionId());

      Intent intent = request.getIntent();
      String intentName = (intent != null) ? intent.getName() : null;

      if ( "Twitter".equals(intentName)) {
        try {
          return getTwitterResponse(intent) ;
        }catch(TwitterException te) {
          throw new SpeechletException(te.getMessage());
        }
      }else if ("AMAZON.HelpIntent".equals(intentName)) {
        return getHelpResponse();
      }else {
        throw new SpeechletException("Invalid Intent");
      }

  }

  @Override
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
          throws SpeechletException {
      log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
              session.getSessionId());
      // any cleanup logic
  }

  /**
   * Creates and returns a {@code SpeechletResponse} with a welcome message.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getWelcomeResponse() {
      String speechText = "Welcome to the Alexa Twitter Skill";

      // Create the Simple card content.
      SimpleCard card = new SimpleCard();
      card.setTitle("Twitter");
      card.setContent(speechText);

      // Create the plain text output.
      PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
      speech.setText(speechText);

      // Create reprompt
      Reprompt reprompt = new Reprompt();
      reprompt.setOutputSpeech(speech);

      return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }

  /**
   * Creates a {@code SpeechletResponse} for the hello intent.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getTwitterResponse(final Intent intent) throws SpeechletException, TwitterException {
    Twitter twitter = null;
    try {
          twitter = new TwitterFactory().getInstance();
          twitter.verifyCredentials();
    }catch(TwitterException te) {
      throw new SpeechletException(te);
    }

    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
    List<Status> statuses = null;
    Slot slot = intent.getSlot(SCREENNAME_SLOT);
    if (slot != null && slot.getValue() != null) {
      String value = slot.getValue();
      value = value.replaceAll("\\s","");
      log.info("Getting Tweets for User : " + value);

      statuses = twitter.getUserTimeline("@".concat(value));
      log.info("Number of Tweets Found : " + statuses.size());
      StringBuilder builder = new StringBuilder();
      // builder.append("<speak>");
      for (Status status : statuses ) {
        log.info("Adding Tweet : " + status.getText());
        builder
        .append( removeUrl(status.getText()))
        .append("\t");
        // .append("<break time=\"1.0s\" />");

      }
      // builder.append("</speak>");
      log.info("Tweets : " + builder.toString() );
      // outputSpeech.setSsml(builder.toString());
      if ( statuses.size() == 0) {
        builder.append("No Tweets Found for : " + value);
      }
      outputSpeech.setText(builder.toString());
    }else{
        slot = intent.getSlot(HASHTAG_SLOT);
        if (slot != null && slot.getValue() != null) {

          String value = slot.getValue();
          value = value.replaceAll("\\s","");
          if ( value.startsWith("sea") || value.startsWith("see")){
            value = "CWIN16";
          }
          log.info("Getting Tweets for Hashtag : " + value);
          QueryResult result = twitter.search(new Query("#".concat(value)));
          statuses = result.getTweets();
          outputSpeech.setText("Not Yet Implemented");
          log.info("Number of Tweets Found : " + statuses.size());
          StringBuilder builder = new StringBuilder();
          // builder.append("<speak>");
          for (Status status : statuses ) {
            log.info("Adding Tweet : " + status.getText());
            builder
            .append( removeUrl(status.getText()))
            .append("\t");
            // .append("<break time=\"1.0s\" />");

          }
          // builder.append("</speak>");
          log.info("Tweets : " + builder.toString() );
          if ( statuses.size() == 0) {
            builder.append("No Tweets Found for : " + value);
          }
          // outputSpeech.setSsml(builder.toString());
          outputSpeech.setText(builder.toString());
        }else {
          return getHelpResponse();
        }
    }
    return SpeechletResponse.newTellResponse(outputSpeech);
  }

  /**
   * Creates a {@code SpeechletResponse} for the help intent.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getHelpResponse() {
      String speechText = "You Retrieve the latest tweets for a user";

      // Create the Simple card content.
      SimpleCard card = new SimpleCard();
      card.setTitle("Twitter");
      card.setContent(speechText);

      // Create the plain text output.
      PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
      speech.setText(speechText);

      // Create reprompt
      Reprompt reprompt = new Reprompt();
      reprompt.setOutputSpeech(speech);

      return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }

  private String removeUrl(String commentstr){
          String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
          Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
          Matcher m = p.matcher(commentstr);
          int i = 0;
          while (m.find()) {
              commentstr = commentstr.replaceAll(m.group(i),"").trim();
              i++;
          }
          return commentstr;
  }

}
