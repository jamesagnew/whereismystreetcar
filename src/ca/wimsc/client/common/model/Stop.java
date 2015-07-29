package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Stop implements Serializable, IsSerializable {

    public static final String SER_DELIM_ALT = "}";
    public static final String SER_DELIM = "{";
    public static final String SER_DELIM_ALT2 = "`";

    public static final int NUM_SERIALIZED_PARTS = 5;

    private static final long serialVersionUID = 1L;
    private static final double D2R = (Math.PI / 180);

    private double myLatitude;
    private double myLongitude;
    private String myStopTag;
    private String myTitle;

    public Stop() {
        // nothing
    }

    public Stop(Stop theClone) {
        myLatitude = theClone.getLatitude();
        myLongitude= theClone.getLongitude();
        myStopTag = theClone.getStopTag();
        myTitle = theClone.getTitle();
    }

    public double getLatitude() {
        return myLatitude;
    }

    public double getLongitude() {
        return myLongitude;
    }

    public String getStopTag() {
        return myStopTag;
    }

    public double distanceFromInKms(Stop theStop) {
        assert theStop != null;
    	
    	double latitude = theStop.getLatitude();
        double longitude = theStop.getLongitude();

        return distanceFromInKms(latitude, longitude);

    }

    public double distanceFromInKms(double theLatitude, double theLongitude) {

        double longitude2 = getLongitude();
        double latitude2 = getLatitude();
        
        return distanceInKms(theLatitude, theLongitude, longitude2, latitude2);
    }

    /**
     * Calculate the distance in between two lat/lon coords
     * 
     * TODO: reorder params
     */
    public static double distanceInKms(double theLatitude, double theLongitude, double theLongitude2, double theLatitude2) {
        double dlong = (theLongitude - theLongitude2) * D2R;
        double dlat = (theLatitude - theLatitude2) * D2R;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(theLatitude2 * D2R) * Math.cos(theLatitude * D2R) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;

        return d;
    }

    public String getTitle() {
        return myTitle;
    }

    public void setLatitude(double theLatitude) {
        myLatitude = theLatitude;
    }

    public void setLongitude(double theLongitude) {
        myLongitude = theLongitude;
    }

    public void setStopTag(String theStopTag) {
        myStopTag = theStopTag;
    }

    public void setTitle(String theTitle) {
        myTitle = theTitle;
    }

    public LatLng asLatLng() {
        return new LatLng(myLatitude, myLongitude);
    }

    @Override
    public int hashCode() {
        return myStopTag.hashCode();
    }

    @Override
    public boolean equals(Object theObj) {
        Stop o = (Stop) theObj;
        return myStopTag.equals(o.myStopTag);
    }

    public List<String> deserializeFromString(String theString) {
        String[] parts = theString.split("\\" + SER_DELIM);
        this.setLatitude(Double.parseDouble(parts[1]));
        this.setLongitude(Double.parseDouble(parts[2]));
        this.setStopTag(parts[3]);
        this.setTitle(parts[4]);

        if (parts.length > NUM_SERIALIZED_PARTS) {
            return Arrays.asList(parts).subList(NUM_SERIALIZED_PARTS, parts.length);
        } else {
            return null;
        }
    }

    public void serializeToString(StringBuilder theBuilder) {
        theBuilder.append(serialVersionUID);
        theBuilder.append(SER_DELIM);
        theBuilder.append(this.getLatitude());
        theBuilder.append(SER_DELIM);
        theBuilder.append(this.getLongitude());
        theBuilder.append(SER_DELIM);
        theBuilder.append(this.getStopTag());
        theBuilder.append(SER_DELIM);
        theBuilder.append(this.getTitle());
    }

}
