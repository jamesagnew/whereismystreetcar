/**
 * 
 */
package ca.wimsc.client.common.rpc;

import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.NumbersAndTimestamps;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author James
 *
 */
public interface IGetStatisticsServiceAsync {

	/**
	 * 
	 * @see ca.wimsc.client.common.rpc.IGetStatisticsService#getRouteVehicleCountsOverLast24Hours(java.util.Set)
	 */
	void getRouteVehicleCountsOverLast24Hours(Set<String> theRouteTags, AsyncCallback<Map<String, NumbersAndTimestamps>> callback);

	void getRouteAverageSpeedsOverLast24Hours(Set<String> theRouteTags, AsyncCallback<Map<String, NumbersAndTimestamps>> callback);

}
