/**
 * 
 */
package ca.wimsc.server.svc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import ca.wimsc.client.common.model.ObjectAndTimestamp;
import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.HashMapCache;

import com.google.gson.Gson;

/**
 * Retrieves recent tweets regarding 
 */
public class TwitterService implements ITwitterService {
	private static final Logger ourLog = Logger.getLogger(TwitterService.class.getName());
	private static final String CACHE_PREFIX_MOST_RECENT_TWEET = "MRT_";
	private static final long MAX_AGE = 5 * 60 * 1000L;
	
	private Cache myCache;
	private Gson myGson = new Gson();
	
	/**
	 * Constructor
	 */
	TwitterService() throws IOException {
		try {
			myCache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		} catch (CacheException e) {
			throw new IOException(e);
		}
	}
	
	
	/**
	 * UNIT TEST Constructor
	 */
	TwitterService(Cache theCache) {
		myCache = theCache;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tweet getMostRecentTweetForRoute(String theRouteTag) throws FailureException {
		String cacheKey = CACHE_PREFIX_MOST_RECENT_TWEET + theRouteTag;
		@SuppressWarnings("unchecked")
		ObjectAndTimestamp<Tweet> retVal = (ObjectAndTimestamp<Tweet>) myCache.get(cacheKey);

		if (retVal == null || retVal.getTimestampAge() > MAX_AGE) {
						
			StringBuilder url = new StringBuilder();
			url.append("http://search.twitter.com/search.json?q=from%3Attcnotices+");
			url.append(theRouteTag);
			url.append("&rpp=1");
			
			URL stopsUrl;
			TwitterResults results;
			try {
				stopsUrl = new URL(url.toString());
				BufferedReader content = new BufferedReader(new InputStreamReader(stopsUrl.openStream()));
				results = myGson.fromJson(content, TwitterResults.class);
			} catch (IOException e) {
				ourLog.log(Level.SEVERE, "Failed to load twitter results", e);
				throw new FailureException();
			}

			Tweet tweet = null;
			if (results != null && results.getResults() != null && !(results.getResults().length == 0)) {
				tweet = results.getResults()[0];
			}

			ourLog.info("Loaded most recent tweet for route " + theRouteTag + " - " + tweet);
			
			retVal = new ObjectAndTimestamp<Tweet>(tweet, System.currentTimeMillis());
			myCache.put(cacheKey, retVal);
			
		}
		
		return retVal.getObject();
	}

	
	@SuppressWarnings("unused")
	private static class TwitterResults {

	    private String maxId;
		private String nextPage;
		private String refreshUrl;
		private Tweet[] results;

		public String getMaxId() {
			return maxId;
		}

	    public String getNextPage() {
			return nextPage;
		}

	    public String getRefreshUrl() {
			return refreshUrl;
		}

	    public Tweet[] getResults() {
			return results;
		}

		public void setMaxId(String theMaxId) {
			maxId = theMaxId;
		}

		public void setNextPage(String theNextPage) {
			nextPage = theNextPage;
		}

		public void setRefreshUrl(String theRefreshUrl) {
			refreshUrl = theRefreshUrl;
		}

		public void setResults(Tweet[] theResults) {
			results = theResults;
		}

	}
	

	public static void main(String[] args) throws FailureException {
		
		TwitterService svc = new TwitterService(new HashMapCache());
		Tweet tweet = svc.getMostRecentTweetForRoute("501");
		
		System.out.println(tweet);
		
	}
	
}
