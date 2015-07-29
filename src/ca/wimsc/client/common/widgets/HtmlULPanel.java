package ca.wimsc.client.common.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * HTML &lt;UL&gt; tag
 */
public class HtmlULPanel extends ComplexPanel {

    private UListElement myListElement;

    public HtmlULPanel() {
        myListElement = Document.get().createULElement();
        setElement(myListElement);
    }

    @Override
    public void add(Widget child) {

        if (child instanceof HtmlULPanel) {
            myListElement.appendChild(child.getElement());
            super.add(child);
        } else {
            com.google.gwt.user.client.Element li = Document.get().createLIElement().cast();
            myListElement.appendChild(li);
            super.add(child, li);
        }
    }
}
