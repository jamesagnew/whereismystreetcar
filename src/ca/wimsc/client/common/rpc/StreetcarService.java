package ca.wimsc.client.common.rpc;

import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.RouteList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("streetcar")
public interface StreetcarService extends RemoteService {
    
    RouteList getRouteList() throws FailureException;
    
    PredictionsList getPredictions(String theRoute, String theDirection, String theStop) throws FailureException;
    
}
