package ca.wimsc.client.common.map;

import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.top.BaseTopPanel;

public abstract class BaseMapTopPanel<T extends BaseMapOuterPanel> extends BaseTopPanel {

    private T myParent;

    @Override
	public T getContainerPanel() {
        return myParent;
    }

    public BaseMapTopPanel(T theMapOuterPanel) {
        super(theMapOuterPanel);

        setHeight(TOP_PANEL_HEIGHT + "px");
        
        myParent = theMapOuterPanel;

        initExtraControls();

        MapDataController.INSTANCE.addAsyncListeners(this);
        if (MapDataController.INSTANCE.isLoadingCompletedAtLeastOnce()) {
            finishedLoading();
        }

    }

    protected abstract int getRouteSelectorBoxSize();

    protected abstract void initExtraControls();

}
