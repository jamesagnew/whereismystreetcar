package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A collection of incidents for a group of routes
 */
public class IncidentMap implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;

	private Map<String, IncidentList> myRouteTagToIncidents;
	
	public void setRouteTagToIncidents(Map<String, IncidentList> myRouteTagToIncidents) {
		this.myRouteTagToIncidents = myRouteTagToIncidents;
	}

	public Map<String, IncidentList> getRouteTagToIncidents() {
		return myRouteTagToIncidents;
	}
	
}
