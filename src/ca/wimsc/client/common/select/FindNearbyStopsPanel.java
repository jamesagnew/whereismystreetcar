package ca.wimsc.client.common.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.NearbyStop;
import ca.wimsc.client.common.model.NearbyStopList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.GeocoderUtil;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.PropertyChangeSupport;
import ca.wimsc.client.mobile.NearbyStopSelectionPanel;

import com.google.code.gwt.geolocation.client.Geolocation;
import com.google.code.gwt.geolocation.client.Position;
import com.google.code.gwt.geolocation.client.PositionError;
import com.google.code.gwt.geolocation.client.PositionOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.geocoder.Geocoder;
import com.google.gwt.maps.client.geocoder.GeocoderCallback;
import com.google.gwt.maps.client.geocoder.GeocoderRequest;
import com.google.gwt.maps.client.geocoder.HasAddressComponent;
import com.google.gwt.maps.client.geocoder.HasGeocoderGeometry;
import com.google.gwt.maps.client.geocoder.HasGeocoderResult;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Panel containing UI for searching out nearby stops using geolocation
 */
public class FindNearbyStopsPanel extends FlowPanel implements IProvidesRoutesAndStops, IClosable {
	private String myCurrentGeocoderAddress;
	private Integer myGeolocateWatchId;
	private HTML myGpsLocationLabel;
	private NearbyStopSelectionPanel myNearbyStopSelectionPanel;
	private MyNearbyStopsGeocoderCallback myNearbyStopsGeocoderCallback;
	private String mySelectedRouteTag;
	private NearbyStop mySelectedStop;
	private PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport();

	/**
	 * Constructor
	 */
	public FindNearbyStopsPanel() {
		myGpsLocationLabel = new HTML();
		this.add(myGpsLocationLabel);

		myGpsLocationLabel.setHTML("Locating, please wait...");
		myGpsLocationLabel.addStyleName("geolocationHeaderLabel");

		if (GWT.isScript()) {
			PositionOptions options = PositionOptions.getPositionOptions(true, 30000, 60000);
			myGeolocateWatchId = Geolocation.getGeolocation().watchPosition(new MyPositionCallback(), options);
		} else {
			new MyPositionCallback().onSuccess(43.6403436, -79.41835641);
		}

	}

	private void cancelGeolocationWatch() {
		if (myGeolocateWatchId != null) {
			Geolocation.getGeolocation().clearWatch(myGeolocateWatchId);
			myGeolocateWatchId = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeNow() {
		cancelGeolocationWatch();	
	}

	private void findNearby(double theLatitude, double theLongitude) {

		if (myNearbyStopsGeocoderCallback == null) {
			myNearbyStopsGeocoderCallback = new MyNearbyStopsGeocoderCallback(theLatitude, theLongitude);
		} else {
			if (!myNearbyStopsGeocoderCallback.isWorthTryingAgain(theLatitude, theLongitude)) {
				return;
			}
		}

		if (GWT.isScript()) {
			GeocoderRequest request = new GeocoderRequest();
			request.setLatLng(new LatLng(theLatitude, theLongitude));
			new Geocoder().geocode(request, myNearbyStopsGeocoderCallback);
		} else {
			List<HasGeocoderResult> results = new ArrayList<HasGeocoderResult>();
			results.add(new MyFakeGeocoderResult());

			myNearbyStopsGeocoderCallback.callback(results, "success");
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stop getFirstSelectedStopIfAny() {
		return mySelectedStop;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getSelectedRoutesTags() {
		return Collections.singleton(mySelectedRouteTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getSelectedStopTags() {
		return Collections.singleton(mySelectedStop.getStopTag());
	}

	private void setGpsResults(List<NearbyStop> theResult) {

		if (myNearbyStopSelectionPanel == null) {
			myNearbyStopSelectionPanel = new NearbyStopSelectionPanel();
			this.add(myNearbyStopSelectionPanel);
		}

		myGpsLocationLabel.setHTML("Showing stops near: " + myCurrentGeocoderAddress);
		myNearbyStopSelectionPanel.setResults(theResult);
	}

	private final class MyFakeGeocoderResult implements HasGeocoderResult {
		@Override
		public List<HasAddressComponent> getAddressComponents() {
			ArrayList<HasAddressComponent> list = new ArrayList<HasAddressComponent>();
			list.add(new HasAddressComponent() {

				@Override
				public JavaScriptObject getJso() {
					return null;
				}

				@Override
				public String getLongName() {
					return "King Street";
				}

				@Override
				public String getShortName() {
					return "King Street";
				}

				@Override
				public List<String> getTypes() {
					return Collections.singletonList("street");
				}
			});
			return list;
		}

		@Override
		public HasGeocoderGeometry getGeometry() {
			return new HasGeocoderGeometry() {

				@Override
				public HasLatLngBounds getBounds() {
					return null;
				}

				@Override
				public JavaScriptObject getJso() {
					return null;
				}

				@Override
				public HasLatLng getLocation() {
					return null;
				}

				@Override
				public GeocoderLocationType getLocationType() {
					return null;
				}

				@Override
				public HasLatLngBounds getViewport() {
					return null;
				}
			};
		}

		@Override
		public JavaScriptObject getJso() {
			return null;
		}

		@Override
		public List<String> getTypes() {
			return Collections.singletonList("street");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getFormattedAddress() {
			return null;
		}
	}

	private final class MyNearbyStopsAsyncCallback implements AsyncCallback<NearbyStopList> {

		public MyNearbyStopsAsyncCallback() {
			// nothing
		}

		@Override
		public void onFailure(Throwable theCaught) {
			GWT.log("Could not find nearby stops. Error: " + theCaught.getMessage(), theCaught);
			Window.alert("Could not find nearby stops. Error: " + theCaught.getMessage());
		}

		@Override
		public void onSuccess(NearbyStopList theResult) {
			setGpsResults(theResult.getNearbyStops());
		}
	}

	public class MyNearbyStopsGeocoderCallback extends GeocoderCallback {

		private int myInvokeCount;
		private double myLatitude;
		private double myLongitude;

		public MyNearbyStopsGeocoderCallback(double theLatitude, double theLongitude) {
			myLatitude = theLatitude;
			myLongitude = theLongitude;
		}

		@Override
		public void callback(List<HasGeocoderResult> theResponses, String theStatus) {
			String loadingMsg = "Finding nearby stops...";

			myCurrentGeocoderAddress = "";
			if (theResponses != null && theResponses.size() > 0) {

				HasGeocoderResult firstResponse = theResponses.get(0);
				myCurrentGeocoderAddress = GeocoderUtil.toTextDescription(firstResponse);
				myGpsLocationLabel.setHTML("Found address: " + myCurrentGeocoderAddress + "<br/>" + loadingMsg);

			} else {

				myGpsLocationLabel.setHTML(loadingMsg);

			}

			Common.SC_SVC_GNB.getNearbyStops(myCurrentGeocoderAddress, myLatitude, myLongitude, 10, new MyNearbyStopsAsyncCallback());

			if (myInvokeCount >= 3) {
				cancelGeolocationWatch();
			}
			myInvokeCount++;

		}

		public boolean isWorthTryingAgain(double theLatitude, double theLongitude) {
			if (Stop.distanceInKms(theLatitude, theLongitude, myLongitude, myLatitude) < 0.1) {
				return false;
			}

			myLatitude = theLatitude;
			myLongitude = theLongitude;
			return true;
		}

	}

	private final class MyPositionCallback implements com.google.code.gwt.geolocation.client.PositionCallback {

		@Override
		public void onFailure(PositionError theError) {
			GWT.log("Could not determine your location. Error: " + theError.getMessage());
			Window.alert("Could not determine your location. Error: " + theError.getMessage());
		}

		public void onSuccess(double theLat, double theLon) {
			findNearby(theLat, theLon);
		}

		@Override
		public void onSuccess(Position thePosition) {
			double latitude = thePosition.getCoords().getLatitude();
			double longitude = thePosition.getCoords().getLongitude();

			findNearby(latitude, longitude);
		}

	}

	@Override
	public void addPropertyChangeListener(String theProperty, IPropertyChangeListener theListener) {
		myPropertyChangeSupport.addPropertyChangeListener(theProperty, theListener);
	}

	public void setSelectedStop(String theRouteTag, NearbyStop theNext) {
		Model.INSTANCE.getStopTagsToRouteTags().put(theNext.getStopTag(), theRouteTag);
		
		mySelectedRouteTag = theRouteTag;
		mySelectedStop = theNext;
		myPropertyChangeSupport.firePropertyChange(STOP_SELECTION_PROPERTY, null, null);
	}

}
