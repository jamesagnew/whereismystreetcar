/**
 * 
 */
package ca.wimsc.client.common.map.layers;

import ca.wimsc.client.common.util.IClosable;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Defines a layer which lives at the bottom of the map
 */
public interface IMapFooterLayer extends IsWidget, IClosable {

	/**
	 * How tall does this layer expect to be
	 */
	int getFooterHeight();


	/**
	 * @return Components will be sorted and displayed in the order according to the value they return here
	 */
	int getBottomIndex();

}
