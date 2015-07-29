package ca.wimsc.server.svc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.apache.commons.lang3.time.DateUtils;

import ca.wimsc.client.common.model.IncidentTypeEnum;
import ca.wimsc.client.common.model.NumberAndTimestamp;
import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.model.ObjectAndTimestamp;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.RoutePath;
import ca.wimsc.client.common.model.RoutePathElement;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.client.common.util.DateUtil;
import ca.wimsc.server.HashMapCache;
import ca.wimsc.server.jpa.PersistedIncident;
import ca.wimsc.server.jpa.PersistedRoute;
import ca.wimsc.server.jpa.PersistedVehicle;
import ca.wimsc.server.jpa.PersistedVehiclePosition;
import ca.wimsc.server.util.ServerConstants;
import ca.wimsc.server.util.VehiclePositionAnalyzer;
import ca.wimsc.server.xml.route.Body;
import ca.wimsc.server.xml.route.Body.Route.Path;
import ca.wimsc.server.xml.route.Body.Route.Path.Point;

import com.google.appengine.api.datastore.GeoPt;

public class ActivityMonitorServiceImpl implements IActivityMonitorService {

	private static final String CACHE_PREFIX_ROUTE_ACTIVE_VEHICLES = "RAV_";
	private static final String CACHE_PREFIX_ROUTE_AVERAGE_SPEEDS = "RAS_";
	private static final long CACHE_TIMEOUT_ROUTE_ACTIVE_VEHICLES = 30 * 60 * 1000l;
	private static final long INCIDENT_RETENTION_MILLIS = 7 * 24 * 60 * 60 * 1000L;
	private static final long INTERVAL = 5 * 60 * 1000;
	private static final Logger logger = Logger.getLogger(ActivityMonitorServiceImpl.class.getName());
	private static final String ROUTE_PATH_PREFIX = "RP_";
	private static final long VEHICLE_AVERAGE_SPEED_CACHE_TIME = 60 * 1000;
	private static final String VEHICLE_AVERAGE_SPEED_PREFIX = "VAS_";

	private Cache myCache;
	private EntityManagerFactory myEntityManagerFactory;
	private Long myHardcodedTime;
	private IntervalEnforcer myIntervalEnforcer = new IntervalEnforcer();

	/**
	 * Speeds below this (in km/h) are considered not moving
	 */
	private int myMinimumMovingSpeed = 2;

	private INextbusFetcherService myNextbusFetcherService;

	private RouteList myRouteList;

	/**
	 * Number of refresh intervals during which a vehicle must be moving slowly to generate an incident
	 */
	private int mySlowIntervalsToGenerateIncident = 2;


	/**
	 * Constructor
	 */
	ActivityMonitorServiceImpl() throws IOException {
		myEntityManagerFactory = Persistence.createEntityManagerFactory("transactions-optional");

		try {
			myCache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		} catch (CacheException e) {
			throw new IOException(e);
		}

	}


	/**
	 * Unit test constructor
	 */
	ActivityMonitorServiceImpl(INextbusFetcherService theFetcherSvc, EntityManagerFactory theEntityManagerFactory) throws IOException {
		myEntityManagerFactory = theEntityManagerFactory;
		myNextbusFetcherService = theFetcherSvc;
		myCache = new HashMapCache();
	}


	private void checkForNewOrUpdateIncidentsWithVehicles(PersistedRoute theRoute, List<PersistedVehicle> vehicles, List<PersistedIncident> theOpenIncidents)
			throws FailureException {

		String routeTag = theRoute.getRouteTag();
		List<PersistedIncident> openDiversionIncident = new ArrayList<PersistedIncident>();
		for (PersistedIncident nextOpenIncident : theOpenIncidents) {
			if (nextOpenIncident.getRoute().equals(routeTag) == false) {
				continue;
			}

			if (nextOpenIncident.getEndDate() != null) {
				continue;
			}

			if (nextOpenIncident.getIncidentType() == IncidentTypeEnum.DIVERTING) {
				openDiversionIncident.add(nextOpenIncident);
			}
		}

		// Check for diversions
		if (ServerConstants.ENABLE_DIVERSION_SCAN) {
			VehiclePositionAnalyzer analyzer = new VehiclePositionAnalyzer(myNextbusFetcherService, theRoute.getRouteTag());
			for (PersistedVehicle nextVehicle : vehicles) {

				List<PersistedVehiclePosition> nextPositions = nextVehicle.getPositions();
				String vehicleTag = nextVehicle.getVehicleTag();

				analyzer.setPositions(vehicleTag, nextPositions);

				analyzer.checkDiversions(theOpenIncidents, openDiversionIncident);

			}
		}

		// Check for any NOT_MOVING incidents
//		if (ServerConstants.ENABLE_NOT_MOVING_SCAN) {
//			for (PersistedVehicle nextVehicle : vehicles) {
//
//				if (!nextVehicle.isMostRecentIncidentActive()) {
//
//					checkVehicleForSlowMovingStatusBegun(theRoute, nextVehicle);
//
//				} else {
//
//					EntityManager em = myEntityManagerFactory.createEntityManager();
//					PersistedIncident mri = null;
//					try {
//						mri = em.find(PersistedIncident.class, nextVehicle.getMostRecentIncidentId());
//
//						if (mri != null) {
//							switch (mri.getIncidentType()) {
//							case NOT_MOVING:
//								checkVehicleForSlowMovingStatusEnded(mri, nextVehicle);
//								break;
//							default:
//								// nothing
//							}
//						}
//
//					} finally {
//						em.close();
//					}
//
//				} // if-else
//
//			} // for vehicles
//
//		} // if enable diversion scan
	}


//	private void checkVehicleForSlowMovingStatusBegun(PersistedRoute theRoute, PersistedVehicle nextVehicle) throws FailureException {
//		// Search for slow moving vehicles
//		List<PersistedVehiclePosition> positions = nextVehicle.getPositions();
//		String previousDirection = null;
//		if (positions.size() >= mySlowIntervalsToGenerateIncident) {
//
//			boolean triggered = true;
//			int endIndex = positions.size() - mySlowIntervalsToGenerateIncident;
//			for (int index = positions.size(); index > endIndex; index--) {
//				PersistedVehiclePosition nextPosition = positions.get(index - 1);
//				if (nextPosition.getDirectionTag().equals("null")) {
//					triggered = false;
//					break;
//				}
//
//				if (nextPosition.isAtExtremity()) {
//					triggered = false;
//					break;
//				}
//
//				if (nextPosition.getSpeedInKmh() == null || nextPosition.getSpeedInKmh() >= myMinimumMovingSpeed) {
//					triggered = false;
//					break;
//				}
//
//				if (previousDirection != null && !previousDirection.equals(nextPosition.getDirectionTag())) {
//					triggered = false;
//					break;
//				}
//
//				previousDirection = nextPosition.getDirectionTag();
//			}
//
//			if (triggered) {
//
//				/*
//				 * We only consider this an incident if we have seen the same vehicle travelling in the same direction
//				 * but with a decent speed. Otherwise, it's probably slow because it just turned around and it's waiting
//				 * to begin a new trip
//				 */
//				int foundSpeed = 0;
//				String stoppedDirection = positions.get(endIndex).getDirectionTag();
//				for (int index = endIndex - 1; index >= 0; index--) {
//					PersistedVehiclePosition nextPosition = positions.get(index);
//					if (!nextPosition.getDirectionTag().equals(stoppedDirection)) {
//						break;
//					}
//
//					if (nextPosition.getSpeedInKmh() != null && nextPosition.getSpeedInKmh() >= myMinimumMovingSpeed) {
//						foundSpeed++;
//					}
//				}
//
//				if (foundSpeed >= 2) {
//					PersistedVehiclePosition beginPosition = nextVehicle.getPositions().get(endIndex);
//					PersistedVehiclePosition currentPosition = nextVehicle.getPositions().get(nextVehicle.getPositions().size() - 1);
//
//					PersistedIncident incident = openNotMovingIncident(theRoute, nextVehicle, beginPosition, currentPosition);
//
//					logger.info("Created new incident: " + incident.toString());
//
//					nextVehicle.setMostRecentIncidentId(incident.getKey());
//					nextVehicle.setMostRecentIncidentActive(true);
//				}
//
//			}
//		}
//	}


//	private void checkVehicleForSlowMovingStatusEnded(PersistedIncident theIncident, PersistedVehicle theVehicle) throws FailureException {
//		Integer speedInKmh = null;
//		List<PersistedVehiclePosition> positions = theVehicle.getPositions();
//		if (positions.size() == 0) {
//			return;
//		}
//
//		speedInKmh = positions.get(positions.size() - 1).getSpeedInKmh();
//		if (speedInKmh != null && speedInKmh < myMinimumMovingSpeed) {
//			return;
//		}
//
//		logger.info("Not moving incident has cleared for vehicle " + theVehicle.getVehicleTag() + " - Speed is now: " + speedInKmh);
//
//		PersistedVehiclePosition endPosition;
//		if (positions.size() == 1) {
//			endPosition = positions.get(0);
//		} else {
//			endPosition = positions.get(positions.size() - 1);
//		}
//
//		closeIncident(theIncident, endPosition);
//
//		theVehicle.setMostRecentIncidentActive(false);
//	}


//	private EntityManager closeIncident(PersistedIncident theIncident, PersistedVehiclePosition theEndPosition) throws FailureException {
//		EntityManager theEm;
//		// Looks like we're moving again..
//		theEm = myEntityManagerFactory.createEntityManager();
//		theIncident = theEm.find(PersistedIncident.class, theIncident.getKey());
//		EntityTransaction transaction = theEm.getTransaction();
//		try {
//			transaction.begin();
//			theIncident.setEndDate(new Date());
//
//			if (theEndPosition != null) {
//				StopList stopList = myNextbusFetcherService.loadDirectionTagToStopList(theIncident.getRoute()).get(theIncident.getDirectionTag());
//				String nearestStop = stopList.findClosestStopTag(theEndPosition.getLatitude(), theEndPosition.getLongitude());
//				theIncident.setEndNearestStopTag(nearestStop);
//			}
//
//			theEm.merge(theIncident);
//			transaction.commit();
//		} finally {
//			if (transaction.isActive()) {
//				transaction.rollback();
//			}
//			theEm.close();
//		}
//		return theEm;
//	}


	private void createRoutePathUsingBackendPath(PersistedRoute thePersistedRoute, RoutePath routePath, Map<String, StopList> stopLists) throws FailureException {
		Body routeConfig = myNextbusFetcherService.loadRouteConfig(thePersistedRoute.getRouteTag());
		String direction1 = routeConfig.getRoute().getDirection().get(0).getTag();
		String direction2 = routeConfig.getRoute().getDirection().get(1).getTag();

		StopList stopList1 = stopLists.get(direction1);
		StopList stopList2 = stopLists.get(direction2);

		List<RoutePathElement> elements = new ArrayList<RoutePathElement>();
		for (Path nextPath : routeConfig.getRoute().getPath()) {

			if (pathOverlapsExisting(nextPath, elements)) {
				continue;
			}

			boolean firstPoint = true;
			for (Point nextPoint : nextPath.getPoint()) {

				RoutePathElement nextElement = new RoutePathElement();
				elements.add(nextElement);

				nextElement.setNewPathEntry(firstPoint);
				firstPoint = false;

				nextElement.setLatitude(nextPoint.getLat());
				nextElement.setLongitude(nextPoint.getLon());

				nextElement.setClosestStopTagDirection1(stopList1.findClosestStopTag(nextPoint.getLat(), nextPoint.getLon()));
				nextElement.setClosestStopTagDirection2(stopList2.findClosestStopTag(nextPoint.getLat(), nextPoint.getLon()));

			} // for point

		} // for path

		routePath.setDirection1Tag(direction1);
		routePath.setDirection2Tag(direction2);
		routePath.setRoutePathElements(elements);
	}


	/**
	 * @param thePersistedRoute
	 * @param theRoutePath
	 * @param theStopListForRoute
	 */
	private void createRoutePathUsingStops(RoutePath theRoutePath, StopListForRoute theStopListForRoute) {

		Iterator<StopList> stopListIter = theStopListForRoute.getUiStopLists().iterator();
		
		StopList stopList1 = stopListIter.next();
		StopList stopList2 = stopListIter.next();
		
		String direction1 = stopList1.getTag();
		String direction2 = stopList2.getTag();

		List<RoutePathElement> elements = new ArrayList<RoutePathElement>();
		for (Stop nextPath : stopList1.getStops()) {

			RoutePathElement nextElement = new RoutePathElement();
			elements.add(nextElement);

			nextElement.setLatitude(nextPath.getLatitude());
			nextElement.setLongitude(nextPath.getLongitude());

			nextElement.setClosestStopTagDirection1(nextPath.getStopTag());

			String oppositeDirStopTag = stopList2.findClosestStopTag(nextPath.getLatitude(), nextPath.getLongitude());
			nextElement.setClosestStopTagDirection2(oppositeDirStopTag);

		} // for path

		/* Sometimes the list has a weird entry at the end */
		{
			RoutePathElement lastStop = elements.get(elements.size() - 1);
			RoutePathElement finalStop = elements.get(elements.size() - 2);
			double dist = Stop.distanceInKms(lastStop.getLatitude(), lastStop.getLongitude(), finalStop.getLongitude(), finalStop.getLatitude());
			if (dist > 1.0) {
				elements.remove(elements.size() - 1);
			}
		}

		{
			RoutePathElement lastStop = elements.get(elements.size() - 1);
			Stop finalStop = stopList2.getStops().get(0);
			double dist = finalStop.distanceFromInKms(lastStop.getLatitude(), lastStop.getLongitude());
			if (dist < (500.0 / 1000.0)) {

				RoutePathElement nextElement = new RoutePathElement();
				elements.add(nextElement);

				nextElement.setLatitude(finalStop.getLatitude());
				nextElement.setLongitude(finalStop.getLongitude());

				nextElement.setClosestStopTagDirection2(finalStop.getStopTag());

				String oppositeDirStopTag = stopList1.findClosestStopTag(finalStop.getLatitude(), finalStop.getLongitude());
				nextElement.setClosestStopTagDirection1(oppositeDirStopTag);

			}
		}

		{
			RoutePathElement firstStop = elements.get(0);
			Stop otherFirstStop = stopList2.getStops().get(stopList2.getStops().size() - 1);
			double dist = otherFirstStop.distanceFromInKms(firstStop.getLatitude(), firstStop.getLongitude());
			if (dist < (500.0 / 1000.0)) {

				RoutePathElement nextElement = new RoutePathElement();
				elements.add(0, nextElement);

				nextElement.setNewPathEntry(true);
				nextElement.setLatitude(otherFirstStop.getLatitude());
				nextElement.setLongitude(otherFirstStop.getLongitude());

				nextElement.setClosestStopTagDirection2(otherFirstStop.getStopTag());

				String oppositeDirStopTag = stopList1.findClosestStopTag(otherFirstStop.getLatitude(), otherFirstStop.getLongitude());
				nextElement.setClosestStopTagDirection1(oppositeDirStopTag);

			}
		}

		theRoutePath.setDirection1Tag(direction1);
		theRoutePath.setDirection2Tag(direction2);
		theRoutePath.setRoutePathElements(elements);

	}


	private void ensureRouteList() throws FailureException {
		if (myRouteList == null || myRouteList.isFullyLoaded() == false) {
			myRouteList = myNextbusFetcherService.loadRouteList();
		}
	}


	private Collection<PersistedIncident> getOpenIncidents(EntityManager theEntityManager) {

		Query query = theEntityManager.createNamedQuery("Incident.getOpenIncidents");
		@SuppressWarnings("unchecked")
		List<PersistedIncident> incidents = query.getResultList();
		return incidents;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, NumbersAndTimestamps> getRouteAverageSpeedsOverLast24Hours(Set<String> theRouteTags) throws FailureException {
		if (theRouteTags == null || theRouteTags.isEmpty()) {
			return new HashMap<String, NumbersAndTimestamps>();
		}

		HashMap<String, NumbersAndTimestamps> retVal = new HashMap<String, NumbersAndTimestamps>();

		// Try to retrieve from cache
		for (String nextRouteTag : theRouteTags) {
			@SuppressWarnings("unchecked")
			ObjectAndTimestamp<NumbersAndTimestamps> cacheVal = (ObjectAndTimestamp<NumbersAndTimestamps>) myCache.get(toCacheKeyForAverageSpeedOver24Hours(nextRouteTag));
			if (cacheVal != null && cacheVal.getTimestampAge() < CACHE_TIMEOUT_ROUTE_ACTIVE_VEHICLES) {
				retVal.put(nextRouteTag, cacheVal.getObject());
			}
		}

		if (retVal.size() < theRouteTags.size()) {
			EntityManager em = myEntityManagerFactory.createEntityManager();
			try {
				for (String nextRouteTag : theRouteTags) {
					if (retVal.containsKey(nextRouteTag) == false) {
						PersistedRoute route = em.find(PersistedRoute.class, nextRouteTag);
						if (route == null) {
							String msg = "Unknown route for vehicle counts: " + nextRouteTag;
							logger.log(Level.WARNING, msg);
							throw new FailureException(msg);
						}

						NumbersAndTimestamps vehiclesInService = route.getAverageRouteSpeed();
						retVal.put(nextRouteTag, vehiclesInService);

						// Store in cache
						ObjectAndTimestamp<NumbersAndTimestamps> cacheVal = new ObjectAndTimestamp<NumbersAndTimestamps>(vehiclesInService, getTime());
						myCache.put(toCacheKeyForAverageSpeedOver24Hours(nextRouteTag), cacheVal);

					}
				}
			} finally {
				em.close();
			}
		}

		return retVal;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoutePathString(String theRouteTag) throws FailureException {
		String key = ROUTE_PATH_PREFIX + theRouteTag;

		@SuppressWarnings("unchecked")
		ObjectAndTimestamp<String> retVal = (ObjectAndTimestamp<String>) myCache.get(key);

		if (retVal == null || retVal.getTimestampAge() > 60000) {

			EntityManager em = myEntityManagerFactory.createEntityManager();
			try {
				PersistedRoute route = em.find(PersistedRoute.class, theRouteTag);
				if (route == null) {
					String msg = "Invalid route: " + theRouteTag;
					logger.warning(msg);
					return null;
				}

				retVal = new ObjectAndTimestamp<String>(route.getRoutePathString(), getTime());
				myCache.put(key, retVal);

			} finally {
				em.close();
			}
		}

		return retVal.getObject();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, NumbersAndTimestamps> getRouteVehicleCountsOverLast24Hours(Set<String> theRouteTags) throws FailureException {
		if (theRouteTags == null || theRouteTags.isEmpty()) {
			return new HashMap<String, NumbersAndTimestamps>();
		}

		HashMap<String, NumbersAndTimestamps> retVal = new HashMap<String, NumbersAndTimestamps>();

		// Try to retrieve from cache
		for (String nextRouteTag : theRouteTags) {
			@SuppressWarnings("unchecked")
			ObjectAndTimestamp<NumbersAndTimestamps> cacheVal = (ObjectAndTimestamp<NumbersAndTimestamps>) myCache.get(toCacheKeyForVehicleCountsOver24Hours(nextRouteTag));
			if (cacheVal != null && cacheVal.getTimestampAge() < CACHE_TIMEOUT_ROUTE_ACTIVE_VEHICLES) {
				retVal.put(nextRouteTag, cacheVal.getObject());
			}
		}

		if (retVal.size() < theRouteTags.size()) {
			EntityManager em = myEntityManagerFactory.createEntityManager();
			try {
				for (String nextRouteTag : theRouteTags) {
					if (retVal.containsKey(nextRouteTag) == false) {
						PersistedRoute route = em.find(PersistedRoute.class, nextRouteTag);
						if (route == null) {
							String msg = "Unknown route for vehicle counts: " + nextRouteTag;
							logger.log(Level.WARNING, msg);
							throw new FailureException(msg);
						}

						NumbersAndTimestamps vehiclesInService = route.getVehiclesInService();
						retVal.put(nextRouteTag, vehiclesInService);

						// Store in cache
						ObjectAndTimestamp<NumbersAndTimestamps> cacheVal = new ObjectAndTimestamp<NumbersAndTimestamps>(vehiclesInService, getTime());
						myCache.put(toCacheKeyForVehicleCountsOver24Hours(nextRouteTag), cacheVal);

					}
				}
			} finally {
				em.close();
			}
		}

		return retVal;
	}


	private long getTime() {
		if (myHardcodedTime != null) {
			return myHardcodedTime;
		}
		return System.currentTimeMillis();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumbersAndTimestamps getVehicleAverageSpeed(String theRouteTag, String theVehicleTag) {
		long start = getTime();
		String key = VEHICLE_AVERAGE_SPEED_PREFIX + theVehicleTag;

		@SuppressWarnings("unchecked")
		ObjectAndTimestamp<String> cacheValue = (ObjectAndTimestamp<String>) myCache.get(key);
		if (cacheValue != null && cacheValue.getTimestampAge() < VEHICLE_AVERAGE_SPEED_CACHE_TIME) {
			String object = cacheValue.getObject();
			if (object != null && object.length() > 0) {
				return new NumbersAndTimestamps(object);
			} else {
				return null;
			}
		}

		NumbersAndTimestamps speeds = new NumbersAndTimestamps();

		EntityManager em = myEntityManagerFactory.createEntityManager();
		try {
			PersistedVehicle vehicle = null;
			PersistedRoute route = em.find(PersistedRoute.class, theRouteTag);
			if (route != null) {
				vehicle = route.getVehicle(theVehicleTag);
			}
			if (vehicle != null) {
				List<PersistedVehiclePosition> positions = vehicle.getPositions();
				for (PersistedVehiclePosition nextPosition : positions) {
					if (nextPosition == null || nextPosition.getSpeedInKmh() == null) {
						continue;
					}
					speeds.addNumber(new NumberAndTimestamp(nextPosition.getSpeedInKmh(), nextPosition.getTimestamp()));
				}
			}

			String marshall = speeds.isEmpty() ? null : speeds.marshall();
			cacheValue = new ObjectAndTimestamp<String>(marshall, getTime());
			myCache.put(key, cacheValue);

			long time = getTime() - start;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Loaded vehicle speeds in " + time + "ms");
			}
		} finally {
			em.close();
		}

		return speeds;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, SpeedAndTimestamp> getVehicleAverageSpeeds(String theRoute) {
		HashMap<String, SpeedAndTimestamp> retVal = new HashMap<String, SpeedAndTimestamp>();

		EntityManager em = myEntityManagerFactory.createEntityManager();
		try {

			PersistedRoute route = em.find(PersistedRoute.class, theRoute);
			if (route == null) {
				return retVal;
			}

			List<PersistedVehicle> vehicles = route.getVehicles();

			if (vehicles != null) {
				for (PersistedVehicle nextVehicle : vehicles) {

					PersistedVehiclePosition nextPosition = nextVehicle.getMostRecentPosition();
					Integer nextSpeed = nextPosition.getSpeedInKmh();
					long nextTimestamp = nextPosition.getTimestamp();

					if (nextSpeed != null) {
						SpeedAndTimestamp nextSat = new SpeedAndTimestamp(nextSpeed, nextPosition.getLatitude(), nextPosition.getLongitude(), nextTimestamp);
						retVal.put(nextVehicle.getVehicleTag(), nextSat);
					}

				}
			}

		} finally {
			em.close();
		}

		return retVal;
	}


	private void initializeRoute(Route nextRoute, PersistedRoute persistedRoute) throws FailureException {
		persistedRoute.setRouteTag(nextRoute.getTag());
		persistedRoute.setRouteEndpoints(new HashSet<GeoPt>());

		StopListForRoute stopLists = myNextbusFetcherService.getStopListForRoute(nextRoute.getTag());
		for (StopList stopList : stopLists.getUiStopLists()) {
			if (stopList.getStops() == null || stopList.getStops().isEmpty()) {
				continue;
			}

			Stop lastStop = stopList.getStops().get(stopList.getStops().size() - 1);
			persistedRoute.getRouteEndpoints().add(new GeoPt((float) lastStop.getLatitude(), (float) lastStop.getLongitude()));

		}
	}


	private void mergeDirtyIncidents(Collection<PersistedIncident> theOpenIncidents) {
		for (PersistedIncident nextIncident : theOpenIncidents) {
			if (nextIncident.isDirtyFlagSet()) {
				EntityManager em = myEntityManagerFactory.createEntityManager();
				try {
					if (nextIncident.getKey() == null) {
						em.merge(nextIncident);
					} else {
						PersistedIncident newIncident = em.find(PersistedIncident.class, nextIncident.getKey());
						newIncident.copyValues(nextIncident);
						em.merge(newIncident);
					}
					nextIncident.clearDirtyFlag();
				} finally {
					em.close();
				}
			}
		}
	}


//	private PersistedIncident openNotMovingIncident(PersistedRoute theRoute, PersistedVehicle nextVehicle, PersistedVehiclePosition theBeginPosition,
//			PersistedVehiclePosition theCurrentPosition) throws FailureException {
//		EntityManager theEm;
//		theEm = myEntityManagerFactory.createEntityManager();
//		EntityTransaction transaction = theEm.getTransaction();
//		PersistedIncident incident;
//		try {
//			transaction.begin();
//
//			incident = new PersistedIncident();
//			incident.setIncidentType(IncidentTypeEnum.NOT_MOVING);
//			incident.setRoute(theRoute.getRouteTag());
//			incident.setStartDate(new Date());
//			incident.setVehicle(nextVehicle.getVehicleTag());
//			incident.setStartingLocation(new GeoPt((float) theCurrentPosition.getLatitude(), (float) theCurrentPosition.getLongitude()));
//			incident.setDirectionTag(theCurrentPosition.getDirectionTag());
//
//			StopList stopList = myNextbusFetcherService.loadDirectionTagToStopList(theRoute.getRouteTag()).get(theBeginPosition.getDirectionTag());
//			String nearestStop = stopList.findClosestStopTag(theBeginPosition.getLatitude(), theBeginPosition.getLongitude());
//			incident.setBeginNearestStopTag(nearestStop);
//
//			incident = theEm.merge(incident);
//			transaction.commit();
//
//		} finally {
//			if (transaction.isActive()) {
//				transaction.rollback();
//			}
//			theEm.close();
//		}
//		return incident;
//	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String purgeIncidents() throws FailureException {
		myIntervalEnforcer.newCall("purgeIncidents");

		long startTime = System.currentTimeMillis();
		EntityManager entityManager = myEntityManagerFactory.createEntityManager();
		try {

			Query query = entityManager.createQuery("DELETE FROM ca.wimsc.server.jpa.PersistedIncident WHERE myStartDate < :cutoff");
			Date cutoff = new Date(getTime() - INCIDENT_RETENTION_MILLIS);
			query.setParameter("cutoff", cutoff, TemporalType.DATE);
			int results = query.executeUpdate();

			long time = System.currentTimeMillis() - startTime;
			String message = "Purged " + results + " incidents in " + time + "ms";
			logger.info(message);

			return message;

		} finally {
			entityManager.close();
		}
	}


	/**
	 * FOR UNIT TESTS ONLY!
	 */
	void setHardcodeTime(long theTime) {
		myHardcodedTime = theTime;
	}


	void setNextbusFetcherService(INextbusFetcherService theNextbusFetcherService) {
		myNextbusFetcherService = theNextbusFetcherService;
	}


	private Object toCacheKeyForAverageSpeedOver24Hours(String theRouteTag) {
		return CACHE_PREFIX_ROUTE_AVERAGE_SPEEDS + theRouteTag;
	}


	private String toCacheKeyForVehicleCountsOver24Hours(String theRouteTag) {
		return CACHE_PREFIX_ROUTE_ACTIVE_VEHICLES + theRouteTag;
	}


	private void updateRoute(PersistedRoute thePersistedRoute, long theNowTime) throws FailureException {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Loading info on route: " + thePersistedRoute.getRouteTag());
		}

		StreetcarLocationList locations = myNextbusFetcherService.loadStreetcarLocations(thePersistedRoute.getRouteTag(), false, false);

//		/*
//		 * Check if any open incidents refer to vehicles which have disappeared
//		 */
//		for (PersistedIncident next : theOpenIncidents) {
//			switch (next.getIncidentType()) {
//			case NOT_MOVING:
//				if (next.getRoute().equals(thePersistedRoute.getRouteTag())) {
//					if (locations.getLocation(next.getVehicle()) == null) {
//						logger.info("Vehicle " + next.getVehicle() + " is no longer on route " + thePersistedRoute.getRouteTag() + ", closing incident " + next.getKey());
//						closeIncident(next, null);
//					}
//				}
//				break;
//			default:
//				// nothing
//			}
//		}

		// Round time to the nearest 30 minute interval
		Calendar nearestHalfHourCalendar = Calendar.getInstance();
		nearestHalfHourCalendar.set(Calendar.SECOND, 0);
		nearestHalfHourCalendar.set(Calendar.MILLISECOND, 0);
		if (nearestHalfHourCalendar.get(Calendar.MINUTE) < 30) {
			nearestHalfHourCalendar.set(Calendar.MINUTE, 0);
		} else {
			nearestHalfHourCalendar.set(Calendar.MINUTE, 30);
		}

		// Check how many active
		Date halfAnHourAgo = new Date(theNowTime - DateUtil.HALF_AN_HOUR_IN_MILLIS);
		NumbersAndTimestamps vehiclesInService = thePersistedRoute.getVehiclesInService();
		NumberAndTimestamp newestTimestamp = vehiclesInService.getNewest();
		if (newestTimestamp == null || newestTimestamp.getTimestamp().getTime() <= halfAnHourAgo.getTime()) {

			// Check if we need to quantize the collection
			// TODO: this can go away soon enough I suppose
			if (vehiclesInService.isEmpty() == false) {
				Calendar time = Calendar.getInstance();
				for (int i = 0; i < vehiclesInService.getValues().size(); i++) {
					NumberAndTimestamp next = vehiclesInService.getValues().get(i);

					time.setTime(next.getTimestamp());
					if (time.get(Calendar.MILLISECOND) == 0 && time.get(Calendar.SECOND) == 0) {
						break;
					}

					time.set(Calendar.SECOND, 0);
					time.set(Calendar.MILLISECOND, 0);
					if (time.get(Calendar.MINUTE) < 30) {
						time.set(Calendar.MINUTE, 0);
					} else {
						time.set(Calendar.MINUTE, 30);
					}
					next.setTimestamp(time.getTime());

				}
			}

			vehiclesInService.addNumber(new NumberAndTimestamp(locations.getLocations().size(), nearestHalfHourCalendar.getTime()), 48);

			thePersistedRoute.setVehiclesInService(vehiclesInService);
		}

		// Check how far they have gone
		List<PersistedVehicle> vehicles = new ArrayList<PersistedVehicle>();
		for (StreetcarLocation nextLocation : locations.getLocations()) {

			String vehicleTag = nextLocation.getVehicleTag();

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Looking at vehicle tag: " + vehicleTag);
			}

			PersistedVehicle persistedVehicle = thePersistedRoute.getVehicle(vehicleTag);
			if (persistedVehicle == null) {
				persistedVehicle = new PersistedVehicle();
				persistedVehicle.setVehicleTag(nextLocation.getVehicleTag());
			}
			vehicles.add(persistedVehicle);

			List<PersistedVehiclePosition> positions = persistedVehicle.getPositions();
			PersistedVehiclePosition newFirstPosition = new PersistedVehiclePosition();
			positions.add(newFirstPosition);

			newFirstPosition.setTimestamp(theNowTime);
			newFirstPosition.setDirectionTag(nextLocation.getDirectionTag());
			newFirstPosition.setLatitude(nextLocation.getLatitude());
			newFirstPosition.setLongitude(nextLocation.getLongitude());
			newFirstPosition.setDirectionTag(nextLocation.getDirectionTag());
			newFirstPosition.setRouteTag(thePersistedRoute.getRouteTag());
			newFirstPosition.setClosestStopTag(nextLocation.getClosestStopTag());

			// Check if we're at the start/end of the route
			for (GeoPt nextEndpoint : thePersistedRoute.getRouteEndpoints()) {
				long distance = nextLocation.distanceFromInMeters(nextEndpoint.getLatitude(), nextEndpoint.getLongitude());
				if (distance < 200) {
					newFirstPosition.setAtExtremity(true);
					break;
				}
			}
			persistedVehicle.setAtExtremity(newFirstPosition.isAtExtremity());

			if (positions.size() > 1) {
				PersistedVehiclePosition oldFirstPosition = positions.get(positions.size() - 2);
				double oldLatitude = oldFirstPosition.getLatitude();
				double oldLongitude = oldFirstPosition.getLongitude();
				double newLongitude = newFirstPosition.getLongitude();
				double newLatitude = newFirstPosition.getLatitude();
				long time = theNowTime - oldFirstPosition.getTimestamp();

				int speedInKmhInt;
				if (time > 0) {
					speedInKmhInt = calculateSpeed(oldLatitude, oldLongitude, newLatitude, newLongitude, time);
				} else {
					speedInKmhInt = 0;
				}

				newFirstPosition.setSpeedInKmh(speedInKmhInt);

			} else {

				if (logger.isLoggable(Level.FINE)) {
					logger.fine("No previous position");
				}

			}

			persistedVehicle.setPositions(positions);

		}

		// Update average route speed
		NumbersAndTimestamps averageRouteSpeed = thePersistedRoute.getAverageRouteSpeed();
		if (averageRouteSpeed == null || averageRouteSpeed.isEmpty() || averageRouteSpeed.getNewest().getTimestampAge() >= DateUtil.HALF_AN_HOUR_IN_MILLIS) {
			int total = 0;
			int count = 0;
			for (PersistedVehicle persistedVehicle : vehicles) {
				PersistedVehiclePosition mostRecentPosition = persistedVehicle.getMostRecentPosition();
				if (mostRecentPosition == null) {
					continue;
				}
				if (mostRecentPosition.getDirectionTag() == null || mostRecentPosition.getDirectionTag().equals("null")) {
					continue;
				}
				Integer speed = mostRecentPosition.getSpeedInKmh();
				if (speed == null || speed.intValue() == 0) {
					continue;
				}

				total += speed;
				count++;
			}

			int averageSpeed = (count > 0) ? (total / count) : 0;
			averageRouteSpeed.addNumber(new NumberAndTimestamp(averageSpeed, nearestHalfHourCalendar.getTime()), 48);
			thePersistedRoute.setAverageRouteSpeed(averageRouteSpeed);
		}

		// Check if any new diversions, not-moving etc incidents have popped up
		// checkForNewOrUpdateIncidentsWithVehicles(thePersistedRoute, vehicles, theOpenIncidents);

		// Update route path
		updateRoutePath(thePersistedRoute, false);

		thePersistedRoute.setVehicles(vehicles);
	}


	private void updateRoutePath(PersistedRoute thePersistedRoute, boolean theForceUpdateRoutePaths) throws FailureException {

		RoutePath routePath = thePersistedRoute.getRoutePath();

		StopListForRoute stopListForRoute = myNextbusFetcherService.getStopListForRoute(thePersistedRoute.getRouteTag());

		// Once a day, wipe and recreate the route path entries
		if (theForceUpdateRoutePaths || !DateUtils.isSameDay(routePath.getLastUpdatedTimestamp(), new Date())) {

			if (thePersistedRoute.getRouteTag().equals("XXX")) {
//				createRoutePathUsingBackendPath(thePersistedRoute, routePath, stopListForRoute);
			} else {
				createRoutePathUsingStops(routePath, stopListForRoute);
			}

		}

		List<StopList> stopLists = new ArrayList<StopList>();
		for (String next : routePath.getDirectionTags()) {
			stopLists.add(stopListForRoute.getUiOrNonUiStopListForDirectionTag(next));
		}

		// Figure out which vehicles passed by which stops in their most recent movement
		List<PersistedVehicle> vehicles = thePersistedRoute.getVehicles();
		for (PersistedVehicle nextVehicle : vehicles) {
			if (nextVehicle.getPositions().size() < 2) {
				continue;
			}

			PersistedVehiclePosition previousPosition = nextVehicle.getPositions().get(nextVehicle.getPositions().size() - 2);
			PersistedVehiclePosition currentPosition = nextVehicle.getPositions().get(nextVehicle.getPositions().size() - 1);
			if (!previousPosition.getDirectionTag().equals(currentPosition.getDirectionTag())) {
				continue;
			}

			if (currentPosition.getSpeedInKmh() == null) {
				continue;
			}

			String directionTag = currentPosition.getDirectionTag();
			directionTag = stopListForRoute.getUiDirectionTagEquivalentToDirecitonTag(directionTag);
			int directionIndex = routePath.getDirectionTags().indexOf(directionTag);
			if (directionIndex == -1) {
				continue;
			}

			StopList stopList = stopLists.get(directionIndex);

			int previousStopTagIndex = stopList.getStopTagIndex(previousPosition.getClosestStopTag());
			int currentStopTagIndex = stopList.getStopTagIndex(currentPosition.getClosestStopTag());
			if (previousStopTagIndex == -1 || currentStopTagIndex == -1) {
				continue;
			}

			for (int stopTagIndex = previousStopTagIndex; stopTagIndex <= currentStopTagIndex; stopTagIndex++) {

				String nextStopTag = stopList.getStops().get(stopTagIndex).getStopTag();
				RoutePathElement element = routePath.getRoutePathElementClosestToStopTag(directionIndex, nextStopTag);
				if (element == null) {
					continue;
				}

				int currentSpeed = element.getSpeedInKmhDirection1(directionIndex);
				if (currentSpeed == RoutePathElement.DEFAULT_SPEED) {
					currentSpeed = currentPosition.getSpeedInKmh();
				} else {
					currentSpeed = (currentSpeed + currentPosition.getSpeedInKmh()) / 2;
				}

				element.setSpeedInKmhDirection(directionIndex, currentSpeed);
			}
		}

		routePath.setLastUpdatedTimestamp(new Date());

		routePath.calculateRouteHeadings();
		thePersistedRoute.setRoutePath(routePath);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateRoutePaths() throws FailureException {
		long time = System.currentTimeMillis();
		ensureRouteList();

		myIntervalEnforcer.newCall("updateRoutePaths");
		logger.info("Updating route paths");

		EntityManager routeEntityManager = myEntityManagerFactory.createEntityManager();
		try {

			for (Route nextRoute : myRouteList.getList()) {

				PersistedRoute persistedRoute = routeEntityManager.find(PersistedRoute.class, nextRoute.getTag());
				if (persistedRoute != null) {
					logger.info("Updating route path for " + nextRoute.getTag());
					updateRoutePath(persistedRoute, true);
					routeEntityManager.merge(persistedRoute);
				}

			}
		} finally {
			routeEntityManager.close();
		}

		time = System.currentTimeMillis() - time;
		return "Success in " + time + "ms";
	}


	@Override
	public String updateVehicles() throws FailureException {
		long start = getTime();

		ensureRouteList();

		myIntervalEnforcer.newCall("updateVehicles");

		EntityManager routeEntityManager = myEntityManagerFactory.createEntityManager();
		String msg;
		try {

//			List<PersistedIncident> openIncidents = new ArrayList<PersistedIncident>(getOpenIncidents(routeEntityManager));

			long now = 0;
			int routeCount = 0;
			for (Route nextRoute : myRouteList.getList()) {
				
				if (!nextRoute.getTag().matches("5[0-9][0-9]")) {
					continue;
				}
				
				now = getTime();

				// GAE only allows a process to run for 30 seconds
				if (start + 20000 < now) {
					msg = "Stopped to prevent exceeding 30 seconds. Ran for " + (now - start) + "ms, and updated " + routeCount;
					logger.info(msg);
					break;
				}

				PersistedRoute persistedRoute = routeEntityManager.find(PersistedRoute.class, nextRoute.getTag());
				if (persistedRoute == null) {
					persistedRoute = new PersistedRoute();
					initializeRoute(nextRoute, persistedRoute);
				}

				long cutoff = now - INTERVAL;
				if (cutoff >= persistedRoute.getLastVehicleRefreshTimestamp() || persistedRoute.getRoutePathString() == null) {
					updateRoute(persistedRoute, now);
					persistedRoute.setLastVehicleRefreshTimestamp(now);
					routeEntityManager.merge(persistedRoute);
					routeCount++;
				} else {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Skipping route " + nextRoute.getTag() + ", still need to wait: " + (persistedRoute.getLastVehicleRefreshTimestamp() - cutoff));
					}
				}

			}

			msg = "Finished updating all. Ran for " + (now - start) + "ms, and updated " + routeCount;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(msg);
			}

		} finally {
			routeEntityManager.close();
		}

		return msg;
	}


	/**
	 * Given two sets of coords and a time period, calculate the speed in km/h
	 */
	public static int calculateSpeed(double theOldLatitude, double theOldLongitude, double theNewLongitude, double theNewLatitude, long theMillisElapsed) {
		double distanceTravelled = Stop.distanceInKms(theOldLatitude, theOldLongitude, theNewLatitude, theNewLongitude);
		double speedInKmh = distanceTravelled * ((60 * 60 * 1000) / theMillisElapsed);
		int speedInKmhInt = (int) Math.floor(speedInKmh);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Distance travelled: " + distanceTravelled + " - Speed: " + speedInKmh);
		}
		return speedInKmhInt;
	}


	private static boolean pathOverlapsExisting(Path theNextPath, List<RoutePathElement> theElements) {

		Point firstPoint = theNextPath.getPoint().get(0);
		Point lastPoint = theNextPath.getPoint().get(theNextPath.getPoint().size() - 1);

		double lat1 = firstPoint.getLat();
		double lon1 = firstPoint.getLon();
		double lon2 = lastPoint.getLon();
		double lat2 = lastPoint.getLat();

		double distance = Stop.distanceInKms(lat1, lon1, lon2, lat2);
		double minDist = 100.0 / 1000.0;
		if (distance <= minDist) {
			minDist = distance * 0.9;
		}

		if (pathOverlapsExisting(firstPoint, theElements, minDist) && pathOverlapsExisting(lastPoint, theElements, minDist)) {
			return true;
		}
		return false;
	}


	private static boolean pathOverlapsExisting(Point theLastPoint, List<RoutePathElement> theElements, double minDist) {
		for (RoutePathElement routePathElement : theElements) {
			double lat1 = theLastPoint.getLat();
			double lon1 = theLastPoint.getLon();
			double lon2 = routePathElement.getLongitude();
			double lat2 = routePathElement.getLatitude();
			double distanceInKms = Stop.distanceInKms(lat1, lon1, lon2, lat2);
			if (distanceInKms < minDist) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Detects if a method has been invoked too recently and throws an exception if so
	 * 
	 * Be default, allows a call every 30 seconds
	 */
	private class IntervalEnforcer {
		private Map<String, Long> myLastCalls = new HashMap<String, Long>();


		public void newCall(String theMethodName) throws FailureException {
			// Prevent abusive/repeated calls to this
			long start = getTime();

			Long lastCall = myLastCalls.get(theMethodName);
			if ((lastCall != null) && ((lastCall + (30 * 1000)) > start)) {
				throw new FailureException("Too soon!");
			}
			lastCall = start;
			myLastCalls.put(theMethodName, lastCall);

		}
	}

}
