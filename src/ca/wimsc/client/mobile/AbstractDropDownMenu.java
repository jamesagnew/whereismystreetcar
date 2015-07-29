package ca.wimsc.client.mobile;

import ca.wimsc.client.common.map.BaseOuterPanel;
import ca.wimsc.client.common.map.BottomPanel;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.widgets.google.MobileScrollPanel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class AbstractDropDownMenu extends PopupPanel {

    private BaseOuterPanel myContainer;
    private MobileScrollPanel myScrollPanel;
	private FlowPanel myScrollContainerPanel;

    public AbstractDropDownMenu(BaseOuterPanel theBaseMapOuterPanel) {
        this(theBaseMapOuterPanel, true);
    }

    public AbstractDropDownMenu(BaseOuterPanel theBaseMapOuterPanel, boolean theAddScrollPanel) {
        myContainer = theBaseMapOuterPanel;

        addStyleName("topMenuPopup");

        if (theAddScrollPanel) {
            myScrollContainerPanel = new FlowPanel();
            this.add(myScrollContainerPanel);
            
            myScrollPanel = new MobileScrollPanel();
            myScrollContainerPanel.add(myScrollPanel);
        }
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
		        resizeMe();
			}
		});
    }

    protected FlowPanel getScrollContainerPanel() {
		return myScrollContainerPanel;
	}

	public BaseOuterPanel getContainer() {
        return myContainer;
    }

    protected MobileScrollPanel getScrollPanel() {
        return myScrollPanel;
    }

    protected void resizeMe() {
        int width = Window.getClientWidth() - 40;
        if (width > 500) {
            width = 500;
        }
        
        int height = Window.getClientHeight() - (BaseTopPanel.TOP_PANEL_HEIGHT + BottomPanel.BOTTOM_PANEL_HEIGHT + 10);
        
        setSize(width + "px", height + "px");
        
        myScrollPanel.setSizePixels(width, height);
    }

    
}
