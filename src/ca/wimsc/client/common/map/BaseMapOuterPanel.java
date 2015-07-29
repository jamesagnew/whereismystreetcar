package ca.wimsc.client.common.map;

import java.util.ArrayList;
import java.util.List;

import ca.wimsc.client.common.map.layers.IMapOverlayLayer;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.ObjectUtil;
import ca.wimsc.client.mobile.MapOuterPanelMobile;
import ca.wimsc.client.normal.MapOuterPanelNormal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.HasMapTypeControlOptions;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Base class for the main view (i.e. the streetcar map showing)
 * 
 * Note that there are actually two implementations of this class which are actually used, this class should be
 * considered abstract, but isn't so that we can instantiate a subclass using GWT.create()
 * 
 * @see MapOuterPanelNormal
 * @see MapOuterPanelMobile
 */
public class BaseMapOuterPanel extends BaseOuterPanel {

	public static final String COLOUR_EAST = "6c4fff";
	public static final String COLOUR_NORTH = "319e4b";
	public static final String COLOUR_SOUTH = "9c9a29";
	public static final String COLOUR_WEST = "ff7d4f";

	public static final String MAP_EVENT_CLICK = "click";
	private HandlerRegistration myHistoryRegistration;
	private List<IMapOverlayLayer> myLayers = new ArrayList<IMapOverlayLayer>();
	private Label myLoadingLabel;
	private MapWidget myMap;
	private boolean myNeedToUpdateBounds;
	private HandlerRegistration myResizeRegistration;
	private String mySelectedStopDirectionTag;
	private HasLatLngBounds myInitialBounds;

	/**
	 * Constructor
	 */
	public BaseMapOuterPanel() {
		myInitialBounds = HistoryUtil.getInitialBounds();
	}

	public void addLayer(IMapOverlayLayer theLayer) {
		myLayers.add(theLayer);

		theLayer.initialize(myMap);
	}

	public void removeLayer(IMapOverlayLayer theLayer) {
		myLayers.remove(theLayer);
		theLayer.destroy();
	}
	
	protected void addMapToLayout(@SuppressWarnings("unused") Label theCurrentLayoutContents) {
		throw new UnsupportedOperationException();
	}

	private void buildUi() {

		SimplePanel topPanelContainer = new SimplePanel();
		addNorth(topPanelContainer, BaseMapTopPanel.TOP_PANEL_HEIGHT);
		createTopPanel(topPanelContainer);

		addSouth(new BottomPanel(), BottomPanel.BOTTOM_PANEL_HEIGHT);

		myLoadingLabel = new Label("Waiting for the streetcar... You could be here a while!");
		add(myLoadingLabel);

		// TODO: this is kind of backward, we should invert control here to have
		// the controller create this panel, then start the reload process
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				// Start fetching from the backend
				MapDataController.INSTANCE.requestReload();
			}
		});

		myResizeRegistration = Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent theEvent) {
				resizeMapWidget();
				updateBounds();
			}
		});
	}

	@Override
	public void closeNow() {
		myHistoryRegistration.removeHandler();
		myResizeRegistration.removeHandler();

		for (IMapOverlayLayer next : myLayers) {
			next.destroy();
		}
	}

	private void createMap() {
		MapOptions options = new MapOptions();

		options.setMapTypeControl(isShowMapTypeControl());

		if (isShowMapTypeControl()) {
			options.setMapTypeControlOptions(provideMapTypeControlOptions());
		}

		options.setNavigationControl(isShowNavigationControl());
		options.setMapTypeId(new MapTypeId().getRoadmap());
		options.setDraggable(true);
		options.setScrollwheel(true);

		myMap = new MapWidget(options);
	}

	// private void clearMarkersAndEventListeners() {
	// // Current streetcar locations
	// for (Marker next : myMarkers) {
	// MarkerImpl.impl.setMap(next.getJso(), null);
	// }
	// myMarkers.clear();
	//
	// // Clear listeners
	// Event.clearListeners(myMap.getMap(), MAP_EVENT_CLICK);
	// }

	/**
	 * Subclasses must implement to add the top navigation panel to the view. They should place it in the container
	 * provided.
	 */
	protected void createTopPanel(@SuppressWarnings("unused") Panel theContainer) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The currently displayed map bounds
	 */
	public HasLatLngBounds getCurrentMapBounds() {
		return myMap.getMap().getBounds();
	}

	protected MapWidget getMap() {
		return myMap;
	}

	public String getSelectedStopDirectionTag() {
		return mySelectedStopDirectionTag;
	}

	public void init() {
		createMap();

		buildUi();

		initMap();

		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, new MyRouteAndStopPropertyChangeListener());
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, new MyRouteAndStopPropertyChangeListener());

	}

	protected void initMap() {
		addMapToLayout(myLoadingLabel);
		resizeMapWidget();
		myMap.setVisible(true);
	}

	/**
	 * Should a map type (road, arial, hybrid, etc) control be shown
	 */
	protected boolean isShowMapTypeControl() {
		return true;
	}

	/**
	 * Should a zoomer control be shown
	 */
	protected boolean isShowNavigationControl() {
		return true;
	}

	protected HasMapTypeControlOptions provideMapTypeControlOptions() {
		throw new UnsupportedOperationException();
	}

	protected void resizeMapWidget() {
		int width = Window.getClientWidth();
		int height = Window.getClientHeight() - (BaseMapTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT);
		GWT.log("Resizing map widget to " + width + " / " + height);
		myMap.setPixelSize(width, height);
	}

	private void updateBounds() {

		HasLatLngBounds bounds = null;

		if (myInitialBounds != null) {
			bounds = myInitialBounds;
		} else {
			for (IMapOverlayLayer next : myLayers) {
				HasLatLngBounds nextBounds = next.getBounds();
				bounds = addToBounds(bounds, nextBounds);
			}
		}

		/*
		 * This might happen if there are no vehicles at all on the given route
		 */
		if (bounds == null) {
			ArrayList<Route> selectedRoutes = Model.INSTANCE.getSelectedRoutes();
			if (selectedRoutes != null && selectedRoutes.size() > 0) {
				for (Route route : selectedRoutes) {
					bounds = addToBounds(bounds, route.getWholeRouteBounds());
				}
			}
		}

		if (bounds != null) {
			myMap.fitBounds(bounds);
			myNeedToUpdateBounds = false;
		}

	}

	private HasLatLngBounds addToBounds(HasLatLngBounds theBoundsToAddTo, HasLatLngBounds theBoundsToAdd) {
		if (theBoundsToAdd != null && theBoundsToAddTo != null) {
			theBoundsToAddTo = theBoundsToAddTo.union(theBoundsToAdd);
		} else if (theBoundsToAdd != null) {
			theBoundsToAddTo = theBoundsToAdd;
		}
		return theBoundsToAddTo;
	}

	private final class MyRouteAndStopPropertyChangeListener implements IPropertyChangeListener {
		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			if (myNeedToUpdateBounds) {
				return;
			}
			myNeedToUpdateBounds = true;
			
			if (!ObjectUtil.equals(theOldValue, theNewValue)) {
				myInitialBounds = HistoryUtil.getInitialBounds();
			}
			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					updateBounds();
				}
			});
		}
	}

}
