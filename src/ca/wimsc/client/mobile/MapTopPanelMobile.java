package ca.wimsc.client.mobile;

import ca.wimsc.client.common.map.BaseMapTopPanel;
import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.OverlayMode;
import ca.wimsc.client.common.select.ChooseRoutesAndStopActionsPanel;
import ca.wimsc.client.common.top.TopMenuButton;
import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

/**
 * Top "red bar" panel for the mobile view
 */
public class MapTopPanelMobile extends BaseMapTopPanel<MapOuterPanelMobile> {

	private TopMenuButton myFavMenu;
	private TopMenuButtonChooseRoute myRouteSelectorMenu;
	private MobileChooseFavPanel myShowingMobileChooseFavPanel;
	private MobileChooseRoutePanel myShowingMobileChooseRoutePanel;
	private MobileChooseToolsPanel myShowingMobileChooseToolsPanel;
	private TopMenuButton myToolsMenu;
	private MyChooseToolsMenuClickHandler myChooseToolsMenuClickHandler;

	/**
	 * Constructor
	 */
	public MapTopPanelMobile(MapOuterPanelMobile theMapOuterPanel) {
		super(theMapOuterPanel);

		History.addValueChangeHandler(new MyHistoryValueChangeHandler());

	}

	@Override
	protected void addAboutLink() {
		// do nothing
	}

	@Override
	public void finishedLoading() {
		super.finishedLoading();

		// Route selector
		boolean chooseRouteOpen = myShowingMobileChooseRoutePanel != null;
		myRouteSelectorMenu.updateText(chooseRouteOpen);

		// Favourites
		myFavMenu.setMenuHtml("Fav");

		// Tools
		myToolsMenu.setMenuHtml("Tools");

	}

	@Override
	protected int getRouteSelectorBoxSize() {
		return 80;
	}

	public void hideChooseFavPanel() {
		if (myShowingMobileChooseFavPanel != null) {
			myShowingMobileChooseFavPanel.hide();
			myShowingMobileChooseFavPanel.closeNow();
			myShowingMobileChooseFavPanel = null;
			myFavMenu.setClosed();
		}
	}

	public void hideChooseRoutePanel() {
		if (myShowingMobileChooseRoutePanel != null) {
			myShowingMobileChooseRoutePanel.hide();
			myShowingMobileChooseRoutePanel.closeNow();
			myShowingMobileChooseRoutePanel = null;
			myRouteSelectorMenu.setClosed();
			myRouteSelectorMenu.updateText(myShowingMobileChooseRoutePanel != null);
		}
	}

	public void hideChooseToolsPanel() {
		if (myShowingMobileChooseToolsPanel != null) {
			myShowingMobileChooseToolsPanel.hide();
			myShowingMobileChooseToolsPanel.closeNow();
			myShowingMobileChooseToolsPanel = null;
			myToolsMenu.setClosed();
		}
	}

	@Override
	protected void initExtraControls() {
		myFavMenu = new TopMenuButton("images/top_heart_14x11.png", "topMenuLinkOneline");
		addWest(myFavMenu, 55);
		myFavMenu.addClickHandler(new MyFavMenuClickHandler());

		myToolsMenu = new TopMenuButton(true, "topMenuLinkOneline");
		addEast(myToolsMenu, 60);
		myChooseToolsMenuClickHandler = new MyChooseToolsMenuClickHandler();
		myToolsMenu.addClickHandler(myChooseToolsMenuClickHandler);
		
		myRouteSelectorMenu = new TopMenuButtonChooseRoute();
		add(myRouteSelectorMenu);
		myRouteSelectorMenu.addClickHandler(new MyChooseRouteMenuClickHandler());
		
		// Show stats popup on startup if the URL calls for it
		if (HistoryUtil.getOverlayMode().contains(OverlayMode.STATS)) {
			MobileChooseToolsPanel.showStats();
		}
		
	}

	public void showChooseRoutePanel(boolean theInitializeWithFavouritesCheckboxChecked) {
		showChooseRoutePanel(null);
		
		if (theInitializeWithFavouritesCheckboxChecked) {
			ChooseRoutesAndStopActionsPanel actionPanel = myShowingMobileChooseRoutePanel.getActionPanel();
			actionPanel.setFavouriteCheckboxInitialValue(true);
		}
	}

	/**
	 * 
	 * @param theFavourite The pre-selected favourite to edit
	 */
	public void showChooseRoutePanel(Favourite theFavourite) {
		if (myShowingMobileChooseRoutePanel == null) {
			myShowingMobileChooseRoutePanel = new MobileChooseRoutePanel(getContainerPanel(), theFavourite);
			myRouteSelectorMenu.setOpened();
			myRouteSelectorMenu.updateText(myShowingMobileChooseRoutePanel != null);
		} else {
			hideChooseRoutePanel();
		}
		hideChooseFavPanel();
		hideChooseToolsPanel();
	}


	private final class MyChooseRouteMenuClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent theEvent) {
			showChooseRoutePanel(false);
		}
	}

	private final class MyChooseToolsMenuClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent theEvent) {
			if (myShowingMobileChooseToolsPanel == null) {
				myShowingMobileChooseToolsPanel = new MobileChooseToolsPanel(MapTopPanelMobile.this, getContainerPanel());
				myToolsMenu.setOpened();
			} else {
				hideChooseToolsPanel();
			}
			hideChooseRoutePanel();
			hideChooseFavPanel();
//			hideChooseStopPanel();
		}

	}

	private final class MyFavMenuClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			if (myShowingMobileChooseFavPanel == null) {
				myShowingMobileChooseFavPanel = new MobileChooseFavPanel(MapTopPanelMobile.this, getContainerPanel());
				myFavMenu.setOpened();
			} else {
				hideChooseFavPanel();
			}
			hideChooseRoutePanel();
			hideChooseToolsPanel();
		}

	}

	private final class MyHistoryValueChangeHandler implements ValueChangeHandler<String> {
		@Override
		public void onValueChange(ValueChangeEvent<String> theEvent) {
			hideChooseFavPanel();
			hideChooseRoutePanel();
			hideChooseToolsPanel();
		}
	}
}
