package ca.wimsc.client.common.rpc;

import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.StopListForRoute;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getRouteConfig")
public interface IGetRouteConfigService extends RemoteService {

    RouteList getRouteList() throws FailureException;
    
    Map<String, String> getRoutePaths(Set<String> theRouteTags) throws FailureException;

	StopListForRoute getStopListForRoute(String theRoute) throws FailureException;
    
    
}
