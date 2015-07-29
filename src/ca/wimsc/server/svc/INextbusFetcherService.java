package ca.wimsc.server.svc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.jpa.StopQuadrant;
import ca.wimsc.server.jpa.StopQuadrantList;
import ca.wimsc.server.xml.route.Body;

public interface INextbusFetcherService {

    StopQuadrantList loadAllStopQuadrants() throws FailureException, MalformedURLException, IOException;

    List<List<StopQuadrant>> loadAllStopQuadrants(int theNumDivisions) throws FailureException, MalformedURLException, IOException;

    PredictionsList loadPredictions(String theRoute, String theStop) throws FailureException;

    Body loadRouteConfig(String theRoute) throws FailureException;

    RouteList loadRouteList() throws FailureException;

    /**
     * 
     * @param theRoute
     * @param theLoadRouteList
     * @param theRelaxedFrequencyMode If true, returned values may be up to 5 minutes old
     * @return
     * @throws FailureException
     */
    StreetcarLocationList loadStreetcarLocations(String theRoute, boolean theLoadRouteList, boolean theRelaxedFrequencyMode) throws FailureException;

	StopListForRoute getStopListForRoute(String theRoute) throws FailureException;

}