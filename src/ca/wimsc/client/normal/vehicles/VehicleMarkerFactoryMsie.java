package ca.wimsc.client.normal.vehicles;

import ca.wimsc.client.common.model.Prediction;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;

import com.google.gwt.maps.client.overlay.MarkerImage;

/**
 * Internet explorer doesn't support the HTML Canvas element
 */
public class VehicleMarkerFactoryMsie extends VehicleMarkerFactory{

    @Override
    public void preloadImages() {
        // nothing
    }

    @Override
    public boolean areAllPreloadedImagesReady() {
        return true;
    }

    @Override
    public MarkerImage createVehicleMarker(DirectionEnum theDirection, VehicleTypeEnum theVehicleType, Prediction thePrediction) {
        return VehicleMarkerFactory.createNormalMarker(theDirection, theVehicleType);
    }

}
