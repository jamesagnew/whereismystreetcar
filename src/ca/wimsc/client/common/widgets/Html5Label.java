package ca.wimsc.client.common.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.user.client.ui.Widget;

public class Html5Label extends Widget {

    public Html5Label(String theText, String theHtmlFor) {
        LabelElement labelElement = Document.get().createLabelElement();
        setElement(labelElement);
        
        labelElement.setHtmlFor(theHtmlFor);
        labelElement.setInnerHTML(theText);
    }
    
}
