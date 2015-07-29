/**
 * 
 */
package ca.wimsc.client.mobile;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.stats.StatsPanel;
import ca.wimsc.client.common.util.SetUtil;
import ca.wimsc.client.common.widgets.HtmlBr;
import ca.wimsc.client.common.widgets.google.MobileScrollPanel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author James
 * 
 */
public class StatsPopupPanel extends PopupPanel {

	private StatsPanel myStatsPanel;


	public StatsPopupPanel() {
		setGlassEnabled(true);
		
		int clientHeight = Window.getClientHeight();
		int clientWidth = Window.getClientWidth();
		int borderWidth = 10;
		
		DockLayoutPanel outerLayoutPanel = new DockLayoutPanel(Unit.PX);
		setWidget(outerLayoutPanel);
		
		FlowPanel controlsPanel = new FlowPanel();
		outerLayoutPanel.addNorth(controlsPanel, 40);
		
		FlowPanel contentPanel = new FlowPanel();
		outerLayoutPanel.add(new MobileScrollPanel(contentPanel));

		Button closeButton = new Button("Close");
		closeButton.setStyleName("statsCloseButton");
		controlsPanel.add(closeButton);
		closeButton.addClickHandler(new MyCloseButtonClickHandler());

		
//		contentPanel.add(new HtmlBr());

		int lineGraphWidth = clientWidth - ((borderWidth * 2) + 40);
		myStatsPanel = new StatsPanel(lineGraphWidth);
		contentPanel.add(myStatsPanel);
		
//		Button allStreetcarButton = myStatsPanel.createAllStreetcarsButton();
//		controlsPanel.add(allStreetcarButton);
		
		MapDataController.INSTANCE.pauseLoading();
		
		setPopupPosition(borderWidth, borderWidth);
		
		int panelWidth = clientWidth - (borderWidth * 4);
		int panelHeight = clientHeight - (borderWidth * 4);
		setPixelSize(panelWidth, panelHeight);
		
		show();
	}


	/**
	 * @author James
	 * 
	 */
	private final class MyCloseButtonClickHandler implements ClickHandler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(ClickEvent theEvent) {
			MapDataController.INSTANCE.resumeLoading();
			myStatsPanel.closeNow();
			StatsPopupPanel.this.hide();
		}
	}

}
