package ca.wimsc.client.common.rpc;

import java.util.Set;

import ca.wimsc.client.common.model.MapDataResponseV2;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getMapData")
public interface IGetMapDataService extends RemoteService {

    MapDataResponseV2 getMapData(int theQueryIndex, Set<String> theSelectedRouteTags, Set<String> theSelectedStopTags, boolean theLoadRouteList, boolean theLoadStopList,
            boolean theLoadPredictions, boolean theLoadLocations) throws FailureException, UnknownStopException;
    
}
