package ca.wimsc.server.svc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.jpa.StopQuadrant;
import ca.wimsc.server.jpa.StopQuadrantList;
import ca.wimsc.server.svc.IActivityMonitorService.SpeedAndTimestamp;
import ca.wimsc.server.util.StopComparatorFactory;
import ca.wimsc.server.xml.loc.Body.Vehicle;
import ca.wimsc.server.xml.route.Body;
import ca.wimsc.server.xml.route.Body.Route.Direction;
import ca.wimsc.server.xml.route.Body.Route.Stop;
import ca.wimsc.server.xml.stops.Body.Predictions;
import ca.wimsc.server.xml.stops.Body.Predictions.Direction.Prediction;

public class NextbusFetcherServiceImpl implements INextbusFetcherService {

	private static final String BASE_MULTISTOPS = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=ttc";
	/**
	 * Google's cache freaks out if the serial UID of something in it changes, so increment this if any cached objects
	 * get a new UID.
	 */
	private static final int CACHE_KEY_VERSION_ID = 13;
	private static final Logger ourLog = Logger.getLogger(NextbusFetcherServiceImpl.class.getName());

	private IActivityMonitorService myActivityMonitorService;
	private Cache myCache;
	private RouteList myRouteList;


	/**
	 * Standard constructor
	 */
	NextbusFetcherServiceImpl() throws IOException {
		try {
			myCache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		} catch (CacheException e) {
			throw new IOException(e);
		}

	}


	/**
	 * NOT FOR PROD USE
	 */
	public NextbusFetcherServiceImpl(Cache theCache) {
		myCache = theCache;
	}


	private Map<String, ca.wimsc.client.common.model.Stop> createStopTagToStopMapForRouteConfig(Body rc) {
		Map<String, ca.wimsc.client.common.model.Stop> allStops = new HashMap<String, ca.wimsc.client.common.model.Stop>();
		for (Stop next : rc.getRoute().getStop()) {
			allStops.put(next.getTag(), toModelStop(next));
		}
		return allStops;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.wimsc.server.svc.INextbusFetcherService#loadAllStopQuadrants()
	 */
	@Override
	public StopQuadrantList loadAllStopQuadrants() throws FailureException, MalformedURLException, IOException {

		String key = getCacheKeyForStopQuadrantList();

		StopQuadrantList retVal = (StopQuadrantList) myCache.get(key);
		if (retVal == null) {
			List<List<StopQuadrant>> sq = loadAllStopQuadrants(20);
			retVal = new StopQuadrantList();
			retVal.setStopQuadrants(sq);
			myCache.put(key, retVal);
		}

		return retVal;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.wimsc.server.svc.INextbusFetcherService#loadAllStopQuadrants(int)
	 */
	@Override
	public List<List<StopQuadrant>> loadAllStopQuadrants(int theNumDivisions) throws FailureException, MalformedURLException, IOException {
		double latMin = Double.MAX_VALUE;
		double lonMin = Double.MAX_VALUE;
		double latMax = -Double.MAX_VALUE;
		double lonMax = -Double.MAX_VALUE;

		Map<String, List<Body.Route.Stop>> allStops = new HashMap<String, List<Body.Route.Stop>>();

		RouteList routeList = loadRouteList();
		for (ca.wimsc.client.common.model.Route nextRoute : routeList.getList()) {
			allStops.put(nextRoute.getTag(), new ArrayList<ca.wimsc.server.xml.route.Body.Route.Stop>());

			ca.wimsc.server.xml.route.Body.Route routeConfig = loadRouteConfig(nextRoute.getTag()).getRoute();
			for (ca.wimsc.server.xml.route.Body.Route.Stop nextStop : routeConfig.getStop()) {

				double latitude = nextStop.getLat();
				double longitude = nextStop.getLon();
				if (latitude < latMin) {
					latMin = latitude;
				}
				if (longitude < lonMin) {
					lonMin = longitude;
				}
				if (latitude > latMax) {
					latMax = latitude;
				}
				if (longitude > lonMax) {
					lonMax = longitude;
				}

				allStops.get(nextRoute.getTag()).add(nextStop);
			}
		}

		double latInc = (latMax - latMin) / theNumDivisions;
		double lonInc = (lonMax - lonMin) / theNumDivisions;

		List<List<StopQuadrant>> quadrants = new ArrayList<List<StopQuadrant>>();
		List<StopQuadrant> allQuadrants = new ArrayList<StopQuadrant>();

		for (double nextLatitude = latMin; nextLatitude < latMax; nextLatitude += latInc) {

			ArrayList<StopQuadrant> nextQuadList = new ArrayList<StopQuadrant>();
			quadrants.add(nextQuadList);

			for (double nextLongitude = lonMin; nextLongitude < lonMax; nextLongitude += lonInc) {

				StopQuadrant quadrant = new StopQuadrant();
				quadrant.setLatMin(nextLatitude);
				quadrant.setLonMin(nextLongitude);
				quadrant.setLatMax(nextLatitude + latInc);
				quadrant.setLonMax(nextLongitude + lonInc);
				quadrant.setStopKeys(new ArrayList<String>());

				allQuadrants.add(quadrant);
				nextQuadList.add(quadrant);

				for (Map.Entry<String, List<ca.wimsc.server.xml.route.Body.Route.Stop>> nextEntries : allStops.entrySet()) {

					String nextRoute = nextEntries.getKey();
					List<ca.wimsc.server.xml.route.Body.Route.Stop> nextStops = nextEntries.getValue();

					for (ca.wimsc.server.xml.route.Body.Route.Stop nextStop : nextStops) {

						boolean matches = (nextStop.getLat() >= quadrant.getLatMin());
						matches &= (nextStop.getLat() < quadrant.getLatMax());
						matches &= (nextStop.getLon() >= quadrant.getLonMin());
						matches &= (nextStop.getLon() < quadrant.getLonMax());

						if (matches) {
							quadrant.getStopKeys().add(StopQuadrant.toStopKey(nextRoute, nextStop.getTag(), nextStop.getLat(), nextStop.getLon()));
						}

					}
				}

			}
		}

		int maxStops = 0;
		for (StopQuadrant stopQuadrant : allQuadrants) {
			if (stopQuadrant.getStopKeys().size() > maxStops) {
				maxStops = stopQuadrant.getStopKeys().size();
			}
		}

		ourLog.info("Max stops in a single quadrant: " + maxStops);
		return quadrants;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public StopListForRoute getStopListForRoute(String theRouteTag) throws FailureException {

		String key = getCacheKeyForStopListForRoute(theRouteTag);
		StopListForRoute retVal = (StopListForRoute) myCache.get(key);
		if (retVal != null) {
			return retVal;
		}

		Body rc = loadRouteConfig(theRouteTag);
		Map<String, ca.wimsc.client.common.model.Stop> allStops = createStopTagToStopMapForRouteConfig(rc);

		retVal = new StopListForRoute(theRouteTag);
		retVal.setStopTagToStop(allStops);

		// Sort route into stops
		for (Direction next : rc.getRoute().getDirection()) {

			StopList list = new StopList();
			list.setTag(next.getTag());
			list.setTitle(next.getTitle());
			list.setName(next.getName());

			ArrayList<String> stopTagList = new ArrayList<String>();
			ArrayList<ca.wimsc.client.common.model.Stop> stopList = new ArrayList<ca.wimsc.client.common.model.Stop>();

			for (ca.wimsc.server.xml.route.Body.Route.Direction.Stop nextDirectionStop : next.getStop()) {
				stopTagList.add(nextDirectionStop.getTag());
				stopList.add(allStops.get(nextDirectionStop.getTag()));
			}

			if (stopList.size() == 0) {
				continue;
			}

			/*
			 * Make sure that the stops in the list are ordered from earliest to latest according to direction
			 */
			int earlyIndex = stopList.size() / 4;
			int lateIndex = earlyIndex * 3;
			if (lateIndex > stopList.size() - 1) {
				lateIndex = stopList.size() - 1;
			}
			ca.wimsc.client.common.model.Stop earlyStop = stopList.get(earlyIndex);
			ca.wimsc.client.common.model.Stop lateStop = stopList.get(lateIndex);

			String directionName = next.getName();
			DirectionEnum nextDir = DirectionEnum.fromNameOrTitle(directionName);
			if (StopComparatorFactory.getComparator(nextDir).compare(earlyStop, lateStop) < 0) {
				Collections.reverse(stopTagList);
			}

			// And we're done, add the stops

			list.setStopTags(stopTagList);

			if (Boolean.TRUE.equals(next.isUseForUI())) {
				retVal.addUiStopList(list);
			} else {
				retVal.addNonUiStopList(list);
			}

		}

		/*
		 * Check if the non-ui stop lists are diversions or short turns
		 */
		for (StopList nextNonUi : retVal.getNonUiStopLists()) {
			StopList nextUi = retVal.getUiStopListWithMatchingName(nextNonUi.getName());
			if (nextUi == null) {
				ourLog.info("No UI StopList matching name: " + nextNonUi.getName());
				continue;
			}

			List<String> nonUiStopTags = nextNonUi.getStopTags();
			List<String> uiStopTags = nextUi.getStopTags();

			// Stop this loop at the second-last stop, since the last stop on a short turn could
			// be a turnaround loop
			for (int nonUiIdx = 1, uiIdx = 0; nonUiIdx < (nonUiStopTags.size() - 1); nonUiIdx++) {

				String nonUi = nonUiStopTags.get(nonUiIdx);

				while (uiIdx < uiStopTags.size()) {
					String ui = uiStopTags.get(uiIdx);
					if (ui.equals(nonUi)) {
						break; // while
					}
					uiIdx++;
				}

				if (uiIdx == uiStopTags.size()) {
					break; // for non-ui
				}

				if (nonUiIdx == nonUiStopTags.size() - 2) {
					if (uiIdx < (uiStopTags.size() - 2)) {
						nextNonUi.setShortTurn(true);
					}
				}

			}

		}

		myCache.put(key, retVal);

		return retVal;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.wimsc.server.svc.INextbusFetcherService#loadPredictions(java.lang.String, java.lang.String)
	 */
	@Override
	public PredictionsList loadPredictions(String theRoute, String theStop) throws FailureException {
		try {

			String key = getCacheKeyForPredictionsList(theRoute, theStop);
			PredictionsList retVal = (PredictionsList) myCache.get(key);
			if (retVal != null && retVal.getTimestamp() != null && retVal.getTimestamp().getTime() >= (System.currentTimeMillis() - (60 * 1000))) {
				ourLog.fine("Returning cached values for route " + theRoute + " (added " + retVal.getTimestamp() + ")");
				return retVal;
			}

			long startTime = System.currentTimeMillis();
			retVal = new PredictionsList();

			StreetcarLocationList locs = loadStreetcarLocations(theRoute, false, false);

			String previousVehicleLocationStopTag = null;

			// Find where our current stop is
			StopListForRoute directionsToStopLists = getStopListForRoute(theRoute);
			for (StopList nextStopList : directionsToStopLists.getUiStopLists()) {

				List<ca.wimsc.client.common.model.Stop> nextStops = nextStopList.getStops();
				boolean found = false;
				for (int i = 0; i < nextStops.size(); i++) {
					ca.wimsc.client.common.model.Stop nextStop = nextStops.get(i);
					if (nextStop.getStopTag().equals(theStop)) {
						retVal.setStopLatitude(nextStop.getLatitude());
						retVal.setStopLongitude(nextStop.getLongitude());

						/*
						 * Once we have found the stop we are looking for, look for the next stop after it with a
						 * predicted vehicle. This can be used to determine approximately how long the wait has been at
						 * the current stop.
						 */
						for (int j = i + 1; j < nextStops.size(); j++) {
							ca.wimsc.client.common.model.Stop nextPossibleStop = nextStops.get(j);
							if (locs.isStopTagHasVehicleNearby(nextPossibleStop.getStopTag())) {

								// Try to go one stop further, since technically it's possible that
								// a vehicle is closest to a given stop despite having
								// already passed that stop
								if (j + 1 < nextStops.size()) {
									previousVehicleLocationStopTag = nextStops.get(j + 1).getStopTag();
								} else {
									previousVehicleLocationStopTag = nextStops.get(j).getStopTag();
								}
								break;
							}

						}

						found = true;
						break;
					}

				}

				if (found) {
					break;
				}
			}

			StringBuilder url = new StringBuilder(BASE_MULTISTOPS);
			url.append("&stops=");
			url.append(theRoute);
			url.append("%7C");
			url.append(theStop);

			if (previousVehicleLocationStopTag != null) {
				url.append("&stops=");
				url.append(theRoute);
				url.append("%7C");
				url.append(previousVehicleLocationStopTag);
			}

			URL stopsUrl = new URL(url.toString());
			BufferedReader content = new BufferedReader(new InputStreamReader(stopsUrl.openStream()));
			ca.wimsc.server.xml.stops.Body loadedStopsBody = JAXB.unmarshal(content, ca.wimsc.server.xml.stops.Body.class);

			Predictions backendPrediction = null;
			Predictions previousVehicleBackendPrediction = null;

			for (Predictions next : loadedStopsBody.getPredictions()) {
				if (next.getStopTag().equals(theStop)) {
					backendPrediction = next;					
				} else if (next.getStopTag().equals(previousVehicleLocationStopTag)) {
					previousVehicleBackendPrediction = next;
				}
			}

			List<ca.wimsc.client.common.model.Prediction> predictions = new ArrayList<ca.wimsc.client.common.model.Prediction>();
			if (backendPrediction != null && backendPrediction.getDirection() != null) {

				for (ca.wimsc.server.xml.stops.Body.Predictions.Direction nextDirection : backendPrediction.getDirection()) {
					for (Prediction nextBackendPred : nextDirection.getPrediction()) {

						// Create UI model prediction
						ca.wimsc.client.common.model.Prediction prediction = new ca.wimsc.client.common.model.Prediction();
						prediction.setSeconds(nextBackendPred.getSeconds().intValue());

						String vehicle = nextBackendPred.getVehicle();
						if (vehicle != null) {

							/*
							 * Look for the predicted vehicle in the list of all vehicles to determine the location,
							 * speed, etc.
							 */
							for (StreetcarLocation nextLocation : locs.getLocations()) {

								if (vehicle.equals(nextLocation.getVehicleTag())) {

									prediction.setVehicleId(nextLocation.getVehicleTag());
									prediction.setVehicleDirectionTag(nextLocation.getDirectionTag());
									prediction.setCurrentSpeed(nextLocation.getCurrentSpeed());

									String closestStopTag = nextLocation.getClosestStopTag();
									prediction.setClosestStopTag(closestStopTag);

									// We found the matching vehicle
									break;
								}

							}

							prediction.setSpeed(myActivityMonitorService.getVehicleAverageSpeed(theRoute, vehicle));

							/*
							 * If this is the first prediction, also try to determine the headway by findind the
							 * predicted vehicle at the next stop where there is a vehicle predicted.
							 */
							if (predictions.isEmpty()) {
								if (previousVehicleBackendPrediction != null && previousVehicleBackendPrediction.getDirection() != null) {
									BOTH:
									for (ca.wimsc.server.xml.stops.Body.Predictions.Direction direction : previousVehicleBackendPrediction.getDirection()) {
										if (direction != null && direction.getPrediction() != null) {
											for (Prediction next : direction.getPrediction()) {
												if (next.getVehicle().equals(prediction.getVehicleId())) {
													prediction.setHeadway(next.getSeconds().intValue());
													break BOTH;
												}
											}
										}
									}
								}
							}

						}

						predictions.add(prediction);

					} // for predictions in direction

				} // for directions in route

			}

			// Sort
			Collections.sort(predictions, new Comparator<ca.wimsc.client.common.model.Prediction>() {
				@Override
				public int compare(ca.wimsc.client.common.model.Prediction theO1, ca.wimsc.client.common.model.Prediction theO2) {
					return theO1.getSeconds() - theO2.getSeconds();
				}
			});
			
			
			retVal.setPredictions(predictions);

			RouteList routeList = loadRouteList();
			
			retVal.setRouteTag(theRoute);
			Route route = routeList.getRoute(theRoute);
			retVal.setRouteTitle(route != null ? route.getTitle() : theRoute);
			retVal.setStopTag(theStop);

			long delay = System.currentTimeMillis() - startTime;
			ourLog.info("Loaded predictions for stop " + theStop + " on route " + theRoute + " in " + delay + "ms");

			retVal.setTimestamp(new Date());
			
			if (route != null) {
				myCache.put(key, retVal);
			} else {
				ourLog.warning("Couldn't find route " + theRoute + " in routeList, so not going to cache");
			}

			return retVal;

		} catch (IOException e) {
			ourLog.log(Level.WARNING, "Failed to load stop list", e);
			throw new FailureException();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Body loadRouteConfig(String theRoute) throws FailureException {
		Body route = (Body) myCache.get(getCacheKeyForRouteConfig(theRoute));
		if (route == null) {

			try {
				route = loadRouteConfigWithoutCache(theRoute);
			} catch (MalformedURLException e) {
				ourLog.log(Level.SEVERE, "Failed to load route config!", e);
				throw new FailureException(e.toString());
			} catch (IOException e) {
				ourLog.log(Level.SEVERE, "Failed to load route config!", e);
				throw new FailureException(e.toString());
			}

			myCache.put(getCacheKeyForRouteConfig(theRoute), route);

		}
		return route;
	}


	private Body loadRouteConfigWithoutCache(String theRoute) throws MalformedURLException, IOException {
		Body route;
		long startTime = System.currentTimeMillis();
		URL routeUrl = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=ttc&r=" + theRoute + "&verbose");
		InputStream content = routeUrl.openStream();
		route = JAXB.unmarshal(content, Body.class);

		/*
		 * Put the cross street first
		 */
		for (Stop nextStop : route.getRoute().getStop()) {
			String stopTitle = nextStop.getTitle();
			int atIndex = stopTitle.indexOf(" At ");
			if (atIndex != -1) {
				stopTitle = stopTitle.substring(atIndex + 4, stopTitle.length()) + " At " + stopTitle.substring(0, atIndex);
				nextStop.setTitle(stopTitle);
			}
		}

		long delay = System.currentTimeMillis() - startTime;
		ourLog.info("Loaded route config for route " + theRoute + " in " + delay + "ms");
		return route;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.wimsc.server.svc.INextbusFetcherService#loadRouteList()
	 */
	@Override
	public RouteList loadRouteList() throws FailureException {
		if (myRouteList != null && myRouteList.isFullyLoaded()) {
			return myRouteList;
		}

		try {

			RouteList retVal = (RouteList) myCache.get(getCacheKeyForRouteList());
			if (retVal != null && retVal.isFullyLoaded()) {
				return retVal;
			}

			retVal = loadRouteListWithoutCache();
			myRouteList = retVal;

			if (myRouteList.isFullyLoaded()) {
				myCache.put(getCacheKeyForRouteList(), retVal);
			}
			
			return retVal;

		} catch (Exception e) {
			ourLog.log(Level.WARNING, "Failed to load route list", e);
			throw new FailureException();
		}
	}


	// private void queryForMultistops(String theUrl, Map<String,
	// StreetcarLocation> theVehicleId2closestLocation, HashMap<String, Integer>
	// theVehicleId2closestLocationSeconds,
	// Map<String, Stop> theTag2stop) throws IOException {
	//
	// ourLog.info("Going to load URL (" + theUrl.length() + " bytes): " +
	// theUrl);
	// URL stopsUrl = new URL(theUrl);
	// BufferedReader content = new BufferedReader(new
	// InputStreamReader(stopsUrl.openStream()));
	// ca.wimsc.server.xml.stops.Body stops = JAXB.unmarshal(content,
	// ca.wimsc.server.xml.stops.Body.class);
	//
	// // PREDICTIONS CAN BE NULL!!
	// for (Predictions nextPredictions : stops.getPredictions()) {
	// String nextStopTag = nextPredictions.getStopTag();
	//
	// ca.wimsc.server.xml.stops.Body.Predictions.Direction nextPDirection =
	// nextPredictions.getDirection();
	// if (nextPDirection == null) {
	// continue;
	// }
	//
	// for (Prediction nextPrediction : nextPDirection.getPrediction()) {
	// String vehicleId = nextPrediction.getVehicle();
	// int seconds = nextPrediction.getSeconds().intValue();
	//
	// if (!theVehicleId2closestLocation.containsKey(vehicleId)) {
	// theVehicleId2closestLocation.put(vehicleId,
	// toStreetcarLocation(theTag2stop.get(nextStopTag)));
	// theVehicleId2closestLocationSeconds.put(vehicleId, seconds);
	// } else if (theVehicleId2closestLocationSeconds.get(vehicleId) > seconds)
	// {
	// theVehicleId2closestLocation.put(vehicleId,
	// toStreetcarLocation(theTag2stop.get(nextStopTag)));
	// theVehicleId2closestLocationSeconds.put(vehicleId, seconds);
	// }
	//
	// }
	// }
	//
	// }

	private RouteList loadRouteListWithoutCache() throws MalformedURLException, IOException, FailureException {
		RouteList retVal;
		long startTime = System.currentTimeMillis();
		URL routeUrl = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=ttc");
		BufferedReader content = new BufferedReader(new InputStreamReader(routeUrl.openStream()));
		ca.wimsc.server.xml.routelist.Body route = JAXB.unmarshal(content, ca.wimsc.server.xml.routelist.Body.class);
		long delay = System.currentTimeMillis() - startTime;

		ourLog.info("Loaded route list in " + delay + "ms");

		boolean fullyLoaded = true;
		
		List<ca.wimsc.client.common.model.Route> routes = new ArrayList<ca.wimsc.client.common.model.Route>();
		for (ca.wimsc.server.xml.routelist.Body.Route nextRoute : route.getRoute()) {
			ca.wimsc.client.common.model.Route modelRoute = new ca.wimsc.client.common.model.Route();
			modelRoute.setTitle(nextRoute.getTitle());
			modelRoute.setTag(nextRoute.getTag());

			Body rc = loadRouteConfig(nextRoute.getTag());
			modelRoute.setLatMax(rc.getRoute().getLatMax());
			modelRoute.setLatMin(rc.getRoute().getLatMin());
			modelRoute.setLonMax(rc.getRoute().getLonMax());
			modelRoute.setLonMin(rc.getRoute().getLonMin());

			routes.add(modelRoute);
			
			delay = System.currentTimeMillis() - startTime;
			if (delay > 20000) {
				ourLog.warning("Aborting loading route configs because we are going over time");
				fullyLoaded = false;
				break;
			}
		}

		retVal = new RouteList();
		retVal.setList(routes);
		retVal.setFullyLoaded(fullyLoaded);
		
		return retVal;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.wimsc.server.svc.INextbusFetcherService#loadStreetcarLocations(java.lang.String, boolean)
	 */
	@Override
	public StreetcarLocationList loadStreetcarLocations(String theRoute, boolean theLoadRouteList, boolean theRelaxedFrequencyMode) throws FailureException {
		StreetcarLocationList retVal = (StreetcarLocationList) myCache.get(getCacheKeyForStreetcarLocationList(theRoute));

		int cutoff = theRelaxedFrequencyMode ? (6 * 60 * 1000) : (60 * 1000);

		if (retVal != null && retVal.getFetchTime() != null && retVal.getFetchTime().getTime() >= (System.currentTimeMillis() - cutoff)) {
			ourLog.fine("Returning cached values for route " + theRoute + " (added " + retVal.getFetchTime() + ")");
		} else {
			long startTime = System.currentTimeMillis();
			try {
				retVal = getStreetcarLocationsInternal(theRoute);
			} catch (MalformedURLException e) {
				ourLog.log(Level.WARNING, "Failed to load location list", e);
				throw new FailureException();
			} catch (IOException e) {
				ourLog.log(Level.WARNING, "Failed to load location list", e);
				throw new FailureException();
			}
			long delay = System.currentTimeMillis() - startTime;

			ourLog.info("Loaded streetcar locations for route " + theRoute + " in " + delay + "ms");

			retVal.setFetchTime(new Date());
			myCache.put(getCacheKeyForStreetcarLocationList(theRoute), retVal);
		}

		if (theLoadRouteList) {
			retVal.setRouteList(loadRouteList());
		}

		return retVal;
	}


	/**
	 * Do not invoke
	 */
	void setActivityMonitorService(IActivityMonitorService theActivityMonitorService) {
		myActivityMonitorService = theActivityMonitorService;
	}


	private ca.wimsc.client.common.model.Stop toModelStop(Stop next) {
		ca.wimsc.client.common.model.Stop modelStop = new ca.wimsc.client.common.model.Stop();
		populateModelStop(next, modelStop);
		return modelStop;
	}


	private static String getCacheKeyForPredictionsList(String theRoute, String theStop) {
		return CACHE_KEY_VERSION_ID + "_" + "PL_" + theRoute + "_" + theStop;
	}


	private static String getCacheKeyForRouteConfig(String theRoute) {
		return CACHE_KEY_VERSION_ID + "_" + "RC_" + "_" + theRoute;
	}


	private static String getCacheKeyForStopListForRoute(String theRoute) {
		return CACHE_KEY_VERSION_ID + "_" + "SLFR_" + "_" + theRoute;
	}


	private static String getCacheKeyForRouteList() {
		return CACHE_KEY_VERSION_ID + "_" + "RL";
	}


	private static String getCacheKeyForStopQuadrantList() {
		return CACHE_KEY_VERSION_ID + "_" + "SQL";
	}


	private static String getCacheKeyForStreetcarLocationList(String theRoute) {
		return CACHE_KEY_VERSION_ID + "_" + "SCL_" + "_" + theRoute;
	}


	private StreetcarLocationList getStreetcarLocationsInternal(String theRoute) throws MalformedURLException, IOException, FailureException {
		URL routeUrl = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=ttc&t=0&r=" + theRoute);

		InputStream content = routeUrl.openStream();
		ca.wimsc.server.xml.loc.Body loc = JAXB.unmarshal(content, ca.wimsc.server.xml.loc.Body.class);

		Map<String, SpeedAndTimestamp> vehicleAverageSpeeds = myActivityMonitorService.getVehicleAverageSpeeds(theRoute);

		StopListForRoute stopListForRoute = getStopListForRoute(theRoute);

		List<StreetcarLocation> values = new ArrayList<StreetcarLocation>();
		for (Vehicle next : loc.getVehicle()) {
			StreetcarLocation streetcarLocation = new StreetcarLocation();
			streetcarLocation.setDirectionTag(next.getDirTag());
			streetcarLocation.setLatitude(next.getLat());
			streetcarLocation.setLongitude(next.getLon());
			streetcarLocation.setVehicleTag(next.getId());
			streetcarLocation.setHeading(next.getHeading());

			SpeedAndTimestamp averageSpeed = vehicleAverageSpeeds.get(next.getId());
			if (averageSpeed != null) {
				averageSpeed.maybeUpdateBasedOnLatestPosition(System.currentTimeMillis(), streetcarLocation.getLatitude(), streetcarLocation.getLongitude());
				streetcarLocation.setCurrentSpeed(averageSpeed.getAverageSpeed());
			}

			// Find the closest stop to the current location of the next streetcar
			double lowestDistance = Double.MAX_VALUE;
			ca.wimsc.client.common.model.Stop closestStop = null;
			StopList stopList = stopListForRoute.getUiOrNonUiStopListForDirectionTag(next.getDirTag());

			/*
			 * This might be null if the direction names on the vehicleLocations query don't match the ones in the
			 * routeConfig tag
			 */
			List<ca.wimsc.client.common.model.Stop> allStops;
			if (stopList == null) {
				allStops = null;
			} else {
				allStops = stopList.getStops();
			}

			if (allStops != null) {
				for (ca.wimsc.client.common.model.Stop nextStop : allStops) {
					double distance = streetcarLocation.distanceFrom(nextStop.getLatitude(), nextStop.getLongitude());
					if (distance < lowestDistance) {
						lowestDistance = distance;
						closestStop = nextStop;
					}
				}

				// If we found the closest stop, store the relevant details
				if (closestStop != null) {
					streetcarLocation.setClosestStopTag(closestStop.getStopTag());
				}

			}

			values.add(streetcarLocation);
		}

		StreetcarLocationList retVal = new StreetcarLocationList();
		retVal.setLocations(values);
		retVal.setRouteId(theRoute);
		return retVal;
	}


	public static void populateModelStop(Stop next, ca.wimsc.client.common.model.Stop modelStop) {
		modelStop.setLatitude(next.getLat());
		modelStop.setLongitude(next.getLon());

		modelStop.setTitle(next.getTitle());
		modelStop.setStopTag(next.getTag());
	}


	private static ca.wimsc.client.common.model.Direction toClientDirection(Direction theNextDirection) {
		ca.wimsc.client.common.model.Direction retVal = new ca.wimsc.client.common.model.Direction();
		retVal.setTag(theNextDirection.getTag());

		retVal.setName(theNextDirection.getName());
		if (retVal.getName() == null || retVal.getName().length() == 0) {
			retVal.setName(theNextDirection.getTitle());
			if (retVal.getName() == null || retVal.getName().length() == 0) {
				retVal.setName(theNextDirection.getTag());
			}
		}

		return retVal;
	}

}
