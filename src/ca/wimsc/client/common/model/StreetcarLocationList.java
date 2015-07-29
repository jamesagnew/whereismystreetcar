package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StreetcarLocationList implements Serializable, IsSerializable {

    private static final long serialVersionUID = 4L;

    private Date myFetchTime;
    private List<StreetcarLocation> myLocations;
    private String myRouteId;
    private RouteList myRouteList;
    private String myRouteName;
    private Map<String, StopList> myStopLists;
    
    public Date getFetchTime() {
        return myFetchTime;
    }

    public List<StreetcarLocation> getLocations() {
        return myLocations;
    }
    
    public Stop getStop(String theStopTag) {
        for (StopList nextList : myStopLists.values()) {
            for (Stop nextStop : nextList.getStops()) {
                if (nextStop.getStopTag().equals(theStopTag)) {
                    return nextStop;
                }
            }
        }
        return null;
    }

    public String getRouteId() {
        return myRouteId;
    }

    public RouteList getRouteList() {
        return myRouteList;
    }

    public String getRouteName() {
        return myRouteName;
    }

    @Deprecated
    public Map<String, StopList> getDirectionTags2StopLists() {
        return myStopLists;
    }

    public void setFetchTime(Date theFetchTime) {
        myFetchTime = theFetchTime;
    }

    public void setLocations(List<StreetcarLocation> theLocations) {
        myLocations = theLocations;
    }

    public void setRouteId(String theRouteId) {
        myRouteId = theRouteId;
    }

    public void setRouteList(RouteList theRouteList) {
        myRouteList = theRouteList;
    }

    public void setRouteName(String theRouteName) {
        myRouteName = theRouteName;
    }

    @Deprecated
    public void setDirectionTags2StopLists(Map<String, StopList> theMap) {
        myStopLists = theMap;
    }

    public StreetcarLocation getLocation(String theVehicleTag) {
        // TODO: maybe cache this in a transient map?
        for (StreetcarLocation next : myLocations) {
            if (next.getVehicleTag().equals(theVehicleTag)) {
                return next;
            }
        }
        return null;
    }

	public boolean isStopTagHasVehicleNearby(String theStopTag) {
		for (StreetcarLocation next : myLocations) {
			if (theStopTag.equals(next.getClosestStopTag())) {
				return true;
			}
		}
		
		return false;
	}


}
