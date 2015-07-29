package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MapDataResponse implements IsSerializable, Serializable {

    private static final long serialVersionUID = 1L;

    private PredictionsList myPredictionList;
    private RouteList myRouteList;
    private String myRouteTag;
    private String myStopDirectionTag;
    private Map<String, StopList> myStopLists;
    private String myStopTag;
    private StreetcarLocationList myStreetcarLocationList;
    private String myWarning;

    /**
     * Does not return null
     */
    public PredictionsList getPredictionList() {
        return myPredictionList;
    }

    public RouteList getRouteList() {
        return myRouteList;
    }

    public String getRouteTag() {
        return myRouteTag;
    }

    public String getStopDirectionTag() {
        return myStopDirectionTag;
    }

    public Map<String, StopList> getStopLists() {
        return myStopLists;
    }

    public String getStopTag() {
        return myStopTag;
    }

    public StreetcarLocationList getStreetcarLocationList() {
        return myStreetcarLocationList;
    }

    public String getWarning() {
        return myWarning;
    }

    public void setPredictionList(PredictionsList thePredictionList) {
        myPredictionList = thePredictionList;
    }

    public void setRouteList(RouteList theRouteList) {
        myRouteList = theRouteList;
    }

    public void setRouteTag(String theRouteTag) {
        myRouteTag = theRouteTag;
    }

    public void setStopDirectionTag(String theDirectionTag) {
        myStopDirectionTag = theDirectionTag;
    }

    public void setStopList(Map<String, StopList> theStopLists) {
        myStopLists = theStopLists;
    }

    public void setStopTag(String theStopTag) {
        myStopTag = theStopTag;
    }

    public void setStreetcarLocationList(StreetcarLocationList theStreetcarLocationList) {
        myStreetcarLocationList = theStreetcarLocationList;
    }

    public void setWarning(String theString) {
        myWarning = theString;
    }

}
