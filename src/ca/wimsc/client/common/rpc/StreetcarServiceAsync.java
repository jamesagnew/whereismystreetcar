package ca.wimsc.client.common.rpc;

import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.RouteList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface StreetcarServiceAsync {
    void getRouteList(AsyncCallback<RouteList> callback);

    void getPredictions(String theRoute, String theDirection, String theStop, AsyncCallback<PredictionsList> callback);
}
