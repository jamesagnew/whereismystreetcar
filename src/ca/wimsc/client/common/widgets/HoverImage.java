package ca.wimsc.client.common.widgets;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * Image that has a mouseover/hover image
 */
public class HoverImage extends Image {

    public HoverImage(final ImageResource theResource, final ImageResource theHoverResource) {
        super(theResource);
        
        addStyleName("hoverImage");

        addMouseOverHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent theEvent) {
                setResource(theHoverResource);
            }
        });

        addMouseOutHandler(new MouseOutHandler() {
            
            @Override
            public void onMouseOut(MouseOutEvent theEvent) {
                setResource(theResource);
            }
        });
    
    }
    
}
