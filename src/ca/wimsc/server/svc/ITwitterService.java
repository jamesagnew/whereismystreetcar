/**
 * 
 */
package ca.wimsc.server.svc;

import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.rpc.FailureException;

/**
 * Service for working with Twitter's API
 */
public interface ITwitterService {

	/**
	 * Retrieves the most recent tweet relating to a given route
	 */
	Tweet getMostRecentTweetForRoute(String theRouteTag) throws FailureException;
	
}
