/**
 * 
 */
package ca.wimsc.client.common.map.layers;


import com.google.gwt.user.client.ui.Button;

import ca.wimsc.client.common.stats.StatsPanel;

/**
 * @author James
 * 
 */
public class RouteStatsLayer extends StatsPanel implements IMapFooterLayer {

	public RouteStatsLayer() {
		super();
		
//		Button allStreetcarsButton = createAllStreetcarsButton();
//		this.insert(allStreetcarsButton, 0);
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFooterHeight() {
		return 150;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getBottomIndex() {
		return 10;
	}

}
