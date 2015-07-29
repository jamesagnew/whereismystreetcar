package ca.wimsc.server.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PersistedVehiclePosition implements Serializable, IsSerializable {

	private static final Logger logger = Logger.getLogger(PersistedVehiclePosition.class.getName());
	private static final long serialVersionUID = 2L;
	private static final String TAG_SEPARATOR = ",";

	private boolean myAtExtremity;
	private Long myClosestStopDistanceInMeters;
	private String myClosestStopTag;
	private String myDirectionTag;
	private double myLatitude;
	private double myLongitude;
	private String myRouteTag;
	private Integer mySpeedInKmh;
	private long myTimestamp;


	/**
	 * Constructor
	 */
	public PersistedVehiclePosition() {
		// nothing
	}


	public Long getClosestStopDistanceInMeters() {
		return myClosestStopDistanceInMeters;
	}


	/**
	 * @return the closestStopTag
	 */
	public String getClosestStopTag() {
		return myClosestStopTag;
	}


	public String getDirectionTag() {
		return myDirectionTag;
	}


	public double getLatitude() {
		return myLatitude;
	}


	public double getLongitude() {
		return myLongitude;
	}


	public String getRouteTag() {
		return myRouteTag;
	}


	public Integer getSpeedInKmh() {
		return mySpeedInKmh;
	}


	public long getTimestamp() {
		return myTimestamp;
	}


	public boolean isAtExtremity() {
		return myAtExtremity;
	}


	public void setAtExtremity(boolean theAtExtremity) {
		myAtExtremity = theAtExtremity;
	}


	public void setClosestStopDistanceInMeters(Long theClosestStopDistanceInMeters) {
		myClosestStopDistanceInMeters = theClosestStopDistanceInMeters;
	}


	/**
	 * @param theClosestStopTag
	 *            the closestStopTag to set
	 */
	public void setClosestStopTag(String theClosestStopTag) {
		myClosestStopTag = theClosestStopTag;
	}


	public void setDirectionTag(String theDirectionTag) {
		myDirectionTag = theDirectionTag;
	}


	public void setLatitude(double theLatitude) {
		myLatitude = theLatitude;
	}


	public void setLongitude(double theLongitude) {
		myLongitude = theLongitude;
	}


	public void setRouteTag(String theRouteTag) {
		myRouteTag = theRouteTag;
	}


	public void setSpeedInKmh(Integer theSpeedInKmh) {
		mySpeedInKmh = theSpeedInKmh;
	}


	public void setTimestamp(long theTimestamp) {
		myTimestamp = theTimestamp;
	}


	@Override
	public String toString() {
		return "Position[timestamp=" + new Date(myTimestamp) + ", dir=" + myDirectionTag + ", stop=" + myClosestStopTag + ", speed=" + mySpeedInKmh + "]";
	}


	public static String marshall(PersistedVehiclePosition theVehiclePosition) {
		if (theVehiclePosition == null) {
			return null;
		}

		StringBuilder b = new StringBuilder();

		b.append(serialVersionUID);
		b.append(TAG_SEPARATOR);
		b.append(Long.toString(theVehiclePosition.getTimestamp(), Character.MAX_RADIX));
		b.append(TAG_SEPARATOR);
		b.append(theVehiclePosition.getLatitude());
		b.append(TAG_SEPARATOR);
		b.append(theVehiclePosition.getLongitude());
		b.append(TAG_SEPARATOR);
		b.append(theVehiclePosition.getRouteTag());
		b.append(TAG_SEPARATOR);
		b.append(theVehiclePosition.getDirectionTag());
		b.append(TAG_SEPARATOR);
		if (theVehiclePosition.getSpeedInKmh() != null) {
			b.append(Integer.toString(theVehiclePosition.getSpeedInKmh(), Character.MAX_RADIX));
		}
		b.append(TAG_SEPARATOR);
		b.append(theVehiclePosition.isAtExtremity() ? "Y" : "N");
		b.append(TAG_SEPARATOR);
		b.append(StringUtil.defaultString(theVehiclePosition.getClosestStopTag()));
		b.append(TAG_SEPARATOR);
		if (theVehiclePosition.getClosestStopDistanceInMeters() != null) {
			b.append(Long.toString(theVehiclePosition.getClosestStopDistanceInMeters().longValue(), Character.MAX_RADIX));
		}
		
		return b.toString();
	}


	public static PersistedVehiclePosition unmarshall(String theVehiclePosition) {
		if (theVehiclePosition == null || theVehiclePosition.length() == 0) {
			return null;
		}

		try {
			String[] split = theVehiclePosition.split(TAG_SEPARATOR);
			PersistedVehiclePosition retVal = new PersistedVehiclePosition();
			retVal.setTimestamp(Long.parseLong(split[1], Character.MAX_RADIX));
			retVal.setLatitude(Double.parseDouble(split[2]));
			retVal.setLongitude(Double.parseDouble(split[3]));
			retVal.setRouteTag(split[4]);
			retVal.setDirectionTag(split[5]);
			if (split.length > 6 && split[6] != null && split[6].length() > 0) {
				retVal.setSpeedInKmh(Integer.parseInt(split[6], Character.MAX_RADIX));
			}
			if (split.length > 7 && split[7] != null && split[7].length() > 0) {
				retVal.setAtExtremity(split[7].equals("Y"));
			}
			if (split.length > 8 && split[8] != null && split[8].length() > 0) {
				retVal.setClosestStopTag(split[8]);
			}
			if (split.length > 9 && split[9] != null && split[9].length() > 0) {
				retVal.setClosestStopDistanceInMeters(Long.parseLong(split[9], Character.MAX_RADIX));
			}

			return retVal;
		} catch (NumberFormatException e) {
			logger.info("Failed to parse number: " + e.toString());
			return null;
		}
	}
}
