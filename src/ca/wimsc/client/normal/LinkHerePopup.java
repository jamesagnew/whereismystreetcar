package ca.wimsc.client.normal;

import java.util.Set;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.widgets.HtmlBr;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel for creating a shareable link
 */
public class LinkHerePopup extends PopupPanel {

	private CheckBox myIncludeBoundsCheckbox;
	private BaseMapOuterPanel myOuterPanel;
	private TextBox myUrlTextBox;

	/**
	 * Constructor
	 */
	public LinkHerePopup(BaseMapOuterPanel theOuterPanel) {
		addStyleName("topMenuPopup");

		myOuterPanel = theOuterPanel;

		FlowPanel contentPanel = new FlowPanel();
		this.add(contentPanel);

		contentPanel.add(new Label("Create a link which may be shared with others"));

		myIncludeBoundsCheckbox = new CheckBox("Preserve current zoom area");
		myIncludeBoundsCheckbox.setValue(true);
		myIncludeBoundsCheckbox.addValueChangeHandler(new MyValueChangeHandler());
		contentPanel.add(myIncludeBoundsCheckbox);
		contentPanel.add(new HtmlBr());
		
		myUrlTextBox = new TextBox();
		myUrlTextBox.setWidth("200px");
		myUrlTextBox.addFocusHandler(new MyUrlBoxFocusHandler());
		myUrlTextBox.setReadOnly(true);
		contentPanel.add(myUrlTextBox);

		update();

		setPopupPositionAndShow(new PositionCallback() {

			@Override
			public void setPosition(int theOffsetWidth, int theOffsetHeight) {
				int left = Window.getClientWidth() - (theOffsetWidth + MapTopPanelNormal.ABOUT_WIDTH);
				int top = BaseTopPanel.TOP_PANEL_HEIGHT;
				setPopupPosition(left, top);
			}
		});
	}

	private void update() {
		HasLatLngBounds bounds = null;
		if (myIncludeBoundsCheckbox.getValue()) {
			bounds = myOuterPanel.getCurrentMapBounds();
		}

		Set<String> routes = Model.INSTANCE.getSelectedRouteTags();
		Set<String> stops = Model.INSTANCE.getSelectedStopTags();
		String token = HistoryUtil.createLinkHere(routes, stops, bounds);
		
		myUrlTextBox.setValue(token);

	}

	public class MyUrlBoxFocusHandler implements FocusHandler {

		@Override
		public void onFocus(FocusEvent theEvent) {
			update();
			
			Common.trackGoogleAnalyticsEvent("LinkHere", "LinkHereFocus");
			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
					update();
					myUrlTextBox.setSelectionRange(0, myUrlTextBox.getValue().length());
				}
			});
		}

	}

	public class MyValueChangeHandler implements ValueChangeHandler<Boolean> {

		@Override
		public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
			update();
		}

	}

}
