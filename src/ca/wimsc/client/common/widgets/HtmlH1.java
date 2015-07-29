package ca.wimsc.client.common.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.user.client.ui.Widget;

/**
 * HTML &lt;h1&gt; tag
 */
public class HtmlH1 extends Widget {

    public HtmlH1(String theText) {
        HeadingElement element = Document.get().createHElement(1);
        setElement(element);
        
        element.setInnerText(theText);
    }
    
}
