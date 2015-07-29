package ca.wimsc.client.common.rpc;

import ca.wimsc.client.common.model.NearbyStopList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getNearbyStops")
public interface IGetNearbyStopsService extends RemoteService {

    NearbyStopList getNearbyStops(String theAddress, double theLatitude, double theLongitude, int theNumToFind) throws FailureException;
    
}
