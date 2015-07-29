package ca.wimsc.client.common.select;

import java.util.Set;

import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.PropertyChangeSupport;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Base class for an individual route and stop provider
 */
public interface IProvidesRoutesAndStops extends IsWidget, IClosable {

	/**
	 * Value will be new route tag
	 */
	public static final String ROUTE_SELECTION_PROPERTY = "RS";

	/**
	 * Value will be new set of selected stops
	 */
	public static final String STOP_SELECTION_PROPERTY = "SS";

	/**
	 * @return Returns the selected route tags
	 */
	public Set<String> getSelectedRoutesTags();

	/**
	 * @return Return all selected stop tags
	 */
	public Set<String> getSelectedStopTags();

	/**
	 * Returns the first selected stop this panel has, or null
	 */
	public Stop getFirstSelectedStopIfAny();
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, IPropertyChangeListener)
	 */
	void addPropertyChangeListener(String theProperty, IPropertyChangeListener theListener);

	
}
