package ca.wimsc.client.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.map.BaseMapTopPanel;
import ca.wimsc.client.common.map.BottomPanel;
import ca.wimsc.client.common.map.PredictionsPanel;
import ca.wimsc.client.common.map.RecentTweetsPanelMobile;
import ca.wimsc.client.common.map.layers.SelectedStopLayer;
import ca.wimsc.client.common.map.layers.SpeedRouteLayer;
import ca.wimsc.client.common.map.layers.VehiclesLayerPredictions;
import ca.wimsc.client.common.map.layers.VehiclesLayerWholeRouteIfNoStopSelected;
import ca.wimsc.client.common.model.AbstractModelListener;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.widgets.google.MobileScrollPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.ControlPosition;
import com.google.gwt.maps.client.HasMapTypeControlOptions;
import com.google.gwt.maps.client.MapTypeControlOptions;
import com.google.gwt.maps.client.MapTypeControlStyle;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.impl.ControlPositionImpl;
import com.google.gwt.maps.client.impl.MapTypeControlStyleImpl;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Main outer window for mobile devices (?mobile=1)
 */
public class MapOuterPanelMobile extends BaseMapOuterPanel {

	private LayoutPanel myMapContainer;
	private int myMaxPredictions = 3;
	private MyPredictionListListener myPredictionListListener = new MyPredictionListListener();
	private Panel myPredictionsContainerPanel;
	private PredictionsPanel myPredictionsPanel;
	private RecentTweetsPanelMobile myRecentTweetsPanel;
	private MySelectedStopPropertyChangeListener mySelectedStopPropertyChangeListener = new MySelectedStopPropertyChangeListener();

	@Override
	protected void addMapToLayout(Label theCurrentLayoutContents) {
		MapWidget map = getMap();

		myMapContainer = new LayoutPanel();
		myMapContainer.add(map);

		remove(theCurrentLayoutContents);
		add(myMapContainer);

		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, mySelectedStopPropertyChangeListener);

	}

	@Override
	public void closeNow() {
		super.closeNow();

		if (myRecentTweetsPanel != null) {
			myRecentTweetsPanel.closeNow();
		}
		
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_STOP_PROPERTY, mySelectedStopPropertyChangeListener);
		
		if (myPredictionListListener != null) {
			Model.INSTANCE.removePredictionListListener(myPredictionListListener);
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createTopPanel(final Panel theContainer) {
		theContainer.add(new MapTopPanelMobile(this));
	}

	@Override
	public void init() {
		super.init();

		addLayer(new VehiclesLayerWholeRouteIfNoStopSelected());
		
		VehiclesLayerPredictions vehiclesLayerPredictions = new VehiclesLayerPredictions();
		vehiclesLayerPredictions.setMaxNumToAffectBounds(myMaxPredictions);
		addLayer(vehiclesLayerPredictions);
		
//		SimpleRouteLayer routeLayer = new SimpleRouteLayer();
		SpeedRouteLayer routeLayer = new SpeedRouteLayer(true);
		addLayer(routeLayer);
		
		// We want to just zoom in on the next few streetcars if
		// we have a stop selected
		routeLayer.setDoesNotAffectBounds(true);
		
		addLayer(new SelectedStopLayer());
	}

	@Override
	protected boolean isShowMapTypeControl() {
		return false;
	}

	@Override
	protected boolean isShowNavigationControl() {
		return false;
	}

	@Override
	protected HasMapTypeControlOptions provideMapTypeControlOptions() {
		ControlPositionImpl controlPositionImpl = new ControlPositionImpl();
		MapTypeControlStyleImpl mapTypeControlStyleImpl = new MapTypeControlStyleImpl();
		MapTypeControlOptions retVal = new MapTypeControlOptions(controlPositionImpl, mapTypeControlStyleImpl);
		retVal.setPosition(ControlPosition.RIGHT);
		retVal.setStyle(MapTypeControlStyle.DROPDOWN_MENU);
		List<String> mapTypeIds = new ArrayList<String>();
		mapTypeIds.add(new MapTypeId().getRoadmap());
		mapTypeIds.add(new MapTypeId().getHybrid());
		mapTypeIds.add(new MapTypeId().getSatellite());
		retVal.setMapTypeIds(mapTypeIds);
		return retVal;
	}

	@Override
	protected void resizeMapWidget() {

		if (myMapContainer == null) {
			return;
		}

		Set<String> selectedStops = Model.INSTANCE.getSelectedStopTags();

		boolean force = false;
		if (selectedStops == null) {
			force = HistoryUtil.getStop() == null && HistoryUtil.getRoute() == null;
		}
		
		if (force || (selectedStops != null && !selectedStops.isEmpty())) {

			Orientation newOrientation = Window.getClientHeight() > Window.getClientWidth() ? Orientation.PORTRAIT : Orientation.LANDSCAPE;
			int mapHeight;
			int mapWidth;
			int predictionsTop;
			int predictionsLeft;
			int predictionsHeight;
			int predictionsWidth;

			switch (newOrientation) {
			case LANDSCAPE:
				mapWidth = Window.getClientWidth() / 2;
				mapHeight = Window.getClientHeight() - (BaseMapTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT);
				predictionsLeft = mapWidth;
				predictionsTop = 0;
				predictionsWidth = mapWidth;
				predictionsHeight = mapHeight;
				break;

			case PORTRAIT:
			default:
				mapWidth = Window.getClientWidth();
				mapHeight = (Window.getClientHeight() - (BaseMapTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT)) / 2;
				predictionsLeft = 0;
				predictionsTop = mapHeight;
				predictionsHeight = mapHeight;
				predictionsWidth = Window.getClientWidth();
				break;
			}

			if (myPredictionsContainerPanel != null) {
				myMapContainer.setWidgetTopHeight(myPredictionsContainerPanel, predictionsTop, Unit.PX, predictionsHeight, Unit.PX);
				myMapContainer.setWidgetLeftWidth(myPredictionsContainerPanel, predictionsLeft, Unit.PX, predictionsWidth, Unit.PX);
			}

			myMapContainer.setWidgetTopHeight(getMap(), 0, Unit.PX, mapHeight, Unit.PX);
			myMapContainer.setWidgetLeftWidth(getMap(), 0, Unit.PX, mapWidth, Unit.PX);
			getMap().setPixelSize(mapWidth, mapHeight);

		} else {

			int width = Window.getClientWidth();
			int height = Window.getClientHeight() - (BaseMapTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT);
			myMapContainer.setWidgetLeftWidth(getMap(), 0, Unit.PX, width, Unit.PX);
			myMapContainer.setWidgetTopHeight(getMap(), 0, Unit.PX, height, Unit.PX);
			GWT.log("Resizing map widget to " + width + " / " + height);
			getMap().setPixelSize(width, height);

		}
	}


	private void updatePredictionsPanelPredictions(Map<String, PredictionsList> theObject) {
		if (myPredictionsPanel != null) {
			myPredictionsPanel.updateList(theObject.values());
		}
	}

	private void updatePredictionsPanelShowingStatus() {
		Set<String> selectedStops = Model.INSTANCE.getSelectedStopTags();
		if (!selectedStops.isEmpty() && myPredictionsPanel == null) {

			myPredictionsPanel = new PredictionsPanel();

			if (myRecentTweetsPanel == null) {
				myRecentTweetsPanel = new RecentTweetsPanelMobile();
				myRecentTweetsPanel.updateShowingTweet();
			}
			myPredictionsPanel.addWidgetToTop(myRecentTweetsPanel);

			myPredictionsContainerPanel = new MobileScrollPanel();
			myPredictionsContainerPanel.add(myPredictionsPanel);

			myMapContainer.add(myPredictionsContainerPanel);
			myMapContainer.setWidgetLeftWidth(myPredictionsContainerPanel, 0, Unit.PCT, 100, Unit.PCT);

			resizeMapWidget();

			Model.INSTANCE.addPredictionListListener(myPredictionListListener);
			
		} else if (selectedStops.isEmpty() && myPredictionsPanel != null) {

			if (myRecentTweetsPanel != null) {
				myPredictionsPanel.removeWidgetFromTop(myRecentTweetsPanel);
			}

			myMapContainer.remove(myPredictionsContainerPanel);
			myPredictionsContainerPanel = null;
			myPredictionsPanel = null;

			resizeMapWidget();

			Model.INSTANCE.removePredictionListListener(myPredictionListListener);
			
		}
	}

	private final class MyPredictionListListener extends AbstractModelListener<Map<String, PredictionsList>> {
		
		@Override
		public void objectLoaded(Map<String, PredictionsList> theObject, boolean theRequiredAsyncLoad) {
//			if (myPredictionsPanel != null) {
				updatePredictionsPanelPredictions(theObject);
//			}
		}
	}

	private final class MySelectedStopPropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			updatePredictionsPanelShowingStatus();
		}
	}

	private static enum Orientation {
		LANDSCAPE, PORTRAIT;
	}
}
