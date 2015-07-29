package ca.wimsc.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.input.PropertyFileInputHandler;

import ca.wimsc.client.common.model.MapDataResponseV2;
import ca.wimsc.client.common.model.MostRecentTweets;
import ca.wimsc.client.common.model.NearbyStop;
import ca.wimsc.client.common.model.NearbyStopList;
import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.client.common.rpc.IGetMapDataService;
import ca.wimsc.client.common.rpc.IGetNearbyStopsService;
import ca.wimsc.client.common.rpc.IGetRouteConfigService;
import ca.wimsc.client.common.rpc.IGetStatisticsService;
import ca.wimsc.client.common.rpc.IGetTwitterService;
import ca.wimsc.client.common.rpc.StreetcarService;
import ca.wimsc.client.common.util.Constants;
import ca.wimsc.server.jpa.StopQuadrant.StopKey;
import ca.wimsc.server.jpa.StopQuadrantList;
import ca.wimsc.server.svc.IActivityMonitorService;
import ca.wimsc.server.svc.INextbusFetcherService;
import ca.wimsc.server.svc.ITwitterService;
import ca.wimsc.server.svc.NextbusFetcherServiceImpl;
import ca.wimsc.server.svc.ServiceFactory;
import ca.wimsc.server.xml.route.Body;
import ca.wimsc.server.xml.route.Body.Route.Direction;
import ca.wimsc.server.xml.route.Body.Route.Stop;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class StreetcarServiceImpl extends RemoteServiceServlet implements StreetcarService, IGetMapDataService, IGetNearbyStopsService, IGetRouteConfigService,
		IGetStatisticsService, IGetTwitterService {

	private static final Logger ourLog = Logger.getLogger(StreetcarServiceImpl.class.getName());

	private static Properties ourStopTagProperties;

	private IActivityMonitorService myActivityMonitor;
	private DateFormat myDateTimeFormat;
	private INextbusFetcherService myNextbusFetcher;
	private StopQuadrantList myStopQuadrants;
	private Map<String, String> myStopTagToRouteTag = new HashMap<String, String>();
	private ITwitterService myTwitterService;
	/**
	 * Constructor
	 */
	public StreetcarServiceImpl() throws IOException {
		myDateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);
		myDateTimeFormat.setTimeZone(TimeZone.getTimeZone("America/Toronto"));

		myNextbusFetcher = ServiceFactory.getInstance().getNextbusFetcherService();
		myActivityMonitor = ServiceFactory.getInstance().getActivityMonitorService();
		myTwitterService = ServiceFactory.getInstance().getTwitterService();		
	}


	/**
	 * By default, GWT throws an exception here. We override this to just
	 * create a warning.
	 * 
	 * For some reason, some versions of firefox seem to not provide this
	 * header properly, but in the end we can probably live without this
	 * check. It's designed to guard against cross-site scripting, and this
	 * site doesn't handle any sensitive data so we don't really need to worry
	 * about that.
	 */
	@Override
	protected void checkPermutationStrongName() throws SecurityException {
		if (getPermutationStrongName() == null) {
			ourLog.warning("Request does not have a permutation strong name");
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public MapDataResponseV2 getMapData(int theQueryIndex, Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags, boolean theLoadRouteList, boolean theLoadStopList,
			boolean theLoadPredictions, boolean theLoadLocations) throws FailureException {
		long start = System.currentTimeMillis();

		if (theSelectedRouteTags == null) {
			theSelectedRouteTags = new HashSet<String>();
		}
		if (theSelectedStopTags == null) {
			theSelectedStopTags = new HashSet<String>();
		}

		replaceStopTagsWithNewStopTags(theSelectedStopTags);
		
		try {
			MapDataResponseV2 retVal = new MapDataResponseV2();
			retVal.setQueryIndex(theQueryIndex);

			/*
			 * If these are both empty, this is probably a user who just navigated to the site for the first time, so
			 * doesn't have any route or stop selected. Therefore, we find some sensible defaults.
			 */
			if (theSelectedRouteTags.isEmpty()) {
				theSelectedRouteTags = new HashSet<String>();
				theSelectedRouteTags.add("504");

				if (theSelectedStopTags.isEmpty()) {
					theSelectedStopTags = new HashSet<String>();
					theSelectedStopTags.add("7034");
				}

			}

			if (theSelectedStopTags.size() > Constants.MAX_STOPS_AT_ONCE) {
				ourLog.warning("Too many selected: " + theSelectedRouteTags.size() + " - " + theSelectedStopTags.size());
				HashSet<String> selectedStopTagsSubCollection = new HashSet<String>();
				Iterator<String> iter = theSelectedStopTags.iterator();
				for (int i = 0; i < Constants.MAX_STOPS_AT_ONCE; i++) {
					selectedStopTagsSubCollection.add(iter.next());
				}
				theSelectedStopTags = selectedStopTagsSubCollection;
			}

			boolean relaxedFrequencyMode = theSelectedRouteTags.size() > 4;

			if (theLoadRouteList) {
				retVal.setRouteList(getRouteList());
			}

			if (theLoadStopList) {
				for (String nextRouteTag : theSelectedRouteTags) {
					retVal.addStopListForRoute(getStopListForRoute(nextRouteTag));
				}
			}

			if (theLoadLocations) {
				retVal.setRouteTagToStreetcarLocationList(new HashMap<String, StreetcarLocationList>());
				for (String nextRouteTag : theSelectedRouteTags) {
					StreetcarLocationList locationList = myNextbusFetcher.loadStreetcarLocations(nextRouteTag, false, relaxedFrequencyMode);
					retVal.getRouteTagToStreetcarLocationList().put(nextRouteTag, locationList);
				}
			}

			ArrayList<String> stopTags = new ArrayList<String>(theSelectedStopTags);
			Map<String, String> stopTagToRouteTag = new HashMap<String, String>();

			retVal.setStopTagToPredictionList(new HashMap<String, PredictionsList>());
			for (Iterator<String> iter = stopTags.iterator(); iter.hasNext();) {
				String nextStopTag = iter.next();
				String routeTag = myStopTagToRouteTag.get(nextStopTag);

				/*
				 * We need to figure out which route a given stop tag belongs to.. But we cache what we figure out so
				 * it's less painful later.
				 */

				if (routeTag == null) {
					for (String nextRouteTag : theSelectedRouteTags) {
						Body rc = myNextbusFetcher.loadRouteConfig(nextRouteTag);
						for (Stop next : rc.getRoute().getStop()) {
							if (next.getTag().equals(nextStopTag)) {
								myStopTagToRouteTag.put(nextStopTag, nextRouteTag);
								routeTag = nextRouteTag;
								break;
							}
						}
						if (routeTag != null) {
							break;
						}
					}
				}

				if (routeTag == null || !theSelectedRouteTags.contains(routeTag)) {
					iter.remove();
				} else {
					stopTagToRouteTag.put(nextStopTag, routeTag);
					if (theLoadPredictions) {
						PredictionsList predictions = myNextbusFetcher.loadPredictions(routeTag, nextStopTag);
						retVal.getStopTagToPredictionList().put(nextStopTag, predictions);
					}
				}
			}

			retVal.setStopTagsToRouteTags(stopTagToRouteTag);
			retVal.setSelectedRouteTags(theSelectedRouteTags);
			retVal.setSelectedStopTags(new HashSet<String>(stopTags));

			long delay = System.currentTimeMillis() - start;

			ourLog.info("getMapData for routes " + theSelectedRouteTags + " and stops " + theSelectedStopTags + " in " + delay + "ms. Query index: " + theQueryIndex);

			return retVal;

		} catch (Throwable e) {
			ourLog.log(Level.SEVERE, "Failed to load map data", e);
			throw new FailureException();
		}
	}


	


	/**
	 * {@inheritDoc}
	 */
	@Override
	public MostRecentTweets getMostRecentTweetForRoutes(Set<String> theRouteTag) throws FailureException {
		HashMap<String, Tweet> retVal = new HashMap<String, Tweet>();
		for (String next : theRouteTag) {
			retVal.put(next, myTwitterService.getMostRecentTweetForRoute(next));
		}

		return new MostRecentTweets(retVal);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public NearbyStopList getNearbyStops(String theAddress, double theLatitude, double theLongitude, int theNumToFind) throws FailureException {
		long start = System.currentTimeMillis();

		try {

			if (theNumToFind > 20) {
				theNumToFind = 20;
			}

			if (myStopQuadrants == null) {
				myStopQuadrants = myNextbusFetcher.loadAllStopQuadrants();
			}

			List<StopKey> closestStops = myStopQuadrants.findClosestStops(theLatitude, theLongitude, theNumToFind);
			ArrayList<NearbyStop> retVal = new ArrayList<NearbyStop>();

			for (StopKey stopKey : closestStops) {

				String routeTag = stopKey.getRouteTag();
				String stopTag = stopKey.getStopTag();

				Body routeConfig = myNextbusFetcher.loadRouteConfig(routeTag);

				String routeTitle = routeConfig.getRoute().getTitle();
				String stopTitle = null;
				String directionTitle = null;
				String directionTag = null;

				Stop stop = null;
				GNS_ALL: for (Direction nextDirection : routeConfig.getRoute().getDirection()) {
					for (ca.wimsc.server.xml.route.Body.Route.Direction.Stop nextDirectionStop : nextDirection.getStop()) {
						if (nextDirectionStop.getTag().equals(stopTag)) {
							for (Stop nextStop : routeConfig.getRoute().getStop()) {
								if (nextStop.getTag().equals(nextDirectionStop.getTag())) {
									stopTitle = nextStop.getTitle();
									directionTitle = nextDirection.getTitle();
									directionTag = nextDirection.getTag();
									stop = nextStop;
									break GNS_ALL;
								}
							}
						}
					}
				}

				if (stop == null) {
					ourLog.log(Level.WARNING, "Can not find stop with tag " + stopTag);
					continue;
				}

				NearbyStop next = new NearbyStop();
				NextbusFetcherServiceImpl.populateModelStop(stop, next);

				next.setDirectionTitle(directionTitle);
				next.setRouteTitle(routeTitle);
				next.setRouteTag(routeTag);
				next.setTitle(stopTitle);
				next.setDirectionTag(directionTag);

				retVal.add(next);
			}

			long delay = System.currentTimeMillis() - start;
			ourLog.info("findNearbyStops for " + theLatitude + "/" + theLongitude + " at address " + theAddress + " took " + delay + "ms");

			NearbyStopList nearbyStopList = new NearbyStopList();
			nearbyStopList.setNearbyStops(retVal);
			return nearbyStopList;

		} catch (IOException e) {
			ourLog.log(Level.SEVERE, "Failed to load map data", e);
			throw new FailureException();
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public PredictionsList getPredictions(String theRoute, String theDirection, String theStop) throws FailureException {
		theStop = replaceStopTagWithNewStopTag(theStop);
		return myNextbusFetcher.loadPredictions(theRoute, theStop);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, NumbersAndTimestamps> getRouteAverageSpeedsOverLast24Hours(Set<String> theRouteTags) throws FailureException {
		return myActivityMonitor.getRouteAverageSpeedsOverLast24Hours(theRouteTags);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RouteList getRouteList() throws FailureException {
		return myNextbusFetcher.loadRouteList();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getRoutePaths(Set<String> theRouteTags) throws FailureException {
		HashMap<String, String> retVal = new HashMap<String, String>();

		for (String nextRouteTag : theRouteTags) {
			String routePathString = myActivityMonitor.getRoutePathString(nextRouteTag);
			retVal.put(nextRouteTag, routePathString);
		}

		return retVal;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, NumbersAndTimestamps> getRouteVehicleCountsOverLast24Hours(Set<String> theRouteTags) throws FailureException {
		return myActivityMonitor.getRouteVehicleCountsOverLast24Hours(theRouteTags);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public StopListForRoute getStopListForRoute(String theRoute) throws FailureException {
		return myNextbusFetcher.getStopListForRoute(theRoute);
	}

	
	public static void replaceStopTagsWithNewStopTags(Set<String> theStopTags) {
		ArrayList<String> tags = new ArrayList<String>(theStopTags);
		for (String next : tags) {
			String replacement = replaceStopTagWithNewStopTag(next);
			if (replacement != next) {
				theStopTags.remove(next);
				theStopTags.add(replacement);
			}
		}		
	}
	

	public static String replaceStopTagWithNewStopTag(String theStopTag) {
		if (ourStopTagProperties == null) {
			
			InputStream stopTagsStream = StreetcarServiceImpl.class.getResourceAsStream("/oldStopTags.properties");
			if (stopTagsStream == null) {
				stopTagsStream = StreetcarServiceImpl.class.getResourceAsStream("oldStopTags.properties");
			}
			if (stopTagsStream == null) {
				throw new IllegalStateException("Could not load oldStopTags.properties");
			}
			
			Properties stopTagProperties = new Properties();
			try {
				stopTagProperties.load(stopTagsStream);
			} catch (IOException e) {
				throw new IllegalStateException("Could not load oldStopTags.properties", e);
			}
			
			ourStopTagProperties = stopTagProperties;
		}
		
		String tag = ourStopTagProperties.getProperty(theStopTag);
		if (tag == null) {
			return theStopTag;
		} else {
			return tag.trim();
		}
		
	}

}
