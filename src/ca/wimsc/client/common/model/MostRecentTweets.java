/**
 * 
 */
package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean containing the most recent tweets for a collection of routes
 */
public class MostRecentTweets implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private transient Tweet myMostRecent;

	private Map<String, Tweet> myTweets;


	/**
	 * Constructor
	 */
	public MostRecentTweets() {
		super();
	}


	/**
	 * Constructor
	 */
	public MostRecentTweets(HashMap<String, Tweet> theRouteTagToTweet) {
		myTweets = theRouteTagToTweet;
	}


	public Tweet getMostRecent() {
		if (myMostRecent == null) {
			if (myTweets != null) {
				for (Tweet next : myTweets.values()) {
					if (next != null) {
						if (myMostRecent == null || myMostRecent.getCreatedAtDate() == null || myMostRecent.getCreatedAtDate().before(next.getCreatedAtDate())) {
							myMostRecent = next;
						}
					}
				}
			}
		}
		return myMostRecent;
	}


	/**
	 * @return the tweets
	 */
	public Map<String, Tweet> getTweets() {
		return myTweets;
	}


	/**
	 * @param theTweets
	 *            the tweets to set
	 */
	public void setTweets(Map<String, Tweet> theTweets) {
		myTweets = theTweets;
	}

}
