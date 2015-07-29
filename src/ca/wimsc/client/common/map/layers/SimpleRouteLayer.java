package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.util.IPropertyChangeListener;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.maps.client.overlay.impl.PolylineImpl;

/**
 * Shows a line containing the route, simply using stops along the route to mark the points
 */
public class SimpleRouteLayer extends AbstractMapOverlayLayer {

    private MapWidget myMap;

    private IPropertyChangeListener myPropertyChangeListener;
    private Map<String, Polyline> myRoutePolylines = new HashMap<String, Polyline>();
    
    @Override
    public void destroy() {
        Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
        for (Iterator<Map.Entry<String, Polyline>> iter = myRoutePolylines.entrySet().iterator(); iter.hasNext();) {
            Entry<String, Polyline> next = iter.next();
            PolylineImpl.impl.setMap(next.getValue().getJso(), null);
            iter.remove();
        }
    }

    @Override
    public void initialize(MapWidget theMap) {
        myMap = theMap;

        myPropertyChangeListener = new IPropertyChangeListener() {

            @Override
            public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
                redraw();
            }
        };
        Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
        redraw();
    }

    private void redraw() {

        Set<String> selectedRoute = Model.INSTANCE.getSelectedRouteTags();
        if (selectedRoute == null) {
            return;
        }

        for (final String nextRoute : selectedRoute) {
            Model.INSTANCE.getStopListForRoute(nextRoute, new MyStopListModelListener(nextRoute));
        }

        for (Iterator<Map.Entry<String, Polyline>> iter = myRoutePolylines.entrySet().iterator(); iter.hasNext();) {
            Entry<String, Polyline> next = iter.next();
            if (!selectedRoute.contains(next.getKey())) {
                next.getValue().setPath(new ArrayList<HasLatLng>());
//            	PolylineImpl.impl.setMap(next.getValue().getJso(), null);
//                iter.remove();
            }
        }

    }

    private final class MyStopListModelListener implements IModelListenerAsync<StopListForRoute> {
        private final String myNextRoute;

        private MyStopListModelListener(String theNextRoute) {
            myNextRoute = theNextRoute;
        }

        @Override
        public void objectLoaded(StopListForRoute theObject, boolean theRequiredAsyncLoad) {
            List<HasLatLng> path = new ArrayList<HasLatLng>();
            
            resetBounds();
            
            // TODO: pick form map from the first selected/visible direction
            StopList points = theObject.getUiStopLists().iterator().next();
            int size = points.getStops().size();
            for (int i = 0; i < size; i++) {
                Stop stop = points.getStops().get(i);
                
                // Add the first and last points as bounds
                // Disabled, as we probably just want vehicles triggering the bounds
//                if (i == 0 || i == size - 1) {
//                    updateBounds(stop.getLatitude(), stop.getLongitude());
//                }

                // Stop data seems to have weird entries at the starts/ends of the route
                if (i == 0) {
                    Stop nextStop = i < points.getStops().size() ? points.getStops().get(i + 1) : null;
                    double distanceFromInKms = stop.distanceFromInKms(nextStop);
                    if (distanceFromInKms > 1.0) {
                        continue;
                    }
                } else {
                    Stop prevStop = i > 0 ? points.getStops().get(i - 1) : null;
                    double distanceFromInKms = stop.distanceFromInKms(prevStop);
                    if (distanceFromInKms > 1.0) {
                        continue;
                    }
                }

                path.add(stop.asLatLng());
            }

            Polyline myRoutePolyLine = myRoutePolylines.get(myNextRoute);
            if (myRoutePolyLine == null) {
                myRoutePolyLine = new Polyline();
                myRoutePolylines.put(myNextRoute, myRoutePolyLine);

                PolylineOptions options = new PolylineOptions();
                options.setStrokeColor("#800000");
                options.setStrokeOpacity(0.5);
                options.setStrokeWeight(3);

                options.setPath(path);
                myRoutePolyLine.setOptions(options);
                myRoutePolyLine.setMap(myMap.getMap());

            } else {
            	
            	myRoutePolyLine.setPath(path);
            	
            }
            
        }

        @Override
        public void startLoadingObject() {
            // nothing
        }
    }

}
