package ca.wimsc.client.common.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconFactory extends ClientBundle {

    public static final IconFactory INSTANCE = GWT.create(IconFactory.class);
    
    @Source(value="images/stop_icon_14x51-none.png")
    ImageResource getStopIcon14x51();

    @Source(value="images/stop_icon_10x36-grey-none.png")
    ImageResource getStopIcon14x51_50pct();

}
