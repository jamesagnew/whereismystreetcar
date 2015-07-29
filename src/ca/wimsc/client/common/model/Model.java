package ca.wimsc.client.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.PropertyChangeSupport;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Contains all loaded model data and acts as a facade to the backend service layer
 */
public class Model extends PropertyChangeSupport {

	public static final String FAVOURITE_LIST_PROPERTY = "MFL";

	/**
	 * Singleton instance
	 */
	public static final Model INSTANCE = new Model();
	private static final int MAX_FAV_STOPS = 3;
	public static final String SELECTED_FAVOURITE_PROPERTY = "MSF";
	public static final String SELECTED_ROUTE_PROPERTY = "MSR";
	public static final String SELECTED_STOP_PROPERTY = "MSS";

	private Favourite myCurrentFavourite;
	private List<Favourite> myFavourites;
	private List<IModelListenerSync<List<FavouriteStop>>> myFavouriteStopListeners = new ArrayList<IModelListenerSync<List<FavouriteStop>>>();
	private List<FavouriteStop> myFavouriteStops;
	private List<IModelListenerAsync<Map<String, PredictionsList>>> myPredictionListListeners = new ArrayList<IModelListenerAsync<Map<String, PredictionsList>>>();
	private Set<String> myPredictionVehicleTags = new HashSet<String>();
	private RouteList myRouteList;
	private Map<String, RoutePath> myRouteTagToRoutePath = new HashMap<String, RoutePath>();
	private Map<String, List<IModelListenerAsync<RoutePath>>> myRouteTagToRoutePathListeners = new HashMap<String, List<IModelListenerAsync<RoutePath>>>();
	private Map<String, StreetcarLocationList> myRouteTagToStreetcarLocationList;
	private Map<String, List<IModelListenerAsync<StreetcarLocationList>>> myRouteTagToStreetcarLocationListListeners = new HashMap<String, List<IModelListenerAsync<StreetcarLocationList>>>();
	private ArrayList<Route> mySelectedRoutes;
	private Set<String> mySelectedRouteTags;
	private ArrayList<Stop> mySelectedStops;
	private Set<String> mySelectedStopTags;
	private Map<String, StopListForRoute> myStopLists = new HashMap<String, StopListForRoute>();
	private Map<String, String> myStopTagsToDirectionTags = new HashMap<String, String>();
	private Map<String, String> myStopTagsToRouteTags = new HashMap<String, String>();
	private Map<String, List<IModelListenerAsync<PredictionsList>>> myStopTagToPredictionListListeners = new HashMap<String, List<IModelListenerAsync<PredictionsList>>>();
	private Map<String, PredictionsList> myStopTagToPredictionLists;


	/**
	 * Adds a listener for changes to the favourite stops
	 */
	public void addFavouriteStopListener(IModelListenerSync<List<FavouriteStop>> theListener) {
		myFavouriteStopListeners.add(theListener);
	}


	/**
	 * @return Returns true if an existing favourite was updated
	 */
	public boolean addOrUpdateFavourite(Favourite theFavourite) {
		List<Favourite> favourites = getFavourites();
		ArrayList<Favourite> oldValue = new ArrayList<Favourite>(favourites);

		// Look for this stop in our current favourites
		boolean found = false;
		for (int i = 0; i < favourites.size(); i++) {
			Favourite nextFavourite = favourites.get(i);
			if (nextFavourite.equals(theFavourite)) {
				found = true;
				favourites.set(i, theFavourite);
			}
		}

		// We didn't find it, so add it
		if (!found) {
			favourites.add(theFavourite);
		}

		DatabaseUtil.setFavourites(favourites);
		firePropertyChange(FAVOURITE_LIST_PROPERTY, oldValue, favourites);

		return found;
	}


	private void addOrUpdateFavouriteOrRecentStop(FavouriteStop theStop, boolean theRecent) {
		List<FavouriteStop> current = getRecentOrFavouriteStops();

		for (FavouriteStop favouriteStop : current) {

			// Look for this stop in our current favourites
			if (favouriteStop.getStopTag().equals(theStop.getStopTag())) {

				// If we found it and it's already pinned, and we're adding a non-pinned stop, get out of here
				if (theRecent && favouriteStop.isPinned()) {
					return;
				}

				// If we find it, replace it with the stop definition passed in
				StringBuilder builder = new StringBuilder();
				theStop.serializeToString(builder);
				favouriteStop.deserializeFromString(builder.toString());

				myFavouriteStops = current;
				DatabaseUtil.setFavouriteStops(current);
				fireRecentStopsChanged();
				return;
			}
		}

		// We didn't find it, so add it
		current.add(theStop);

		while (current.size() > MAX_FAV_STOPS) {

			FavouriteStop oldest = null;
			for (FavouriteStop favouriteStop : current) {
				if (oldest == null || favouriteStop.getLastAccess().before(oldest.getLastAccess())) {
					if (favouriteStop.isPinned() == false) {
						oldest = favouriteStop;
					}
				}
			}

			if (oldest != null) {
				current.remove(oldest);
			} else {
				break;
			}

		}

		myFavouriteStops = current;
		DatabaseUtil.setFavouriteStops(current);
		fireRecentStopsChanged();
	}


	/**
	 * Add or update a stop to the list of favourites as a "favourite" stop, meaning that it is pinned
	 */
	public void addOrUpdateFavouriteStop(FavouriteStop theStop) {
		assert theStop.isPinned();
		addOrUpdateFavouriteOrRecentStop(theStop, false);
	}


	// private Map<String, List<IModelListenerAsync<Map<String, StopList>>>> myRouteTagToStopListListeners = new
	// HashMap<String, List<IModelListenerAsync<Map<String, StopList>>>>();
	//
	// /**
	// * Retrieve the stop list for a particular route
	// */
	// public void getStopList(String theRouteTag, IModelListenerAsync<Map<String, StopList>> theListener) {
	// if (myStopLists.containsKey(theRouteTag)) {
	// theListener.objectLoaded(myStopLists.get(theRouteTag), false);
	// }
	//
	// if (myRouteTagToStopListListeners.containsKey(theRouteTag) == false) {
	// myRouteTagToStopListListeners.put(theRouteTag, new ArrayList<IModelListenerAsync<Map<String,StopList>>>());
	// }
	// myRouteTagToStopListListeners.get(theRouteTag).add(theListener);
	// theListener.startLoadingObject();
	// }

	/**
	 * Add or update a stop to the list of favourites as a "recent" stop, meaning that it is not pinned, and might drop
	 * off the list later.
	 */
	public void addOrUpdateRecentStop(FavouriteStop theStop) {
		assert !theStop.isPinned();
		addOrUpdateFavouriteOrRecentStop(theStop, true);
	}


	/**
	 * @param thePredictionListListener
	 */
	public void addPredictionListListener(IModelListenerAsync<Map<String, PredictionsList>> thePredictionListListener) {
		assert thePredictionListListener != null;
		assert myPredictionListListeners.contains(thePredictionListListener) == false;

		myPredictionListListeners.add(thePredictionListListener);

		if (myStopTagToPredictionLists != null) {
			thePredictionListListener.objectLoaded(myStopTagToPredictionLists, false);
		}
	}


	public void addPredictionListListener(String theStopTag, IModelListenerAsync<PredictionsList> theListener) {
		ensurePredictionListListener(theStopTag);
		myStopTagToPredictionListListeners.get(theStopTag).add(theListener);

		if (myStopTagToPredictionLists.containsKey(theStopTag)) {
			theListener.objectLoaded(myStopTagToPredictionLists.get(theStopTag), false);
		}
	}


	/**
	 * Adds a listener for changes to the route path
	 */
	public void addRoutePathListener(String theRouteTag, IModelListenerAsync<RoutePath> theListener) {
		ensureRoutePathListeners(theRouteTag);

		assert !myRouteTagToRoutePathListeners.get(theRouteTag).contains(theListener);

		if (myRouteTagToRoutePath.containsKey(theRouteTag)) {
			theListener.objectLoaded(myRouteTagToRoutePath.get(theRouteTag), false);
		}

		myRouteTagToRoutePathListeners.get(theRouteTag).add(theListener);
	}


	public void addStreetcarLocationListListener(String theRouteTag, IModelListenerAsync<StreetcarLocationList> theListener) {
		ensureStreetcarLocationListListener(theRouteTag);
		myRouteTagToStreetcarLocationListListeners.get(theRouteTag).add(theListener);

		if (myRouteTagToStreetcarLocationList.containsKey(theRouteTag)) {
			theListener.objectLoaded(myRouteTagToStreetcarLocationList.get(theRouteTag), false);
		}
	}


	private void ensurePredictionListListener(String theStopTag) {
		if (myStopTagToPredictionListListeners.containsKey(theStopTag) == false) {
			myStopTagToPredictionListListeners.put(theStopTag, new ArrayList<IModelListenerAsync<PredictionsList>>());
		}
	}


	private void ensureRoutePathListeners(String theRouteTag) {
		if (!myRouteTagToRoutePathListeners.containsKey(theRouteTag)) {
			myRouteTagToRoutePathListeners.put(theRouteTag, new ArrayList<IModelListenerAsync<RoutePath>>());
		}
	}


	private void ensureStreetcarLocationListListener(String theRouteTag) {
		if (myRouteTagToStreetcarLocationListListeners.containsKey(theRouteTag) == false) {
			myRouteTagToStreetcarLocationListListeners.put(theRouteTag, new ArrayList<IModelListenerAsync<StreetcarLocationList>>());
		}
	}


	private void fireRecentStopsChanged() {
		for (IModelListenerSync<List<FavouriteStop>> next : myFavouriteStopListeners) {
			next.objectLoaded(myFavouriteStops);
		}
	}


	public Favourite getCurrentFavourite() {
		return myCurrentFavourite;
	}


	/**
	 * Only invoke this in situations where you are sure this has been loaded
	 */
	public StopListForRoute getStopListForRoute(String theRouteTag) {
		return myStopLists.get(theRouteTag);
	}


	/**
	 * Load a list of stops for a particular route tag and direction tag
	 */
	public void getStopListForRoute(final String theRouteTag, final IModelListenerAsync<StopListForRoute> theListener) {
		assert StringUtil.isNotBlank(theRouteTag);
		assert theListener != null;

		if (myStopLists.containsKey(theRouteTag)) {
			theListener.objectLoaded(myStopLists.get(theRouteTag), false);
			return;
		}

		// Map<String, StopList> stopList = DatabaseUtil.retriveStopList(theRouteTag);
		StopListForRoute stopList = null;
		// if (stopList != null) {
		// myStopLists.put(theRouteTag, stopList);
		// theListener.objectLoaded(myStopLists.get(theRouteTag), false);
		// return;
		// }

		// need to load from backend

		theListener.startLoadingObject();

		Common.SC_SVC_GRC.getStopListForRoute(theRouteTag, new AsyncCallback<StopListForRoute>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Common.handleUnexpectedError(theCaught);
			}


			@Override
			public void onSuccess(StopListForRoute theResult) {
				myStopLists.put(theRouteTag, theResult);
				// DatabaseUtil.storeStopList(theRouteTag, myStopLists.get(theRouteTag));
				theListener.objectLoaded(theResult, true);
			}
		});

	}


	// /**
	// * Load a list of stops for a particular route tag and direction tag
	// */
	// public void getDirectionToStopLists(final String theRouteTag, final String theDirectionTag, final
	// IModelListenerAsync<StopList> theListener) {
	// assert StringUtil.isNotBlank(theRouteTag);
	// assert StringUtil.isNotBlank(theDirectionTag);
	// assert theListener != null;
	//
	// if (myStopLists.containsKey(theRouteTag)) {
	// theListener.objectLoaded(myStopLists.get(theRouteTag).get(theDirectionTag), false);
	// return;
	// }
	//
	// Map<String, StopList> stopList = DatabaseUtil.retriveStopList(theRouteTag);
	// if (stopList != null) {
	// myStopLists.put(theRouteTag, stopList);
	// theListener.objectLoaded(myStopLists.get(theRouteTag).get(theDirectionTag), false);
	// return;
	// }
	//
	// // need to load from backend
	//
	// theListener.startLoadingObject();
	//
	// Common.SC_SVC_GRC.getStops(theRouteTag, theDirectionTag, new AsyncCallback<StopList>() {
	//
	// @Override
	// public void onFailure(Throwable theCaught) {
	// Common.handleUnexpectedError(theCaught);
	// }
	//
	//
	// @Override
	// public void onSuccess(StopList theResult) {
	// if (!myStopLists.containsKey(theRouteTag)) {
	// myStopLists.put(theRouteTag, new HashMap<String, StopList>());
	// }
	//
	// myStopLists.get(theRouteTag).put(theDirectionTag, theResult);
	// DatabaseUtil.storeStopList(theRouteTag, myStopLists.get(theRouteTag));
	// theListener.objectLoaded(theResult, true);
	// }
	// });
	//
	// }

	/**
	 * @return Return the current stored favourites
	 */
	public List<Favourite> getFavourites() {
		if (myFavourites == null) {
			myFavourites = DatabaseUtil.getFavourites();
		}
		return myFavourites;
	}


	// public TwitterResults fetchRecentMessagesForRoute(String theRouteTag) {
	//
	// String url = "http://search.twitter.com/search.json?q=&ands=%23ttcu+" + theRouteTag + "&result_type=recent";
	//
	// RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
	// try {
	// Request request = builder.sendRequest(null, new RequestCallback() {
	// public void onResponseReceived(Request request, Response response) {
	// if (200 == response.getStatusCode()) {
	// String res = response.getText();
	// TwitterResults results = TwitterResults.parseJson(res);
	// }
	// }
	//
	// @Override
	// public void onError(Request theRequest, Throwable theException) {
	// GWT.log("Error!", theException);
	// }
	// });
	// } catch (RequestException e) {
	// GWT.log("Error!", e);
	// }
	//
	//
	// }

	/**
	 * Returns the favourite stop matching a given stop tag, or null
	 */
	public FavouriteStop getFavouriteStop(String theStopTag) {
		for (FavouriteStop next : getRecentOrFavouriteStops()) {
			if (next.getStopTag().equals(theStopTag)) {
				return next;
			}
		}
		return null;
	}


	public List<FavouriteStop> getFavouriteStops() {
		List<FavouriteStop> all = getRecentOrFavouriteStops();
		ArrayList<FavouriteStop> retVal = new ArrayList<FavouriteStop>();
		for (FavouriteStop favouriteStop : all) {
			if (favouriteStop.isPinned() == true) {
				retVal.add(favouriteStop);
			}
		}
		return retVal;
	}


	/**
	 * Return right away if we have the particular list, or add the listener so that we will be notified when it shows
	 * up
	 */
	public void getPredictionListOrRegisterListener(String theStopTag, IModelListenerAsync<PredictionsList> theModelListener) {
		PredictionsList retVal = myStopTagToPredictionLists.get(theStopTag);

		if (retVal != null) {
			theModelListener.objectLoaded(retVal, false);
			return;
		}

		theModelListener.startLoadingObject();
		if (!myStopTagToPredictionListListeners.containsKey(theStopTag)) {
			myStopTagToPredictionListListeners.put(theStopTag, new ArrayList<IModelListenerAsync<PredictionsList>>());
		}
		myStopTagToPredictionListListeners.get(theStopTag).add(theModelListener);
	}


	public List<FavouriteStop> getRecentOrFavouriteStops() {
		if (myFavouriteStops != null) {
			return myFavouriteStops;
		}
		myFavouriteStops = DatabaseUtil.getFavouriteStops();
		return myFavouriteStops;
	}


	public List<FavouriteStop> getRecentStops() {
		List<FavouriteStop> all = getRecentOrFavouriteStops();
		ArrayList<FavouriteStop> retVal = new ArrayList<FavouriteStop>();
		for (FavouriteStop favouriteStop : all) {
			if (favouriteStop.isPinned() == false) {
				retVal.add(favouriteStop);
			}
		}
		return retVal;
	}


	public Route getRouteForStopTag(String theStopTag) {
		String routeTag = myStopTagsToRouteTags.get(theStopTag);
		if (routeTag == null) {
			return null;
		}
		return myRouteList.getRoute(routeTag);
	}


	public void getRouteList(final IModelListenerAsync<RouteList> theListener) {
		if (myRouteList != null) {
			theListener.objectLoaded(myRouteList, false);
			return;
		}

		theListener.startLoadingObject();
		Common.SC_SVC_GRC.getRouteList(new AsyncCallback<RouteList>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Common.handleUnexpectedError(theCaught);
			}


			@Override
			public void onSuccess(RouteList theResult) {
				myRouteList = theResult;
				theListener.objectLoaded(theResult, true);
			}
		});
	}


	/**
	 * @param theRouteTag
	 * @return
	 */
	public RoutePath getRoutePath(String theRouteTag) {
		return myRouteTagToRoutePath.get(theRouteTag);
	}


	public ArrayList<Route> getSelectedRoutes() {
		return mySelectedRoutes;
	}


	public Set<String> getSelectedRouteTags() {
		return mySelectedRouteTags;
	}


	public Stop getSelectedStop(String theStopTag) {
		for (Stop next : mySelectedStops) {
			if (next.getStopTag().equals(theStopTag)) {
				return next;
			}
		}
		return null;
	}


	public String getSelectedStopDirectionTag(String theStopTag) {
		return myStopTagsToDirectionTags.get(theStopTag);
	}


	public ArrayList<Stop> getSelectedStops() {
		return mySelectedStops;
	}


	public Set<String> getSelectedStopTags() {
		return mySelectedStopTags;
	}


	public Set<String> getSelectedStopTagsForRoute(String theRouteTag) {

		HashSet<String> retVal = new HashSet<String>();

		Set<String> selectedStopTags = getSelectedStopTags();
		if (selectedStopTags == null) {
			return retVal;
		}

		for (String next : selectedStopTags) {
			if (theRouteTag.equals(myStopTagsToRouteTags.get(next))) {
				retVal.add(next);
			}
		}

		return retVal;
	}


	public Map<String, String> getStopTagsToRouteTags() {
		return myStopTagsToRouteTags;
	}


	public StreetcarLocation getStreetcarLocation(String theRouteTag, String theVehicleId) {
		StreetcarLocationList list = myRouteTagToStreetcarLocationList.get(theRouteTag);
		if (list == null) {
			return null;
		} else {
			return list.getLocation(theVehicleId);
		}
	}


	public boolean isHaveAllStopLists(Collection<String> theRouteTags) {
		for (String next : theRouteTags) {
			if (!isHaveAllStopLists(next)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns true if we have all stop lists available for a given route tag
	 */
	public boolean isHaveAllStopLists(String theRouteTag) {
		assert StringUtil.isNotBlank(theRouteTag);

		if (myStopLists.get(theRouteTag) == null) {
			return false;
		}

		return true;

	}


	public boolean isHavePredictionForVehicle(String theVehicleTag) {
		return myPredictionVehicleTags.contains(theVehicleTag);
	}


	/**
	 * Do we have a route list loaded?
	 */
	public boolean isHaveRouteList() {
		return myRouteList != null;
	}


	public void removeFavourite(Favourite theFavourite) {
		List<Favourite> favourites = getFavourites();
		ArrayList<Favourite> oldValue = new ArrayList<Favourite>(favourites);

		favourites.remove(theFavourite);

		if (oldValue.size() != favourites.size()) {
			DatabaseUtil.setFavourites(favourites);
			firePropertyChange(FAVOURITE_LIST_PROPERTY, oldValue, favourites);
		}

	}


	public void removeFavouriteStopListener(IModelListenerSync<List<FavouriteStop>> theModelListener) {
		myFavouriteStopListeners.remove(theModelListener);
	}


	/**
	 * @param thePredictionListListener
	 */
	public void removePredictionListListener(IModelListenerAsync<Map<String, PredictionsList>> thePredictionListListener) {
		assert thePredictionListListener != null;
		assert myPredictionListListeners.contains(thePredictionListListener);

		myPredictionListListeners.remove(thePredictionListListener);
	}


	public void removePredictionListListener(String theRouteTag, IModelListenerAsync<PredictionsList> theListener) {
		ensurePredictionListListener(theRouteTag);
		myStopTagToPredictionListListeners.get(theRouteTag).remove(theListener);
	}


	/**
	 * Remove an entry from the favourite stops list
	 */
	public void removeRecentStop(FavouriteStop theFavourite) {
		myFavouriteStops.remove(theFavourite);
		DatabaseUtil.setFavouriteStops(myFavouriteStops);
		fireRecentStopsChanged();
	}


	/**
	 * Adds a listener for changes to the route path
	 */
	public void removeRoutePathListener(String theRouteTag, IModelListenerAsync<RoutePath> theListener) {
		ensureRoutePathListeners(theRouteTag);

		assert myRouteTagToRoutePathListeners.get(theRouteTag).contains(theListener);

		myRouteTagToRoutePathListeners.get(theRouteTag).remove(theListener);
	}


	public void removeStreetcarLocationListListener(String theRouteTag, IModelListenerAsync<StreetcarLocationList> theListener) {
		ensureStreetcarLocationListListener(theRouteTag);
		myRouteTagToStreetcarLocationListListeners.get(theRouteTag).remove(theListener);
	}


	public void setRouteList(RouteList theRouteList) {
		myRouteList = theRouteList;
	}


	public void setRoutePath(String theRouteTag, RoutePath theRoutePath) {
		ensureRoutePathListeners(theRouteTag);

		myRouteTagToRoutePath.put(theRouteTag, theRoutePath);

		for (IModelListenerAsync<RoutePath> next : myRouteTagToRoutePathListeners.get(theRouteTag)) {
			next.objectLoaded(theRoutePath, true);
		}

	}


	public void setSelectedRouteAndStopTags(Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags) {
		assert theSelectedRouteTags != null;

		Set<String> selectedStopTags = theSelectedStopTags;
		if (selectedStopTags == null) {
			selectedStopTags = Collections.emptySet();
		}

		// Set the two variables before firing changes so that any listeners that listen to
		// both properties can respond just once by remembering the previous values

		boolean routeChanged = false;
		Set<String> oldSelectedRouteTags = mySelectedRouteTags;
		Set<String> oldSelectedStopTags = mySelectedStopTags;

		if (mySelectedRouteTags == null || !mySelectedRouteTags.equals(theSelectedRouteTags)) {
			mySelectedRouteTags = theSelectedRouteTags;
			routeChanged = true;

			mySelectedRoutes = new ArrayList<Route>();
			for (String nextRouteTag : mySelectedRouteTags) {
				Route route = myRouteList.getRoute(nextRouteTag);
				if (route != null) {
					mySelectedRoutes.add(route);
				}
			}

		}

		boolean stopChanged = false;
		if (mySelectedStopTags == null || !mySelectedStopTags.equals(selectedStopTags)) {
			mySelectedStopTags = selectedStopTags;

			mySelectedStops = new ArrayList<Stop>();

			if (myStopLists != null) {
				for (String nextRouteTag : mySelectedRouteTags) {
					for (StopList nextStopLists : myStopLists.get(nextRouteTag).getUiStopLists()) {
						String nextDirectionTag = nextStopLists.getTag();

						for (Stop next : nextStopLists.getStops()) {
							if (mySelectedStopTags.contains(next.getStopTag())) {
								mySelectedStops.add(next);

								if (GWT.isProdMode() == false) {
									if (myStopTagsToDirectionTags.containsKey(next.getStopTag())) {
										assert false;
									}
								}

								myStopTagsToDirectionTags.put(next.getStopTag(), nextDirectionTag);

								if (mySelectedStops.size() == mySelectedStopTags.size()) {
									break;
								}
							}
						}
						if (mySelectedStops.size() == mySelectedStopTags.size()) {
							break;
						}
					}
					if (mySelectedStops.size() == mySelectedStopTags.size()) {
						break;
					}
				}
			}

			stopChanged = true;
		}

		if (routeChanged) {
			firePropertyChange(SELECTED_ROUTE_PROPERTY, oldSelectedRouteTags, mySelectedRouteTags);
		}

		if (stopChanged) {
			firePropertyChange(SELECTED_STOP_PROPERTY, oldSelectedStopTags, mySelectedStopTags);
		}

		if (stopChanged == false && routeChanged == false) {
			return;
		}

		// Check if the selected favourite has changed
		Favourite currentFavourite = HistoryUtil.getCurrentFavourite();
		if (currentFavourite == null) {

			// Look for a favourite matching the selected routes and stops
			for (Favourite next : getFavourites()) {
				if (next.matches(mySelectedRouteTags, mySelectedStopTags)) {
					currentFavourite = next;
					break;
				}
			}

			if (myCurrentFavourite != null) {
				Favourite oldValue = myCurrentFavourite;
				myCurrentFavourite = currentFavourite;
				firePropertyChange(SELECTED_FAVOURITE_PROPERTY, oldValue, myCurrentFavourite);
			}

		}

		// Fire a property change on the selected favourite if needed
		if (currentFavourite != null) {
			if (myCurrentFavourite == null || myCurrentFavourite.getId().equals(currentFavourite.getId()) == false) {
				Favourite oldValue = myCurrentFavourite;
				myCurrentFavourite = currentFavourite;
				firePropertyChange(SELECTED_FAVOURITE_PROPERTY, oldValue, myCurrentFavourite);
			}
		}

	}


	/**
	 * Returns true if we have all stop lists available for a given route tag
	 */
	public void addStopListForRoute(StopListForRoute theStopListForRoute) {
		myStopLists.put(theStopListForRoute.getRouteTag(), theStopListForRoute);
	}


	public void setStopTagsToRouteTags(Map<String, String> theStopTagsToRouteTags) {
		myStopTagsToRouteTags.putAll(theStopTagsToRouteTags);
	}


	public void setStreetcarLocationAndPredictionLists(Map<String, StreetcarLocationList> theRouteTagToStreetcarLocationList,
			Map<String, PredictionsList> theStopTagToPredictionList) {
		if (theRouteTagToStreetcarLocationList == null) {
			myRouteTagToStreetcarLocationList = new HashMap<String, StreetcarLocationList>();
		} else {
			myRouteTagToStreetcarLocationList = theRouteTagToStreetcarLocationList;
		}

		if (theStopTagToPredictionList == null) {
			myStopTagToPredictionLists = new HashMap<String, PredictionsList>();
		} else {
			myStopTagToPredictionLists = theStopTagToPredictionList;
		}

		myPredictionVehicleTags.clear();
		for (PredictionsList next : theStopTagToPredictionList.values()) {
			Set<String> tags = next.getVehicleTags();
			myPredictionVehicleTags.addAll(tags);
		}

		for (Entry<String, List<IModelListenerAsync<PredictionsList>>> nextEntry : myStopTagToPredictionListListeners.entrySet()) {
			String nextStopTag = nextEntry.getKey();
			PredictionsList predictionsForStop = myStopTagToPredictionLists.get(nextStopTag);

			if (predictionsForStop != null) {
				for (IModelListenerAsync<PredictionsList> nextListener : nextEntry.getValue()) {
					nextListener.objectLoaded(predictionsForStop, true);
				}
			}
		}

		for (IModelListenerAsync<Map<String, PredictionsList>> next : myPredictionListListeners) {
			next.objectLoaded(myStopTagToPredictionLists, true);
		}

		for (Entry<String, List<IModelListenerAsync<StreetcarLocationList>>> nextEntry : myRouteTagToStreetcarLocationListListeners.entrySet()) {
			for (IModelListenerAsync<StreetcarLocationList> nextListener : nextEntry.getValue()) {
				StreetcarLocationList locationList = myRouteTagToStreetcarLocationList.get(nextEntry.getKey());
				if (locationList != null) {
					nextListener.objectLoaded(locationList, true);
				}
			}
		}

	}
}
