package ca.wimsc.client.common.map.layers;

import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;

public abstract class AbstractMapOverlayLayer implements IMapOverlayLayer {
	private double myLatMax;
	private double myLatMin;
	private double myLonMax;
	private double myLonMin;
	private boolean myDoesNotAffectBounds;

	/**
	 * Constructor
	 */
	public AbstractMapOverlayLayer() {
		resetBounds();
	}

	/**
	 * Clear the bounds values which will be returned
	 */
	protected final void resetBounds() {
		myLatMin = Double.MAX_VALUE;
		myLonMin = Double.MAX_VALUE;
		myLatMax = -Double.MAX_VALUE;
		myLonMax = -Double.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HasLatLngBounds getBounds() {
		if (myLatMin == Double.MAX_VALUE) {
			return null;
		}

		LatLng sw = new LatLng(myLatMin, myLonMin);
		LatLng ne = new LatLng(myLatMax, myLonMax);
		return new LatLngBounds(sw, ne);
	}

	/**
	 * If true, this layer does not affect the map visible bounds
	 */
	public void setDoesNotAffectBounds(boolean theDoesNotAffectBounds) {
		myDoesNotAffectBounds = theDoesNotAffectBounds;
	}

	/**
	 * Add a new value to the bounds that this layer provides
	 */
	protected void updateBounds(double latitude, double longitude) {
		if (myDoesNotAffectBounds) {
			return;
		}

		if (latitude < myLatMin) {
			myLatMin = latitude;
		}
		if (longitude < myLonMin) {
			myLonMin = longitude;
		}
		if (latitude > myLatMax) {
			myLatMax = latitude;
		}
		if (longitude > myLonMax) {
			myLonMax = longitude;
		}
	}

}
