/**
 * 
 */
package ca.wimsc.client.common.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

/**
 * A &lt;p style="clear: both;"&gt;  
 */
public class HtmlPWithClear extends Widget {

	/**
	 * Constructor
	 */
	public HtmlPWithClear() {
		setElement(Document.get().createPElement());
		getElement().getStyle().setProperty("clear", "both");
	}

}
