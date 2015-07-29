package ca.wimsc.client.common.map.layers;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLngBounds;

public interface IMapOverlayLayer {

    /**
     * Invoked when the layer is first being added to the map
     */
    void initialize(MapWidget theMap);
    
    /**
     * Invoked when the layer is being destroyed
     */
    void destroy();
    
    /**
     * @return Provide the map bounds associated with this layer 
     */
    HasLatLngBounds getBounds();
     
    
}
