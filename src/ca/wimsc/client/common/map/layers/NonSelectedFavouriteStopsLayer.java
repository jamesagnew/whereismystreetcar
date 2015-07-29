package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.model.FavouriteStop;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.resources.IconFactory;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.event.Event;
import com.google.gwt.maps.client.event.EventCallback;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.google.gwt.maps.client.overlay.MarkerImage.Builder;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.impl.MarkerImpl;

/**
 * Displays clickable icons for the favourite stops that aren't currently selected
 */
public class NonSelectedFavouriteStopsLayer extends AbstractMapOverlayLayer {
    protected static final int ZINDEX_STOP_NOT_SELECTED = 5;

    private MapWidget myMap;
    private IPropertyChangeListener myPropertyChangeListener;
    private Set<String> mySelectedStopTags;
    private List<Marker> myMarkers = new ArrayList<Marker>();

    public NonSelectedFavouriteStopsLayer() {
    	
    }
    
    @Override
    public void initialize(MapWidget theMap) {
        myMap = theMap;

        myPropertyChangeListener = new IPropertyChangeListener() {

            @Override
            public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
                redrawIfSelectionsHaveChanged();
            }
        };

        Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myPropertyChangeListener);

        redrawIfSelectionsHaveChanged();
    }

    private void redrawIfSelectionsHaveChanged() {
        Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();

        if (selectedStopTags == null) {
            return;
        }
        if (mySelectedStopTags != null && mySelectedStopTags.equals(selectedStopTags)) {
            return;
        }

//        resetBounds();
        clearMarkers();

        for (final FavouriteStop next : Model.INSTANCE.getRecentOrFavouriteStops()) {
            if (!Model.INSTANCE.getSelectedStopTags().contains(next.getStopTag())) {
                
//                updateBounds(next.getLatitude(), next.getLongitude());
                
                MarkerOptions options = new MarkerOptions();
                options.setPosition(next.asLatLng());
                options.setClickable(true);
                options.setVisible(true);
                options.setIcon(createStopMarkerImage50pct());
                options.setZIndex(ZINDEX_STOP_NOT_SELECTED);
                options.setTitle(next.getAssignedName());
                Marker marker = new Marker(options);

                Event.addListener(marker, BaseMapOuterPanel.MAP_EVENT_CLICK, new EventCallback() {

                    @Override
                    public void callback() {
                        HistoryUtil.setStop(next.getRouteTag(), next.getStopTag());
                    }
                });

                myMarkers.add(marker);
                marker.setMap(myMap.getMap());
            }
        }

        mySelectedStopTags = selectedStopTags;

    }

    public static MarkerImage createStopMarkerImage50pct() {
        Builder retValBuilder = new MarkerImage.Builder(IconFactory.INSTANCE.getStopIcon14x51_50pct().getURL());
        retValBuilder.setAnchor(new Point(5, 36));
        return retValBuilder.build();
    }

    private void clearMarkers() {
        for (Marker marker : myMarkers) {
            MarkerImpl.impl.setMap(marker.getJso(), null);
            Event.clearListeners(marker, BaseMapOuterPanel.MAP_EVENT_CLICK);
        }
        myMarkers.clear();
    }

    @Override
    public void destroy() {
        Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myPropertyChangeListener);
        clearMarkers();
    }

}
