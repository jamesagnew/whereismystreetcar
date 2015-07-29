package ca.wimsc.client.common.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.wimsc.client.common.model.AbstractModelListener;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.util.Constants;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.widgets.HtmlBr;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel containing controls suitable for selecting a new route / stop
 */
public class ChooseRoutesAndStopsPanel extends FlowPanel implements IClosable {

	private static final String DEFAULT_ROUTE = "501";
	private List<Widget> myActionLink = new ArrayList<Widget>();
	private ChooseRoutesAndStopActionsPanel myActionsPanel;
	private IPropertyChangeListener myModelListener;
	private MyRoutePanelPropertyChangeListener myRoutePanelListener = new MyRoutePanelPropertyChangeListener();
	private final Set<String> mySelectedRouteTags = new TreeSet<String>();
	private final Set<String> mySelectedStopTags = new HashSet<String>();
	private final List<IProvidesRoutesAndStops> mySelectionBoxesPanels = new ArrayList<IProvidesRoutesAndStops>();
	private boolean myUpdateNeeded;
	private int myExtraComponentsAtBottom;


	/**
	 * Constructor
	 */
	public ChooseRoutesAndStopsPanel(ChooseRoutesAndStopActionsPanel theActionsPanel) {

		myActionsPanel = theActionsPanel;
		myModelListener = new IPropertyChangeListener() {

			@Override
			public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {

				if (thePropertyName == Model.SELECTED_ROUTE_PROPERTY) {
					mySelectedRouteTags.clear();
					@SuppressWarnings("unchecked")
					Collection<String> value = (Collection<String>) theNewValue;
					mySelectedRouteTags.addAll(value);
				}

				if (thePropertyName == Model.SELECTED_STOP_PROPERTY) {
					mySelectedStopTags.clear();
					@SuppressWarnings("unchecked")
					Collection<String> value = (Collection<String>) theNewValue;
					mySelectedStopTags.addAll(value);
				}

				updateUiLater();
			}

		};
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myModelListener);
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myModelListener);

		Set<String> selectedRouteTags = Model.INSTANCE.getSelectedRouteTags();
		Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();

		HTML addAnotherRouteButton = new HTML("<img src='images/tag_blue_add-16x16.png'" + ChooseRoutesAndStopActionsPanel.HW16 + "/>&nbsp;Add Another Route");
		addAnotherRouteButton.setStyleName("leftAddAnotherRouteButton");
		addAnotherRouteButton.addClickHandler(new MyAddAnotherRouteClickHandler());
		myActionLink.add(addAnotherRouteButton);

		if (selectedRouteTags != null) {
			mySelectedRouteTags.addAll(selectedRouteTags);
			mySelectedStopTags.addAll(selectedStopTags);

			updateUiLater();
		}
	}


	public void insertActionLink(Widget theActionLink) {
		myActionLink.add(0, theActionLink);
	}


	public void addRoute() {

		Model.INSTANCE.getRouteList(new AbstractModelListener<RouteList>() {
			@Override
			public void objectLoaded(RouteList theObject, boolean theRequiredAsyncLoad) {

				// Find a stop which is already selected, to help with selecting the next one
				Stop firstSelectedStop = null;
				for (IProvidesRoutesAndStops nextRouteBox : mySelectionBoxesPanels) {
					firstSelectedStop = nextRouteBox.getFirstSelectedStopIfAny();
					if (firstSelectedStop != null) {
						break;
					}
				}

				// Look for a route which isn't already selected
				for (Route next : theObject.getList()) {

					boolean foundMatch = false;
					for (IProvidesRoutesAndStops nextPanel : mySelectionBoxesPanels) {
						foundMatch |= nextPanel.getSelectedRoutesTags().contains(next.getTag());
					}

					if (foundMatch == false) {
						ChooseStopsForSingleRoutePanel panel = createSelectionBoxesPanel(next.getTag(), null);
						panel.preSelectStopsClosestTo(firstSelectedStop);
						mySelectionBoxesPanels.add(panel);
						ChooseRoutesAndStopsPanel.this.insert(panel, ChooseRoutesAndStopsPanel.this.getWidgetCount() - (myExtraComponentsAtBottom));

						updateSelectedRoutesAndStops();
						myActionsPanel.enableSelectButton();
						return;
					}

				}

			}
		});

	}


	private void closeChildren() {
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget next = getWidget(i);
			if (next instanceof IClosable) {
				((IClosable) next).closeNow();
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeNow() {
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myModelListener);
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myModelListener);
		closeChildren();
	}


	private ChooseStopsForSingleRoutePanel createSelectionBoxesPanel(String theRouteTag, String theStopTag) {
		ChooseStopsForSingleRoutePanel panel;
		panel = new ChooseStopsForSingleRoutePanel(this, theRouteTag, theStopTag);
		panel.addPropertyChangeListener(ChooseStopsForSingleRoutePanel.ROUTE_REMOVED_PROPERTY, myRoutePanelListener);
		panel.addPropertyChangeListener(ChooseStopsForSingleRoutePanel.STOP_SELECTION_PROPERTY, myRoutePanelListener);
		panel.addPropertyChangeListener(ChooseStopsForSingleRoutePanel.ROUTE_SELECTION_PROPERTY, myRoutePanelListener);
		panel.addStyleName("leftStopPanel");
		return panel;
	}


	public Set<String> getSelectedRouteTags() {
		return mySelectedRouteTags;
	}


	public Set<String> getSelectedStopTags() {
		return mySelectedStopTags;
	}


	public void resetStops() {
		mySelectedRouteTags.clear();
		mySelectedRouteTags.add(DEFAULT_ROUTE);
		mySelectedStopTags.clear();
		myUpdateNeeded = true;
		updateUi();
	}


	private void updateSelectedRoutesAndStops() {
		mySelectedRouteTags.clear();
		mySelectedStopTags.clear();
		for (IProvidesRoutesAndStops next : mySelectionBoxesPanels) {
			mySelectedRouteTags.addAll(next.getSelectedRoutesTags());
			mySelectedStopTags.addAll(next.getSelectedStopTags());
		}
	}


	/**
	 * Remove all components and add new panels for each selected route
	 */
	public void updateUi() {

		if (myUpdateNeeded == false) {
			return;
		}
		myUpdateNeeded = false;

		if (mySelectedRouteTags.isEmpty()) {
			return;
		}

		closeChildren();
		this.clear();

		ArrayList<IProvidesRoutesAndStops> existingSelectionBoxesPanels = new ArrayList<IProvidesRoutesAndStops>(mySelectionBoxesPanels);
		mySelectionBoxesPanels.clear();

		for (String nextRouteTag : mySelectedRouteTags) {

			ChooseStopsForSingleRoutePanel panel = null;
			for (int i = 0; i < existingSelectionBoxesPanels.size(); i++) {
				IProvidesRoutesAndStops nextExisting = existingSelectionBoxesPanels.get(i);
				if (nextExisting instanceof ChooseStopsForSingleRoutePanel) {
					ChooseStopsForSingleRoutePanel nextExistingCasted = (ChooseStopsForSingleRoutePanel) nextExisting;
					if (nextExistingCasted.getSelectedRouteTag().equals(nextRouteTag)) {
						panel = nextExistingCasted;
						existingSelectionBoxesPanels.remove(i);
						break;
					}
				}
			}

			if (panel == null) {
				panel = createSelectionBoxesPanel(nextRouteTag, null);
			}

			mySelectionBoxesPanels.add(panel);

			this.add(panel);

		}

		for (IProvidesRoutesAndStops next : existingSelectionBoxesPanels) {
			if (next instanceof IClosable) {
				((IClosable) next).closeNow();
			}
		}

		addActionLinks();

		myActionsPanel.disableSelectButton();

	}


	private void addActionLinks() {

		int extraComponents = 0;
		for (int i = 0; i < myActionLink.size(); i++) {
			if (i > 0) {
				this.add(new HtmlBr());
				extraComponents++;
			}
			this.add(myActionLink.get(i));
			extraComponents++;
		}

		if (getSelectedStopTags().size() == Constants.MAX_STOPS_AT_ONCE) {
			this.add(new Label("Note: No more than two stops may be selected at a time"));
			extraComponents++;
		}

		myExtraComponentsAtBottom = extraComponents;

	}


	private void removeActionLinks() {
		for (int i = 0; i < myActionLink.size(); i++) {
			this.remove(myActionLink.get(i));
		}
	}


	public void updateUiLater() {
		myUpdateNeeded = true;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				updateUi();
			}
		});
	}


	private final class MyAddAnotherRouteClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent theEvent) {
			addRoute();
		}
	}


	private class MyRoutePanelPropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			if (thePropertyName == ChooseStopsForSingleRoutePanel.ROUTE_REMOVED_PROPERTY) {
				assert theOldValue != null && theNewValue == null;
				mySelectionBoxesPanels.remove(theOldValue);
				ChooseRoutesAndStopsPanel.this.remove((Widget) theOldValue);

				if (mySelectionBoxesPanels.isEmpty()) {
					addRoute();
				}

			}

			updateSelectedRoutesAndStops();

			myActionsPanel.enableSelectButton();
		}

	}


	public boolean hasRoutesWithNoStopsSelected() {
		for (IProvidesRoutesAndStops next : mySelectionBoxesPanels) {
			if (next.getSelectedRoutesTags().isEmpty() == false && next.getSelectedStopTags().isEmpty() == true) {
				return true;
			}
		}
		return false;
	}
}
