package ca.wimsc.client.common.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface VehicleMarkers extends ClientBundle {

    public static final VehicleMarkers INSTANCE = GWT.create(VehicleMarkers.class);

    @Source("/images/streetcar-13x18.png")
    ImageResource streetcarIcon();

    @Source("/images/bus-13x18.png")
    ImageResource busIcon();
    
    @Source("/images/long-streetcar-25x30-0.png")
    ImageResource getLongContainer0();
    
    @Source("/images/long-streetcar-25x30-90.png")
    ImageResource getLongContainer90();

    @Source("/images/long-streetcar-25x30-270.png")
    ImageResource getLongContainer270();

    @Source("/images/long-streetcar-25x30-180.png")
    ImageResource getLongContainer180();

}
