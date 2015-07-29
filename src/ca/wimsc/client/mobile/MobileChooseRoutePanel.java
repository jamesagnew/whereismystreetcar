package ca.wimsc.client.mobile;

import ca.wimsc.client.common.map.BaseOuterPanel;
import ca.wimsc.client.common.map.BottomPanel;
import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.select.ChooseRoutesAndStopActionsPanel;
import ca.wimsc.client.common.select.ChooseRoutesAndStopsPanel;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.util.IClosable;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class MobileChooseRoutePanel extends AbstractDropDownMenu implements IClosable, ResizeHandler {

	private HandlerRegistration myResizeHanglerReg;
	private ChooseRoutesAndStopsPanel myChooseStopsPanel;
	private ChooseRoutesAndStopActionsPanel myActionPanel;
	private Favourite myPreSelectedFavourite;

	/**
	 * Constructor
	 * 
	 * @param thePreSelectedFavourite
	 *            May be null
	 */
	public MobileChooseRoutePanel(BaseOuterPanel theBaseMapOuterPanel, Favourite thePreSelectedFavourite) {
		super(theBaseMapOuterPanel);

		myPreSelectedFavourite = thePreSelectedFavourite;
		
		initUi();

		setPopupPosition(BaseTopPanel.SPINNER_WIDTH, BaseTopPanel.TOP_PANEL_HEIGHT - 4);

		myResizeHanglerReg = Window.addResizeHandler(this);
	}

	ChooseRoutesAndStopActionsPanel getActionPanel() {
		return myActionPanel;
	}

	@Override
	public void closeNow() {
		myResizeHanglerReg.removeHandler();

		if (myChooseStopsPanel != null) {
			myChooseStopsPanel.closeNow();
		}
	}

	@Override
	public void onResize(ResizeEvent theEvent) {
		resizeMe();
	}

	@Override
	protected void resizeMe() {
		int width = Window.getClientWidth() - 60;
		if (width > 500) {
			width = 500;
		}

		int height = Window.getClientHeight() - (BaseTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT + 10);
		setSize(width + "px", height + "px");

		getScrollPanel().setSizePixels(width, height);
		
		if (!isShowing()) {
			show();
		}
		
	}

	private void initUi() {
		getScrollPanel().clear();

		if (myChooseStopsPanel == null) {

			FlowPanel container = new FlowPanel();
			getScrollPanel().add(container);

			myActionPanel = new ChooseRoutesAndStopActionsPanel(myPreSelectedFavourite);
			container.add(myActionPanel);

			myChooseStopsPanel = new ChooseRoutesAndStopsPanel(myActionPanel);
			myChooseStopsPanel.insertActionLink(new FindNearbyStopsActionButton(this));
			myActionPanel.setChooseStopsPanel(myChooseStopsPanel);
			myActionPanel.updateUi();
			myChooseStopsPanel.updateUi();
			container.add(myChooseStopsPanel);


		}

	}

	/**
	 * @param theAlternateStopSelector
	 */
	public void setAlternateStopSelector(Panel theAlternateStopSelector) {
		getScrollPanel().clear();
		getScrollPanel().add(theAlternateStopSelector);
	}

	


}
