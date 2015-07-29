/**
 * 
 */
package ca.wimsc.client.common.model;

import java.io.Serializable;

import ca.wimsc.client.common.util.ObjectUtil;

import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An element on the {@link RoutePath Route Path}
 */
public class RoutePathElement implements Serializable, IsSerializable {

	/**
	 * @see #getHeadingToNextStop()
	 */
	public static final int DEFAULT_HEADING_TO_NEXT_STOP = -999;


	public static final int DEFAULT_SPEED = -1;
	private static final long serialVersionUID = 1L;
	private String myClosestStopTagDirection1;
	private String myClosestStopTagDirection2;
	private int myHeadingToNextStop = DEFAULT_HEADING_TO_NEXT_STOP;
	private double myLatitude;
	private double myLongitude;
	private boolean myNewPathEntry;
	private int mySpeedInKmhDirection1 = DEFAULT_SPEED;
	private int mySpeedInKmhDirection2 = DEFAULT_SPEED;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof RoutePathElement)) {
			return false;
		}
		
		RoutePathElement obj = (RoutePathElement)theObj;
		
		boolean retVal = ObjectUtil.equals(myClosestStopTagDirection1, obj.myClosestStopTagDirection1);
		retVal &= ObjectUtil.equals(myClosestStopTagDirection2, obj.myClosestStopTagDirection2);
		retVal &= (myNewPathEntry == obj.myNewPathEntry);
		retVal &= (myHeadingToNextStop == obj.myHeadingToNextStop);
		retVal &= (myLatitude == obj.myLatitude);
		retVal &= (myLongitude == obj.myLongitude);
		retVal &= (mySpeedInKmhDirection1 == obj.mySpeedInKmhDirection1);
		retVal &= (mySpeedInKmhDirection2 == obj.mySpeedInKmhDirection2);
		
		return retVal;
	}
	
	/**
	 * zero indexed!!
	 */
	public String getClosestStopTagDirection(int theDirectionIndex) {
		if (theDirectionIndex == 0) {
			return getClosestStopTagDirection1();
		}
		return getClosestStopTagDirection2();
	}
	
	/**
	 * @return the closestStopTagDirection1
	 */
	public String getClosestStopTagDirection1() {
		return myClosestStopTagDirection1;
	}


	/**
	 * @return the closestStopTagDirection2
	 */
	public String getClosestStopTagDirection2() {
		return myClosestStopTagDirection2;
	}


	/**
	 * @return the heading to next stop in degrees
	 */
	public int getHeadingToNextStop() {
		return myHeadingToNextStop;
	}


	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return myLatitude;
	}


	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return myLongitude;
	}


	/**
	 * @return the speedInKmhDirection1
	 */
	public int getSpeedInKmhDirection1() {
		return mySpeedInKmhDirection1;
	}


	/**
	 * Zero indexed!
	 */
	public int getSpeedInKmhDirection1(int theIndex) {
		if (theIndex == 0) {
			return mySpeedInKmhDirection1;
		} else {
			return mySpeedInKmhDirection2;
		}
	}


	/**
	 * @return the speedInKmhDirection2
	 */
	public int getSpeedInKmhDirection2() {
		return mySpeedInKmhDirection2;
	}


	/**
	 * Returns the speed for a given direction, and if no speed has been set, defaults first to another direction and if
	 * nothing, uses 0
	 */
	public int getSpeedInKmhDirectionWithDefault(int theIndex, int theDefault) {
		int value = getSpeedInKmhDirection1(theIndex);
		if (value == -1) {
			if (theIndex == 0) {
				value = getSpeedInKmhDirection1(1);
			} else if (theIndex == 1) {
				value = getSpeedInKmhDirection1(0);
			}
		}
		if (value == -1) {
			value = theDefault;
		}
		return value;
	}


	/**
	 * @return the newPathEntry
	 */
	public boolean isNewPathEntry() {
		return myNewPathEntry;
	}


	/**
	 * Zero indexed!
	 */
	public void setClosestStopTagDirection1(int theIndex, String theClosestStopTagDirection) {
		if (theIndex == 0) {
			myClosestStopTagDirection1 = theClosestStopTagDirection;
		} else {
			myClosestStopTagDirection2 = theClosestStopTagDirection;
		}
	}


	/**
	 * @param theClosestStopTagDirection1
	 *            the closestStopTagDirection1 to set
	 */
	public void setClosestStopTagDirection1(String theClosestStopTagDirection1) {
		myClosestStopTagDirection1 = theClosestStopTagDirection1;
	}


	/**
	 * @param theClosestStopTagDirection2
	 *            the closestStopTagDirection2 to set
	 */
	public void setClosestStopTagDirection2(String theClosestStopTagDirection2) {
		myClosestStopTagDirection2 = theClosestStopTagDirection2;
	}


	/**
	 * @param theHeadingToNextStop the heading to next stop in degrees
	 */
	public void setHeadingToNextStop(int theHeadingToNextStop) {
		myHeadingToNextStop = theHeadingToNextStop;
	}


	/**
	 * @param theLatitude
	 *            the latitude to set
	 */
	public void setLatitude(double theLatitude) {
		myLatitude = theLatitude;
	}


	/**
	 * @param theLongitude
	 *            the longitude to set
	 */
	public void setLongitude(double theLongitude) {
		myLongitude = theLongitude;
	}


	/**
	 * @param theNewPathEntry
	 *            the newPathEntry to set
	 */
	public void setNewPathEntry(boolean theNewPathEntry) {
		myNewPathEntry = theNewPathEntry;
	}


	/**
	 * @param theSpeedInKmhDirection2
	 *            the speedInKmhDirection2 to set
	 */
	public void setSpeedInKmhDirection(int theDirection, int theSpeedInKmhDirection2) {
		if (theDirection == 0) {
			mySpeedInKmhDirection1 = theSpeedInKmhDirection2;
		} else {
			mySpeedInKmhDirection2 = theSpeedInKmhDirection2;
		}
	}


	/**
	 * @param theSpeedInKmhDirection1
	 *            the speedInKmhDirection1 to set
	 */
	public void setSpeedInKmhDirection1(int theSpeedInKmhDirection1) {
		mySpeedInKmhDirection1 = theSpeedInKmhDirection1;
	}


	/**
	 * @param theSpeedInKmhDirection2
	 *            the speedInKmhDirection2 to set
	 */
	public void setSpeedInKmhDirection2(int theSpeedInKmhDirection2) {
		mySpeedInKmhDirection2 = theSpeedInKmhDirection2;
	}


	/**
	 * @return
	 */
	public HasLatLng toLatLng() {
		return new LatLng(myLatitude, myLongitude);
	}

}
