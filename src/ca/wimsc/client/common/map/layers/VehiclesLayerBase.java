package ca.wimsc.client.common.map.layers;

import java.util.HashMap;
import java.util.Map;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Prediction;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;
import ca.wimsc.client.normal.vehicles.VehicleMarkerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.impl.MarkerImpl;
import com.google.gwt.user.client.Timer;

public abstract class VehiclesLayerBase extends AbstractMapOverlayLayer {
	protected static final int ZINDEX_NEXT_STREETCAR_BASE = 20;

	protected static final int ZINDEX_STREETCAR = 4;

	private MapWidget myMap;
	private VehicleMarkerFactory myVehicleMarkerFactory;
	private Map<String, Map<String, Marker>> myClassifierToVehicleTagToMarker = new HashMap<String, Map<String, Marker>>();


	/**
	 * Constructor
	 */
	public VehiclesLayerBase() {
		myVehicleMarkerFactory = GWT.create(VehicleMarkerFactory.class);
		myVehicleMarkerFactory.preloadImages();
	}


	/**
	 * Create a streetcar marker
	 * 
	 * @param theMarker
	 *            The existing marker, or null if none exists
	 * @param thePoint
	 * @param theDirectionEnum
	 * @param theVehicleType
	 * @param thePrediction
	 */
	protected Marker createDirectionalMarker(Marker theMarker, LatLng thePoint, DirectionEnum theDirectionEnum, VehicleTypeEnum theVehicleType, Prediction thePrediction) {
		MarkerOptions options = new MarkerOptions();
		MarkerImage markerImage;

		if (theDirectionEnum == null) {
			theDirectionEnum = DirectionEnum.NORTHBOUND;
		}

		markerImage = provideMarkerImage(theDirectionEnum, theVehicleType, thePrediction);

		if (theMarker == null) {
			options.setIcon(markerImage);
			options.setPosition(thePoint);
			options.setVisible(true);
			options.setZIndex(ZINDEX_STREETCAR);

			Marker marker = new Marker(options);
			return marker;
		} else {
			theMarker.setIcon(markerImage);
			theMarker.setPosition(thePoint);
			theMarker.setVisible(true);
			theMarker.setZIndex(ZINDEX_STREETCAR);
			return theMarker;
		}

	}


	@Override
	public void destroy() {
		for (Map<String, Marker> next : myClassifierToVehicleTagToMarker.values()) {
			destroyMarkers(next);
		}
	}


	/**
	 * Destroy a collection of markers
	 */
	protected void destroyMarkers(Map<String, Marker> theVehicleTagsToMarkers) {
		for (Marker next : theVehicleTagsToMarkers.values()) {
			MarkerImpl.impl.setMap(next.getJso(), null);
		}
	}


	/**
	 * Returns a map of vehicles to markers
	 * 
	 * @param theClassifier
	 *            This layer might maintain more than one list of vehicles, so the classifier determines which list this
	 *            is. This might be a route tag or a stop tag for instance
	 */
	protected Map<String, Marker> getVehicleTagToMarker(String theClassifier) {
		Map<String, Marker> retVal = myClassifierToVehicleTagToMarker.get(theClassifier);
		if (retVal == null) {
			retVal = new HashMap<String, Marker>();
			myClassifierToVehicleTagToMarker.put(theClassifier, retVal);
		}
		return retVal;
	}


	@Override
	public void initialize(MapWidget theMap) {
		myMap = theMap;
	}


	private MarkerImage provideMarkerImage(DirectionEnum theDirectionEnum, VehicleTypeEnum theVehicleType, Prediction thePrediction) {
		return myVehicleMarkerFactory.createVehicleMarker(theDirectionEnum, theVehicleType, thePrediction);
	}


	/**
	 * @see #getVehicleTagToMarker(String)
	 */
	protected void setVehicleTagToMarker(String theClassifier, Map<String, Marker> theVehicleTagToMarker) {
		assert theVehicleTagToMarker != null;
		assert theClassifier != null && theClassifier.length() > 0;

		myClassifierToVehicleTagToMarker.put(theClassifier, theVehicleTagToMarker);
	}


	protected abstract class MyAbstractAsyncListener<T> implements IModelListenerAsync<T> {

		private String myRouteTag;


		public MyAbstractAsyncListener(String theRouteTag) {
			myRouteTag = theRouteTag;
		}


		protected DirectionEnum determineDirection(StopList theList, String theHeadingString) {

			DirectionEnum vehDir = theList != null ? theList.getDirectionEnum() : null;
			
			if (theHeadingString != null) {
				try {
					Integer heading = (int)Long.parseLong(theHeadingString);

					/*
					 * The weird logic here is because the headings occasionally seem to be completely backwards
					 */
					if (heading > 315 || heading <= 45) {
						if (vehDir != DirectionEnum.SOUTHBOUND) {
							vehDir = DirectionEnum.NORTHBOUND;
						}
					} else if (heading > 45 && heading <= 135) {
						if (vehDir != DirectionEnum.WESTBOUND) {
							vehDir = DirectionEnum.EASTBOUND;
						}
					} else if (heading > 135 && heading <= 225) {
						if (vehDir != DirectionEnum.NORTHBOUND) {
							vehDir = DirectionEnum.SOUTHBOUND;
						}
					} else {
						if (vehDir != DirectionEnum.EASTBOUND) {
							vehDir = DirectionEnum.WESTBOUND;
						}
					}

				} catch (NumberFormatException e) {
					// ignore this
				}
			}

			if (vehDir == null) {
				vehDir = DirectionEnum.EASTBOUND;
			}

			return vehDir;
		}


		public String getRouteTag() {
			return myRouteTag;
		}


		@Override
		public final void objectLoaded(final T theList, final boolean theRequiredAsyncLoad) {
			resetBounds();
			
			if (myVehicleMarkerFactory.areAllPreloadedImagesReady() == false) {
				new Timer() {
					
					@Override
					public void run() {
						objectLoaded(theList, theRequiredAsyncLoad);
					}
				}.schedule(500);
			} else {
				doObjectLoaded(theList, theRequiredAsyncLoad);
			}
		}


		protected abstract void doObjectLoaded(T theList, boolean theRequiredAsyncLoad);


		protected void addMarker(Marker theMarker) {
			theMarker.setMap(myMap.getMap());
		}


		@Override
		public void startLoadingObject() {
			// nothing
		}

	}

}
