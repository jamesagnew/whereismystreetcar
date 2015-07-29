package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.util.Pair;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class StopList implements Serializable, IsSerializable {

	private static final long serialVersionUID = 3L;
	private static final String STOP_LIST_SERIALIZATION_PREFIX = "StopList_" + serialVersionUID;
	private boolean myDiversion;
	private String myName;
	private boolean myShortTurn;
	private StopListForRoute myStopListForRoute;
	private transient List<Stop> myStops;
	private List<String> myStopTags;
	private transient Map<String, Integer> myStopTagToIndex;
	private String myTag;
	private String myTitle;


	public String findClosestStopTag(double theLatitude, double theLongitude) {
		Stop found = null;
		double distance = 0;
		for (Stop next : getStops()) {
			double nextDistance = next.distanceFromInKms(theLatitude, theLongitude);
			if (found == null || distance > nextDistance) {
				distance = nextDistance;
				found = next;
			}
		}

		return found != null ? found.getStopTag() : null;
	}


	/**
	 * Find the closest stop with a particular distance
	 */
	public Pair<Stop, Long> findClosestStopWithDistance(double theLatitude, double theLongitude) {
		Stop found = null;
		double distance = 0;
		for (Stop next : getStops()) {
			double nextDistance = next.distanceFromInKms(theLatitude, theLongitude);
			if (found == null || distance > nextDistance) {
				distance = nextDistance;
				found = next;
			}
		}

		long distanceMeters = (long) (distance * 1000);
		return found != null ? new Pair<Stop, Long>(found, distanceMeters) : null;
	}


	/**
	 * Does this list have a stop with any of the given tags
	 */
	public Stop findFirstStopWithTag(Set<String> theSelectedStopTag) {
		if (theSelectedStopTag == null || theSelectedStopTag.isEmpty()) {
			return null;
		}
		for (Stop next : getStops()) {
			if (theSelectedStopTag.contains(next.getStopTag())) {
				return next;
			}
		}
		return null;
	}


	public Stop findFirstStopWithTag(String theStopTag) {
		if (theStopTag == null) {
			return null;
		}
		for (Stop next : getStops()) {
			if (theStopTag.equals(next.getStopTag())) {
				return next;
			}
		}
		return null;
	}


	/**
	 * @return
	 */
	public DirectionEnum getDirectionEnum() {
		return DirectionEnum.fromNameOrTitle(myName);
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	
	public List<String> getStopTags() {
		return myStopTags;
	}
	

	public List<Stop> getStops() {
		if (myStops == null) {
			myStops = new ArrayList<Stop>();
			for (String next : myStopTags) {
				Stop nextStop = myStopListForRoute.getStopTagToStop().get(next);
				if (nextStop == null) {
					GWT.log("** Unknown stop! " + next);
					assert false;
					continue;
				}

				myStops.add(nextStop);
			}
		}
		return myStops;
	}


	public int getStopTagIndex(String theStopTag) {
		assert StringUtil.isNotBlank(theStopTag);

		if (myStopTagToIndex == null) {
			myStopTagToIndex = new HashMap<String, Integer>();
			List<Stop> stops = getStops();
			for (int i = 0; i < stops.size(); i++) {
				myStopTagToIndex.put(stops.get(i).getStopTag(), Integer.valueOf(i));
			}
		}

		Integer retVal = myStopTagToIndex.get(theStopTag);
		if (retVal == null) {
			return -1;
		} else {
			return retVal.intValue();
		}
	}


	/**
	 * @return the tag
	 */
	public String getTag() {
		return myTag;
	}


	/**
	 * @return the title
	 */
	public String getTitle() {
		return myTitle != null ? myTitle : "";
	}


	/**
	 * Does this list have a stop with the given tag
	 */
	public boolean hasStopWithTag(String theStopTag) {
		assert StringUtil.isNotBlank(theStopTag);

		return myStopTags.contains(theStopTag);
	}


	/**
	 * @return the diversion
	 */
	public boolean isDiversion() {
		return myDiversion;
	}


	/**
	 * @return the shortTurn
	 */
	public boolean isShortTurn() {
		return myShortTurn;
	}


	/**
	 * @param theDiversion
	 *            the diversion to set
	 */
	public void setDiversion(boolean theDiversion) {
		myDiversion = theDiversion;
	}


	/**
	 * @param theName
	 *            the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}


	/**
	 * @param theShortTurn
	 *            the shortTurn to set
	 */
	public void setShortTurn(boolean theShortTurn) {
		myShortTurn = theShortTurn;
	}


	void setStopListForRoute(StopListForRoute theStopListForRoute) {
		myStopListForRoute = theStopListForRoute;
	}


	public void setStopTags(List<String> theStopTagList) {
		myStopTags = theStopTagList;
	}


	/**
	 * @param theTag
	 *            the tag to set
	 */
	public void setTag(String theTag) {
		myTag = theTag;
	}


	/**
	 * @param theTitle
	 *            the title to set
	 */
	public void setTitle(String theTitle) {
		myTitle = theTitle;
	}


	public static Map<String, StopList> fromSerializedString_(String theString) {
		if (theString == null) {
			return null;
		}

		if (!theString.startsWith(STOP_LIST_SERIALIZATION_PREFIX)) {
			return null;
		}

		try {
			HashMap<String, StopList> retVal = new HashMap<String, StopList>();

			theString = theString.substring(STOP_LIST_SERIALIZATION_PREFIX.length());

			String[] mapEntryStrings = theString.split("\\`");
			for (String nextMapEntryParts : mapEntryStrings) {
				String[] mapEntryParts = nextMapEntryParts.split("\\@");

				String nextDirectionTag = mapEntryParts[0];

				List<Stop> stops = new ArrayList<Stop>();
				String[] stopStrings = mapEntryParts[1].split("\\}");
				for (String nextString : stopStrings) {

					Stop nextStop = new Stop();
					nextStop.deserializeFromString(nextString);
					stops.add(nextStop);

				}

				StopList stopList = new StopList();
				// stopList.setStops(stops);

				retVal.put(nextDirectionTag, stopList);

			}

			return retVal;

		} catch (Exception e) {
			return null;
		}

	}


	/**
	 * TODO: this really needs a unit test!
	 */
	public static String toSerializedString_(Map<String, StopList> theStopLists) {
		StringBuilder builder = new StringBuilder();

		builder.append(STOP_LIST_SERIALIZATION_PREFIX);

		boolean firstEntry = true;
		for (Map.Entry<String, StopList> nextEntry : theStopLists.entrySet()) {
			if (!firstEntry) {
				builder.append("`");
			} else {
				firstEntry = false;
			}

			builder.append(nextEntry.getKey());
			builder.append("@");

			boolean firstStop = true;
			for (Stop next : nextEntry.getValue().getStops()) {

				if (!firstStop) {
					builder.append("}");
				} else {
					firstStop = false;
				}

				next.serializeToString(builder);
			}
		}

		return builder.toString();
	}


	/**
	 * @return
	 */
	public String getShortTitle() {
		return getTitle().replaceAll("-.*owards ", "- ");	
		}

}
