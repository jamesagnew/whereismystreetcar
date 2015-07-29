package ca.wimsc.client.mobile;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.IClosable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class MobileChooseToolsPanel extends AbstractDropDownMenu implements IClosable {

    private static final int WIDTH = 150;
    private static final int RIGHT_OFFSET = 10;
    private static final int HEIGHT = 100;
	private MapTopPanelMobile myMapTopPanelMobile;

    public MobileChooseToolsPanel(MapTopPanelMobile theMapTopPanelMobile, BaseMapOuterPanel theBaseMapOuterPanel) {
        super(theBaseMapOuterPanel, false);
        
        myMapTopPanelMobile = theMapTopPanelMobile;

        int left = Window.getClientWidth() - (WIDTH + RIGHT_OFFSET);
        setPopupPosition(left, BaseTopPanel.TOP_PANEL_HEIGHT);

        FlowPanel container = new FlowPanel();
        this.add(container);
        
        Anchor aboutLink = new Anchor("About This Site", "whatisthis.html");
        aboutLink.setTarget("_blank");
        aboutLink.setStyleName("topMenuSubButtonLink");
        aboutLink.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
            	myMapTopPanelMobile.hideChooseToolsPanel();
			}
		});
        container.add(aboutLink);
        
        Label systemMapLink = new Label("Show Nerdy Stats");
        systemMapLink.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent theEvent) {
            	showStats();
            	myMapTopPanelMobile.hideChooseToolsPanel();
            }

        });
        systemMapLink.setStyleName("topMenuSubButtonLink");
        container.add(systemMapLink);
        
        show();
    }

    @Override
    protected void resizeMe() {
        setSize(WIDTH + "px", HEIGHT + "px");
    }

    @Override
    public void closeNow() {
        // nothing yet
    }

    
	public static void showStats() {
		GWT.runAsync(StatsPopupPanel.class, new RunAsyncCallback() {
			
			@Override
			public void onSuccess() {
				new StatsPopupPanel().show();
			}
			
		
			@Override
			public void onFailure(Throwable theReason) {
				Common.handleUnexpectedError(theReason);
			}
		});
	}
    
}
