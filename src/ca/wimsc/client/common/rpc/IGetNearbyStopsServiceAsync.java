package ca.wimsc.client.common.rpc;

import ca.wimsc.client.common.model.NearbyStopList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGetNearbyStopsServiceAsync {

    void getNearbyStops(String theAddress, double theLatitude, double theLongitude, int theNumToFind, AsyncCallback<NearbyStopList> callback);

}
