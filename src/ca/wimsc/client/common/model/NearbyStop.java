package ca.wimsc.client.common.model;

import java.util.List;

import ca.wimsc.client.common.model.Route.DirectionEnum;

import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A stop being used as a result of a GPS "nearby stops" search 
 */
public class NearbyStop extends Stop implements IsSerializable {

    private static final long serialVersionUID = 1L;
    public static final int NUM_SERIALIZED_PARTS = 5;

    private String myDirectionTag;
    private String myDirectionTitle;
    private String myRouteTag;
    private String myRouteTitle;

    public NearbyStop() {
        // nothing
    }

    public NearbyStop(Stop theClone) {
        super(theClone);
    }

    @Override
    public List<String> deserializeFromString(String theString) {
        List<String> parts = super.deserializeFromString(theString);
        myDirectionTag = parts.get(1);
        myDirectionTitle = parts.get(2);
        myRouteTag = parts.get(3);
        myRouteTitle = parts.get(4);

        if (parts.size() > NUM_SERIALIZED_PARTS) {
            return parts.subList(NUM_SERIALIZED_PARTS, parts.size());
        } else {
            return null;
        }
    }

    public String getDirectionTag() {
        return myDirectionTag;
    }

    public String getDirectionTitle() {
        return myDirectionTitle;
    }

    public DirectionEnum getDirectionEnum() {
        return DirectionEnum.fromNameOrTitle(getDirectionTitle());
    }
    
    public HasLatLng getLocation() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public String getRouteTag() {
        return myRouteTag;
    }

    public String getRouteTitle() {
        return myRouteTitle;
    }

    @Override
    public void serializeToString(StringBuilder theBuilder) {
        super.serializeToString(theBuilder);

        theBuilder.append(SER_DELIM);
        theBuilder.append(serialVersionUID);
        theBuilder.append(SER_DELIM);
        theBuilder.append(myDirectionTag);
        theBuilder.append(SER_DELIM);
        theBuilder.append(myDirectionTitle);
        theBuilder.append(SER_DELIM);
        theBuilder.append(myRouteTag);
        theBuilder.append(SER_DELIM);
        theBuilder.append(myRouteTitle);
        
    }

    public void setDirectionTag(String theDirectionTag) {
        myDirectionTag = theDirectionTag;
    }

    public void setDirectionTitle(String theDirectionTitle) {
        myDirectionTitle = theDirectionTitle;
    }

    public void setRouteTag(String theRouteTag) {
        myRouteTag = theRouteTag;
    }

    public void setRouteTitle(String theRouteTitle) {
        myRouteTitle = theRouteTitle;
    }

}
