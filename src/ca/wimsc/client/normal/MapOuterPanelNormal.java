package ca.wimsc.client.normal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.map.BottomPanel;
import ca.wimsc.client.common.map.layers.IMapFooterLayer;
import ca.wimsc.client.common.map.layers.RecentTweetsFooterLayer;
import ca.wimsc.client.common.map.layers.RouteStatsLayer;
import ca.wimsc.client.common.map.layers.SelectedStopLayer;
import ca.wimsc.client.common.map.layers.SpeedRouteLayer;
import ca.wimsc.client.common.map.layers.SystemMapLayer;
import ca.wimsc.client.common.map.layers.VehiclesLayerPredictions;
import ca.wimsc.client.common.map.layers.VehiclesLayerWholeRoute;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.OverlayMode;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.widgets.HtmlBr;
import ca.wimsc.client.common.widgets.NonManagedLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.ControlPosition;
import com.google.gwt.maps.client.HasMapTypeControlOptions;
import com.google.gwt.maps.client.MapTypeControlOptions;
import com.google.gwt.maps.client.MapTypeControlStyle;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.impl.ControlPositionImpl;
import com.google.gwt.maps.client.impl.MapTypeControlStyleImpl;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class MapOuterPanelNormal extends BaseMapOuterPanel {

	private static final String IMG_DOWN_ARROW = "<img src='images/direction_10x5_down.png' height='5' width='10' valign='baseline' style='margin-left: 5px; margin-bottom: 2px;'/>";
	private static final String IMG_SPEED_GRADIENT = "<img src='images/speed_gradient_56x8.png' height='8' width='56' valign='baseline'/>";
	private static final String IMG_TWEETS = "<img src='images/tweets_layer_10x12.png' height='12' width='10' valign='baseline'/>";
	private static final String IMG_SYSTEMMAP = "<img src='images/layer_systemmap_36x15.png' height='15' width='36'/>";
	private static final String IMG_STATS = "<img src='images/stats_layer_29x14.png' height='14' width='29' valign='baseline'/>";

	private static final int LAYERS_BOX_RIGHT = 112;
	private static final int LAYERS_BOX_WIDTH = 160;
	private static final int LAYERS_BOX_TOP_OFFSET = 5;
	private static final int LAYERS_BOX_HEIGHT = 16;
	static final int FLOATING_WIDTH = 220;

	private static final int MAP_MARGIN_TOP = 5;
	static final int MARGINS_WIDTH = 5;
	private ChooseFromAddressPanel myChooseFromAddressPanel;
	private LeftSideControlsPanel myLeftSidePanel;
	private LayoutPanel myMapContainer;
	private boolean myShowingAddress;
	private boolean myShowingMap;
	private NonManagedLabel myTtcMapTypeButton;
	private SpeedRouteLayer mySpeedRouteLayer;
	private SystemMapLayer mySystemMapLayer;
	private List<IMapFooterLayer> myFooterLayers = new ArrayList<IMapFooterLayer>();
	private RecentTweetsFooterLayer myRecentTweetsFooterLayer;
	private RouteStatsLayer myStatsFooterLayer;


	/**
	 * Constructor
	 */
	public MapOuterPanelNormal() {
	}


	@Override
	public void init() {
		super.init();

		addLayer(new VehiclesLayerWholeRoute());
		addLayer(new VehiclesLayerPredictions());
		addLayer(new SelectedStopLayer());

		// TODO: fix this to work with new favourite model and re-add
		// addLayer(new NonSelectedFavouriteStopsLayer());

	}


	/**
	 * Subclasses may override
	 * 
	 * @return
	 */
	protected boolean allowSystemMapOverlay() {
		return true;
	}


	@Override
	protected void addMapToLayout(Label theCurrentLayoutContents) {
		myMapContainer = new LayoutPanel();
		myLeftSidePanel = new LeftSideControlsPanel();
		myMapContainer.add(myLeftSidePanel);

		remove(theCurrentLayoutContents);
		add(myMapContainer);

		// Add map
		showMap();

		addTtcMapOverlay();

	}


	@Override
	public void closeNow() {
		super.closeNow();

		if (myLeftSidePanel != null) {
			myLeftSidePanel.closeNow();
		}

		for (IMapFooterLayer next : myFooterLayers) {
			next.closeNow();
		}
	}


	public void addFooterLayer(IMapFooterLayer theFooterLayer) {
		assert theFooterLayer != null;
		assert myFooterLayers.contains(theFooterLayer) == false;

		myFooterLayers.add(theFooterLayer);
		
		Collections.sort(myFooterLayers, new Comparator<IMapFooterLayer>() {
			@Override
			public int compare(IMapFooterLayer theO1, IMapFooterLayer theO2) {
				return theO2.getBottomIndex() - theO2.getBottomIndex();
			}});
		
		myMapContainer.add(theFooterLayer);

		resizeMapWidget();
	}


	public void removeFooterLayer(IMapFooterLayer theFooterLayer) {
		assert theFooterLayer != null;
		assert myFooterLayers.contains(theFooterLayer) == true;

		myFooterLayers.remove(theFooterLayer);
		myMapContainer.remove(theFooterLayer);

		resizeMapWidget();
	}


	/**
	 * Adds the system map overlay
	 */
	private void addTtcMapOverlay() {
		myTtcMapTypeButton = new NonManagedLabel();
		myTtcMapTypeButton.addClickHandler(new MyTtcMapTypeClickHandler());

		myTtcMapTypeButton.getElement().getStyle().setPosition(Position.ABSOLUTE);
		myTtcMapTypeButton.getElement().getStyle().setPropertyPx("height", LAYERS_BOX_HEIGHT);
		myTtcMapTypeButton.getElement().getStyle().setPropertyPx("top", LAYERS_BOX_TOP_OFFSET);
		myTtcMapTypeButton.getElement().getStyle().setPropertyPx("right", LAYERS_BOX_RIGHT);
		myTtcMapTypeButton.getElement().getStyle().setProperty("whitespace", "nowrap");
		myTtcMapTypeButton.getElement().getStyle().setPropertyPx("width", LAYERS_BOX_WIDTH);
		myTtcMapTypeButton.getElement().getStyle().setOverflow(Overflow.HIDDEN);

		/*
		 * The Maps Widget doesn't expose any api to get access to the DIV it creates through normal GWT channels, so we
		 * manipulate the DOM directly to create another button on the map
		 */
		getMap().getElement().appendChild(myTtcMapTypeButton.getElement());
		myTtcMapTypeButton.onAttach();

		/*
		 * This is the proper GWT way of adding the button, but then it doesn't overflow properly (i.e. it goes over the
		 * left hand side of the map div if you resize the window to be too small to hold it. Hopefully one day the
		 * official maps v3 GWT API will be released and we'll remove this hack
		 */
		// myMapContainer.add(myTtcMapTypeButton);
		// myMapContainer.setWidgetTopHeight(myTtcMapTypeButton, 35, Unit.PX, 18, Unit.PX);
		// myMapContainer.setWidgetRightWidth(myTtcMapTypeButton, 160, Unit.PX, 120, Unit.PX);

		updateOverlays();

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createTopPanel(final Panel theContainer) {
		GWT.runAsync(MapTopPanelNormal.class, new RunAsyncCallback() {

			@Override
			public void onSuccess() {
				theContainer.add(new MapTopPanelNormal(MapOuterPanelNormal.this));
			}


			@Override
			public void onFailure(Throwable theReason) {
				Common.handleUnexpectedError(theReason);
			}
		});
	}


	@Override
	protected HasMapTypeControlOptions provideMapTypeControlOptions() {
		ControlPositionImpl controlPositionImpl = new ControlPositionImpl();
		MapTypeControlStyleImpl mapTypeControlStyleImpl = new MapTypeControlStyleImpl();
		MapTypeControlOptions retVal = new MapTypeControlOptions(controlPositionImpl, mapTypeControlStyleImpl);
		retVal.setPosition(ControlPosition.RIGHT);
		retVal.setStyle(MapTypeControlStyle.HORIZONTAL_BAR);
		List<String> mapTypeIds = new ArrayList<String>();
		mapTypeIds.add(new MapTypeId().getRoadmap());
		mapTypeIds.add(new MapTypeId().getHybrid());
		mapTypeIds.add(new MapTypeId().getSatellite());
		retVal.setMapTypeIds(mapTypeIds);
		return retVal;
	}


	@Override
	protected void resizeMapWidget() {

		int mapHeight = 0;
		if (myShowingMap) {
			mapHeight = getMapHeight();
			getMap().setPixelSize(getMapWidth() - 2, mapHeight - 2);
		}

		int floatingHeight = Window.getClientHeight() - (BaseTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT);
		myMapContainer.setWidgetLeftWidth(myLeftSidePanel, 0, Unit.PX, FLOATING_WIDTH - MARGINS_WIDTH, Unit.PX);
		myMapContainer.setWidgetTopHeight(myLeftSidePanel, 0, Unit.PX, floatingHeight, Unit.PX);

		int tweetsPanelTop = mapHeight + MAP_MARGIN_TOP + MAP_MARGIN_TOP;
		for (IMapFooterLayer next : myFooterLayers) {
			myMapContainer.setWidgetLeftWidth((Widget) next, FLOATING_WIDTH, Unit.PX, getMapWidth(), Unit.PX);
			myMapContainer.setWidgetTopHeight((Widget) next, tweetsPanelTop, Unit.PX, next.getFooterHeight(), Unit.PX);

			tweetsPanelTop += next.getFooterHeight();
		}

	}


	// private MarkerImage provideMarkerImage(int width, int depth, int pointx, int pointy, int angle) {
	// String key = "" + width + depth + pointx + pointy + angle;
	// MarkerImage markerImage;
	// markerImage = myKeyToLocationMarkerImage.get(key);
	// if (markerImage != null) {
	// return markerImage;
	// }
	//
	// // Builder iconBuilder = new MarkerImage.Builder("images/clrv/bubble-" + width + "x" + depth + "-" + angle +
	// ".png");
	// Builder iconBuilder = new MarkerImage.Builder(VehicleMarkerFactory.createVehicleMarker());
	//
	// iconBuilder.setAnchor(new Point(pointx, pointy));
	// markerImage = iconBuilder.build();
	//
	// myKeyToLocationMarkerImage.put(key, markerImage);
	//
	// return markerImage;
	// }

	private void updateOverlays() {
		Set<OverlayMode> overlays = HistoryUtil.getOverlayMode();

		String firstLayer = null;
		if (overlays.contains(OverlayMode.ROUTE_SPEED)) {
			if (mySpeedRouteLayer == null) {
				mySpeedRouteLayer = new SpeedRouteLayer(true);
				addLayer(mySpeedRouteLayer);
			}
			if (firstLayer == null) {
				firstLayer = "Speed&nbsp;" + IMG_SPEED_GRADIENT;
			}
		} else if (mySpeedRouteLayer != null) {
			removeLayer(mySpeedRouteLayer);
			mySpeedRouteLayer = null;
		}

		if (allowSystemMapOverlay()) {
			if (overlays.contains(OverlayMode.WHOLE_SYSTEM_MAP)) {
				if (mySystemMapLayer == null) {
					mySystemMapLayer = new SystemMapLayer();
					addLayer(mySystemMapLayer);
				}
				if (firstLayer == null) {
					firstLayer = "System&nbsp;Map";
				}
			} else if (mySystemMapLayer != null) {
				removeLayer(mySystemMapLayer);
				mySystemMapLayer = null;
			}
		}

		if (overlays.contains(OverlayMode.RECENT_TWEETS)) {
			if (myRecentTweetsFooterLayer == null) {
				myRecentTweetsFooterLayer = new RecentTweetsFooterLayer();
				addFooterLayer(myRecentTweetsFooterLayer);
			}
			if (firstLayer == null) {
				firstLayer = "Recent&nbsp;Tweets";
			}
		} else {
			if (myRecentTweetsFooterLayer != null) {
				removeFooterLayer(myRecentTweetsFooterLayer);
				myRecentTweetsFooterLayer = null;
			}
		}

		if (overlays.contains(OverlayMode.STATS)) {
			if (myStatsFooterLayer == null) {
				myStatsFooterLayer = new RouteStatsLayer();
				addFooterLayer(myStatsFooterLayer);
			}
			if (firstLayer == null) {
				firstLayer = "Stats";
			}
		} else {
			if (myStatsFooterLayer != null) {
				removeFooterLayer(myStatsFooterLayer);
				myStatsFooterLayer = null;
			}
		}

		StringBuilder html = new StringBuilder();
		html.append(IMG_DOWN_ARROW).append("&nbsp;");
		if (overlays.size() > 1) {
			html.append("Layers:&nbsp;");
		} else if (overlays.size() == 1) {
			html.append("Layer:&nbsp;");
		} else {
			html.append("No&nbsp;Layers&nbsp;");
		}

		if (firstLayer != null) {
			html.append(firstLayer);
		}
		myTtcMapTypeButton.setHTML(html.toString());

		myTtcMapTypeButton.setStyleName("ttcMapTypeButtonActivated");

		// if (myTtcMapShowing != null && theValue == myTtcMapShowing) {
		// return;
		// }
		//
		// if (theValue) {
		//
		// myTtcMapOverlay = new TtcSystemMapOverlay(getMap().getMap());
		// myTtcMapOverlay.setMap(getMap().getMap());
		// myTtcMapTypeButton.setStyleName("ttcMapTypeButtonActivated");
		// myTtcMapTypeButton.setText(TTC_SYSTEM_MAP_ON);
		//
		// } else {
		//
		// if (myTtcMapOverlay != null) {
		// myTtcMapOverlay.kill();
		//
		// // FIXME: google maps throws a weird error here.. Possibly a bug in their
		// // implementation? try again later and see if it's fixed. For now, we
		// // mostly remove traces of the overlay manually
		// // myTtcMapOverlay.setMap(null);
		//
		// myTtcMapOverlay = null;
		//
		// }
		//
		// myTtcMapTypeButton.setStyleName("ttcMapTypeButton");
		// myTtcMapTypeButton.setText(TTC_SYSTEM_MAP_OFF);
		//
		// }
		//
		// myTtcMapShowing = theValue;
		// HistoryUtil.setOverlayMode(OverlayMode.SYSTEM_MAP, myTtcMapShowing);

	}


	void showChooseFromAddress(String theAddressText) {

		if (myShowingAddress == false) {
			if (myChooseFromAddressPanel == null) {
				myChooseFromAddressPanel = new ChooseFromAddressPanel(this);
			}

			myMapContainer.remove(getMap());

			int mapWidth = getMapWidth();
			int mapHeight = getMapHeight();

			if (myMapContainer.getWidgetIndex(myChooseFromAddressPanel) == -1) {
				myMapContainer.add(myChooseFromAddressPanel);
			}
			myMapContainer.setWidgetLeftWidth(myChooseFromAddressPanel, FLOATING_WIDTH, Unit.PX, mapWidth, Unit.PX);
			myMapContainer.setWidgetTopHeight(myChooseFromAddressPanel, MAP_MARGIN_TOP, Unit.PX, mapHeight, Unit.PX);
		}

		myChooseFromAddressPanel.setAddressText(theAddressText);

		myShowingMap = false;
		myShowingAddress = true;

	}


	void showMap() {
		if (myShowingMap) {
			return;
		}

		if (myChooseFromAddressPanel != null) {
			myMapContainer.remove(myChooseFromAddressPanel);
			myChooseFromAddressPanel = null;
		}

		MapWidget map = getMap();
		map.addStyleName("mapItself");

		int mapWidth = getMapWidth();
		int mapHeight = getMapHeight();

		myMapContainer.add(map);
		myMapContainer.setWidgetLeftWidth(map, FLOATING_WIDTH, Unit.PX, mapWidth, Unit.PX);
		myMapContainer.setWidgetTopHeight(map, MAP_MARGIN_TOP, Unit.PX, mapHeight, Unit.PX);

		myShowingMap = true;
		myShowingAddress = false;

		resizeMapWidget();
	}


	public int getMapHeight() {
		int retVal = Window.getClientHeight() - (BaseTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT + MAP_MARGIN_TOP /*
																																 * +
																																 * MAP_MARGIN_BOTTOM
																																 */+ MARGINS_WIDTH);

		for (IMapFooterLayer next : myFooterLayers) {
			retVal -= next.getFooterHeight();
		}

		return retVal;
	}


	public int getMapWidth() {
		return Window.getClientWidth() - (FLOATING_WIDTH + MARGINS_WIDTH);
	}


	public class MyTtcMapTypeClickHandler implements ClickHandler {

		private MyLayersPopup myLayersPopup;


		@Override
		public void onClick(ClickEvent theEvent) {
			if (myLayersPopup != null) {
				myLayersPopup.hide();
				myLayersPopup = null;
			} else {
				myLayersPopup = new MyLayersPopup();
			}

			// if (newValue) {
			// Common.trackGoogleAnalyticsEvent("Config", "ShowSystemMap");
			// } else {
			// Common.trackGoogleAnalyticsEvent("Config", "HideSystemMap");
			// }
		}

	}


	private class MyLayersPopup extends PopupPanel {

		private CheckBox mySpeedCheckbox;
		private CheckBox mySystemMapCheckbox;
		private CheckBox myTwitterCheckbox;
		private CheckBox myStatsCheckbox;


		public MyLayersPopup() {
			addStyleName("layersPopup");

			FlowPanel contentPanel = new FlowPanel();
			this.add(contentPanel);

			Set<OverlayMode> overlayMode = HistoryUtil.getOverlayMode();
			GWT.log("Overlay mode is: " + overlayMode);

			mySpeedCheckbox = new CheckBox();
			mySpeedCheckbox.setHTML("Speed&nbsp;" + IMG_SPEED_GRADIENT);
			contentPanel.add(mySpeedCheckbox);
			contentPanel.add(new HtmlBr());
			mySpeedCheckbox.setValue(overlayMode.contains(OverlayMode.ROUTE_SPEED));
			mySpeedCheckbox.addValueChangeHandler(new MyCheckboxHandler());

			if (allowSystemMapOverlay()) {
				mySystemMapCheckbox = new CheckBox();
				mySystemMapCheckbox.setHTML("System&nbsp;Map&nbsp;" + IMG_SYSTEMMAP);
				contentPanel.add(mySystemMapCheckbox);
				mySystemMapCheckbox.setValue(overlayMode.contains(OverlayMode.WHOLE_SYSTEM_MAP));
				mySystemMapCheckbox.addValueChangeHandler(new MyCheckboxHandler());
				contentPanel.add(new HtmlBr());
			}

			myTwitterCheckbox = new CheckBox();
			myTwitterCheckbox.setHTML("Recent&nbsp;Tweets&nbsp;" + IMG_TWEETS);
			contentPanel.add(myTwitterCheckbox);
			contentPanel.add(new HtmlBr());
			myTwitterCheckbox.setValue(overlayMode.contains(OverlayMode.RECENT_TWEETS));
			myTwitterCheckbox.addValueChangeHandler(new MyCheckboxHandler());

			myStatsCheckbox = new CheckBox();
			myStatsCheckbox.setHTML("Nerdy&nbsp;Stats&nbsp;" + IMG_STATS);
			contentPanel.add(myStatsCheckbox);
			contentPanel.add(new HtmlBr());
			myStatsCheckbox.setValue(overlayMode.contains(OverlayMode.STATS));
			myStatsCheckbox.addValueChangeHandler(new MyCheckboxHandler());

			setWidth((LAYERS_BOX_WIDTH - 5) + "px");
			int left = Window.getClientWidth() - (LAYERS_BOX_WIDTH + LAYERS_BOX_RIGHT + MARGINS_WIDTH + 4);
			int top = BaseTopPanel.TOP_PANEL_HEIGHT + MARGINS_WIDTH + LAYERS_BOX_TOP_OFFSET + LAYERS_BOX_HEIGHT + 3;
			setPopupPosition(left, top);
			show();
		}


		private class MyCheckboxHandler implements ValueChangeHandler<Boolean> {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				Set<OverlayMode> layers = new HashSet<OverlayMode>();
				if (mySpeedCheckbox.getValue()) {
					layers.add(OverlayMode.ROUTE_SPEED);
				}
				if (mySystemMapCheckbox != null && mySystemMapCheckbox.getValue()) {
					layers.add(OverlayMode.WHOLE_SYSTEM_MAP);
				}
				if (myStatsCheckbox.getValue()) {
					layers.add(OverlayMode.STATS);
				}
				if (myTwitterCheckbox.getValue()) {
					layers.add(OverlayMode.RECENT_TWEETS);
				}
				if (layers.isEmpty()) {
					layers.add(OverlayMode.NONE);
				}
				
				Set<OverlayMode> currentOverlays = HistoryUtil.getOverlayMode();
				for (OverlayMode overlayMode : currentOverlays) {
					if (layers.contains(overlayMode) == false) {
						Common.trackGoogleAnalyticsEvent("Overlays", "Disable_" + overlayMode.name());
					}
				}
				for (OverlayMode overlayMode : layers) {
					if (currentOverlays.contains(overlayMode) == false) {
						Common.trackGoogleAnalyticsEvent("Overlays", "Enable_" + overlayMode.name());
					}
				}
				
				HistoryUtil.setOverlayMode(layers);
				updateOverlays();
			}

		}
	}

}
