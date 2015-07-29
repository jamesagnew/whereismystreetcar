package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Return type for getMapData backend service call
 */
public class MapDataResponseV2 implements IsSerializable, Serializable {

    private static final long serialVersionUID = 1L;

    private int myQueryIndex;
    private RouteList myRouteList;
    private List<StopListForRoute> myStopListForRoute;
    private Map<String, StreetcarLocationList> myRouteTagToStreetcarLocationList;
    private Set<String> mySelectedRouteTags;
    private Set<String> mySelectedStopTags;

    private Map<String, PredictionsList> myStopTagToPredictionList;

    private String myWarning;

    private Map<String, String> myStopTagsToRouteTags;

    public int getQueryIndex() {
        return myQueryIndex;
    }

    public RouteList getRouteList() {
        return myRouteList;
    }

   

    /**
	 * @return the stopListForRoute
	 */
	public List<StopListForRoute> getStopListForRoute() {
		if (myStopListForRoute == null) {
			myStopListForRoute = new ArrayList<StopListForRoute>();
		}
		return myStopListForRoute;
	}

	/**
	 * @param theStopListForRoute the stopListForRoute to set
	 */
	public void setStopListForRoute(List<StopListForRoute> theStopListForRoute) {
		myStopListForRoute = theStopListForRoute;
	}

	public Map<String, StreetcarLocationList> getRouteTagToStreetcarLocationList() {
        return myRouteTagToStreetcarLocationList;
    }

    public Set<String> getSelectedRouteTags() {
        return mySelectedRouteTags;
    }

    public Set<String> getSelectedStopTags() {
        return mySelectedStopTags;
    }

    public Map<String, PredictionsList> getStopTagToPredictionList() {
        return myStopTagToPredictionList;
    }

   
    public String getWarning() {
        return myWarning;
    }

    public void setQueryIndex(int theQueryIndex) {
        myQueryIndex = theQueryIndex;
    }

    public void setRouteList(RouteList theRouteList) {
        myRouteList = theRouteList;
    }

    public void addStopListForRoute(StopListForRoute theRouteTagToDirectionTagToStopLists) {
        getStopListForRoute().add(theRouteTagToDirectionTagToStopLists);
    }


    public void setSelectedRouteTags(Set<String> theSelectedRouteTags) {
        mySelectedRouteTags = theSelectedRouteTags;
    }

    public void setSelectedStopTags(Set<String> theSelectedStopTags) {
        mySelectedStopTags = theSelectedStopTags;
    }

    public void setStopTagToPredictionList(Map<String, PredictionsList> theStopTagToPredictionList) {
        myStopTagToPredictionList = theStopTagToPredictionList;
    }

    public void setWarning(String theWarning) {
        myWarning = theWarning;
    }

    public void setStopTagsToRouteTags(Map<String, String> theStopTagToRouteTag) {
        myStopTagsToRouteTags = theStopTagToRouteTag;
    }

    public Map<String, String> getStopTagsToRouteTags() {
        return myStopTagsToRouteTags;
    }

	/**
	 * @param theHashMap
	 */
	public void setRouteTagToStreetcarLocationList(HashMap<String, StreetcarLocationList> theHashMap) {
		myRouteTagToStreetcarLocationList = theHashMap;
	}

}
