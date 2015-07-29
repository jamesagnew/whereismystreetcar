package ca.wimsc.client.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * GWT replacement for java property change support
 */
public class PropertyChangeSupport {

    private Map<String, List<IPropertyChangeListener>> myListeners = new HashMap<String, List<IPropertyChangeListener>>();

    public void addPropertyChangeListener(String theProperty, IPropertyChangeListener theListener) {
        ensureHaveList(theProperty);

        assert theProperty != null && theProperty.length() > 0;
        assert theListener != null;
        assert !myListeners.get(theProperty).contains(theListener);

        myListeners.get(theProperty).add(theListener);
    }

    public void removePropertyChangeListener(String theProperty, IPropertyChangeListener theListener) {
        ensureHaveList(theProperty);

        assert theProperty != null && theProperty.length() > 0;
        assert theListener != null && myListeners.get(theProperty).contains(theListener);
        
        myListeners.get(theProperty).remove(theListener);
    }
    
    public void firePropertyChange(String theProperty, Object theOldValue, Object theNewValue) {
        assert theProperty != null && theProperty.length() > 0;

        ensureHaveList(theProperty);
        
        List<IPropertyChangeListener> listeners = myListeners.get(theProperty);
        GWT.log("Firing property change for property " + theProperty + " to " + listeners.size() + " listeners: " + listeners);
        
		for (IPropertyChangeListener next : listeners) {
            try {
                next.propertyChanged(theProperty, theOldValue, theNewValue);
            } catch (Throwable t) {
                GWT.log("Failure during property notification", t);
                Common.report(Common.CLIENT_LOGGING_HANDLER, t);
            }
        }
    }
    
    private void ensureHaveList(String theProperty) {
        if (myListeners.containsKey(theProperty) == false) {
            myListeners.put(theProperty, new ArrayList<IPropertyChangeListener>());
        }
    }
}
