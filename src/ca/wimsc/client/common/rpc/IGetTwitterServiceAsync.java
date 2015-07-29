/**
 * 
 */
package ca.wimsc.client.common.rpc;

import java.util.Set;

import ca.wimsc.client.common.model.MostRecentTweets;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author James
 * 
 */
public interface IGetTwitterServiceAsync {

	void getMostRecentTweetForRoutes(Set<String> theRouteTag, AsyncCallback<MostRecentTweets> callback);

}
