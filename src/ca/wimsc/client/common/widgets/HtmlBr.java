package ca.wimsc.client.common.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

public class HtmlBr extends Widget {

    public HtmlBr() {
        setElement(Document.get().createBRElement());
    }
    
}
