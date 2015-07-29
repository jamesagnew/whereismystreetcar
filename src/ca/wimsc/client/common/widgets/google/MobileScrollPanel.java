package ca.wimsc.client.common.widgets.google;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * ScrollPanel for touch devices.
 * </p>
 * <p>
 * Has up and down arrow indicators to make it a bit more obvious on touch devices that the panel can scroll
 * </p>
 */
public class MobileScrollPanel extends DockLayoutPanel {

    private Image myDownArrow;
    private MobileScrollPanelInternal myScrollPanel;
    private Image myUpArrow;

    /**
	 * Constructor
	 */
    public MobileScrollPanel() {
        this(null);
    }

    /**
	 * Constructor
	 */
	public MobileScrollPanel(Widget theContents) {
        super(Unit.PX);

        FlowPanel upArrowPanel = new FlowPanel();
        upArrowPanel.addStyleName("mobileScrollPanelArrow");
        myUpArrow = new Image("images/direction_10x5_up.png");
        myUpArrow.addStyleName("mobileScrollPanelArrowImg");
        myUpArrow.getElement().getStyle().setMarginLeft(10, Unit.PX);
        upArrowPanel.add(myUpArrow);

        super.addNorth(upArrowPanel, 5);

        FlowPanel downArrowPanel = new FlowPanel();
        downArrowPanel.addStyleName("mobileScrollPanelArrow");
        myDownArrow = new Image("images/direction_10x5_down.png");
        myDownArrow.addStyleName("mobileScrollPanelArrowImg");
        myDownArrow.getElement().getStyle().setMarginLeft(10, Unit.PX);
        downArrowPanel.add(myDownArrow);

        super.addSouth(downArrowPanel, 5);

        myScrollPanel = new MobileScrollPanelInternal();
        super.add(myScrollPanel);

        myScrollPanel.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent theEvent) {
                updateArrowVisibility();
            }

        });
		
        if (theContents != null) {
        	add(theContents);
        }
        
	}

	@Override
    public void add(Widget theWidget) {
        myScrollPanel.add(theWidget);
        
        scheduleUpdateArrowVisibility();
    }
    
    @Override
    public void clear() {
        myScrollPanel.clear();
    }

    /**
     * Ensure that we are scrolled to a position that allows the given Y point to be visible
     */
    public void ensureVerticalPointsShowing(int theYtop, int theYbottom) {
        myScrollPanel.ensureVerticalPointsShowing(theYtop, theYbottom);
    }

    @Override
    public void onResize() {
        super.onResize();
        
        scheduleUpdateArrowVisibility();
    }

    @Override
    public boolean remove(IsWidget theChild) {
        return myScrollPanel.remove(theChild);
    }

    private void scheduleUpdateArrowVisibility() {
        new Timer() {
            
            @Override
            public void run() {
                updateArrowVisibility();
            }
        }.schedule(100);

    }

    /**
     * Sets the horizontal scroll position.
     * 
     * @param left
     *            the horizontal scroll position, in pixels
     */
    public void setScrollLeft(int left) {
        myScrollPanel.setScrollLeft(left);
    }

    /**
     * Sets the vertical scroll position.
     * 
     * @param top
     *            the vertical scroll position, in pixels
     */
    public void setScrollTop(int top) {
        myScrollPanel.setScrollTop(top);
    }

    public void setSizePixels(int theWidth, int theHeight) {
        setSize(theWidth + "px", theHeight + "px");
        myScrollPanel.setSize(theWidth + "px", (theHeight - 10) + "px");

        scheduleUpdateArrowVisibility();
    }

    private void updateArrowVisibility() {
        myUpArrow.setVisible(myScrollPanel.hasContentAbove());
        myDownArrow.setVisible(myScrollPanel.hasContentBelow());
    }

}
