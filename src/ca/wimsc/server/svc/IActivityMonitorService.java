package ca.wimsc.server.svc;

import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.rpc.FailureException;

public interface IActivityMonitorService {

	/**
	 * Returns the marshalled form of the route path entyr for the given route tag
	 */
	String getRoutePathString(String theRouteTag) throws FailureException;

	/**
	 * Retieve the average speed for a particular vehicle
	 */
	NumbersAndTimestamps getVehicleAverageSpeed(String theRouteTag, String theVehicleTag);

	/**
	 * Return a map of the average speeds of the vehicles on the route with the given tag
	 * 
	 * @param theRoute
	 *            The route tag
	 */
	Map<String, SpeedAndTimestamp> getVehicleAverageSpeeds(String theRoute);

	/**
	 * Purge old incidents from the DB
	 * 
	 * @return A status string
	 */
	String purgeIncidents() throws FailureException;

	/**
	 * Update vehicle positions in the DB, and calculate speeds and look for incidents
	 * 
	 * @return A status string
	 */
	String updateVehicles() throws FailureException;

	/**
	 * Update all route paths
	 * 
	 * @return A status string
	 */
	String updateRoutePaths() throws FailureException;
	
	/**
	 * Retrieve the number of vehicles on the given routes
	 */
	Map<String, NumbersAndTimestamps> getRouteVehicleCountsOverLast24Hours(Set<String> theRouteTags) throws FailureException;

	/**
	 * Retrieve the number of vehicles on the given routes
	 */
	Map<String, NumbersAndTimestamps> getRouteAverageSpeedsOverLast24Hours(Set<String> theRouteTags) throws FailureException;
	
	/**
	 * Bean storing a speed value (in km/h) and a timestamp and most recent position associated with that speed
	 */
	public static class SpeedAndTimestamp {
		private static final long TWO_MINUTES = 2 * 60 * 1000L;
		
		// TODO: this should just be speed, not average speed
		private int myAverageSpeed;
		private double myLatitude;
		private double myLongitude;
		private long myTimestamp;

		/**
		 * Constructor
		 */
		public SpeedAndTimestamp() {
			// nothing
		}

		/**
		 * Constructor
		 */
		public SpeedAndTimestamp(Integer theSpeed, double theLatitude, double theLongitude, long theTimestamp) {
			myAverageSpeed = theSpeed;
			myTimestamp = theTimestamp;
			myLatitude = theLatitude;
			myLongitude = theLongitude;
		}

		public int getAverageSpeed() {
			return myAverageSpeed;
		}

		public double getLatitude() {
			return myLatitude;
		}

		public double getLongitude() {
			return myLongitude;
		}

		public long getTimestamp() {
			return myTimestamp;
		}

		/**
		 * Given a timestamp and a position, update the average speed in this bean <b>only if</b> more than two moinutes
		 * has elapsed since the timestamp attached to the current speed. This is useful because the vehicle monitor
		 * service only runs periodically, so it can be a bit out of date
		 */
		public void maybeUpdateBasedOnLatestPosition(long theTimestamp, double theLatitude, double theLongitude) {
			long elapsed = theTimestamp - myTimestamp;
			if (elapsed > TWO_MINUTES) {
				int speed = ActivityMonitorServiceImpl.calculateSpeed(myLatitude, myLongitude, theLatitude, theLongitude, elapsed);
				myAverageSpeed = speed;
			}
		}

		public void setAverageSpeed(int theAverageSpeed) {
			this.myAverageSpeed = theAverageSpeed;
		}

		public void setLatitude(double theLatitude) {
			this.myLatitude = theLatitude;
		}

		public void setLongitude(double theLongitude) {
			this.myLongitude = theLongitude;
		}

		public void setTimestamp(long theTimestamp) {
			this.myTimestamp = theTimestamp;
		}

	}

}