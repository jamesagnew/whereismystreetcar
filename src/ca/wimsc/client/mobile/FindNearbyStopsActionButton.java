package ca.wimsc.client.mobile;

import ca.wimsc.client.common.select.ChooseRoutesAndStopActionsPanel;
import ca.wimsc.client.common.select.FindNearbyStopsPanel;
import ca.wimsc.client.common.util.Common;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class FindNearbyStopsActionButton extends HTML {

	private MobileChooseRoutePanel myMobileChooseRoutePanel;

	public FindNearbyStopsActionButton(MobileChooseRoutePanel theMobileChooseRoutePanel) {
		setStyleName("leftAddAnotherRouteButton");
		setHTML("<img src='images/world_16x16.png'"+ChooseRoutesAndStopActionsPanel.HW16+">&nbsp;Find Nearby Stops Using GPS");
		
		myMobileChooseRoutePanel = theMobileChooseRoutePanel;
		
		addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				myMobileChooseRoutePanel.setAlternateStopSelector(new FindNearbyStopsPanel());
				
				Common.trackGoogleAnalyticsEvent("Search", "MobileByGps");
			}
		});
	}
	
}
