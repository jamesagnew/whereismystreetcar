package ca.wimsc.client.common.systemmap;

import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.top.TopMenuButton;
import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

public class SystemMapTopPanel extends BaseTopPanel {

    private TopMenuButton myExitButton;

    public SystemMapTopPanel(SystemMapOuterPanel theContainerPanel) {
        super(theContainerPanel);
        
        myExitButton = new TopMenuButton(false);
        myExitButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent theEvent) {
                HistoryUtil.setHideSystemMap();
                
                // TODO: remove this (See similar call in ChooseFromAddressPanel for an explanation)
                Window.Location.reload();
            }
        });
        addWest(myExitButton, 100);
        
        setShowReloadButton(false);

        MapDataController.INSTANCE.addAsyncListeners(this);
        if (MapDataController.INSTANCE.isLoadingCompletedAtLeastOnce()) {
            finishedLoading();
        }

    }

    @Override
    public void finishedLoading() {
        super.finishedLoading();
        
        myExitButton.setMenuHtml("Exit TTC<br>System Map");
    }

   

    
}
