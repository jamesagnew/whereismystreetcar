package ca.wimsc.client.common.model;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Route implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private String myTag;
	private String myTitle;
	private double myLatMax;
	private double myLatMin;
	private double myLonMax;
	private double myLonMin;

//	public Map<String, DirectionEnum> getDirectionEnumMap() {
//		Map<String, DirectionEnum> directionTag2Name = new HashMap<String, DirectionEnum>();
//		directionTag2Name.clear();
//		for (Direction direction : myDirections) {
//			DirectionEnum dir = DirectionEnum.fromTag(direction.getTag());
//			directionTag2Name.put(direction.getTag(), dir);
//		}
//		return directionTag2Name;
//	}
//
//	public List<Direction> getDirections() {
//		if (myDirectionsSorted == false) {
//			myDirections = new ArrayList<Direction>(myDirections);
//			Collections.sort(myDirections, new Comparator<Direction>() {
//				@Override
//				public int compare(Direction theO1, Direction theO2) {
//					return theO1.getName().compareToIgnoreCase(theO2.getName());
//				}
//			});
//			myDirectionsSorted = true;
//		}
//		return myDirections;
//	}

	public double getLatMax() {
		return myLatMax;
	}

	public double getLatMin() {
		return myLatMin;
	}

	public double getLonMax() {
		return myLonMax;
	}

	public double getLonMin() {
		return myLonMin;
	}

//	public void setDirections(List<Direction> theDirections) {
//		myDirections = theDirections;
//	}

	public void setLatMax(double theLatMax) {
		myLatMax = theLatMax;
	}

	public void setLatMin(double theLatMin) {
		myLatMin = theLatMin;
	}

	public void setLonMax(double theLonMax) {
		myLonMax = theLonMax;
	}

	public void setLonMin(double theLonMin) {
		myLonMin = theLonMin;
	}

	public String getTag() {
		return myTag;
	}

	public String getTitle() {
		return myTitle;
	}

	public void setTag(String theTag) {
		myTag = theTag;
	}

	public void setTitle(String theTitle) {
		myTitle = theTitle;
	}

	public enum DirectionEnum {
		EASTBOUND, NORTHBOUND, SOUTHBOUND, WESTBOUND;

		public static DirectionEnum fromNameOrTitle(String theTag) {
			if (theTag == null) {
				theTag = "";
			}
			if (theTag.toLowerCase().contains("east")) {
				return DirectionEnum.EASTBOUND;
			} else if (theTag.toLowerCase().contains("west")) {
				return DirectionEnum.WESTBOUND;
			} else if (theTag.toLowerCase().contains("south")) {
				return DirectionEnum.SOUTHBOUND;
			} else {
				if (!GWT.isProdMode() && !theTag.toLowerCase().contains("north")) {
					GWT.log("**Direction name " + theTag + " contains no directions**");
				}
				return DirectionEnum.NORTHBOUND;
			}
		}
	}
//
//	public List<String> getDirectionTags() {
//		List<String> retVal = new ArrayList<String>();
//		for (Direction next : myDirections) {
//			retVal.add(next.getTag());
//		}
//		return retVal;
//	}
//
//	public Direction getDirection(String theDirectionTag) {
//		for (Direction next : myDirections) {
//			if (next.getTag().equals(theDirectionTag)) {
//				return next;
//			}
//		}
//		return null;
//	}

	public HasLatLngBounds getWholeRouteBounds() {
		LatLng sw = new LatLng(myLatMin, myLonMin);
		LatLng ne = new LatLng(myLatMax, myLonMax);
		return new LatLngBounds(sw, ne);
	}

}
