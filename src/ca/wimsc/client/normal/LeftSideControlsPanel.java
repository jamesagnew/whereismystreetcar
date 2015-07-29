package ca.wimsc.client.normal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.map.FavouritesGrid.IFavouriteActionHandler;
import ca.wimsc.client.common.model.AbstractModelListener;
import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.select.ChooseRoutesAndStopActionsPanel;
import ca.wimsc.client.common.select.ChooseRoutesAndStopsPanel;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel which takes up the whole left side of the window and has a stop chooser, the list of favourites, and
 * predictions
 */
public class LeftSideControlsPanel extends TabLayoutPanel implements IClosable {

	private static final int FAVOURITES_HEIGHT = 100;
	private static final int TAB_INDEX_CHOOSE_ROUTES_AND_STOPS = 1;
	private static final int TAB_INDEX_PREDICTIONS = 0;
	
	private ChooseRoutesAndStopActionsPanel myChooseRoutesAndStopsActionPanel;
	private Label myFavHideShowLabel;
	private LeftFavouritesPanel myFavouritesPanel;
	private ScrollPanel myFavouritesScrollPanel;
	private MyFavPanelHideShowClickHandler myFavPanelHideShowClickHandler;
	private boolean myHideFavPanel = HistoryUtil.isNormalModeHideFavouritesPanel();
	private DockLayoutPanel myInfoContainer;
	private ChooseRoutesAndStopsPanel myLeftChooseStopsPanel;
	private DockLayoutPanel myOuterPredictionsContainer;
	private ArrayList<LeftPredictionsPanel> myPredictionsPanels;
	private FlowPanel myTabChooseStopsPanel;
	private LayoutPanel myTabbarLayoutPanel;
	/**
	 * Constructor
	 */
	public LeftSideControlsPanel() {
		super(21, Unit.PX);
		
		// This is a hack, but the new tabbarlayout is not terribly extendible.. This
		// is placing the whole tab bar one pixel below where it goes by default, so that
		// the selected tab does not appear to have a bottom border
		Widget tabbar = myTabbarLayoutPanel.getWidget(0);
		tabbar.getElement().getStyle().setZIndex(999);
	    myTabbarLayoutPanel.setWidgetTopHeight(tabbar, 1d, Unit.PX, 21d, Unit.PX);
		
		addStyleName("mapFloatingPanel");

		initControls();
		updateControls();

		updateVisiblePredictions();
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, new IPropertyChangeListener() {

			@Override
			public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
				updateVisiblePredictions();
			}
		});

	}

	@Override
	protected void initWidget(Widget theWidget) {
		super.initWidget(theWidget);
		
		if (theWidget instanceof LayoutPanel) {
			myTabbarLayoutPanel = (LayoutPanel) theWidget;
		}
	}

	private void initControls() {
		this.clear();

		// Choose stops

		myTabChooseStopsPanel = new FlowPanel();

		myChooseRoutesAndStopsActionPanel = new ChooseRoutesAndStopActionsPanel();
		myTabChooseStopsPanel.add(myChooseRoutesAndStopsActionPanel);
		myChooseRoutesAndStopsActionPanel.addSelectButtonClickHandler(new MyStopSelectButtonClickHandler());

		myLeftChooseStopsPanel = new ChooseRoutesAndStopsPanel(myChooseRoutesAndStopsActionPanel);
		myChooseRoutesAndStopsActionPanel.setChooseStopsPanel(myLeftChooseStopsPanel);
		myTabChooseStopsPanel.add(myLeftChooseStopsPanel);

		// Favourites

		myInfoContainer = new DockLayoutPanel(Unit.PX);

		LayoutPanel favHeaderPanel = new LayoutPanel();
		myInfoContainer.addNorth(favHeaderPanel, 20);
		favHeaderPanel.setWidth((MapOuterPanelNormal.FLOATING_WIDTH - (4 + MapOuterPanelNormal.MARGINS_WIDTH)) + "px");
		favHeaderPanel.addStyleName("mapFloatingHeader");

		Label favHeaderLabel = new Label("Favourites");
		favHeaderPanel.add(favHeaderLabel);
		favHeaderPanel.setWidgetHorizontalPosition(favHeaderLabel, Alignment.BEGIN);

		myFavHideShowLabel = new Label();
		myFavHideShowLabel.addStyleName("mapFloatingStopsHideShowLabel");
		favHeaderPanel.add(myFavHideShowLabel);
		favHeaderPanel.setWidgetHorizontalPosition(myFavHideShowLabel, Alignment.END);

		myFavPanelHideShowClickHandler = new MyFavPanelHideShowClickHandler(myHideFavPanel);
		myFavHideShowLabel.addClickHandler(myFavPanelHideShowClickHandler);

		myFavouritesPanel = new LeftFavouritesPanel(new MyFavouritesPanelActionHandler());
		myFavouritesScrollPanel = new ScrollPanel(myFavouritesPanel);
		myInfoContainer.addNorth(myFavouritesScrollPanel, FAVOURITES_HEIGHT);

		// Predictions

		myOuterPredictionsContainer = new DockLayoutPanel(Unit.PCT);
		myInfoContainer.add(myOuterPredictionsContainer);

		this.add(myInfoContainer, "Predictions");
		this.add(myTabChooseStopsPanel, "Choose Stops");

	}

	private void updateControls() {

		// Favourite

		if (myHideFavPanel) {
			myFavHideShowLabel.setText("Show");
			myFavPanelHideShowClickHandler.setValue(false);
			myFavouritesScrollPanel.setVisible(false);
			myInfoContainer.setWidgetSize(myFavouritesScrollPanel, 3);
		} else {
			myFavHideShowLabel.setText("Hide");
			myFavPanelHideShowClickHandler.setValue(true);
			myFavouritesScrollPanel.setVisible(true);
			myInfoContainer.setWidgetSize(myFavouritesScrollPanel, FAVOURITES_HEIGHT);
		}

	}

	private void updateVisiblePredictions() {
		Set<String> selectedStop = Model.INSTANCE.getSelectedStopTags();
		if (selectedStop == null) {
			return;
		}
		Set<String> selectedRoutesSet = Model.INSTANCE.getSelectedRouteTags();
		if (selectedRoutesSet == null) {
			return;
		}
		List<String> selectedRoutes = new ArrayList<String>(selectedRoutesSet);
		Collections.sort(selectedRoutes);

		if (myPredictionsPanels != null) {
			for (LeftPredictionsPanel next : myPredictionsPanels) {
				next.destroy();
			}
		}

		myOuterPredictionsContainer.clear();
		myPredictionsPanels = new ArrayList<LeftPredictionsPanel>();

		int routesWithStops = 0;
		for (final String nextRouteTag : selectedRoutes) {
			Set<String> selectedStops = Model.INSTANCE.getSelectedStopTagsForRoute(nextRouteTag);
			if (selectedStops.isEmpty()) {
				continue;
			}
			routesWithStops++;
		}		

		double routePanelHeightPct = (routesWithStops == 0) ? 100 : 100 / routesWithStops;
		int headerLabelHeight = 18;
		int headerLabelHeight2 = 22;

		boolean nextHeaderNeedsDivider = true;
		for (final String nextRouteTag : selectedRoutes) {
			Set<String> selectedStops = Model.INSTANCE.getSelectedStopTagsForRoute(nextRouteTag);
			
			if (selectedStops.isEmpty()) {
				continue;
			}
			
			DockLayoutPanel nextRouteLayoutPanel = new DockLayoutPanel(Unit.PX);
			myOuterPredictionsContainer.addNorth(nextRouteLayoutPanel, routePanelHeightPct);

			final Label routeHeaderLabel = new Label();
			routeHeaderLabel.addStyleName("mapFloatingHeader");
			nextRouteLayoutPanel.addNorth(routeHeaderLabel, headerLabelHeight);
			if (nextHeaderNeedsDivider) {
				routeHeaderLabel.addStyleName("mapFloatingHeaderDivider");
				nextHeaderNeedsDivider = false;
			}

			Model.INSTANCE.getRouteList(new AbstractModelListener<RouteList>() {
				@Override
				public void objectLoaded(RouteList theObject, boolean theRequiredAsyncLoad) {
					Route route = theObject.getRoute(nextRouteTag);
					if (route != null) {
						routeHeaderLabel.setText(route.getTitle());
					} else {
						GWT.log("Didn't find route " + nextRouteTag + " in RouteList from server!");
					}
				}
			});

			double stopsHeightPct = 100 / selectedStops.size();
			DockLayoutPanel stopsLayoutPanel = new DockLayoutPanel(Unit.PCT);
			nextRouteLayoutPanel.add(stopsLayoutPanel);

			for (String nextStop : selectedStops) {

				DockLayoutPanel nextStopLayout = new DockLayoutPanel(Unit.PX);
				stopsLayoutPanel.addNorth(nextStopLayout, stopsHeightPct);

				final HTML stopHeaderLabel = new HTML();
				stopHeaderLabel.addStyleName("mapFloatingHeaderLevel2");
				int height2 = headerLabelHeight2;
				if (nextHeaderNeedsDivider) {
					stopHeaderLabel.addStyleName("mapFloatingHeaderDivider");
					nextHeaderNeedsDivider = false;
					height2 += 2;
				}
				
				nextStopLayout.addNorth(stopHeaderLabel, height2);

				Stop stop = Model.INSTANCE.getSelectedStop(nextStop);
				String directionTag = Model.INSTANCE.getSelectedStopDirectionTag(nextStop);
				StopList stopList = Model.INSTANCE.getStopListForRoute(nextRouteTag).getUiOrNonUiStopListForDirectionTag(directionTag);

				stopHeaderLabel.setHTML(stopList.getShortTitle() + " - " + stop.getTitle());

				LeftPredictionsPanel nextPredictionPanel = new LeftPredictionsPanel(nextStop);
				myPredictionsPanels.add(nextPredictionPanel);
				nextStopLayout.add(nextPredictionPanel);

				nextHeaderNeedsDivider = true;
			}

		}

		// for (final String nextStopTag : selectedStop) {
		//
		// FlowPanel flowHeaderPanel = new FlowPanel();
		// myPredictionsContainer.add(flowHeaderPanel);
		//
		// flowHeaderPanel.setWidth((MapOuterPanelNormal.FLOATING_WIDTH - (4 + MapOuterPanelNormal.MARGINS_WIDTH)) +
		// "px");
		// flowHeaderPanel.addStyleName("mapFloatingHeader");
		//
		// final FavouriteStopGrid floatingPredictionsStopTitlePanel = new FavouriteStopGrid(false);
		// floatingPredictionsStopTitlePanel.setCellPadding(0);
		// floatingPredictionsStopTitlePanel.setCellSpacing(0);
		//
		// FavouriteStop favStop = Model.INSTANCE.getFavouriteStop(nextStopTag);
		// if (favStop != null) {
		//
		// floatingPredictionsStopTitlePanel.updateFavourites(Collections.singletonList(favStop));
		//
		// } else {
		//
		// // If the currently selected stop isn't one of our favourites, create
		// // a fake favourite object to use
		//
		// Model.INSTANCE.addPredictionListListener(nextStopTag, new AbstractModelListener<PredictionsList>() {
		//
		// @Override
		// public void objectLoaded(PredictionsList theObject, boolean theRequiredAsyncLoad) {
		// FavouriteStop stop = new FavouriteStop();
		//
		// String nextRouteTag = Model.INSTANCE.getStopTagsToRouteTags().get(nextStopTag);
		//
		// stop.setRouteTitle(theObject.getRouteName());
		// stop.setDirectionTitle(theObject.getDirectionName());
		// stop.setStopTag(nextStopTag);
		// stop.setTitle(theObject.getStopName());
		// stop.setRouteTag(nextRouteTag);
		//
		// floatingPredictionsStopTitlePanel.updateFavourites(Collections.singletonList(stop));
		// Model.INSTANCE.removePredictionListListener(nextStopTag, this);
		// }
		// });
		// }
		//
		// flowHeaderPanel.add(floatingPredictionsStopTitlePanel);
		//
		// LeftPredictionsPanel predictionsPanel = new LeftPredictionsPanel(nextStopTag);
		// myPredictionsPanels.add(predictionsPanel);
		// myPredictionsContainer.add(predictionsPanel);
		//
		// }

		// resizePredictionsPanel();

	}

	public class MyFavouritesPanelActionHandler implements IFavouriteActionHandler {

		@Override
		public void edit(Favourite theFavourite) {
			myChooseRoutesAndStopsActionPanel.setFavourite(theFavourite);
			LeftSideControlsPanel.this.selectTab(TAB_INDEX_CHOOSE_ROUTES_AND_STOPS);
		}

	}

	public class MyFavPanelHideShowClickHandler implements ClickHandler {

		private boolean myValue;

		public MyFavPanelHideShowClickHandler(boolean theValue) {
			myValue = theValue;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			myHideFavPanel = myValue;
			HistoryUtil.setNormalModeHideFavouritesPanel(myValue);
			updateControls();
		}

		public void setValue(boolean theValue) {
			myValue = theValue;
		}

	}

	public class MyStopSelectButtonClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			LeftSideControlsPanel.this.selectTab(TAB_INDEX_PREDICTIONS);
		}

	}

	@Override
	public void closeNow() {
		myFavouritesPanel.closeNow();
	}

}
