package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.Vehicles;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlay.Marker;

/**
 * Layer which shows all vehicles on selected routes
 * (except possibly vehicles which are a part of predictions for a selected stop)
 */
public class VehiclesLayerWholeRoute extends VehiclesLayerBase {

    private IPropertyChangeListener myPropertyChangeListener;
    private Set<String> mySelectedRouteTags;
    private Map<String, MyStreetcarLocationListListener> myRouteTagToListener = new HashMap<String, MyStreetcarLocationListListener>();

    
    @Override
    public void initialize(MapWidget theMap) {
        super.initialize(theMap);
        
        myPropertyChangeListener = new IPropertyChangeListener() {

            @Override
            public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
                redrawIfSelectionsHaveChanged();
            }
        };

        Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
        MapDataController.INSTANCE.incrementWantLocationsCount();
        
        redrawIfSelectionsHaveChanged();
    }

    private void redrawIfSelectionsHaveChanged() {
        Set<String> selectedRouteTags = Model.INSTANCE.getSelectedRouteTags();

        if (selectedRouteTags == null) {
            return;
        }
        if (mySelectedRouteTags != null && mySelectedRouteTags.equals(selectedRouteTags)) {
            return;
        }

        for (String nextRouteTag : selectedRouteTags) {
            if (!myRouteTagToListener.containsKey(nextRouteTag)) {
                MyStreetcarLocationListListener listener = new MyStreetcarLocationListListener(nextRouteTag);
                myRouteTagToListener.put(nextRouteTag, listener);
                Model.INSTANCE.addStreetcarLocationListListener(nextRouteTag, listener);
            }
        }

        for (Iterator<Entry<String, MyStreetcarLocationListListener>> iter = myRouteTagToListener.entrySet().iterator(); iter.hasNext();) {
            Entry<String, MyStreetcarLocationListListener> nextEntry = iter.next();
            if (!selectedRouteTags.contains(nextEntry.getKey())) {
                Model.INSTANCE.removeStreetcarLocationListListener(nextEntry.getKey(), nextEntry.getValue());
                nextEntry.getValue().destroy();
                iter.remove();
            }
        }

        mySelectedRouteTags = selectedRouteTags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        
        Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
        for (Iterator<Entry<String, MyStreetcarLocationListListener>> iter = myRouteTagToListener.entrySet().iterator(); iter.hasNext();) {
            Entry<String, MyStreetcarLocationListListener> nextEntry = iter.next();
            Model.INSTANCE.removeStreetcarLocationListListener(nextEntry.getKey(), nextEntry.getValue());
            iter.remove();
        }

        MapDataController.INSTANCE.decrementWantLocationsCount();

    }

    /**
     * Process the list of streetcars (useful for subclassing)
     */
	protected List<StreetcarLocation> preProcessList(StreetcarLocationList theList) {
		if (HistoryUtil.isShowOnlyPredictions()) {
			return new ArrayList<StreetcarLocation>();
		}
		return theList.getLocations();
	}

    
    private class MyStreetcarLocationListListener extends MyAbstractAsyncListener<StreetcarLocationList> {

        public MyStreetcarLocationListListener(String theRouteTag) {
            super(theRouteTag);
        }

        @Override
        public void startLoadingObject() {
            // nothing
        }

        @Override
        public void doObjectLoaded(StreetcarLocationList theList, boolean theRequiredAsyncLoad) {
            processNewList(theList);
            
        }

		private void processNewList(StreetcarLocationList theList) {
			
			Map<String, Marker> existingVehicleTagsToMarkers = getVehicleTagToMarker(getRouteTag());
            Map<String, Marker> newVehicleTagsToMarkers = new HashMap<String, Marker>();
            
            // Add streetcar location markers
            for (StreetcarLocation next : preProcessList(theList)) {
                
            	/*
            	 * Null direction can mean the vehicle is out of service, or
            	 * it can mean that it is diverting somehow. We try and avoid clutter
            	 * on the map by only displaying "null" direction vehicles if they
            	 * are actually going somewhere.
            	 */
            	if ("null".equals(next.getDirectionTag())) {
                    Integer nextSpeed = next.getCurrentSpeed();
                    if (nextSpeed == null || nextSpeed.equals(Integer.valueOf(0))) {
                    	continue;
                    }
                }

                if (Model.INSTANCE.isHavePredictionForVehicle(next.getVehicleTag())) {
                    continue;
                }

                double latitude = next.getLatitude();
                double longitude = next.getLongitude();
                updateBounds(latitude, longitude);
                LatLng point = new LatLng(latitude, longitude);

                String directionTag = next.getDirectionTag();
				StopListForRoute stopListForRoute = Model.INSTANCE.getStopListForRoute(getRouteTag());
				StopList stopList = stopListForRoute.getUiOrNonUiStopListForDirectionTag(directionTag);
				DirectionEnum directionEnum = determineDirection(stopList, next.getHeading());
                
                VehicleTypeEnum vehicleType = Vehicles.getVehicleType(next.getVehicleTag());
                
                // might be null
                Marker marker = existingVehicleTagsToMarkers.remove(next.getVehicleTag());
                
                marker = createDirectionalMarker(marker, point, directionEnum, vehicleType, null);
                
                newVehicleTagsToMarkers.put(next.getVehicleTag(), marker);
                addMarker(marker);

            }

            destroyMarkers(existingVehicleTagsToMarkers);
            setVehicleTagToMarker(getRouteTag(), newVehicleTagsToMarkers);
		}

		/**
		 * Clear all markers
		 */
		public void destroy() {
			destroyMarkers(getVehicleTagToMarker(getRouteTag()));
		}

    }


}
