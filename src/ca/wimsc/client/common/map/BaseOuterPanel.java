package ca.wimsc.client.common.map;

import ca.wimsc.client.common.util.IClosable;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public abstract class BaseOuterPanel extends DockLayoutPanel implements IClosable {

    public BaseOuterPanel() {
        super(Unit.PX);
    }


}
