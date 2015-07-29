package ca.wimsc.client.common.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Prediction implements Serializable, IsSerializable, Comparable<Prediction> {

	private static final long serialVersionUID = 2L;

	private String myClosestStopTag;
	private Integer myCurrentSpeed;
	private Integer myHeadway;
	private transient PredictionsList myPredictionsList;
	private int mySeconds;
	private String mySpeed;
	private String myVehicleDirectionTag;
	private String myVehicleId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Prediction theO) {
		return mySeconds - theO.getSeconds();
	}

	public String getClosestStopTag() {
		return myClosestStopTag;
	}

	public Integer getCurrentSpeed() {
		Integer retVal = myCurrentSpeed;
		if (retVal == null) {
			NumbersAndTimestamps speedGraph = getSpeed();
			if (speedGraph != null && speedGraph.getNewest() != null) {
				retVal = speedGraph.getNewest().getNumber();
			}
		}

		return retVal;
	}

	/**
	 * The number of seconds between vehicles. In other words, when this prediction arrives to the given stop, the wait
	 * will have been this number of minutes
	 */
	public Integer getHeadway() {
		return myHeadway;
	}

	public PredictionsList getPredictionsList() {
		return myPredictionsList;
	}

	public int getSeconds() {
		return mySeconds;
	}

	public NumbersAndTimestamps getSpeed() {
		if (mySpeed == null) {
			return null;
		}
		return new NumbersAndTimestamps(mySpeed);
	}

	public String getVehicleDirectionTag() {
		return myVehicleDirectionTag;
	}

	public String getVehicleId() {
		return myVehicleId;
	}

	public void setClosestStopTag(String theClosestStopTag) {
		myClosestStopTag = theClosestStopTag;
	}

	public void setCurrentSpeed(Integer theCurrentSpeed) {
		myCurrentSpeed = theCurrentSpeed;
	}

	/**
	 * The number of seconds between vehicles. In other words, when this prediction arrives to the given stop, the wait
	 * will have been this number of minutes
	 */
	public void setHeadway(Integer theHeadway) {
		myHeadway = theHeadway;
	}

	void setPredictionsList(PredictionsList thePredictionsList) {
		myPredictionsList = thePredictionsList;
	}

	public void setSeconds(int theSeconds) {
		mySeconds = theSeconds;
	}

	public void setSpeed(NumbersAndTimestamps theSpeed) {
		if (theSpeed != null) {
			mySpeed = theSpeed.marshall();
		} else {
			mySpeed = null;
		}
	}

	public void setVehicleDirectionTag(String theDirectionTag) {
		myVehicleDirectionTag = theDirectionTag;
	}

	public void setVehicleId(String theVehicleTag) {
		myVehicleId = theVehicleTag;
	}

}
