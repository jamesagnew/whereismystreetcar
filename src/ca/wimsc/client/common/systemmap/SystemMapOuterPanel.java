package ca.wimsc.client.common.systemmap;

import java.util.ArrayList;
import java.util.List;

import ca.wimsc.client.common.map.BaseMapTopPanel;
import ca.wimsc.client.common.map.BaseOuterPanel;
import ca.wimsc.client.common.map.BottomPanel;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.IClosable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.LayoutPanel;

public class SystemMapOuterPanel extends BaseOuterPanel implements IClosable {
    private MapWidget myMap;
    private HandlerRegistration myResizeHandlerReg;
    private TtcSystemMapOverlay myTtcMapOverlay;
    private LayoutPanel myMapContainer;
    private Route mySelectedRoute;
    private List<Marker> myMarkers = new ArrayList<Marker>();

    /**
     * Constructor
     */
    public SystemMapOuterPanel() {
        super();

        myResizeHandlerReg = Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent theEvent) {
                resizeMapWidget();
            }
        });

        GWT.runAsync(SystemMapOuterPanel.class, new RunAsyncCallback() {
            
            @Override
            public void onSuccess() {
                initUi();
            }
            
            @Override
            public void onFailure(Throwable theReason) {
                Common.handleUnexpectedError(theReason);
            }
        });

    }

    private void initUi() {

        addNorth(new SystemMapTopPanel(this), BaseMapTopPanel.TOP_PANEL_HEIGHT);

        addSouth(new BottomPanel(), BottomPanel.BOTTOM_PANEL_HEIGHT);

        MapOptions options = new MapOptions();

        options.setMapTypeControl(false);
        options.setNavigationControl(false);
        options.setMapTypeId(new MapTypeId().getRoadmap());
        options.setDraggable(true);
        options.setScrollwheel(true);

        myMap = new MapWidget(options);
        add(myMap);
        
        myMapContainer = new LayoutPanel();
        myMapContainer.add(myMap);

        add(myMapContainer);
        
        myTtcMapOverlay = new TtcSystemMapOverlay(myMap.getMap());
        myTtcMapOverlay.setMap(myMap.getMap());
        
        resizeMapWidget();
        

    }


    private void resizeMapWidget() {
        myMap.setPixelSize(Window.getClientWidth(), Window.getClientHeight() - (BaseMapTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT));
    }

    @Override
    public void closeNow() {
        myResizeHandlerReg.removeHandler();
    }

    public Route getSelectedRoute() {
        return mySelectedRoute;
    }

    /**
     * Add a marker to the map, and add it to the list of markers to be cleared when the next redraw happens
     */
    protected void addMarker(Marker marker) {
        marker.setMap(myMap.getMap());
        myMarkers.add(marker);
    }
    
}
