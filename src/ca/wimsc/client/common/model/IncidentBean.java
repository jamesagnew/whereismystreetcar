package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class IncidentBean implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private double myLatitude;
	private double myLongitude;
	private Date myStartDate;
	private Date myEndDate;
	private IncidentTypeEnum myIncidentType;
	private String myNearestStopTag;
	private String myVehicleTag;

	public double getLatitude() {
		return myLatitude;
	}

	public void setLatitude(double myLatitude) {
		this.myLatitude = myLatitude;
	}

	public double getLongitude() {
		return myLongitude;
	}

	public void setLongitude(double myLongitude) {
		this.myLongitude = myLongitude;
	}

	public Date getStartDate() {
		return myStartDate;
	}

	public void setStartDate(Date myStartDate) {
		this.myStartDate = myStartDate;
	}

	public Date getEndDate() {
		return myEndDate;
	}

	public void setEndDate(Date myEndDate) {
		this.myEndDate = myEndDate;
	}

	public IncidentTypeEnum getIncidentType() {
		return myIncidentType;
	}

	public void setIncidentType(IncidentTypeEnum myIncidentType) {
		this.myIncidentType = myIncidentType;
	}

	public String getNearestStopTag() {
		return myNearestStopTag;
	}

	public void setNearestStopTag(String myNearestStopTag) {
		this.myNearestStopTag = myNearestStopTag;
	}

	public String getVehicleTag() {
		return myVehicleTag;
	}

	public void setVehicleTag(String myVehicleTag) {
		this.myVehicleTag = myVehicleTag;
	}

}
