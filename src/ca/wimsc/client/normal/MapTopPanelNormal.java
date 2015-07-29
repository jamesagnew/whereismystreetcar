package ca.wimsc.client.normal;

import ca.wimsc.client.common.map.BaseMapTopPanel;
import ca.wimsc.client.common.top.TopMenuButton;
import ca.wimsc.client.common.util.Common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Top "red bar" panel for the normal view
 */
public class MapTopPanelNormal extends BaseMapTopPanel<MapOuterPanelNormal> {
	public static final int LINK_HERE_WIDTH = 60;

	private Button myByAddressButton;
	private TextBox myByAddressTextBox;
	private TopMenuButton myLinkHereButton;
	private LinkHerePopup myLinkHerePopup;

	/**
	 * Constructor
	 */
	public MapTopPanelNormal(MapOuterPanelNormal theMapOuterPanel) {
		super(theMapOuterPanel);

	}

	@Override
	protected int getRouteSelectorBoxSize() {
		return 200;
	}

	@Override
	protected void initExtraControls() {

		// Logo

		Image logoImage = new Image("images/top_logo.png", 0, 0, 150, 40);
		logoImage.addStyleName("onTop");
		addWest(logoImage, 150);

		// Link here

		myLinkHereButton = new TopMenuButton();
		addEast(myLinkHereButton, LINK_HERE_WIDTH);
		myLinkHereButton.setMenuHtml("Link<br>Here");
		myLinkHereButton.addClickHandler(new MyLinkHereClickHandler());
		myLinkHereButton.setClosed();

		// Search TextBox and Button

		FlowPanel searchPanel = new FlowPanel();
		addEast(searchPanel, 250);

		myByAddressTextBox = new TextBox();
		searchPanel.add(myByAddressTextBox);
		myByAddressTextBox.setStyleName("topSearchTextBox");
		myByAddressTextBox.addValueChangeHandler(new MyByAddressTextBoxValueChangeHandler());
		myByAddressTextBox.addKeyPressHandler(new MyByAddressTextBoxValueChangeHandler());

		myByAddressButton = new Button("Search");
		searchPanel.add(myByAddressButton);
		myByAddressButton.setStyleName("topSearchButton");
		myByAddressButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent theEvent) {
				searchForStopsByAddress();
			}
		});

	}

	private void searchForStopsByAddress() {
		String newValue = myByAddressTextBox.getValue();
		if (newValue == null || newValue.length() == 0) {
			getContainerPanel().showMap();
			return;
		}

		GWT.log("By address value: " + newValue);

		getContainerPanel().showChooseFromAddress(newValue);
		
		Common.trackGoogleAnalyticsEvent("Search", "NormalModeWithQueryTerms");
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startLoading() {
		super.startLoading();

		getLoadingLabelContainerBox().clear();
		getLoadingLabelContainerBox().add(getLoadingLabel());
	}

	public class MyByAddressTextBoxValueChangeHandler implements ValueChangeHandler<String>, KeyPressHandler {

		private Timer myTimer;

		private void change() {
			if (myTimer != null) {
				myTimer.cancel();
			}
			myTimer = new Timer() {

				@Override
				public void run() {
					searchForStopsByAddress();
				}
			};
			myTimer.schedule(2000);

		}

		@Override
		public void onKeyPress(KeyPressEvent theEvent) {
			change();
		}

		@Override
		public void onValueChange(ValueChangeEvent<String> theEvent) {
			change();
		}

	}

	public class MyLinkHereClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			if (myLinkHerePopup != null) {
				myLinkHerePopup.hide();
				myLinkHerePopup = null;
				myLinkHereButton.setClosed();
			} else {
				myLinkHerePopup = new LinkHerePopup(getContainerPanel());
				myLinkHereButton.setOpened();
			}
		}

	}

}
