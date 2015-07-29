package ca.wimsc.client.common.select;

import java.util.ArrayList;
import java.util.Set;

import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.StringUtil;
import ca.wimsc.client.common.widgets.Html5Label;
import ca.wimsc.client.common.widgets.HtmlBr;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel displaying selection actions for selected stop and route
 */
public class ChooseRoutesAndStopActionsPanel extends FlowPanel {

	private static final String FAVOURITE = "Save as favourite";

	/** TODO: This is hopefully temporary- We should create the images that use this using GWT's normal means */
	public static final String HW16 = " height='16' width='16'";

	private ChooseRoutesAndStopsPanel myChooseStopsPanel;
	private Button myClearButton;
	private Favourite myFavourite;
	private CheckBox myFavouriteCheckbox;
	private FlowPanel myFavouritePanel;
	private TextBox myNameItTextBox;
	private CheckBox myOnlyPredictionsCheckBox;
	private Button mySelectButton;
	private boolean myUpdateUiNeeded;
	private boolean myFavouriteCheckboxInitialValue;
	private HTML myTopLabel;
	private HtmlBr myFavouriteCheckboxBr;


	public ChooseRoutesAndStopActionsPanel() {
		this(null);
	}


	/**
	 * Constructor
	 * 
	 * @param thePreSelectedFavourite
	 *            May be null
	 */
	public ChooseRoutesAndStopActionsPanel(Favourite thePreSelectedFavourite) {
		addStyleName("mapFloatingHeader");

		myFavourite = thePreSelectedFavourite;

		mySelectButton = new Button();
		mySelectButton.setStyleName("unstyledHButton");
		mySelectButton.addStyleName("leftStopSelectButton");
		mySelectButton.addClickHandler(new MyShowItClickHandler());

		myClearButton = new Button("<img src='images/trashcan_delete_16x16.png'" + HW16 + ">&nbsp;Clear");
		myClearButton.setStyleName("unstyledHButton");
		myClearButton.addClickHandler(new MyClearButtonClickHandler());

		requestUpdateUi();

		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, new MySelectedStopPropertyChangeListener());
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_FAVOURITE_PROPERTY, new MySelectedStopPropertyChangeListener());

	}


	public void addSelectButtonClickHandler(ClickHandler theClickHandler) {
		mySelectButton.addClickHandler(theClickHandler);
	}


	public void disableSelectButton() {
		myOnlyPredictionsCheckBox.setValue(HistoryUtil.isShowOnlyPredictions());
	}


	public void enableSelectButton() {
		if (myChooseStopsPanel.hasRoutesWithNoStopsSelected()) {
			myOnlyPredictionsCheckBox.setEnabled(false);
		} else {
			myOnlyPredictionsCheckBox.setEnabled(true);
		}

		updateSelectButtons();
	}


	private String getImageUrlDisplay() {
		// return mySelectButtonEnabled ? "<img src='images/checkmark_circle_16x16.png'>" :
		// "<img src='images/checkmark_circle_bw_16x16.png'>";
		return "<img src='images/checkmark_circle_16x16.png'" + HW16 + ">";
	}


	private String getImageUrlSave() {
		// return mySelectButtonEnabled ? "<img src='images/save_accept_16x16.png'>" :
		// "<img src='images/save_accept_bw_16x16.png'>";
		return "<img src='images/save_accept_16x16.png'" + HW16 + ">";
	}


	public void requestUpdateUi() {
		if (myUpdateUiNeeded) {
			return;
		}

		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				updateUi();
			}
		});
	}


	public void setChooseStopsPanel(ChooseRoutesAndStopsPanel theChooseStopsPanel) {
		myChooseStopsPanel = theChooseStopsPanel;
	}


	public void setFavourite(Favourite theFavourite) {
		myFavourite = theFavourite;
		myFavouriteCheckbox.setValue(myFavourite != null);
		updateUi();
	}


	public void setFavouriteCheckboxInitialValue(boolean theB) {
		// TODO: make this work
		myFavouriteCheckbox.setValue(theB, true);
		myFavouriteCheckboxInitialValue = theB;
	}


	private void setSelectButtonHtml(String theString) {
		String existing = mySelectButton.getHTML().replace("\"", "'");
		if (theString.equals(existing) == false) {
			mySelectButton.setHTML(theString);
		}
	}


	private void showIt() {
		if (myFavourite != null || myFavouriteCheckbox.getValue()) {

			if (StringUtil.isBlank(myNameItTextBox.getValue())) {
				Window.alert("Please enter a name (e.g. Home, Work) to save this to your favourites.");
				myNameItTextBox.setFocus(true);
				myNameItTextBox.getElement().getStyle().setBackgroundColor("FFC0C0");
			}

			if (myFavourite == null) {
				// This is a new favourite
				myFavourite = new Favourite();
				myFavourite.setId(Long.toString(System.currentTimeMillis(), Character.MAX_RADIX));

			}

			myFavourite.setRouteTags(new ArrayList<String>(myChooseStopsPanel.getSelectedRouteTags()));
			myFavourite.setStopTags(new ArrayList<String>(myChooseStopsPanel.getSelectedStopTags()));
			myFavourite.setName(myNameItTextBox.getValue());
			myFavourite.setShowPredictionsOnly(myOnlyPredictionsCheckBox.isEnabled() && myOnlyPredictionsCheckBox.getValue());
			if (StringUtil.isBlank(myFavourite.getName())) {
				myFavourite.setName("New Favourite");
			}

			MapDataController.INSTANCE.navigateToNewFavouriteAndAddIfNeccesary(myFavourite);

		} else {

			Set<String> selectedRouteTags = myChooseStopsPanel.getSelectedRouteTags();
			Set<String> selectedStopTags = myChooseStopsPanel.getSelectedStopTags();
			Boolean showOnlyPredictions = myOnlyPredictionsCheckBox.isEnabled() && myOnlyPredictionsCheckBox.getValue();
			MapDataController.INSTANCE.navigateToNewRouteAndStop(selectedRouteTags, selectedStopTags, showOnlyPredictions);

		}
	}


	private void updateSelectButtons() {
		// mySelectButton.setEnabled(mySelectButtonEnabled);

		if (myFavourite != null) {
			if (myFavourite.getId() == null) {
				setSelectButtonHtml(getImageUrlSave() + "&nbsp;Create new favourite");
			} else {
				setSelectButtonHtml(getImageUrlSave() + "&nbsp;Update favourite");
			}
		} else {
			if (myFavouriteCheckbox.getValue()) {
				setSelectButtonHtml(getImageUrlSave() + "&nbsp;Create new favourite");
			} else {
				setSelectButtonHtml(getImageUrlDisplay() + "&nbsp;Show it");
			}
		}
	}


	/**
	 * Update the UI immediately
	 */
	public void updateUi() {

		if (myTopLabel == null) {
			myTopLabel = new HTML();
			myTopLabel.addStyleName("stopChooserMessage");
			this.add(myTopLabel);

			this.add(mySelectButton);
			this.add(myClearButton);

			this.add(new HtmlBr());

			// Only predictions

			myOnlyPredictionsCheckBox = new CheckBox("Only map nearby vehicles");
			this.add(myOnlyPredictionsCheckBox);

			this.add(new HtmlBr());

			// Favourite Toggle

			myFavouriteCheckbox = new CheckBox(FAVOURITE);
			myFavouriteCheckbox.addStyleName("leftNameItCheckbox");
			myFavouriteCheckbox.addClickHandler(new MyFavouriteCheckBoxClickHandler());
			this.add(myFavouriteCheckbox);
			myFavouriteCheckboxBr = new HtmlBr();
			this.add(myFavouriteCheckboxBr);

			// Favourite Details Panel

			myFavouritePanel = new FlowPanel();
			myFavouritePanel.addStyleName("leftFavouritePanel");
			this.add(myFavouritePanel);

			// Fav Name

			String id = Document.get().createUniqueId();
			Html5Label nameLabel = new Html5Label("Name: ", id);
			myFavouritePanel.add(nameLabel);

			myNameItTextBox = new TextBox();
			myNameItTextBox.getElement().setAttribute("id", id);
			myNameItTextBox.setWidth("100px");
			myNameItTextBox.addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent theEvent) {
					enableSelectButton();
					myFavouriteCheckbox.setValue(true);
				}
			});
			myFavouritePanel.add(myNameItTextBox);

		}

		// Update controls

		if (myFavourite != null) {
			myNameItTextBox.setValue(myFavourite.getName());
			myOnlyPredictionsCheckBox.setValue(myFavourite.isShowPredictionsOnly());
			myFavouriteCheckbox.setVisible(false);
			myFavouriteCheckboxBr.setVisible(false);
			myFavouritePanel.setVisible(true);
		} else {
			myFavouriteCheckbox.setVisible(true);
			myFavouriteCheckboxBr.setVisible(true);
			myFavouritePanel.setVisible(false);
		}

		myFavouriteCheckbox.setValue(myFavouriteCheckboxInitialValue);

		String topMsg;
		if (myFavourite != null) {
			topMsg = "Editing favourite: <b>" + SafeHtmlUtils.htmlEscape(myFavourite.getName()) + "</b>";
		} else {
			topMsg = "Choose new stop";
		}
		myTopLabel.setHTML(topMsg);

		updateSelectButtons();

		myUpdateUiNeeded = false;

	}


	public class MyClearButtonClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			myFavourite = null;
			myChooseStopsPanel.resetStops();
			updateUi();
		}

	}


	private final class MyFavouriteCheckBoxClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent theEvent) {
			Boolean newValue = myFavouriteCheckbox.getValue();
			myFavouritePanel.setVisible(newValue);
			enableSelectButton();

			if (newValue) {
				myFavouriteCheckbox.setText(FAVOURITE + ":");
			} else {
				myFavouriteCheckbox.setText(FAVOURITE);
			}
		}
	}


	public class MySelectedStopPropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			requestUpdateUi();
		}

	}


	private final class MyShowItClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent theEvent) {
			showIt();
		}
	}

}