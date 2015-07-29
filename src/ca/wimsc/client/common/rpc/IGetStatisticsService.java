/**
 * 
 */
package ca.wimsc.client.common.rpc;

import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.NumbersAndTimestamps;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Retrieve statistics
 */
@RemoteServiceRelativePath("getStatistics")
public interface IGetStatisticsService extends RemoteService {

	/**
	 * Retrieve the number of vehicles on the given routes
	 */
	Map<String, NumbersAndTimestamps> getRouteVehicleCountsOverLast24Hours(Set<String> theRouteTags) throws FailureException;

	/**
	 * Retrieve the acerage speed on the given routes
	 */
	Map<String, NumbersAndTimestamps> getRouteAverageSpeedsOverLast24Hours(Set<String> theRouteTags) throws FailureException;
	
}
