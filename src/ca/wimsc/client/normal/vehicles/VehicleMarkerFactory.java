package ca.wimsc.client.normal.vehicles;

import java.util.HashMap;
import java.util.Map;

import ca.wimsc.client.common.model.Prediction;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.resources.VehicleMarkers;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.google.gwt.maps.client.overlay.MarkerImage.Builder;
import com.google.gwt.resources.client.ImageResource;

public class VehicleMarkerFactory {

	private static final ImageResource IMAGES_BUS_ICON = VehicleMarkers.INSTANCE.busIcon();
	private static final ImageResource IMAGES_STREETCAR_ICON = VehicleMarkers.INSTANCE.streetcarIcon();
	private static final ImageResource IMAGES_CLRV_LONG_STREETCAR_25X30_90_PNG = VehicleMarkers.INSTANCE.getLongContainer90();
	private static final ImageResource IMAGES_CLRV_LONG_STREETCAR_25X30_270_PNG = VehicleMarkers.INSTANCE.getLongContainer270();
	private static final ImageResource IMAGES_CLRV_LONG_STREETCAR_25X30_180_PNG = VehicleMarkers.INSTANCE.getLongContainer180();
	private static final ImageResource IMAGES_CLRV_LONG_STREETCAR_25X30_0_PNG = VehicleMarkers.INSTANCE.getLongContainer0();
	private static Map<String, ImageElement> ourImageElements;
	private static boolean ourAllPreloadedImagesReady;


	public void preloadImages() {
		if (ourImageElements != null) {
			return;
		}

		ourImageElements = new HashMap<String, ImageElement>();
		prefetchImage(IMAGES_BUS_ICON.getURL());
		prefetchImage(IMAGES_STREETCAR_ICON.getURL());
		prefetchImage(IMAGES_CLRV_LONG_STREETCAR_25X30_0_PNG.getURL());
		prefetchImage(IMAGES_CLRV_LONG_STREETCAR_25X30_90_PNG.getURL());
		prefetchImage(IMAGES_CLRV_LONG_STREETCAR_25X30_180_PNG.getURL());
		prefetchImage(IMAGES_CLRV_LONG_STREETCAR_25X30_270_PNG.getURL());
	}


	public boolean areAllPreloadedImagesReady() {
		if (ourAllPreloadedImagesReady) {
			return true;
		}

		for (ImageElement next : ourImageElements.values()) {
			if (next.getPropertyBoolean("complete")) {
				ourAllPreloadedImagesReady = true;
				GWT.log("Preloaded images are ready");
				return true;
			}
		}

		return false;
	}


	private static void prefetchImage(String theUrl) {
		ImageElement element = Document.get().createImageElement();
		element.setSrc(theUrl);
		ourImageElements.put(theUrl, element);
	}


	public MarkerImage createVehicleMarker(DirectionEnum theDirection, VehicleTypeEnum theVehicleType, Prediction thePrediction) {

		if (thePrediction == null) {
			return createNormalMarker(theDirection, theVehicleType);
		}

		ImageResource resource;
		int vehicleLeft;
		int vehicleTop;
		int textLeft = 0;
		int textTop = 0;
		int originX;
		int originY;

		ImageResource url;
		switch (theDirection) {
		case NORTHBOUND:
			resource = VehicleMarkers.INSTANCE.getLongContainer0();
			vehicleLeft = 13;
			vehicleTop = 6;
			textTop = 34;
			textLeft = 13;
			originX = 0;
			originY = 20;
			url = IMAGES_CLRV_LONG_STREETCAR_25X30_0_PNG;
			break;
		case SOUTHBOUND:
			resource = VehicleMarkers.INSTANCE.getLongContainer180();
			vehicleLeft = 4;
			vehicleTop = 2;
			textTop = 32;
			textLeft = 4;
			url = IMAGES_CLRV_LONG_STREETCAR_25X30_180_PNG;
			originX = 30;
			originY = 20;
			break;
		case WESTBOUND:
			resource = VehicleMarkers.INSTANCE.getLongContainer270();
			vehicleLeft = 7;
			vehicleTop = 2;
			textTop = 17;
			textLeft = 22;
			url = IMAGES_CLRV_LONG_STREETCAR_25X30_270_PNG;
			originX = 20;
			originY = 30;
			break;
		case EASTBOUND:
		default:
			resource = VehicleMarkers.INSTANCE.getLongContainer90();
			vehicleLeft = 5;
			vehicleTop = 10;
			textTop = 25;
			textLeft = 19;
			url = IMAGES_CLRV_LONG_STREETCAR_25X30_90_PNG;
			originX = 20;
			originY = 0;
			break;
		}

		ImageResource vehicleResource;
		switch (theVehicleType) {
		case STREETCAR:
			vehicleResource = IMAGES_STREETCAR_ICON;
			break;
		case BUS:
		default:
			vehicleResource = IMAGES_BUS_ICON;
			break;
		}

		String prediction = Integer.toString(thePrediction.getSeconds() / 60);

		ImageElement image = ourImageElements.get(url.getURL());
		ImageElement vehicleImage = ourImageElements.get(vehicleResource.getURL());

		try {
			
			String createMarker = createMarker(image, vehicleImage, resource.getWidth(), resource.getHeight(), vehicleResource.getURL(), vehicleLeft, vehicleTop, prediction,
					textLeft, textTop);
			Builder iconBuilder = new MarkerImage.Builder(createMarker);
			iconBuilder.setAnchor(new Point(originX, originY));
			MarkerImage markerImage = iconBuilder.build();
			return markerImage;
			
		} catch (Exception e) {
			
			GWT.log("Failed to create special marker, using default", e);
			return createNormalMarker(theDirection, theVehicleType);
			
		}

	}


	/**
	 * Creates a normal (no prediction associated) marker
	 */
	public static MarkerImage createNormalMarker(DirectionEnum theDirection, VehicleTypeEnum theVehicleType) {

		String url;
		int originX;
		int originY;

		String vehicleType = theVehicleType == VehicleTypeEnum.STREETCAR ? "streetcar" : "bus";
		switch (theDirection) {
		case NORTHBOUND:
			url = "/images/clrv/small-" + vehicleType + "-25x30-0.png";
			originX = 0;
			originY = 12;
			break;
		case SOUTHBOUND:
			url = "/images/clrv/small-" + vehicleType + "-25x30-180.png";
			originX = 30;
			originY = 12;
			break;
		case WESTBOUND:
			url = "/images/clrv/small-" + vehicleType + "-25x30-270.png";
			originX = 12;
			originY = 30;
			break;
		case EASTBOUND:
		default:
			url = "/images/clrv/small-" + vehicleType + "-25x30-90.png";
			originX = 12;
			originY = 0;
			break;
		}

		Builder iconBuilder = new MarkerImage.Builder(url);
		iconBuilder.setAnchor(new Point(originX, originY));
		MarkerImage markerImage = iconBuilder.build();

		return markerImage;
	}


	/**
	 * Use HTML canvas to create a vehicle marker with an icon representing the vehicle type as well as a number
	 * representing the number of minutes until that vehicle arrives.
	 */
	private static native String createMarker(Element theImage, Element theVehicleImage, int theWidth, int theHeight, String theVehicleImageUrl, int theVehicleLeft,
			int theVehicleTop, String thePrediction, int theTextLeft, int theTextTop) /*-{
		var myCanvas=document.createElement("canvas");
		var myCanvasContext=myCanvas.getContext("2d");

		myCanvas.width = theWidth;
		myCanvas.height = theHeight;

		myCanvasContext.drawImage(theImage, 0, 0);
		myCanvasContext.drawImage(theVehicleImage, theVehicleLeft, theVehicleTop);

		myCanvasContext.font = "bold 13px Arial";
		myCanvasContext.fillStyle = "#804040";
		if (thePrediction.length == 1) {
		    theTextLeft += 2;
		}
		myCanvasContext.fillText(thePrediction, theTextLeft, theTextTop);

		var dataUrl = myCanvas.toDataURL("image/png");
		return dataUrl;
	}-*/;

}
