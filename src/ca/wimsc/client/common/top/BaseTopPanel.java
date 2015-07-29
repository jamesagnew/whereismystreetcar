package ca.wimsc.client.common.top;

import ca.wimsc.client.common.map.BaseOuterPanel;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.util.IAsyncListener;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class BaseTopPanel extends DockLayoutPanel implements IAsyncListener {

    public static final int ABOUT_WIDTH = 48;
	public static final int SPINNER_WIDTH = 20;
    public static final int TOP_PANEL_HEIGHT = 40;

    private Image myLoadingLabel;
    private SimplePanel myLoadingLabelContainerBox;
    private Image myReloadLabel;
    private boolean myShowReloadButton = true;
    private BaseOuterPanel myContainerPanel;

    public BaseTopPanel(final BaseOuterPanel theContainerPanel) {
        super(Unit.PX);

        myContainerPanel = theContainerPanel;
        
        addStyleName("topPanel");

        myLoadingLabel = new Image("images/topPanelSpinner.gif");
        myLoadingLabel.addStyleName("onTop");

        myReloadLabel = new Image("images/topPanelReload.png");
//        myReloadLabel.addStyleName("onTop");
        myReloadLabel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent theEvent) {
                MapDataController.INSTANCE.requestReload();
            }
        });

        addLogo();
        
        myLoadingLabelContainerBox = new SimplePanel();
        myLoadingLabelContainerBox.addStyleName("topPanelSpinner");
        myLoadingLabelContainerBox.add(myLoadingLabel);
        addWest(myLoadingLabelContainerBox, SPINNER_WIDTH);

        addAboutLink();

    }

    protected void addLogo() {
        // nothing, can be overridden
    }

    public BaseOuterPanel getContainerPanel() {
        return myContainerPanel;
    }

    protected void addAboutLink() {
		TopMenuButton aboutButton = new TopMenuButton(false, "topMenuLinkOneline");
		addEast(aboutButton, ABOUT_WIDTH);
		aboutButton.setMenuHtml("<a href='whatisthis.html' target='_blank' class='topMenuLink'>About</a>");
		aboutButton.setClosed();
    }

    public Image getLoadingLabel() {
        return myLoadingLabel;
    }

    public SimplePanel getLoadingLabelContainerBox() {
        return myLoadingLabelContainerBox;
    }

    @Override
    public void finishedLoading() {
        getLoadingLabelContainerBox().clear();
        if (myShowReloadButton) {
            getLoadingLabelContainerBox().add(myReloadLabel);
        }
    }

    @Override
    public void startLoading() {
        getLoadingLabelContainerBox().clear();
        getLoadingLabelContainerBox().add(myLoadingLabel);
    }

    public void setShowReloadButton(boolean theShowReloadButton) {
        myShowReloadButton = theShowReloadButton;
    }
}
