package ca.wimsc.server.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import ca.wimsc.client.common.model.IncidentTypeEnum;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.util.StringUtil;

import com.google.appengine.api.datastore.GeoPt;

@Entity(name = "INCIDENT")
@NamedQueries(value = { @NamedQuery(name = "Incident.getOpenIncidents", query = "SELECT FROM ca.wimsc.server.jpa.PersistedIncident WHERE myEndDate IS NULL") })
public class PersistedIncident implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "NEAREST_STOP_BEGIN", nullable = false)
	private String myBeginNearestStopTag;

	private transient Integer myBeginNearestStopTagIndex;

	@Column(name = "DIR_TAG", length = 20)
	private String myDirectionTag;

	private transient boolean myDirty;

	@Column(name = "END_DATE", nullable = true)
	private Date myEndDate;

	@Column(name = "NEAREST_STOP_END", nullable = false)
	private String myEndNearestStopTag;

	private transient Integer myEndNearestStopTagIndex;

	@Column(name = "INCIDENT_TYPE", length = 10, nullable = false)
	private IncidentTypeEnum myIncidentType;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "KEY", nullable = false)
	private Long myKey;

	@Column(name = "ROUTE", length = 20, nullable = false)
	private String myRoute;

	@Column(name = "START_DATE", nullable = false)
	private Date myStartDate;

	@Column(name = "START_POINT", nullable = true)
	private GeoPt myStartingLocation;

	@Column(name = "VEHICLE", length = 20, nullable = true)
	private String myVehicle;


	/**
	 * Clear the dirty flag
	 */
	public void clearDirtyFlag() {
		myDirty = false;
	}


	/**
	 * @param theNextIncident
	 */
	public void copyValues(PersistedIncident theNextIncident) {
		myBeginNearestStopTag = theNextIncident.myBeginNearestStopTag;
		myDirectionTag = theNextIncident.myDirectionTag;
		myEndDate = theNextIncident.myEndDate;
		myEndNearestStopTag = theNextIncident.myEndNearestStopTag;
		myIncidentType = theNextIncident.myIncidentType;
		myRoute = theNextIncident.myRoute;
		myStartDate = theNextIncident.myStartDate;
		myStartingLocation = theNextIncident.myStartingLocation;
		myVehicle = theNextIncident.myVehicle;
	}


	public String getBeginNearestStopTag() {
		return myBeginNearestStopTag;
	}


	/**
	 * @return the beginNearestStopTagIndex
	 */
	public int getOrCalculateBeginNearestStopTagIndex(StopList theStopList) {
		if (myBeginNearestStopTagIndex == null) {
			myBeginNearestStopTagIndex = theStopList.getStopTagIndex(myBeginNearestStopTag);
		}
		return myBeginNearestStopTagIndex;
	}


	public String getDirectionTag() {
		return myDirectionTag;
	}


	public Date getEndDate() {
		return myEndDate;
	}


	public String getEndNearestStopTag() {
		return myEndNearestStopTag;
	}


	/**
	 * @return the endNearestStopTagIndex
	 */
	public int getOrCalculateEndNearestStopTagIndex(StopList theStopList) {
		if (myEndNearestStopTagIndex == null) {
			myEndNearestStopTagIndex = theStopList.getStopTagIndex(myEndNearestStopTag);
		}
		return myEndNearestStopTagIndex;
	}


	public IncidentTypeEnum getIncidentType() {
		return myIncidentType;
	}


	public Long getKey() {
		return myKey;
	}


	public String getRoute() {
		return myRoute;
	}


	public Date getStartDate() {
		return myStartDate;
	}


	public GeoPt getStartingLocation() {
		return myStartingLocation;
	}


	public String getVehicle() {
		return myVehicle;
	}


	/**
	 * @return the dirty flag
	 */
	public boolean isDirtyFlagSet() {
		return myDirty || myKey == null;
	}


	public void setBeginNearestStopTag(String theNearestStop) {
		if (!StringUtil.equals(myBeginNearestStopTag, theNearestStop)) {
			myDirty = true;
			myBeginNearestStopTag = theNearestStop;
		}
	}



	public void setDirectionTag(String theDirectionTag) {
		myDirectionTag = theDirectionTag;
	}


	public void setEndDate(Date theEndDate) {
		myDirty = true;
		myEndDate = theEndDate;
	}


	public void setEndNearestStopTag(String theNearestStop) {
		if (!StringUtil.equals(myEndNearestStopTag, theNearestStop)) {
			myDirty = true;
			myEndNearestStopTag = theNearestStop;
		}
	}




	public void setIncidentType(IncidentTypeEnum theIncidentType) {
		myIncidentType = theIncidentType;
	}


	public void setKey(Long theKey) {
		myKey = theKey;
	}


	public void setRoute(String theRouteTag) {
		myRoute = theRouteTag;
	}


	public void setStartDate(Date theStartDate) {
		myStartDate = theStartDate;
	}


	public void setStartingLocation(GeoPt theLocation) {
		myStartingLocation = theLocation;
	}


	public void setVehicle(String theVehicle) {
		myVehicle = theVehicle;
	}


	@Override
	public String toString() {
		return "Incident[type=" + myIncidentType.name() + ", startpoint=" + myStartingLocation + ", nearbegin=" + myBeginNearestStopTag + "]";
	}

}
