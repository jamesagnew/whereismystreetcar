/**
 * 
 */
package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.util.GeocoderUtil;
import ca.wimsc.client.common.util.ObjectUtil;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Maps out the path of a given route
 */
public class RoutePath implements Serializable, IsSerializable {

	private static final char MARSHALL_DIV = ',';
	private static final long serialVersionUID = 2L;

	private String myDirection1Tag;
	private String myDirection2Tag;
	private Date myLastUpdatedTimestamp;
	private List<RoutePathElement> myRoutePathElements;
	private transient List<Map<String, RoutePathElement>> myStopTagToRoutePathElements;


	/**
	 * Constructor
	 */
	public RoutePath() {
		// nothing
	}

	/**
	 * Unmarshalling constructor
	 */
	public RoutePath(String theMarshalledString) {
		if (theMarshalledString == null || theMarshalledString.length() == 0) {
			return;
		}

		String[] split = theMarshalledString.split(",");
		int index = 0;

		// version
		int version = parseInt(split[index]);
		index++;

		myLastUpdatedTimestamp = new Date(parseLong(split[index++]));

		// direction count
		index++;

		myDirection1Tag = split[index++];
		myDirection2Tag = split[index++];

		myRoutePathElements = new ArrayList<RoutePathElement>();
		String previousClosestStopDirection1 = "";
		String previousClosestStopDirection2 = "";
		double previousLat = 0.0;
		double previousLon = 0.0;

		while (index < split.length) {

			RoutePathElement nextElement = new RoutePathElement();
			myRoutePathElements.add(nextElement);

			nextElement.setNewPathEntry("1".equals(split[index++]));

			String nextClosestStopTagDirection1 = split[index++];
			if (nextClosestStopTagDirection1.length() > 0) {
				previousClosestStopDirection1 = nextClosestStopTagDirection1;
			}
			nextElement.setClosestStopTagDirection1(previousClosestStopDirection1);

			String nextClosestStopTagDirection2 = split[index++];
			if (nextClosestStopTagDirection2.length() > 0) {
				previousClosestStopDirection2 = nextClosestStopTagDirection2;
			}
			nextElement.setClosestStopTagDirection2(previousClosestStopDirection2);

			previousLat = GeocoderUtil.decodeLatOrLonDeltaFromString(split[index++], previousLat);
			nextElement.setLatitude(previousLat);

			previousLon = GeocoderUtil.decodeLatOrLonDeltaFromString(split[index++], previousLon);
			nextElement.setLongitude(previousLon);

			if (index == split.length) {
				break;
			}

			String nextSpeed1 = split[index++];
			if (nextSpeed1.length() > 0) {
				int previousSpeedDirection1 = parseInt(nextSpeed1);
				nextElement.setSpeedInKmhDirection1(previousSpeedDirection1);
			}

			if (index == split.length) {
				break;
			}

			String nextSpeed2 = split[index++];
			if (nextSpeed2.length() > 0) {
				int previousSpeedDirection2 = parseInt(nextSpeed2);
				nextElement.setSpeedInKmhDirection2(previousSpeedDirection2);
			}

			if (version >= 2) {
				String heading = split[index++];
				int headingInt = parseInt(heading);
				nextElement.setHeadingToNextStop(headingInt);
			}
			
			for (long ver = serialVersionUID; ver < version; ver++) {
				index++;
			}

		}

		// // Check if the headings are set. If not, set them
		// if (myRoutePathElements.get(0).getHeadingToNextStop() == RoutePathElement.DEFAULT_HEADING_TO_NEXT_STOP) {
		//
		// }

	}

	public void calculateRouteHeadings() {

		if (myRoutePathElements.get(0).getHeadingToNextStop() != RoutePathElement.DEFAULT_HEADING_TO_NEXT_STOP) {
			return;
		}
		
		for (int i = 0; i < myRoutePathElements.size() - 1; i++) {
			RoutePathElement current = myRoutePathElements.get(i);
			RoutePathElement next = myRoutePathElements.get(i + 1);
			
			int heading = GeocoderUtil.getAngleFromPointAToPointBInDegrees(current.getLatitude(), current.getLongitude(), next.getLatitude(), next.getLongitude());
			current.setHeadingToNextStop(heading);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof RoutePath)) {
			return false;
		}

		RoutePath obj = (RoutePath) theObj;
		boolean retVal = ObjectUtil.equals(myDirection1Tag, obj.myDirection1Tag);
		retVal &= ObjectUtil.equals(myDirection2Tag, obj.myDirection2Tag);
		retVal &= ObjectUtil.equals(myRoutePathElements, obj.myRoutePathElements);

		return retVal;
	}


	/**
	 * @return the direction1Tag
	 */
	public String getDirection1Tag() {
		return myDirection1Tag;
	}


	/**
	 * @return the direction2Tag
	 */
	public String getDirection2Tag() {
		return myDirection2Tag;
	}


	/**
	 * @return
	 */
	public List<String> getDirectionTags() {
		ArrayList<String> retVal = new ArrayList<String>();
		retVal.add(myDirection1Tag);
		retVal.add(myDirection2Tag);
		return retVal;
	}


	/**
	 * @return the lastUpdatedTimestamp
	 */
	public Date getLastUpdatedTimestamp() {
		if (myLastUpdatedTimestamp == null) {
			return new Date(1);
		}
		return myLastUpdatedTimestamp;
	}


	/**
	 * @param theDirectionIndex
	 * @param theNextStopTag
	 * @return
	 */
	public RoutePathElement getRoutePathElementClosestToStopTag(int theDirectionIndex, String theStopTag) {
		if (myStopTagToRoutePathElements == null) {
			myStopTagToRoutePathElements = new ArrayList<Map<String, RoutePathElement>>();
		}

		while (myStopTagToRoutePathElements.size() <= theDirectionIndex) {
			HashMap<String, RoutePathElement> hashMap = new HashMap<String, RoutePathElement>();

			int size = myStopTagToRoutePathElements.size();
			for (RoutePathElement next : myRoutePathElements) {
				String closestStopTag = next.getClosestStopTagDirection(size);
				hashMap.put(closestStopTag, next);
			}

			myStopTagToRoutePathElements.add(hashMap);
		}

		return myStopTagToRoutePathElements.get(theDirectionIndex).get(theStopTag);
	}


	/**
	 * @return the routePathElements
	 */
	public List<RoutePathElement> getRoutePathElements() {
		return myRoutePathElements;
	}


	public String marshall() {
		StringBuilder b = new StringBuilder();

		b.append(serialVersionUID);
		b.append(MARSHALL_DIV);
		b.append(Long.toString(getLastUpdatedTimestamp().getTime(), Character.MAX_RADIX));
		b.append(MARSHALL_DIV);
		b.append(2); // direction count
		b.append(MARSHALL_DIV);
		b.append(myDirection1Tag);
		b.append(MARSHALL_DIV);
		b.append(myDirection2Tag);
		b.append(MARSHALL_DIV);

		int previousLatitude = 0;
		int previousLongitude = 0;
		String previousDirection1ClosestStopTag = null;
		String previousDirection2ClosestStopTag = null;
		for (RoutePathElement next : myRoutePathElements) {

			if (next.isNewPathEntry()) {
				b.append("1");
			}
			b.append(MARSHALL_DIV);

			if (next.getClosestStopTagDirection1() != null) {
				if (next.getClosestStopTagDirection1().equals(previousDirection1ClosestStopTag)) {
					b.append(MARSHALL_DIV);
				} else {
					b.append(next.getClosestStopTagDirection1());
					b.append(MARSHALL_DIV);
				}
			} else {
				b.append(MARSHALL_DIV);
			}

			if (next.getClosestStopTagDirection2() != null) {
				if (next.getClosestStopTagDirection2().equals(previousDirection2ClosestStopTag)) {
					b.append(MARSHALL_DIV);
				} else {
					b.append(next.getClosestStopTagDirection2());
					b.append(MARSHALL_DIV);
				}
			} else {
				b.append(MARSHALL_DIV);
			}

			int nextLat = GeocoderUtil.encodeLatOrLonDelta(next.getLatitude(), previousLatitude);
			previousLatitude += nextLat;
			b.append(Integer.toString(nextLat, Character.MAX_RADIX));
			b.append(MARSHALL_DIV);

			int nextLon = GeocoderUtil.encodeLatOrLonDelta(next.getLongitude(), previousLongitude);
			previousLongitude += nextLon;
			b.append(Integer.toString(nextLon, Character.MAX_RADIX));
			b.append(MARSHALL_DIV);

			int nextSpeed1 = next.getSpeedInKmhDirection1();
			b.append(Integer.toString(nextSpeed1, Character.MAX_RADIX));
			b.append(MARSHALL_DIV);

			int nextSpeed2 = next.getSpeedInKmhDirection2();
			b.append(Integer.toString(nextSpeed2, Character.MAX_RADIX));
			b.append(MARSHALL_DIV);

			int heading = next.getHeadingToNextStop();
			b.append(Integer.toString(heading, Character.MAX_RADIX));
			b.append(MARSHALL_DIV);
			
		}

		return b.toString();
	}


	/**
	 * @param theDirection1Tag
	 *            the direction1Tag to set
	 */
	public void setDirection1Tag(String theDirection1Tag) {
		myDirection1Tag = theDirection1Tag;
	}


	/**
	 * @param theDirection2Tag
	 *            the direction2Tag to set
	 */
	public void setDirection2Tag(String theDirection2Tag) {
		myDirection2Tag = theDirection2Tag;
	}


	/**
	 * @param theLastUpdatedTimestamp
	 *            the lastUpdatedTimestamp to set
	 */
	public void setLastUpdatedTimestamp(Date theLastUpdatedTimestamp) {
		myLastUpdatedTimestamp = theLastUpdatedTimestamp;
	}


	/**
	 * @param theRoutePathElements
	 *            the routePathElements to set
	 */
	public void setRoutePathElements(List<RoutePathElement> theRoutePathElements) {
		myRoutePathElements = theRoutePathElements;
	}


	private static int parseInt(String theInput) {
		return (int)Long.parseLong(theInput.replaceAll("[^0-9A-Z]", ""), Character.MAX_RADIX);
	}

	private static long parseLong(String theInput) {
		return Long.parseLong(theInput.replaceAll("[^0-9A-Z]", ""), Character.MAX_RADIX);
	}

}
