package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PredictionsList implements Serializable, IsSerializable {

	private static final long serialVersionUID = 2L;

	private List<Prediction> myPredictions;
	private String myRouteTitle;
	private String myRouteTag;
	private double myStopLatitude;
	private double myStopLongitude;
	private String myStopTag;
	private Date myTimestamp;
	private transient HashSet<String> myVehicleTags;

	public boolean containsVehicleTag(String theVehicleTag) {
		ensureVehicleTags();
		return myVehicleTags.contains(theVehicleTag);
	}

	private void ensureVehicleTags() {
		if (myVehicleTags == null) {
			myVehicleTags = new HashSet<String>();
			for (Prediction next : myPredictions) {
				myVehicleTags.add(next.getVehicleId());
			}
		}
	}

	/**
	 * Does not return null
	 */
	public List<Prediction> getPredictions() {
		if (myPredictions != null && myPredictions.size() > 0 && myPredictions.get(0).getPredictionsList() == null) {
			for (Prediction next : myPredictions) {
				next.setPredictionsList(this);
			}
		}
		
		return myPredictions;
	}

	public String getRouteTitle() {
		return myRouteTitle;
	}

	public String getRouteTag() {
		return myRouteTag;
	}

	public double getStopLatitude() {
		return myStopLatitude;
	}

	public double getStopLongitude() {
		return myStopLongitude;
	}

	public String getStopTag() {
		return myStopTag;
	}

	public Date getTimestamp() {
		return myTimestamp;
	}

	public Set<String> getVehicleTags() {
		ensureVehicleTags();
		return myVehicleTags;
	}

	public void setPredictions(List<Prediction> thePredictions) {
		myPredictions = thePredictions;
	}

	public void setRouteTitle(String theRouteTitle) {
		myRouteTitle = theRouteTitle;
	}

	public void setRouteTag(String theRouteTag) {
		myRouteTag = theRouteTag;
	}

	public void setStopLatitude(double theStopLatitude) {
		myStopLatitude = theStopLatitude;
	}

	public void setStopLongitude(double theStopLongitude) {
		myStopLongitude = theStopLongitude;
	}

	public void setStopTag(String theStopTag) {
		myStopTag = theStopTag;
	}

	public void setTimestamp(Date theDate) {
		myTimestamp = theDate;
	}

}
