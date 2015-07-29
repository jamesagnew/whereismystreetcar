package ca.wimsc.client.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IAsyncListener;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MapDataController {

	public static final MapDataController INSTANCE = new MapDataController();

	private String myCurrentFavouriteId;
	private int myCurrentQueryIndex;
	private boolean myFetchingMapData;
	private boolean myMissingStopTagsWarningHasFired;
	private long myLastLoadTime;
	private MyLoadAgainSoonerCommand myLoadAgainSoonerCommand = new MyLoadAgainSoonerCommand();
	private boolean myLoadingCompletedAtLeastOnce;
	private long myNextRoutePathUpdate = 0L;
	private List<IAsyncListener> myRouteListeners = new ArrayList<IAsyncListener>();
	private Timer myTimer;
	private int myWantLocationsCount = 0;
	private int myWantPredictionsCount = 0;

	private boolean myPaused;


	/**
	 * Non instantiable
	 */
	private MapDataController() {

		myCurrentFavouriteId = HistoryUtil.getCurrentFavouriteId();

		History.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myNextRoutePathUpdate = 0L;

				Set<String> newRoute = HistoryUtil.getRoute();
				Set<String> currentRoute = Model.INSTANCE.getSelectedRouteTags();
				String newFavourite = HistoryUtil.getCurrentFavouriteId();

				Set<String> newStop = HistoryUtil.getStop();
				Set<String> currentStop = Model.INSTANCE.getSelectedStopTags();
				if (currentRoute != null && currentRoute.equals(newRoute) && currentStop != null && currentStop.equals(newStop)) {
					return;
				}

				if (!StringUtil.equals(myCurrentFavouriteId, newFavourite)) {
					Common.trackGoogleAnalyticsEvent("PageChange", "NewFavourite");
				} else {
					Common.trackGoogleAnalyticsEvent("PageChange", "NewRouteAndStop");
				}

				myCurrentFavouriteId = newFavourite;

				loadRoute(newRoute, newStop);
			}
		});

		MySelectedRouteOrStopListener listener = new MySelectedRouteOrStopListener();
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, listener);
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, listener);

	}


	/**
	 * Add a listener for loading and display change events
	 */
	public void addAsyncListeners(IAsyncListener theRouteListeners) {
		myRouteListeners.add(theRouteListeners);
	}


	public void decrementWantLocationsCount() {
		myWantLocationsCount--;
	}


	public void decrementWantPredictionsCount() {
		myWantPredictionsCount--;
	}


	private void fireLoadingComplete() {
		for (IAsyncListener next : new ArrayList<IAsyncListener>(myRouteListeners)) {
			next.finishedLoading();
		}
	}


	private void fireLoadingStarted() {
		for (IAsyncListener next : myRouteListeners) {
			next.startLoading();
		}
	}


	public void incrementWantLocationsCount() {
		myWantLocationsCount++;
	}


	public void incrementWantPredictionsCount() {
		myWantPredictionsCount++;
	}


	public boolean isLoadingCompletedAtLeastOnce() {
		return myLoadingCompletedAtLeastOnce;
	}


	/**
	 * Force a call to the backend server
	 */
	private void loadRoute(Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags) {
		myFetchingMapData = true;

		if (theSelectedRouteTags == null) {
			theSelectedRouteTags = new HashSet<String>();
		}
		if (theSelectedStopTags == null) {
			theSelectedStopTags = new HashSet<String>();
		}
		
		GWT.log(new Date() + " About to load route");

		fireLoadingStarted();

		boolean loadRouteList = !Model.INSTANCE.isHaveRouteList();
		boolean loadStopList = Model.INSTANCE.getSelectedRouteTags() == null || !Model.INSTANCE.isHaveAllStopLists(theSelectedRouteTags);
		boolean loadPredictions = myWantPredictionsCount > 0;
		boolean loadLocations = myWantLocationsCount > 0;

		myLastLoadTime = System.currentTimeMillis();
		myCurrentQueryIndex++;
		MyGetMapDataAsyncCallback callback = new MyGetMapDataAsyncCallback(theSelectedStopTags);
		Common.SC_SVC_GMD.getMapData(myCurrentQueryIndex, theSelectedRouteTags, theSelectedStopTags, loadRouteList, loadStopList, loadPredictions, loadLocations, callback);
	}


	/**
	 * Begin a call to the backend server
	 */
	private void loadRouteOrFakeLoad() {

		/*
		 * The prediction data isn't so accurate that much changes in 30 seconds, so it doesn't make sense to let people
		 * reload more often than that, given that the site is at risk of running out of quota.. Maybe one day clients
		 * will download data direct from NextBus, after which it won't matter
		 */
		if (GWT.isProdMode() && (myLastLoadTime + 30000) > System.currentTimeMillis()) {
			fireLoadingStarted();
			GWT.log("Starting fake reload");
			new Timer() {

				@Override
				public void run() {
					fireLoadingComplete();
				}
			}.schedule(3000);
			return;
		}

		Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();
		if (selectedStopTags == null) {
			selectedStopTags = HistoryUtil.getStop();
		}

		Set<String> selectedRouteTags = Model.INSTANCE.getSelectedRouteTags();
		if (selectedRouteTags == null) {
			selectedRouteTags = HistoryUtil.getRoute();
		}

		if (myFetchingMapData) {
			return;
		}

		loadRoute(selectedRouteTags, selectedStopTags);

	}


	/**
	 * Go to a new favourite and add it to the list of favourites if neccesary
	 */
	public void navigateToNewFavouriteAndAddIfNeccesary(Favourite theFavourite) {
		assert theFavourite != null;
		assert StringUtil.isNotBlank(theFavourite.getId());

		boolean updatedExisting = Model.INSTANCE.addOrUpdateFavourite(theFavourite);
		HistoryUtil.setFavourite(theFavourite);

		if (updatedExisting) {
			Common.trackGoogleAnalyticsEvent("Favourite", "Add");
		} else {
			Common.trackGoogleAnalyticsEvent("Favourite", "Update");
		}

	}


	/**
	 * Navigate to a new route and stop
	 */
	public void navigateToNewRouteAndStop(Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags, boolean theShowOnlyPredictions) {
		assert theSelectedRouteTags != null && theSelectedRouteTags.size() > 0;
		assert theSelectedStopTags != null;

		HistoryUtil.setRoutesAndStops(theSelectedRouteTags, theSelectedStopTags, theShowOnlyPredictions);
	}


	private void processNewMapData(MapDataResponseV2 theResult) {
		if (theResult.getQueryIndex() != myCurrentQueryIndex) {
			// Ignore, since there is a new outstanding query
			GWT.log("Ignoring query with index: " + theResult.getQueryIndex() + ", waiting for result for: " + myCurrentQueryIndex);
			return;
		}

		GWT.log(new Date() + " - Received route data");
		myFetchingMapData = false;
		if (theResult.getRouteList() != null) {
			Model.INSTANCE.setRouteList(theResult.getRouteList());
		}

			for (StopListForRoute next : theResult.getStopListForRoute()) {
				Model.INSTANCE.addStopListForRoute(next);
			}

		Model.INSTANCE.setStreetcarLocationAndPredictionLists(theResult.getRouteTagToStreetcarLocationList(), theResult.getStopTagToPredictionList());

		if (theResult.getStopTagToPredictionList() != null) {
			// TODO: add to favourites (recent)
			/*
			 * FavouriteStop fStop = new FavouriteStop(mySelectedStop); fStop.setLastAccess(new Date());
			 * fStop.setDirectionTag(mySelectedStopDirectionTag);
			 * fStop.setDirectionTitle(DirectionEnum.fromTag(mySelectedStopDirectionTag).name());
			 * fStop.setRouteTag(mySelectedRouteTag); fStop.setRouteTitle(mySelectedRoute.getTitle());
			 * Model.INSTANCE.addOrUpdateRecentStop(fStop);
			 */
		}

		Model.INSTANCE.setStopTagsToRouteTags(theResult.getStopTagsToRouteTags());
		Model.INSTANCE.setSelectedRouteAndStopTags(theResult.getSelectedRouteTags(), theResult.getSelectedStopTags());

		GWT.log(new Date() + "  - Done loading route");

		long now = System.currentTimeMillis();
		if (now > myNextRoutePathUpdate) {
			GWT.log("Going to load route path entries");
			Common.SC_SVC_GRC.getRoutePaths(theResult.getSelectedRouteTags(), new MyGetRoutePathsAsyncCallback());

			myNextRoutePathUpdate = now + (5 * 60 * 1000);
		}

	}


	private void processRoutePaths(Map<String, String> theResult) {
		for (Map.Entry<String, String> nextEntry : theResult.entrySet()) {
			String nextRouteTag = nextEntry.getKey();
			if (nextEntry.getValue() != null) {
				RoutePath nextRoutePath = new RoutePath(nextEntry.getValue());
				Model.INSTANCE.setRoutePath(nextRouteTag, nextRoutePath);
			}
		}
	}


	public void removeAsyncListeners(IAsyncListener theAsyncListener) {
		myRouteListeners.remove(theAsyncListener);
	}


	/**
	 * Remove a favourite stop from the list of favourites
	 */
	public void removeFavourite(Favourite theFavourite) {
		Common.trackGoogleAnalyticsEvent("Favourite", "Delete");
		Model.INSTANCE.removeFavourite(theFavourite);
	}


	public void requestReload() {
		loadRouteOrFakeLoad();
	}


	private void scheduleToLoadAgainLater() {
		if (myTimer != null) {
			myTimer.cancel();
		}

		myTimer = new Timer() {

			@Override
			public void run() {
				myLastLoadTime = 0;
				loadRouteOrFakeLoad();
			}
		};

		int delay = 60 * 1000;
		if (myCurrentQueryIndex > 10) {
			delay = 3 * 60 * 1000;
		}
		
		delay = GWT.isScript() ? delay : 300 * 1000;
		
		myTimer.schedule(delay);
	}


	private void scheduleToLoadAgainSooner() {
		myLoadAgainSoonerCommand.markNeeded();
		Scheduler.get().scheduleDeferred(myLoadAgainSoonerCommand);
	}


	private final class MyGetMapDataAsyncCallback implements AsyncCallback<MapDataResponseV2> {
		private int mySuccessiveFailures;
		private Set<String> myStopTags;

		private MyGetMapDataAsyncCallback(Set<String> theStopTags) {
			myStopTags = theStopTags;
		}

		@Override
		public void onFailure(Throwable theCaught) {
			
			mySuccessiveFailures++;

			if (mySuccessiveFailures == 5) {

				// If we keep having problems here, let the server know (only
				// after a few successive failures, since this method is constantly
				// refreshing, and fails from time to time)
				Common.report(Common.CLIENT_LOGGING_HANDLER, theCaught);

			} else if (mySuccessiveFailures > 5) {

				// If we keep failing over and over, something is busted. Reload the page!
				Window.Location.reload();

			}

			myFetchingMapData = false;
			myLoadingCompletedAtLeastOnce = true;
			scheduleToLoadAgainLater();
			fireLoadingComplete();
			/*
			 * TODO: maybe also a status line change when this happens? Either way, we have to expect this to happen
			 * from time to time, so don't fire a window.alert here
			 */
		}


		@Override
		public void onSuccess(MapDataResponseV2 theResult) {
			
			if (myStopTags.size() > theResult.getStopTagsToRouteTags().keySet().size() && !myMissingStopTagsWarningHasFired) {
				Window.alert("Error: One or more of your selected stops could not be found. "
						+ "This may be because the TTC recently renamed and retagged all of their "
						+ "stop data. Unfortunately, this means that bookmarks and favourites " 
						+ "will need to be updated. We are very sorry for the inconvenience");
				myMissingStopTagsWarningHasFired = true;
			} 
			
			mySuccessiveFailures = 0;
			myFetchingMapData = false;
			myLoadingCompletedAtLeastOnce = true;
			processNewMapData(theResult);
			scheduleToLoadAgainLater();
			fireLoadingComplete();
		}
	}


	/**
	 * @author James
	 * 
	 */
	public class MyGetRoutePathsAsyncCallback implements AsyncCallback<Map<String, String>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable theCaught) {
			GWT.log("Failed to load route paths", theCaught);
			Common.report(Common.CLIENT_LOGGING_HANDLER, theCaught);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(Map<String, String> theResult) {
			processRoutePaths(theResult);
		}

	}


	private final class MyLoadAgainSoonerCommand implements ScheduledCommand {

		private boolean myNeeded;


		@Override
		public void execute() {
			if (myNeeded) {
				myLastLoadTime = 0;
				loadRouteOrFakeLoad();
				myNeeded = false;
			}
		}


		public void markNeeded() {
			myNeeded = true;
		}
	}


	private final class MySelectedRouteOrStopListener implements IPropertyChangeListener {
		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			if (theOldValue != null) {
				scheduleToLoadAgainSooner();
			}
		}
	}


	/**
	 * 
	 */
	public void pauseLoading() {
		myPaused = true;
		if (myTimer != null) {
			myTimer.cancel(); 
			myTimer = null;
		}
	}


	public void resumeLoading() {
		if (myPaused) {
			myPaused = false;
			scheduleToLoadAgainLater();
		}
	}

}
