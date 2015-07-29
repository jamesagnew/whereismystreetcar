package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NearbyStopList implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;
    
    private List<NearbyStop> myNearbyStops;

    public List<NearbyStop> getNearbyStops() {
        return myNearbyStops;
    }

    public void setNearbyStops(List<NearbyStop> theNearbyStops) {
        myNearbyStops = theNearbyStops;
    }
    
}
