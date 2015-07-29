package ca.wimsc.client.common.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StreetcarLocation implements Serializable, IsSerializable {

	private static final double D2R = (Math.PI / 180);
	private static final long serialVersionUID = 3L;

	private Integer myCurrentSpeed;
	private String myDirectionTag;
	private String myHeading;
	private double myLatitude;
	private double myLongitude;
	private String myStopTag;
	private String myVehicleTag;


	public double distanceFrom(double theLat, double theLon) {
		double dlong = (theLon - myLongitude) * D2R;
		double dlat = (theLat - myLatitude) * D2R;
		double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(myLatitude * D2R) * Math.cos(theLat * D2R) * Math.pow(Math.sin(dlong / 2.0), 2);
		double c = Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return c;
	}


	public long distanceFromInMeters(double theLat, double theLon) {
		double c = distanceFrom(theLat, theLon);
		double d = 2 * 1000 * 6367 * c;
		return (long) d;
	}


	public String getClosestStopTag() {
		return myStopTag;
	}


	/**
	 * @return Value is in km/h. May be null.
	 */
	public Integer getCurrentSpeed() {
		return myCurrentSpeed;
	}


	public String getDirectionTag() {
		return myDirectionTag;
	}


	public String getHeading() {
		return myHeading;
	}


	public double getLatitude() {
		return myLatitude;
	}


	public double getLongitude() {
		return myLongitude;
	}


	public String getVehicleTag() {
		return myVehicleTag;
	}


	public void setClosestStopTag(String theStopId) {
		myStopTag = theStopId;
	}


	public void setCurrentSpeed(int theCurrentSpeed) {
		myCurrentSpeed = theCurrentSpeed;

	}


	public void setDirectionTag(String theDirTag) {
		myDirectionTag = theDirTag;
	}


	public void setHeading(String theHeading) {
		myHeading = theHeading;
	}


	public void setLatitude(double theLatitude) {
		myLatitude = theLatitude;
	}


	public void setLongitude(double theLongitude) {
		myLongitude = theLongitude;
	}


	public void setVehicleTag(String theVehicleTag) {
		myVehicleTag = theVehicleTag;
	}


	public static double convertDeg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}


	public static double convertRad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

}
