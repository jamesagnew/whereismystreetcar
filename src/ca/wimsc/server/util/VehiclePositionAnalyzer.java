/**
 * 
 */
package ca.wimsc.server.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.model.IncidentTypeEnum;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.client.common.util.Pair;
import ca.wimsc.server.jpa.PersistedIncident;
import ca.wimsc.server.jpa.PersistedVehiclePosition;
import ca.wimsc.server.svc.INextbusFetcherService;

import com.google.appengine.api.datastore.GeoPt;

/**
 * <p>
 * Examines a list of vehicle positions and looks for diversions, full runs, etc.
 * </p>
 * 
 * <p>
 * Note that this class is not designed to be thread safe, as {@link #setPositions(String, List)} needs to be called
 * between check calls
 * </p>
 */
public class VehiclePositionAnalyzer {
	private static final java.util.logging.Logger ourLog = java.util.logging.Logger.getLogger(VehiclePositionAnalyzer.class.getName());
	public static final String NULL_DIR = "null";
	private Map<String, StopList> myDirectionTagToStopList;
	private INextbusFetcherService myNextbusFetcherService;
	private List<PersistedVehiclePosition> myPositions;
	private String myRouteTag;
	private String myVehicleTag;


	/**
	 * Constructor
	 */
	public VehiclePositionAnalyzer(INextbusFetcherService theNextbusFetcherService, String theRouteTag) {
		myNextbusFetcherService = theNextbusFetcherService;
		myRouteTag = theRouteTag;
	}


	private Diversion getJustEndedDiversionIfAny() throws FailureException {
		if (myPositions.size() < 3) {
			return null;
		}

		String currentDirectionTag = myPositions.get(myPositions.size() - 1).getDirectionTag();
		String previousDirectionTag = myPositions.get(myPositions.size() - 2).getDirectionTag();

		if (currentDirectionTag == null || previousDirectionTag == null) {
			return null;
		}

		if (NULL_DIR.equals(currentDirectionTag)) {
			return null;
		}

		if (!NULL_DIR.equals(previousDirectionTag)) {
			return null;
		}

		long maxDistanceFromRoute = 0;
		for (int i = myPositions.size() - 3; i >= 0; i--) {

			PersistedVehiclePosition nextPosition = myPositions.get(i);
			String nextDirectionTag = nextPosition.getDirectionTag();

			if (NULL_DIR.equals(nextDirectionTag)) {
				
				StopList stopList = getStopListForDirectionTag(currentDirectionTag);
				Pair<Stop, Long> stopAndDistance = stopList.findClosestStopWithDistance(nextPosition.getLatitude(), nextPosition.getLongitude());
				if (stopAndDistance != null) {
					long nextDistance = stopAndDistance.getObject2().longValue();
					if (nextDistance > maxDistanceFromRoute) {
						maxDistanceFromRoute = nextDistance;
					}
				}
				
				continue;
			}

			/*
			 * If we were going a particular direction, then we were going "null", then we were back to that
			 * same direction, that looks like a diversion 
			 */
			if (currentDirectionTag.equals(nextDirectionTag)) {
				// This is a diversion!
				String closestStopTag = nextPosition.getClosestStopTag();
				if (closestStopTag == null) {
					return null;
				}

				String endingStopTag = myPositions.get(myPositions.size() - 1).getClosestStopTag();
				if (endingStopTag == null) {
					return null;
				}

				if (maxDistanceFromRoute < ServerConstants.DIVERSION_MIN_DISTANCE_FROM_ROUTE) {
					return null;
				}
				
				Diversion retVal = new Diversion();
				retVal.setStartTimestamp(nextPosition.getTimestamp());
				retVal.setStartingStopTag(closestStopTag);
				retVal.setEndingStopTag(endingStopTag);
				retVal.setDirectionTag(nextDirectionTag);

				StopList stopList = getStopListForDirectionTag(nextDirectionTag);
				Stop startingStop = stopList.findFirstStopWithTag(closestStopTag);

				retVal.setStartingLatitude(startingStop.getLatitude());
				retVal.setStartingLongitude(startingStop.getLongitude());

				return retVal;

			} else {

				// probably a short turn
				return null;

			}
		}

		return null;
	}


	public void checkDiversions(List<PersistedIncident> theAllOpenIncidents, List<PersistedIncident> theOpenDiversionIncidents) throws FailureException {

		Diversion justEnded = getJustEndedDiversionIfAny();
		if (justEnded != null) {

			String directionTag = justEnded.getDirectionTag();
			StopList stopList = getStopListForDirectionTag(directionTag);

			boolean foundMatch = false;
			for (PersistedIncident persistedIncident : theOpenDiversionIncidents) {
				if (!persistedIncident.getDirectionTag().equals(directionTag)) {
					continue;
				}
				if (persistedIncident.getEndDate() != null) {
					continue;
				}

				foundMatch = justEnded.shrinkBoundsIfPossible(stopList, persistedIncident);

				if (foundMatch) {
					break;
				}

			} // for

			if (!foundMatch) {
				PersistedIncident newIncident = justEnded.createNewIncident();
				theAllOpenIncidents.add(newIncident);
			}

		} else {

			/*
			 * The most recent position was not the end of a diversion, so now check if it was one cancelling out
			 */
			if (myPositions.size() < 2) {
				return;
			}
			PersistedVehiclePosition currentPosition = myPositions.get(myPositions.size() - 1);
			String currentDirectionTag = currentPosition.getDirectionTag();
			if (currentDirectionTag.equals(NULL_DIR)) {
				return;
			}

			PersistedVehiclePosition firstPositionInSameDirection = null;

			for (int i = myPositions.size() - 2; i >= 0; i--) {
				PersistedVehiclePosition nextPosition = myPositions.get(i);
				if (nextPosition.getDirectionTag().equals(currentDirectionTag)) {
					firstPositionInSameDirection = nextPosition;
				} else {
					break;
				}
			}

			if (firstPositionInSameDirection == null) {
				return;
			}

			StopList stopList = getStopListForDirectionTag(currentDirectionTag);
			int firstPositionInSameDirectionIndex = stopList.getStopTagIndex(firstPositionInSameDirection.getClosestStopTag());
			int currentPositionIndex = stopList.getStopTagIndex(currentPosition.getClosestStopTag());

			for (PersistedIncident nextPersistedIncident : theOpenDiversionIncidents) {
				if (!nextPersistedIncident.getRoute().equals(myRouteTag)) {
					continue;
				}
				if (!nextPersistedIncident.getDirectionTag().equals(currentDirectionTag)) {
					continue;
				}
				if (nextPersistedIncident.getEndDate() != null) {
					continue;
				}

				int incidentStartTagIndex = nextPersistedIncident.getOrCalculateBeginNearestStopTagIndex(stopList);
				int incidentEndTagIndex = nextPersistedIncident.getOrCalculateEndNearestStopTagIndex(stopList);
				if (firstPositionInSameDirectionIndex <= incidentStartTagIndex) {
					if (currentPositionIndex >= incidentEndTagIndex) {
						ourLog.info("Vehicle " + myVehicleTag + " has travelled from " + firstPositionInSameDirection.getClosestStopTag() + " to "
								+ currentPosition.getClosestStopTag() + " on route " + myRouteTag + " - Ending incident " + nextPersistedIncident.getKey());
						nextPersistedIncident.setEndDate(new Date());
					}
				}

			}

		}

	}


	private StopList getStopListForDirectionTag(String directionTag) throws FailureException {
		if (myDirectionTagToStopList == null) {
//			myDirectionTagToStopList = myNextbusFetcherService.loadDirectionTagToStopList(myRouteTag);
		}
		return myDirectionTagToStopList.get(directionTag);
	}


	/**
	 * Update fields for next use
	 */
	public void setPositions(String theVehicleTag, List<PersistedVehiclePosition> thePositions) {
		myVehicleTag = theVehicleTag;
		myPositions = thePositions;
	}


	public class Diversion {
		private String myDirectionTag;
		private String myEndingStopTag;
		private double myStartingLatitude;
		private double myStartingLongitude;
		private String myStartingStopTag;
		private long myStartTimestamp;


		/**
		 * @return
		 */
		public PersistedIncident createNewIncident() {
			PersistedIncident retVal = new PersistedIncident();

			retVal.setBeginNearestStopTag(myStartingStopTag);
			retVal.setEndNearestStopTag(myEndingStopTag);
			retVal.setStartDate(new Date(myStartTimestamp));
			retVal.setDirectionTag(myDirectionTag);
			retVal.setStartingLocation(new GeoPt((float) myStartingLatitude, (float) myStartingLongitude));
			retVal.setRoute(myRouteTag);
			retVal.setIncidentType(IncidentTypeEnum.DIVERTING);

			return retVal;
		}


		/**
		 * @return the directionTag
		 */
		public String getDirectionTag() {
			return myDirectionTag;
		}


		/**
		 * @return the endingStopTag
		 */
		public String getEndingStopTag() {
			return myEndingStopTag;
		}


		/**
		 * @return the startingLatitude
		 */
		public double getStartingLatitude() {
			return myStartingLatitude;
		}


		/**
		 * @return the startingLongitude
		 */
		public double getStartingLongitude() {
			return myStartingLongitude;
		}


		/**
		 * @return the startingStopTag
		 */
		public String getStartingStopTag() {
			return myStartingStopTag;
		}


		/**
		 * @return the startDate
		 */
		public long getStartTimestamp() {
			return myStartTimestamp;
		}


		/**
		 * @param theDirectionTag
		 *            the directionTag to set
		 */
		public void setDirectionTag(String theDirectionTag) {
			myDirectionTag = theDirectionTag;
		}


		/**
		 * @param theEndingStopTag
		 *            the endingStopTag to set
		 */
		public void setEndingStopTag(String theEndingStopTag) {
			myEndingStopTag = theEndingStopTag;
		}


		/**
		 * @param theLatitude
		 */
		public void setStartingLatitude(double theLatitude) {
			myStartingLatitude = theLatitude;
		}


		/**
		 * @param theLongitude
		 */
		public void setStartingLongitude(double theLongitude) {
			myStartingLongitude = theLongitude;
		}


		/**
		 * @param theStartingStopTag
		 *            the startingStopTag to set
		 */
		public void setStartingStopTag(String theStartingStopTag) {
			myStartingStopTag = theStartingStopTag;
		}


		/**
		 * @param theStartDate
		 *            the startDate to set
		 */
		public void setStartTimestamp(long theStartDate) {
			myStartTimestamp = theStartDate;
		}


		/**
		 * @return True if the bounds of this diversion align with / overlap the given incident, meaning that they both
		 *         refer to the same diversion
		 */
		public boolean shrinkBoundsIfPossible(StopList theStopList, PersistedIncident thePersistedIncident) {
			int thisStartTagIndex = theStopList.getStopTagIndex(myStartingStopTag);
			int thisEndTagIndex = theStopList.getStopTagIndex(myEndingStopTag);

			int incidentStartTagIndex = thePersistedIncident.getOrCalculateBeginNearestStopTagIndex(theStopList);
			int incidentEndTagIndex = thePersistedIncident.getOrCalculateEndNearestStopTagIndex(theStopList);

			if (thisStartTagIndex > incidentEndTagIndex && thisEndTagIndex > incidentEndTagIndex) {
				// the incident is after this one on the route
				return false;
			}

			if (thisStartTagIndex < incidentStartTagIndex && thisEndTagIndex < incidentStartTagIndex) {
				// the incident is before this one on the route
				return false;
			}

			if (thisStartTagIndex > incidentStartTagIndex) {
				thePersistedIncident.setBeginNearestStopTag(myStartingStopTag);
			}

			if (thisEndTagIndex < incidentEndTagIndex) {
				thePersistedIncident.setEndNearestStopTag(myEndingStopTag);
			}

			return true;
		}
	}

}
