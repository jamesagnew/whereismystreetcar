package ca.wimsc.client.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.util.HistoryUtil;

import com.google.code.gwt.storage.client.Storage;
import com.google.gwt.core.client.GWT;

/**
 * Long term storage, to keep data transfers fast
 */
public class DatabaseUtil {

	private static final String SHOW_TWEETS_MODE_KEY = "STM_";
	private static final String OVERLAY_MODE_KEY = "OM_";
	private static final String STOP_LIST_KEY = "STPL_";
	private static final String FAVOURITE_STOPS_KEY = "FSL_";
	private static final String NORMAL_MODE_HIDE_FAVOURITES_PANEL = "NMHF_";
	private static final String FAVOURITES_LIST_KEY = "FL_";

	public static final int CURRENT_CONFIG_VERSION = 1;


	public static void storeStopList_(String theRouteTag, Map<String, StopList> theStopList) {
		try {
			String serialized = StopList.toSerializedString_(theStopList);
			// Date expiry = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L));

			Storage storage = Storage.getLocalStorage();
			if (storage != null) {
				GWT.log("Storing stop list to local storage");
				storage.setItem(STOP_LIST_KEY + theRouteTag, serialized);
			}
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}

	}


	public static Map<String, StopList> retriveStopList_(String theRouteTag) {
		try {
			Storage storage = Storage.getLocalStorage();
			if (storage != null) {
				GWT.log("Trying to retrieving stop list for " + theRouteTag + " to local storage");
				String string = storage.getItem(STOP_LIST_KEY + theRouteTag);

				Map<String, StopList> fromSerializedString = StopList.fromSerializedString_(string);
				// GWT.log("Retrieved " + fromSerializedString);
				return fromSerializedString;
			}
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}

		return null;
	}


	public static List<FavouriteStop> getFavouriteStops() {
		try {
			Storage storage = Storage.getLocalStorage();
			if (storage != null) {
				String wholeString = storage.getItem(FAVOURITE_STOPS_KEY);
				if (wholeString == null || wholeString.length() == 0) {
					return new ArrayList<FavouriteStop>();
				}

				List<FavouriteStop> retVal = new ArrayList<FavouriteStop>();
				String[] parts = wholeString.split("\\" + Stop.SER_DELIM_ALT);
				for (String nextString : parts) {
					FavouriteStop favouriteStop = new FavouriteStop();
					favouriteStop.deserializeFromString(nextString);
					retVal.add(favouriteStop);
				}

				return retVal;
			}
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}

		return new ArrayList<FavouriteStop>();
	}


	public static void setFavouriteStops(List<FavouriteStop> theStops) {
		try {
			Storage storage = Storage.getLocalStorage();
			if (storage != null) {

				StringBuilder retVal = new StringBuilder();
				for (FavouriteStop next : theStops) {
					if (retVal.length() > 0) {
						retVal.append(Stop.SER_DELIM_ALT);
					}

					next.serializeToString(retVal);
				}

				storage.setItem(FAVOURITE_STOPS_KEY, retVal.toString());
			}
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}

	}


	public static String getOverlayMode() {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			return storage.getItem(OVERLAY_MODE_KEY);
		} else {
			return null;
		}
	}


	public static void setOverlayMode(String theOverlayMode) {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			storage.setItem(OVERLAY_MODE_KEY, theOverlayMode);
		}
	}


	public static void setShowTweetsMode(String theShowTweetsMode) {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			storage.setItem(SHOW_TWEETS_MODE_KEY, theShowTweetsMode);
		}
	}


	public static String getShowTweetsMode() {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			return storage.getItem(SHOW_TWEETS_MODE_KEY);
		} else {
			return null;
		}
	}


	public static boolean isNormalModeHideFavouritesPanel() {
		boolean retVal = false;
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			retVal = Boolean.parseBoolean(storage.getItem(NORMAL_MODE_HIDE_FAVOURITES_PANEL));
		}
		return retVal;
	}


	public static void setNormalModeHideFavouritesPanel(boolean theValue) {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			storage.setItem(NORMAL_MODE_HIDE_FAVOURITES_PANEL, Boolean.toString(theValue));
		}
	}


	public static List<Favourite> getFavourites() {
		ArrayList<Favourite> retVal = new ArrayList<Favourite>();
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			String favouritesList = storage.getItem(FAVOURITES_LIST_KEY);

			// Check if there are any legacy favourites kicking around
			if (favouritesList == null) {
				for (FavouriteStop next : getFavouriteStops()) {
					if (next.isPinned()) {
						Favourite fav = new Favourite();
						fav.setId(Long.toString(retVal.size()));
						fav.setName(next.getAssignedName());
						ArrayList<String> routeTags = new ArrayList<String>();
						routeTags.add(next.getRouteTag());
						fav.setRouteTags(routeTags);
						ArrayList<String> stopTags = new ArrayList<String>();
						stopTags.add(next.getStopTag());
						fav.setStopTags(stopTags);
						retVal.add(fav);
					}
				}
				storage.removeItem(FAVOURITE_STOPS_KEY);
			}

			if (favouritesList != null) {
				retVal.addAll(HistoryUtil.parseStringForFavourites(favouritesList));
			}
		}

		return retVal;
	}


	public static void setFavourites(List<Favourite> theFavourites) {
		Storage storage = Storage.getLocalStorage();
		if (storage != null) {
			String favourites = HistoryUtil.encodeStringWithFavourites(theFavourites);
			storage.setItem(FAVOURITES_LIST_KEY, favourites);
		}
	}

}
