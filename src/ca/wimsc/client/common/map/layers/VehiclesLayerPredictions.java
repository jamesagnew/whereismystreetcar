package ca.wimsc.client.common.map.layers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Prediction;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.Vehicles;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlay.Marker;

/**
 * Layer which shows all predicted vehicles for selected stops
 */
public class VehiclesLayerPredictions extends VehiclesLayerBase {

	private int myMaxNumToAffectBounds;
	private IPropertyChangeListener myPropertyChangeListener;
	private Set<String> mySelectedStopTags;
	private Map<String, MyPredictionListListener> myStopTagToListener = new HashMap<String, MyPredictionListListener>();

	@Override
	public void destroy() {
		super.destroy();

		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myPropertyChangeListener);
		for (Iterator<Entry<String, MyPredictionListListener>> iter = myStopTagToListener.entrySet().iterator(); iter.hasNext();) {
			Entry<String, MyPredictionListListener> nextEntry = iter.next();
			Model.INSTANCE.removePredictionListListener(nextEntry.getKey(), nextEntry.getValue());
			iter.remove();
		}
		MapDataController.INSTANCE.decrementWantPredictionsCount();

	}

	@Override
	public void initialize(MapWidget theMap) {
		super.initialize(theMap);

		myPropertyChangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
				redrawIfSelectionsHaveChanged();
			}
		};

		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_STOP_PROPERTY, myPropertyChangeListener);
		MapDataController.INSTANCE.incrementWantPredictionsCount();

		redrawIfSelectionsHaveChanged();
	}

	private void redrawIfSelectionsHaveChanged() {
		Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();

		if (selectedStopTags == null) {
			return;
		}
		if (mySelectedStopTags != null && mySelectedStopTags.equals(selectedStopTags)) {
			return;
		}

		for (String nextStopTag : selectedStopTags) {
			if (!myStopTagToListener.containsKey(nextStopTag)) {
				String routeTag = Model.INSTANCE.getStopTagsToRouteTags().get(nextStopTag);
				MyPredictionListListener listener = new MyPredictionListListener(routeTag, nextStopTag);
				myStopTagToListener.put(nextStopTag, listener);
				Model.INSTANCE.addPredictionListListener(nextStopTag, listener);
			}
		}

		for (Iterator<Entry<String, MyPredictionListListener>> iter = myStopTagToListener.entrySet().iterator(); iter.hasNext();) {
			Entry<String, MyPredictionListListener> nextEntry = iter.next();
			if (!selectedStopTags.contains(nextEntry.getKey())) {
				Model.INSTANCE.removePredictionListListener(nextEntry.getKey(), nextEntry.getValue());
				nextEntry.getValue().destroy();
				iter.remove();
			}
		}

		mySelectedStopTags = selectedStopTags;
	}

	/**
	 * The bounds will only be affected by the first x predictions
	 */
	public void setMaxNumToAffectBounds(int theMaxNumToAffectBounds) {
		myMaxNumToAffectBounds = theMaxNumToAffectBounds;
	}

	private class MyPredictionListListener extends MyAbstractAsyncListener<PredictionsList> {

		private String myStopTag;

		public MyPredictionListListener(String theRouteTag, String theStopTag) {
			super(theRouteTag);

			myStopTag = theStopTag;
		}

		/**
		 * Clear all of my markers
		 */
		public void destroy() {
			destroyMarkers(getVehicleTagToMarker(myStopTag));
		}

		@Override
		public void doObjectLoaded(PredictionsList theList, boolean theRequiredAsyncLoad) {
			Map<String, Marker> existingVehicleTagsToMarkers = getVehicleTagToMarker(myStopTag);
			Map<String, Marker> newVehicleTagsToMarkers = new HashMap<String, Marker>();

			// Add streetcar location markers
			int predictionIndex = -1;
			for (Prediction next : theList.getPredictions()) {
				predictionIndex++;

				String directionTag = next.getVehicleDirectionTag();
//
//				StopList stopList = directionToStopLists.get(directionTag);
//				if (stopList == null) {
//					GWT.log("Unknown direction: " + directionTag);
//					
//					continue;
//				}
//				
//				Stop closestStop = stopList.findFirstStopWithTag(next.getClosestStopTag());
				

				StreetcarLocation streetcarLocation = Model.INSTANCE.getStreetcarLocation(getRouteTag(), next.getVehicleId());
				if (streetcarLocation == null) {
					continue;
				}
				
				double latitude = streetcarLocation.getLatitude();
				double longitude = streetcarLocation.getLongitude();

				if (myMaxNumToAffectBounds <= 0 || myMaxNumToAffectBounds > predictionIndex) {
					updateBounds(latitude, longitude);
				}

				LatLng point = new LatLng(latitude, longitude);
				
				StopListForRoute stopListForRoute = Model.INSTANCE.getStopListForRoute(getRouteTag());
				StopList stopList = stopListForRoute.getUiOrNonUiStopListForDirectionTag(directionTag);
				DirectionEnum directionEnum = determineDirection(stopList, streetcarLocation.getHeading());

				VehicleTypeEnum vehicleType = Vehicles.getVehicleType(streetcarLocation.getVehicleTag());
				Prediction prediction = next;

				// might be null
				Marker marker = existingVehicleTagsToMarkers.remove(next.getVehicleId());

				marker = createDirectionalMarker(marker, point, directionEnum, vehicleType, prediction);
				if (predictionIndex != -1) {
					marker.setZIndex(ZINDEX_NEXT_STREETCAR_BASE - predictionIndex);
				}

				newVehicleTagsToMarkers.put(next.getVehicleId(), marker);
				addMarker(marker);

			}

			destroyMarkers(existingVehicleTagsToMarkers);
			setVehicleTagToMarker(myStopTag, newVehicleTagsToMarkers);

		}

		@Override
		public void startLoadingObject() {
			// nothing
		}

	}

}
