package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.resources.IconFactory;
import ca.wimsc.client.common.util.IPropertyChangeListener;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.google.gwt.maps.client.overlay.MarkerImage.Builder;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.impl.MarkerImpl;

public class SelectedStopLayer extends AbstractMapOverlayLayer {
    protected static final int ZINDEX_STOP = 6;

    private MapWidget myMap;
    private IPropertyChangeListener myPropertyChangeListener;
    private Set<String> mySelectedStopTags;
    private List<Marker> myMarkers = new ArrayList<Marker>();

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

        resetBounds();
        clearMarkers();

        for (final String nextStopTag : selectedStopTags) {
            Model.INSTANCE.getPredictionListOrRegisterListener(nextStopTag, new IModelListenerAsync<PredictionsList>() {

                @Override
                public void startLoadingObject() {
                    // nothing
                }

                @Override
                public void objectLoaded(PredictionsList theObject, boolean theRequiredAsyncLoad) {
                    
                    double latitude = theObject.getStopLatitude();
                    double longitude = theObject.getStopLongitude();
                    updateBounds(latitude, longitude);
                    
                    LatLng point = new LatLng(latitude, longitude);

                    MarkerOptions options = new MarkerOptions();
                    options.setPosition(point);
                    options.setClickable(false);
                    options.setVisible(true);
                    options.setIcon(createStopMarkerImage());
                    options.setZIndex(ZINDEX_STOP);
                    Marker marker = new Marker(options);

                    myMarkers.add(marker);
                    marker.setMap(myMap.getMap());
                    
                    if (theRequiredAsyncLoad) {
                        Model.INSTANCE.removePredictionListListener(nextStopTag, this);
                    }
                }
            });
        }
        
        mySelectedStopTags = selectedStopTags;

    }

    @Override
    public void destroy() {
        Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myPropertyChangeListener);
        clearMarkers();
    }

    private void clearMarkers() {
        for (Marker marker : myMarkers) {
            MarkerImpl.impl.setMap(marker.getJso(), null);
        }
        myMarkers.clear();
    }

    
    public static MarkerImage createStopMarkerImage() {
        Builder retValBuilder = new MarkerImage.Builder(IconFactory.INSTANCE.getStopIcon14x51().getURL());
        retValBuilder.setAnchor(new Point(7, 51));
        return retValBuilder.build();
    }
}
