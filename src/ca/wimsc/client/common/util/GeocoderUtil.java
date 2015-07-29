package ca.wimsc.client.common.util;

import java.util.List;

import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.geocoder.HasAddressComponent;
import com.google.gwt.maps.client.geocoder.HasGeocoderResult;

public class GeocoderUtil {

	private static final double DOUBLE_MULTIPLIER = 100000;


	/**
	 * Create a simple text description of a result
	 */
	public static String toTextDescription(final HasGeocoderResult nextResult) {
		StringBuilder addressDescBuilder = new StringBuilder();
		List<HasAddressComponent> addressComponents = nextResult.getAddressComponents();
		for (HasAddressComponent nextComponent : addressComponents) {
			List<String> types = nextComponent.getTypes();
			if (types.contains("administrative_area_level_3") || types.contains("administrative_area_level_2") || types.contains("administrative_area_level_1")) {
				break;
			}

			if (addressDescBuilder.length() > 0) {
				if (types.contains("route")) {
					addressDescBuilder.append(" ");
				} else {
					addressDescBuilder.append(", ");
				}
			}
			addressDescBuilder.append(nextComponent.getLongName());
		}
		final String addressDesc = addressDescBuilder.toString();
		return addressDesc;
	}


	/**
	 * May return null
	 */
	public static HasLatLngBounds fromLatLngBoundsString(String theString) {

		String[] coords = theString.split(",");
		if (coords.length < 4) {
			return null;
		}
		try {
			long swLatLong = Long.parseLong(coords[0], Character.MAX_RADIX);
			long swLonLong = Long.parseLong(coords[1], Character.MAX_RADIX);
			long neLatLong = Long.parseLong(coords[2], Character.MAX_RADIX);
			long neLonLong = Long.parseLong(coords[3], Character.MAX_RADIX);

			double swLat = swLatLong / DOUBLE_MULTIPLIER;
			double swLon = swLonLong / DOUBLE_MULTIPLIER;
			double neLat = (neLatLong / DOUBLE_MULTIPLIER) + swLat;
			double neLon = (neLonLong / DOUBLE_MULTIPLIER) + swLon;

			LatLng sw = new LatLng(swLat, swLon);
			LatLng ne = new LatLng(neLat, neLon);
			LatLngBounds bounds = new LatLngBounds(sw, ne);
			return bounds;
		} catch (NumberFormatException e) {
			return null;
		}

	}


	public static String toLatLngBoundsString(HasLatLngBounds theBounds) {
		assert theBounds != null;

		StringBuilder b = new StringBuilder();
		appendNum(b, theBounds.getSouthWest().getLatitude());
		b.append(",");
		appendNum(b, theBounds.getSouthWest().getLongitude());
		b.append(",");
		appendNum(b, theBounds.getNorthEast().getLatitude() - theBounds.getSouthWest().getLatitude());
		b.append(",");
		appendNum(b, theBounds.getNorthEast().getLongitude() - theBounds.getSouthWest().getLongitude());

		return b.toString();
	}


	private static void appendNum(StringBuilder theB, double theDouble) {
		double multDouble = theDouble * DOUBLE_MULTIPLIER;
		long multLong = (long) multDouble;
		theB.append(Long.toString(multLong, Character.MAX_RADIX));
	}


	public static int encodeLatOrLon(double theLatOrLon) {
		double multDouble = theLatOrLon * DOUBLE_MULTIPLIER;
		int retVal = (int) (multDouble);
		return retVal;
	}

	/**
	 * Only useful for unit tests! 
	 */
	public static double recodeLatOrLon(double theValue) {
		return decodeLatOrLonDeltaFromString(Integer.toString(encodeLatOrLon(theValue), Character.MAX_RADIX), 0.0);
	}

	public static int encodeLatOrLonDelta(double theLatOrLon, int thePreviousValue) {
		double multDouble = theLatOrLon * DOUBLE_MULTIPLIER;
		int retVal = ((int) (multDouble)) - thePreviousValue;
		return retVal;
	}


	public static double decodeLatOrLonDeltaFromString(String theString, double thePreviousValue) {
		if (theString == null || theString.length() == 0) {
			return thePreviousValue;
		}

		int value = (int)Long.parseLong(theString, Character.MAX_RADIX);
		double valueMult = ((double) value) / DOUBLE_MULTIPLIER;

		return valueMult + thePreviousValue;
	}


	public static int getAngleFromPointAToPointBInDegrees(double theLatA, double theLonA, double theLatB, double theLonB) {
		double angleInRads = Math.atan2(Math.sin(theLonB - theLonA) * Math.cos(theLatB),
				Math.cos(theLatA) * Math.sin(theLatB) - Math.sin(theLatA) * Math.cos(theLatB) * Math.cos(theLonB - theLonA));

		double angleInDegreesDouble = angleInRads * (180 / Math.PI);
		int angleInDegrees = (int) angleInDegreesDouble;
		int retVal = (angleInDegrees + 360) % 360;
		return (int) retVal;
	}

}
