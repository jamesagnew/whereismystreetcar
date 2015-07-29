/**
 * 
 */
package ca.wimsc.client.common.rpc;

import java.util.Set;

import ca.wimsc.client.common.model.MostRecentTweets;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Retrieve statistics
 */
@RemoteServiceRelativePath("getTwitter")
public interface IGetTwitterService extends RemoteService {

	
	/**
	 * Retrieves the most recent tweet relating to a given routes
	 */
	MostRecentTweets getMostRecentTweetForRoutes(Set<String> theRouteTag) throws FailureException;
	
	
}
