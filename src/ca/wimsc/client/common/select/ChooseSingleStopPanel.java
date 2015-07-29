package ca.wimsc.client.common.select;

import java.util.ArrayList;
import java.util.List;

import ca.wimsc.client.common.model.AbstractModelListener;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.widgets.Html5Label;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;

class ChooseSingleStopPanel extends FlowPanel {

		private ChooseStopsForSingleRoutePanel myContainer;
		private List<ListBox> myFloatingStopsComboBoxes = new ArrayList<ListBox>();
		private List<List<Stop>> myFloatingStopsComboBoxValues = new ArrayList<List<Stop>>();
		private List<RadioButton> myFloatingStopsDirectionCheckBoxes = new ArrayList<RadioButton>();
		private boolean myIgnoreEvents;
		private String myInitialStopTag;
		private String myRadioButtonName = DOM.createUniqueId();

		public ChooseSingleStopPanel(ChooseStopsForSingleRoutePanel theChooseStopsForSingleRoutePanel, String theStopTag) {
			addStyleName("chooseStopPanelDivider");					
			
			myInitialStopTag = theStopTag;
			myContainer = theChooseStopsForSingleRoutePanel;

			updateChooseStops();
		}

		public Stop getSelectedStop() {
			for (int i = 0; i < myFloatingStopsComboBoxes.size(); i++) {
				RadioButton nextRadioButton = myFloatingStopsDirectionCheckBoxes.get(i);
				if (nextRadioButton.getValue()) {
					ListBox listBox = myFloatingStopsComboBoxes.get(i);
					List<Stop> values = myFloatingStopsComboBoxValues.get(i);
					return values.get(listBox.getSelectedIndex());
				}
			}

			return null;
		}

		private void preSelectStopClosestToStop(Stop newSelectedStop, Integer theDirectionIndexToIgnore) {
			for (int directionIndex = 0; directionIndex < myFloatingStopsComboBoxes.size(); directionIndex++) {
				if (theDirectionIndexToIgnore != null && directionIndex == theDirectionIndexToIgnore) {
					continue;
				}

				int index = -1;
				double distance = Double.MAX_VALUE;
				List<Stop> stopsForCombo = myFloatingStopsComboBoxValues.get(directionIndex);
				for (int i = 0; i < stopsForCombo.size(); i++) {
					double nextDistance = stopsForCombo.get(i).distanceFromInKms(newSelectedStop);
					if (nextDistance < distance) {
						distance = nextDistance;
						index = i;
					}
				}

				if (index != -1) {
					myFloatingStopsComboBoxes.get(directionIndex).setSelectedIndex(index);
				}

			}
		}

		/**
		 * Pre-selects the entries in the combo boxes which are closest to the given stop
		 */
		public void preSelectStopsClosestToStopAndIgnoreEvents(Stop theStop) {
			myIgnoreEvents = true;
			preSelectStopClosestToStop(theStop, null);
			myIgnoreEvents = false;
		}

		private void updateChooseStops() {
			Model.INSTANCE.getStopListForRoute(myContainer.getSelectedRouteTag(), new AbstractModelListener<StopListForRoute>() {

				@Override
				public void objectLoaded(final StopListForRoute theStopListForRoute, boolean theRequiredAsyncLoad) {

					Model.INSTANCE.getRouteList(new AbstractModelListener<RouteList>() {

						@Override
						public void objectLoaded(RouteList theRouteList, boolean theRequiredAsyncLoad) {
							updateChooseStops(theStopListForRoute, theRouteList);
						}
					});

				}

			});
		}

		private void updateChooseStops(StopListForRoute theStopListForRoute, RouteList theRouteList) {
			List<StopList> stopLists = theStopListForRoute.getUiStopLists();

//			myChooseStopsPanel.clear();

			// Iterate through directions and add a stop selector for each
			myFloatingStopsComboBoxes.clear();
			myFloatingStopsComboBoxValues.clear();
			myFloatingStopsDirectionCheckBoxes.clear();

			for (StopList nextStopList : stopLists) {
				int directionIndex = myFloatingStopsComboBoxes.size();

				FlowPanel nextFormContainer = new FlowPanel();
				this.add(nextFormContainer);

				String uid = DOM.createUniqueId();
				Html5Label leftLabel = new Html5Label(nextStopList.getName() + ": ", uid);
				leftLabel.addStyleName("mapFloatingStopsLabel");
				nextFormContainer.add(leftLabel);

				RadioButton directionSelect = new RadioButton(myRadioButtonName);
				directionSelect.addStyleName("mapFloatingStopsSelectRadio");
				directionSelect.setFormValue(nextStopList.getTag());

				directionSelect.addValueChangeHandler(new MyFloatingStopsRadioButtonChangeHandler());

				myFloatingStopsDirectionCheckBoxes.add(directionSelect);
				nextFormContainer.add(directionSelect);

				// Create the listbox and add it to the collection

				ListBox stopsCombo = new ListBox();
				stopsCombo.addStyleName("mapFloatingStopsCombo");
				stopsCombo.getElement().setPropertyString("id", uid);
				stopsCombo.addChangeHandler(new MyFloatingStopsComboChangeHandler(directionIndex));
				stopsCombo.addFocusHandler(new MyFloatingStopsComboChangeHandler(directionIndex));
				nextFormContainer.add(stopsCombo);

				ArrayList<Stop> stopsComboValues = new ArrayList<Stop>();
				myFloatingStopsComboBoxValues.add(stopsComboValues);
				myFloatingStopsComboBoxes.add(stopsCombo);

				// See if any stops are selected in the current direction
				// Note: myInitialStopTag may be null here!
				Stop foundStop = nextStopList.findFirstStopWithTag(myInitialStopTag);

				/*
				 * If this is the direction of the currently selected stop, pre-select the stop in the combobox. If
				 * not, pre-select the closest stop in the opposite direction
				 */
				if (myInitialStopTag == null) {

					// do nothing

				} else if (foundStop != null) {

					directionSelect.setValue(true);

				} else {

					double dist = Double.MAX_VALUE;
					Stop selectedStop = null;
					for (StopList nextEntry : theStopListForRoute.getUiStopLists()) {
						if (nextEntry == nextStopList) {
							continue;
						}

						selectedStop = nextEntry.findFirstStopWithTag(myInitialStopTag);
						if (selectedStop != null) {
							break;
						}
					}

					if (selectedStop != null) {

						/*
						 * If we found a selected stop in the opposite direction, find the closest stop in the
						 * current direction, so that the user conveniently has the stop across the street (or so)
						 * pre-selected if they want it
						 */
						for (Stop nextStop : nextStopList.getStops()) {
							double nextDist = nextStop.distanceFromInKms(selectedStop);
							if (nextDist < dist) {
								dist = nextDist;
								foundStop = nextStop;
							}
						}

					} else {

						/*
						 * If all else fails, just choose a stop in the middle of the route
						 */
						foundStop = nextStopList.getStops().get(nextStopList.getStops().size() / 2);

					}

				}

				int index = 0;
				for (Stop nextStop : nextStopList.getStops()) {
					stopsCombo.addItem(nextStop.getTitle(), nextStop.getStopTag());
					stopsComboValues.add(nextStop);

					if (foundStop != null && foundStop.getStopTag().equals(nextStop.getStopTag())) {
						stopsCombo.setSelectedIndex(index);
					}

					index++;
				}

				// nextFormContainer.add(new HtmlBr());

			}

		}

		private class MyFloatingStopsComboChangeHandler implements ChangeHandler, FocusHandler {

			private int myDirectionIndex;

			public MyFloatingStopsComboChangeHandler(int theDirectionIndex) {
				myDirectionIndex = theDirectionIndex;
			}

			@Override
			public void onChange(ChangeEvent theEvent) {
				if (myIgnoreEvents) {
					return;
				}
				
				/*
				 * If we just chose a new stop in a particular direction, also choose the closest stop in the
				 * opposite direction, just to be nice
				 */

				ListBox sourceComboBox = myFloatingStopsComboBoxes.get(myDirectionIndex);
				Stop newSelectedStop = myFloatingStopsComboBoxValues.get(myDirectionIndex).get(sourceComboBox.getSelectedIndex());
				preSelectStopClosestToStop(newSelectedStop, myDirectionIndex);

				// Set the radiobutton for this direction to selected

				RadioButton directionSelect = myFloatingStopsDirectionCheckBoxes.get(myDirectionIndex);
				directionSelect.setValue(true, false);

				// And finally fire a property change

				myContainer.getPropertyChangeSupport().firePropertyChange(ChooseStopsForSingleRoutePanel.STOP_SELECTION_PROPERTY, null, myContainer.getSelectedStopTags());

			}

			@Override
			public void onFocus(FocusEvent theEvent) {
				RadioButton directionSelect = myFloatingStopsDirectionCheckBoxes.get(myDirectionIndex);
				if (directionSelect.getValue() == false) {
					directionSelect.setValue(true, false);
					myContainer.getPropertyChangeSupport().firePropertyChange(ChooseStopsForSingleRoutePanel.STOP_SELECTION_PROPERTY, null, myContainer.getSelectedStopTags());
				}
			}

		}

		private class MyFloatingStopsRadioButtonChangeHandler implements ValueChangeHandler<Boolean> {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myContainer.getPropertyChangeSupport().firePropertyChange(ChooseStopsForSingleRoutePanel.STOP_SELECTION_PROPERTY, null, myContainer.getSelectedStopTags());
			}

		}

	}