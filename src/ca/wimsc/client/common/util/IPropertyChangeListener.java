package ca.wimsc.client.common.util;

/**
 * GWT replacement for java property change listener
 */
public interface IPropertyChangeListener {

    /**
     * Fired when a property changes
     */
    void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue);
    
}
