package ca.wimsc.client.common.map.layers;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLngBounds;

public class AllStopsLayer implements IMapOverlayLayer {

//    private MapWidget myMap;

    @Override
    public void initialize(MapWidget theMap) {
//        myMap = theMap;

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    
    public void redraw() {
        
//        for (String nextDirection : Model.INSTANCE.getDirectionToStopLists(mySelectedRouteTag).keySet()) {
//            if (HistoryUtil.isDirectionVisible(nextDirection)) {
//                StopList stopList = Model.INSTANCE.getDirectionToStopLists(mySelectedRouteTag).get(nextDirection);
//                for (Stop next : stopList.getStops()) {
//                    LatLng latLng = next.asLatLng();
//
//                    MarkerOptions options = new MarkerOptions();
//                    options.setPosition(latLng);
//                    options.setClickable(false);
//                    options.setVisible(true);
//                    Marker marker = new Marker(options);
//
//                    addMarker(marker);
//
//                }
//            }
//        }
        
    }

    @Override
    public HasLatLngBounds getBounds() {
        return null;
    }
    
}
