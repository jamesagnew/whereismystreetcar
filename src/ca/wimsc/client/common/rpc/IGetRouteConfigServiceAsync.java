package ca.wimsc.client.common.rpc;

import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.StopListForRoute;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGetRouteConfigServiceAsync {

    void getRouteList(AsyncCallback<RouteList> callback);

    void getStopListForRoute(String theRoute, AsyncCallback<StopListForRoute> callback);

	void getRoutePaths(Set<String> theRouteTags, AsyncCallback<Map<String, String>> callback);

}
