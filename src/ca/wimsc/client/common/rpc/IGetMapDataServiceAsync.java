package ca.wimsc.client.common.rpc;

import java.util.Set;

import ca.wimsc.client.common.model.MapDataResponseV2;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGetMapDataServiceAsync {

    void getMapData(int theQueryIndex, Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags, boolean theLoadRouteList, boolean theLoadStopList,
            boolean theLoadPredictions, boolean theLoadLocations, AsyncCallback<MapDataResponseV2> theGetMapDataAsyncCallback);

}
