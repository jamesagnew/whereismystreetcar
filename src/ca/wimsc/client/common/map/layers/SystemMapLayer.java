/**
 * 
 */
package ca.wimsc.client.common.map.layers;

import ca.wimsc.client.common.systemmap.TtcSystemMapOverlay;

import com.google.gwt.maps.client.MapWidget;

/**
 * Displays the "system map", which is the TTC's own route map, overlaid
 * on top of the regular google map
 */
public class SystemMapLayer extends AbstractMapOverlayLayer {

	private TtcSystemMapOverlay myTtcMapOverlay;

	/**
	 * Constructor
	 */
	public SystemMapLayer() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(MapWidget theMap) {
		myTtcMapOverlay = new TtcSystemMapOverlay(theMap.getMap());
		myTtcMapOverlay.setMap(theMap.getMap());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		myTtcMapOverlay.kill();
		
		// FIXME: google maps throws a weird error here.. Possibly a bug in their
		// implementation? try again later and see if it's fixed. For now, we
		// mostly remove traces of the overlay manually
		// myTtcMapOverlay.setMap(null);
//		myTtcMapOverlay.setMap(null);
		
		myTtcMapOverlay = null;
	}

}
