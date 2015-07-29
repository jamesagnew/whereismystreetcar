package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.model.StreetcarLocationList;

/**
 * Displays all vehicles on a route only if no stop is selected for that route (useful for mobile view, where space is
 * scarce)
 */
public class VehiclesLayerWholeRouteIfNoStopSelected extends VehiclesLayerWholeRoute {

    /**
     * Constructor
     */
    public VehiclesLayerWholeRouteIfNoStopSelected() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<StreetcarLocation> preProcessList(StreetcarLocationList theList) {
        List<StreetcarLocation> preProcessList = super.preProcessList(theList);

        Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();
        if (selectedStopTags != null && selectedStopTags.isEmpty() == false) {
            preProcessList = new ArrayList<StreetcarLocation>();
        }

        return preProcessList;
    }

}
