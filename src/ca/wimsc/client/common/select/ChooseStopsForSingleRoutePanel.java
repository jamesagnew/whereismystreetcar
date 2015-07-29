package ca.wimsc.client.common.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.AbstractModelListener;
import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.resources.FavouritesResources;
import ca.wimsc.client.common.util.Constants;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.PropertyChangeSupport;
import ca.wimsc.client.common.util.StringUtil;
import ca.wimsc.client.common.widgets.HoverImage;
import ca.wimsc.client.common.widgets.Html5Label;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Panel containing a route selection combo box and stop selection combos for each direction
 */
class ChooseStopsForSingleRoutePanel extends FlowPanel implements IProvidesRoutesAndStops {

	/**
	 * Value will be {@link SelectionBoxesPanel#this}
	 */
	public static final String ROUTE_REMOVED_PROPERTY = "RR";

	private ListBox myChooseRouteComboBox;
	private FlowPanel myChooseStopsPanel;
	private HoverImage myDeleteButton;
	private SimplePanel myDeleteButtonContainer;
	private Stop myPreSelectStop;
	private PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport();
	private String mySelectedRouteTag;
	private List<ChooseSingleStopPanel> myStopPanels = new ArrayList<ChooseSingleStopPanel>();
	private String myInitialStopTag;
	private ChooseRoutesAndStopsPanel myChooseRoutesAndStopsPanel;

	/**
	 * Constructor
	 */
	public ChooseStopsForSingleRoutePanel(ChooseRoutesAndStopsPanel theChooseRoutesAndStopsPanel, String theInitialRouteTag, String theInitialStopTag) {
		assert StringUtil.isNotBlank(theInitialRouteTag);

		myChooseRoutesAndStopsPanel = theChooseRoutesAndStopsPanel;
		mySelectedRouteTag = theInitialRouteTag;
		myInitialStopTag = theInitialStopTag;

		String uid = DOM.createUniqueId();
		Html5Label leftLabel = new Html5Label("Route: ", uid);
		leftLabel.addStyleName("mapFloatingRouteLabel");
		this.add(leftLabel);

		myChooseRouteComboBox = new ListBox();
		myChooseRouteComboBox.addStyleName("leftPanelRouteBox");
		myChooseRouteComboBox.addChangeHandler(new MyRouteBoxChangeHandler());
		myChooseRouteComboBox.getElement().setPropertyString("id", uid);
		this.add(myChooseRouteComboBox);

		myDeleteButtonContainer = new SimplePanel();
		myDeleteButtonContainer.addStyleName("leftPanelRouteDeleteButtonContainer");
		this.add(myDeleteButtonContainer);

		myDeleteButton = new HoverImage(FavouritesResources.INSTANCE.favDel(), FavouritesResources.INSTANCE.favDelHover());
		myDeleteButton.addClickHandler(new MyDeleteButtonClickHandler());
		myDeleteButtonContainer.add(myDeleteButton);

		myChooseStopsPanel = new FlowPanel();
		this.add(myChooseStopsPanel);

		updateChooseRoute();

			Model.INSTANCE.getStopListForRoute(theInitialRouteTag, new IModelListenerAsync<StopListForRoute>() {

				@Override
				public void startLoadingObject() {
					// nothing
				}

				@Override
				public void objectLoaded(StopListForRoute theObject, boolean theRequiredAsyncLoad) {
					addStopSelectionPanels();
				}
			});
	}

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, IPropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(String theProperty, IPropertyChangeListener theListener) {
		myPropertyChangeSupport.addPropertyChangeListener(theProperty, theListener);
	}

	private void addStopSelectionPanels() {
		myStopPanels.clear();
		myChooseStopsPanel.clear();

		boolean foundAStop = false;

		Set<String> selectedStopTags;
		if (myInitialStopTag != null) {
			selectedStopTags = Collections.singleton(myInitialStopTag);
		} else {
			selectedStopTags = Model.INSTANCE.getSelectedStopTags();
		}

		for (String nextStop : selectedStopTags) {
			String routeTag = Model.INSTANCE.getStopTagsToRouteTags().get(nextStop);
			if (mySelectedRouteTag.equals(routeTag)) {
				ChooseSingleStopPanel stopPanel = new ChooseSingleStopPanel(this, nextStop);
				myStopPanels.add(stopPanel);
				myChooseStopsPanel.add(stopPanel);

				foundAStop = true;
			}
		}

		if (!foundAStop) {
			if (myChooseRoutesAndStopsPanel.getSelectedStopTags().size() == Constants.MAX_STOPS_AT_ONCE) {
				// Should we put a label here?
//				myChooseStopsPanel.add(new Label("Note: No more than two stops may be selected at a time"));
			} else {
				ChooseSingleStopPanel stopPanel = new ChooseSingleStopPanel(this, null);
				myStopPanels.add(stopPanel);
				myChooseStopsPanel.add(stopPanel);
			}
		}

		preSelectStopsClosestTo(myPreSelectStop);

	}

	@Override
	public void closeNow() {
		// nothing
	}

	FlowPanel getChooseStopsPanel() {
		return myChooseStopsPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stop getFirstSelectedStopIfAny() {
		for (ChooseSingleStopPanel next : myStopPanels) {
			Stop selectedStop = next.getSelectedStop();
			if (selectedStop != null) {
				return selectedStop;
			}
		}

		return null;
	}

	PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChangeSupport;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getSelectedRoutesTags() {
		HashSet<String> retVal = new HashSet<String>();
		retVal.add(getSelectedRouteTag());
		return retVal;
	}

	public String getSelectedRouteTag() {
		return mySelectedRouteTag;
	}

	/**
	 * @return Return all selected stop tags
	 */
	@Override
	public Set<String> getSelectedStopTags() {
		Set<String> retVal = new HashSet<String>();
		for (ChooseSingleStopPanel next : myStopPanels) {
			Stop selectedStop = next.getSelectedStop();
			if (selectedStop != null) {
				retVal.add(selectedStop.getStopTag());
			}
		}
		return retVal;
	}

	public void preSelectStopsClosestTo(Stop theStop) {
		if (theStop == null) {
			return;
		}

		myPreSelectStop = theStop;

		for (ChooseSingleStopPanel next : myStopPanels) {
			next.preSelectStopsClosestToStopAndIgnoreEvents(theStop);
		}

	}

	/**
	 * Remove a route selection widget
	 */
	private void removeRoute() {
		myPropertyChangeSupport.firePropertyChange(ROUTE_REMOVED_PROPERTY, this, null);
	}

	private void updateChooseRoute() {
		Model.INSTANCE.getRouteList(new AbstractModelListener<RouteList>() {

			@Override
			public void objectLoaded(RouteList theRouteList, boolean theRequiredAsyncLoad) {

				myChooseRouteComboBox.clear();
				int i = 0;
				for (Route next : theRouteList.getList()) {

					String tag = next.getTag();
					myChooseRouteComboBox.addItem(next.getTitle(), tag);

					if (tag.equals(mySelectedRouteTag)) {
						myChooseRouteComboBox.setSelectedIndex(i);
					}

					i++;
				}

			}
		});
	}

	private class MyDeleteButtonClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			removeRoute();
		}

	}

	public class MyRouteBoxChangeHandler implements ChangeHandler {

		@Override
		public void onChange(ChangeEvent theEvent) {
			final String oldSelectedRouteTag = mySelectedRouteTag;
			mySelectedRouteTag = myChooseRouteComboBox.getValue(myChooseRouteComboBox.getSelectedIndex());
			Model.INSTANCE.getStopListForRoute(mySelectedRouteTag, new IModelListenerAsync<StopListForRoute>() {

				@Override
				public void objectLoaded(StopListForRoute theObject, boolean theRequiredAsyncLoad) {
					addStopSelectionPanels();
					myPropertyChangeSupport.firePropertyChange(ROUTE_SELECTION_PROPERTY, oldSelectedRouteTag, mySelectedRouteTag);
				}

				@Override
				public void startLoadingObject() {
					// nothing
				}
			});
		}

	}

}