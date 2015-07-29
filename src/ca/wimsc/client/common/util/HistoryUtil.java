package ca.wimsc.client.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.DatabaseUtil;
import ca.wimsc.client.common.model.Direction;
import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.OverlayMode;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.ShowTweetsMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.user.client.History;

public class HistoryUtil {

//	private static final String FALSE = "0";
	private static final String KEY_FAVOURITE_ID = "AFI";
	private static final String KEY_FAVOURITE_NAME = "FVN";
	private static final String KEY_INITIAL_BOUNDS = "INB";
	private static final String KEY_OVERLAY_MODE = "OVM";
	private static final String KEY_OVERLAY_HIDE_MODE = "OVH";
	private static final String KEY_ROUTE = "RTE";
	private static final String KEY_SHOW_ONLY_PREDICTIONS = "SOP";
	private static final String KEY_SHOW_SYSTEM_MAP = "SSM";
	private static final String KEY_STOP = "STP";

	private static String myCurrentToken;
	private static String myOverlayMode;
	private static boolean myShowStops;
	private static ShowTweetsMode myShowTweetsMode;
	private static Boolean ourNormalModeHideFavouritesPanel;
	private static final String TRUE = "1";
	private static final HashSet<OverlayMode> ourDefaultOverlayModes;

	static {
		ourDefaultOverlayModes = new HashSet<OverlayMode>();
		ourDefaultOverlayModes.add(OverlayMode.ROUTE_SPEED);
		ourDefaultOverlayModes.add(OverlayMode.RECENT_TWEETS);
	}


	private static void addToken(String theKey, String theValue) {
		String token = getToken();

		List<String> split = new ArrayList<String>(Arrays.asList(token.split("\\|")));
		for (String next : split) {
			if (next.equals(theKey + "_" + theValue)) {
				return;
			}
		}

		split.add(theKey + "_" + theValue);
		Collections.sort(split);

		StringBuilder builder = new StringBuilder();
		for (String string : split) {
			if (builder.length() > 0) {
				builder.append("|");
			}
			builder.append(string);
		}
		myCurrentToken = builder.toString();
	}


	private static void addTokenWithoutSort(String theKey, String theValue) {
		String token = getToken();
		myCurrentToken = token + "|" + theKey + "_" + theValue;
	}


	private static void apply() {
		apply(true);
	}


	private static void apply(boolean theFireEvents) {
		if (myCurrentToken != null && myCurrentToken.equals(History.getToken())) {
			myCurrentToken = null;
			return;
		}
		History.newItem(myCurrentToken, theFireEvents);
		myCurrentToken = null;
	}


	private static void clearCurrentTokenForFreshRebuild() {
		myCurrentToken = "";
	}


	public static String createLinkHere(Set<String> theRoutes, Set<String> theStops, HasLatLngBounds theBounds) {
		Set<OverlayMode> currentOverlayModes = getOverlayMode();

		clearCurrentTokenForFreshRebuild();

		for (String route : theRoutes) {
			addTokenWithoutSort(KEY_ROUTE, route);
		}
		for (String stop : theStops) {
			addTokenWithoutSort(KEY_STOP, stop);
		}

		for (OverlayMode next : currentOverlayModes) {
			if (!ourDefaultOverlayModes.contains(next)) {
				addTokenWithoutSort(KEY_OVERLAY_MODE, next.name());
			}
		}
		for (OverlayMode next : ourDefaultOverlayModes) {
			if (!currentOverlayModes.contains(next)) {
				addTokenWithoutSort(KEY_OVERLAY_HIDE_MODE, next.name());
			}
		}

		
		if (theBounds != null) {
			addTokenWithoutSort(KEY_INITIAL_BOUNDS, GeocoderUtil.toLatLngBoundsString(theBounds));
		}

		sortToken();
		String retVal = myCurrentToken;
		myCurrentToken = null;

		retVal = "http://whereismystreetcar.appspot.com/#" + retVal;

		return retVal;
	}


	public static String encodeStringWithFavourites(List<Favourite> theFavourites) {
		StringBuilder b = new StringBuilder();

		for (Favourite favourite : theFavourites) {
			if (b.length() > 0) {
				b.append("|");
			}
			b.append(getTokenForFavourite(favourite));
		}

		return b.toString();
	}


	/**
	 * @return Returns a favourite object if the current URL contains a ref to one, or null otherwise
	 */
	public static Favourite getCurrentFavourite() {
		if (!isCurrentlyShowingFavourite() == false) {
			return null;
		}

		List<Favourite> favs = parseStringForFavourites(getToken());
		if (favs.size() > 0) {
			return favs.get(0);
		} else {
			return null;
		}

	}


	public static String getCurrentFavouriteId() {
		return getFirstTokenWithKey(KEY_FAVOURITE_ID);
	}


	// public static String getDirection() {
	// List<String> tokensWithKey = getTokensWithKey(KEY_DIRECTION);
	// if (tokensWithKey.isEmpty()) {
	// return null;
	// }
	// return tokensWithKey.get(0);
	// }

	private static String getFirstTokenWithKey(String theKey) {
		List<String> tokens = getTokensWithKey(theKey);
		if (tokens == null || tokens.size() == 0) {
			return null;
		}
		return tokens.get(0);
	}


	public static HasLatLngBounds getInitialBounds() {
		String token = getTokenWithKey(KEY_INITIAL_BOUNDS);
		if (token == null) {
			return null;
		}

		return GeocoderUtil.fromLatLngBoundsString(token);
	}


	public static Set<OverlayMode> getOverlayMode() {

		List<String> overlayToken = getTokensWithKey(KEY_OVERLAY_MODE);
		List<String> overlayHideToken = getTokensWithKey(KEY_OVERLAY_HIDE_MODE);

		HashSet<OverlayMode> retVal = new HashSet<OverlayMode>();
		retVal.addAll(ourDefaultOverlayModes);

		if (overlayToken.size() > 0 || overlayHideToken.size() > 0) {
			for (String next : overlayToken) {
				try {
					retVal.add(OverlayMode.valueOf(next));
				} catch (IllegalArgumentException e) {
					GWT.log("Unknown overlaymode: " + next);
				}
			}
			for (String next : overlayHideToken) {
				try {
					retVal.remove(OverlayMode.valueOf(next));
				} catch (IllegalArgumentException e) {
					GWT.log("Unknown overlaymode: " + next);
				}
			}

			return retVal;
		}

		if (myOverlayMode == null) {
			String dbValue = DatabaseUtil.getOverlayMode();
			if (dbValue == null) {
				myOverlayMode = "";
			} else {
				myOverlayMode = dbValue;
			}
		}

		String[] showAndHide = myOverlayMode.split(",");

		if (showAndHide != null && showAndHide.length > 0) {
		String[] showValues = showAndHide[0].split("\\|");
		for (String string : showValues) {
			if (string.trim().length() == 0) {
				continue;
			}
			try {
				retVal.add(OverlayMode.valueOf(string));
			} catch (IllegalArgumentException e) {
				GWT.log("Unknown overlay mode: " + string, e);
			}
		}
		}
		
		if (showAndHide != null && showAndHide.length > 1) {
			String[] hideValues = showAndHide[1].split("\\|");
			for (String string : hideValues) {
				if (string.trim().length() == 0) {
					continue;
				}
				try {
					retVal.remove(OverlayMode.valueOf(string));
				} catch (IllegalArgumentException e) {
					GWT.log("Unknown overlay mode: " + string, e);
				}
			}
		}

		return retVal;
	}


	/**
	 * @return The currently selected route
	 */
	public static Set<String> getRoute() {
		List<String> retVal = getTokensWithKey(KEY_ROUTE);
		if (retVal.isEmpty()) {
			return null;
		} else {
			return new HashSet<String>(retVal);
		}
	}


	public static ShowTweetsMode getShowTweetsMode() {
		if (myShowTweetsMode == null) {
			String stored = DatabaseUtil.getShowTweetsMode();
			try {
				myShowTweetsMode = ShowTweetsMode.valueOf(stored);
			} catch (Exception e) {
				// ignore
			}
		}

		if (myShowTweetsMode == null) {
			myShowTweetsMode = ShowTweetsMode.SHOW_ONE;
		}

		return myShowTweetsMode;
	}


	public static Set<String> getStop() {
		List<String> tokensWithKey = getTokensWithKey(KEY_STOP);
		if (tokensWithKey.isEmpty()) {
			return null;
		}
		return new HashSet<String>(tokensWithKey);
	}


	private static String getToken() {
		if (myCurrentToken != null) {
			return myCurrentToken;
		}

		String token = History.getToken();
		if (token == null) {
			token = "";
		}
		return token;
	}


	public static String getTokenForFavourite(Favourite theFavourite) {
		clearCurrentTokenForFreshRebuild();

		for (String nextRoute : theFavourite.getRouteTags()) {
			addTokenWithoutSort(KEY_ROUTE, nextRoute);
		}

		for (String nextStop : theFavourite.getStopTags()) {
			addTokenWithoutSort(KEY_STOP, nextStop);
		}

		if (theFavourite.isShowPredictionsOnly()) {
			addTokenWithoutSort(KEY_SHOW_ONLY_PREDICTIONS, TRUE);
		}

		if (theFavourite.getBounds() != null) {
			addTokenWithoutSort(KEY_INITIAL_BOUNDS, GeocoderUtil.toLatLngBoundsString(theFavourite.getBounds()));
		}

		addTokenWithoutSort(KEY_FAVOURITE_NAME, StringUtil.stripSubstrings(theFavourite.getName(), "|", "_"));

		addTokenWithoutSort(KEY_FAVOURITE_ID, theFavourite.getId());
		sortToken();

		String token = myCurrentToken;
		myCurrentToken = null;

		assert token.startsWith(KEY_FAVOURITE_ID);

		return token;
	}


	// public static void initDirectionVisible(String theTag) {
	// if (theTag == null) {
	// throw new NullPointerException("Null direction");
	// }
	//
	// removeTokens(KEY_DIRECTION);
	// addToken(KEY_DIRECTION, theTag);
	// apply(false);
	// }

	// public static void setDirectionVisible(String theDirection) {
	// if (theDirection == null) {
	// throw new NullPointerException("Null direction");
	// }
	//
	// removeTokens(KEY_DIRECTION);
	// addToken(KEY_DIRECTION, theDirection);
	// apply(true);
	// }

	// /**
	// * Are we displaying a particular direction?
	// */
	// public static void setDirectionVisible(String theTag, boolean theVisible) {
	// if (theTag == null) {
	// throw new NullPointerException("Null direction");
	// }
	//
	// if (theVisible) {
	// addToken(KEY_DIRECTION, theTag);
	// } else {
	// removeToken(KEY_DIRECTION, theTag);
	// }
	// apply(true);
	// }

	// public static Set<String> getDirections() {
	// HashSet<String> retVal = new HashSet<String>();
	// List<String> tokens = getTokensWithKey(KEY_DIRECTION);
	// retVal.addAll(tokens);
	// return retVal;
	// }

	public static String getTokenForNewRoute(Route theRoute, Direction theDirection) {
		if (theDirection == null) {
			throw new NullPointerException("Null direction");
		}

		removeTokens(KEY_STOP);

		removeTokens(KEY_ROUTE);
		addToken(KEY_ROUTE, theRoute.getTag());
		String retVal = getToken();
		myCurrentToken = null;

		return retVal;
	}


	public static String getTokenForNewStop(String theRouteTag, String theStopTag) {
		clearCurrentTokenForFreshRebuild();

		addTokenWithoutSort(KEY_ROUTE, theRouteTag);
		addTokenWithoutSort(KEY_STOP, theStopTag);

		sortToken();

		String retVal = myCurrentToken;
		myCurrentToken = null;
		return retVal;
	}


	public static String getTokenShowSystemMap() {
		addToken(KEY_SHOW_SYSTEM_MAP, "true");

		String retVal = myCurrentToken;
		myCurrentToken = null;
		return retVal;
	}


	private static List<String> getTokensWithKey(String theKey) {
		ArrayList<String> retVal = new ArrayList<String>();
		String token = getToken();

		String[] split = token.split("\\|");
		for (String next : split) {
			if (next.startsWith(theKey + "_")) {
				retVal.add(next.substring(theKey.length() + 1));
			}
		}

		return retVal;
	}


	private static String getTokenWithKey(String theKey) {
		List<String> tokens = getTokensWithKey(theKey);
		if (tokens.size() > 0) {
			return tokens.get(0);
		}
		return null;
	}


	public static boolean hasRouteOrStopToken() {
		return hasTokenWithKey(KEY_ROUTE, KEY_STOP);
	}


	private static boolean hasTokenWithKey(String... theKeys) {
		String token = getToken();
		for (String string : theKeys) {
			if (token.contains(string + "_")) {
				return true;
			}
		}
		return false;
	}


	public static boolean isCurrentlyShowingFavourite() {
		return getToken().contains(KEY_FAVOURITE_ID);
	}


	public static boolean isNormalModeHideFavouritesPanel() {
		if (ourNormalModeHideFavouritesPanel == null) {
			ourNormalModeHideFavouritesPanel = DatabaseUtil.isNormalModeHideFavouritesPanel();
		}
		return ourNormalModeHideFavouritesPanel;
	}


	public static boolean isShowOnlyPredictions() {
		return getTokensWithKey(KEY_SHOW_ONLY_PREDICTIONS).contains(TRUE);
	}


	public static boolean isShowStops() {
		return myShowStops;
	}


	public static boolean isShowSystemMap() {
		return getTokensWithKey(KEY_SHOW_SYSTEM_MAP).contains(TRUE);
	}


	/**
	 * Parse a combined token with 0 or more favourites
	 */
	public static List<Favourite> parseStringForFavourites(String theToken) {
		List<Favourite> retVal = new ArrayList<Favourite>();

		if (StringUtil.isBlank(theToken)) {
			return retVal;
		}

		String[] parts = theToken.split("\\|");

		Favourite current = null;
		for (int i = 0; i < parts.length; i++) {
			String nextToken = parts[i];
			int uIndex = nextToken.indexOf('_');
			if (uIndex == -1) {
				GWT.log("No '_' in token: " + nextToken);
				continue;
			}

			String nextKey = nextToken.substring(0, uIndex);
			String nextValue = nextToken.substring(uIndex + 1);

			if (KEY_FAVOURITE_ID.equals(nextKey)) {

				if (current != null) {
					retVal.add(current);
				}
				current = new Favourite();
				current.setId(nextValue);

			} else if (current == null) {

				continue;

			} else if (KEY_FAVOURITE_NAME.equals(nextKey)) {

				current.setName(nextValue);

			} else if (KEY_ROUTE.equals(nextKey)) {

				current.getRouteTags().add(nextValue);

			} else if (KEY_STOP.equals(nextKey)) {

				current.getStopTags().add(nextValue);

			} else if (KEY_SHOW_ONLY_PREDICTIONS.equals(nextKey)) {

				current.setShowPredictionsOnly(TRUE.equals(nextValue));

			} else if (KEY_INITIAL_BOUNDS.equals(nextKey)) {

				current.setBounds(GeocoderUtil.fromLatLngBoundsString(nextValue));

			}

		}

		if (current != null) {
			retVal.add(current);
		}

		return retVal;
	}


	private static void removeTokens(String theKey) {
		String token = getToken();

		StringBuilder builder = new StringBuilder();
		String[] split = token.split("\\|");
		for (String next : split) {
			if (next.startsWith(theKey + "_")) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('|');
			}
			builder.append(next);
		}

		myCurrentToken = builder.toString();
	}


	public static void setFavourite(Favourite theFavourite) {
		myCurrentToken = getTokenForFavourite(theFavourite);
		apply();
	}


	public static void setHideSystemMap() {
		removeTokens(KEY_SHOW_SYSTEM_MAP);
		apply();
	}


	public static void setNormalModeHideFavouritesPanel(boolean theNormalModeHideFavouritesPanel) {
		DatabaseUtil.setNormalModeHideFavouritesPanel(theNormalModeHideFavouritesPanel);
		ourNormalModeHideFavouritesPanel = theNormalModeHideFavouritesPanel;
	}


	/**
	 * @param theLayers
	 */
	public static void setOverlayMode(Set<OverlayMode> theModes) {
		assert theModes != null && theModes.size() > 0;

		// We're storing this in the database, so clear any URL keys
		// to prevent the database values from being overwritten
		removeTokens(KEY_OVERLAY_MODE);
		removeTokens(KEY_OVERLAY_HIDE_MODE);
		apply(false);

		StringBuilder b = new StringBuilder();

		// Modes which are not shown by default
		boolean first = true;
		for (OverlayMode next : theModes) {
			if (ourDefaultOverlayModes.contains(next)) {
				continue;
			}

			if (first) {
				first = false;
			} else {
				b.append("|");
			}
			b.append(next.name());
		}

		// Modes which are shown by default
		b.append(",");
		first = true;
		for (OverlayMode next : ourDefaultOverlayModes) {
			if (theModes.contains(next)) {
				continue;
			}

			if (first) {
				first = false;
			} else {
				b.append("|");
			}
			b.append(next.name());
		}

		myOverlayMode = b.toString();
		DatabaseUtil.setOverlayMode(myOverlayMode);
	}


	/**
	 * Sets the route and direction and clears the selected stop
	 */
	public static void setRoute(Route theRoute, Direction theDirection) {
		myCurrentToken = getTokenForNewRoute(theRoute, theDirection);
		apply();
	}


	/**
	 * @param theTag
	 *            Set the current displayed route
	 */
	public static void setRoute(String theTag) {
		removeTokens(KEY_ROUTE);
		addToken(KEY_ROUTE, theTag);
		apply();
	}


	public static void setRoutesAndStops(Set<String> theRouteTags, Set<String> theStopTags, boolean theShowOnlyPredictions) {
		clearCurrentTokenForFreshRebuild();

		for (String next : theRouteTags) {
			addTokenWithoutSort(KEY_ROUTE, next);
		}

		for (String next : theStopTags) {
			addTokenWithoutSort(KEY_STOP, next);
		}

		if (theShowOnlyPredictions) {
			addTokenWithoutSort(KEY_SHOW_ONLY_PREDICTIONS, TRUE);
		}

		sortToken();

		apply();
	}


	public static void setShowStops(boolean theValue) {
		myShowStops = theValue;
	}


	public static void setShowTweetsMode(ShowTweetsMode theShowTweetsMode) {
		assert theShowTweetsMode != null;

		myShowTweetsMode = theShowTweetsMode;
		DatabaseUtil.setShowTweetsMode(theShowTweetsMode.name());
	}


	public static void setStop(String theStop) {
		removeTokens(KEY_STOP);
		addToken(KEY_STOP, theStop);
		apply();
	}


	public static void setStop(String theRouteTag, String theStopTag) {
		myCurrentToken = getTokenForNewStop(theRouteTag, theStopTag);
		apply();
	}


	private static void sortToken() {
		String token = getToken();

		List<String> split = new ArrayList<String>(Arrays.asList(token.split("\\|")));
		Collections.sort(split);

		StringBuilder builder = new StringBuilder();
		for (String string : split) {
			if (builder.length() > 0) {
				builder.append("|");
			}
			builder.append(string);
		}
		myCurrentToken = builder.toString();
	}
}
