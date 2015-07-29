package ca.wimsc.server.jpa;

import static ca.wimsc.client.common.model.StreetcarLocation.convertDeg2rad;
import static ca.wimsc.client.common.model.StreetcarLocation.convertRad2deg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StopQuadrant implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private transient String myId;
    private double myLatMax;
    private double myLatMin;
    private double myLonMax;
    private double myLonMin;
    private List<String> myStopKeys;
    
    private transient List<StopKey> myStops;
    
    public StopQuadrant() {
        
    }
    
    public String getId() {
        if (myId == null) {
            myId = "" + myLatMin + myLatMax + myLonMin + myLonMax;
        }
        return myId;
    }

    public double getLatMax() {
        return myLatMax;
    }

    public double getLatMin() {
        return myLatMin;
    }

    public double getLonMax() {
        return myLonMax;
    }

    public double getLonMin() {
        return myLonMin;
    }

    public List<String> getStopKeys() {
        return myStopKeys;
    }

    public List<StopKey> getStops() {
        if (myStops == null) {
            myStops = new ArrayList<StopKey>();
            for (String next : myStopKeys) {
                myStops.add(fromStopKey(next));
            }
        }
        return myStops;
    }

    public void setLatMax(double theLatMax) {
        myLatMax = theLatMax;
    }

    public void setLatMin(double theLatMin) {
        myLatMin = theLatMin;
    }

    public void setLonMax(double theLonMax) {
        myLonMax = theLonMax;
    }

    public void setLonMin(double theLonMin) {
        myLonMin = theLonMin;
    }

    public void setStopKeys(List<String> theStops) {
        myStopKeys = theStops;
    }

    public static StopKey fromStopKey(String theStopKey) {
        String[] parts = theStopKey.split("\\|");
        StopKey retVal = new StopKey();
        retVal.setRouteTag(parts[0]);
        retVal.setStopTag(parts[1]);
        retVal.setLat(Double.parseDouble(parts[2]));
        retVal.setLon(Double.parseDouble(parts[3]));
        return retVal;
    }

    public static String toStopKey(String theRouteTag, String theStopTag, double theLat, double theLon) {
        return theRouteTag + "|" + theStopTag + "|" + theLat + "|" + theLon;
    }

    public static class StopKey {
        private double myLat;
        private double myLon;
        private String myRouteTag;
        private String myStopTag;

        public double distanceFrom(double theLat, double theLon) {
            double theta = myLon - theLon;
            double dist = Math.sin(convertDeg2rad(myLat)) * Math.sin(convertDeg2rad(theLat)) + Math.cos(convertDeg2rad(myLat)) * Math.cos(convertDeg2rad(theLat))
                    * Math.cos(convertDeg2rad(theta));
            dist = Math.acos(dist);
            dist = convertRad2deg(dist);
            return (dist);
        }

        @Override
        public boolean equals(Object theObj) {
            StopKey o = (StopKey) theObj;
            return myStopTag.equals(o.myStopTag) && myRouteTag.equals(o.myRouteTag);
        }

        public double getLat() {
            return myLat;
        }

        public double getLon() {
            return myLon;
        }

        public String getRouteTag() {
            return myRouteTag;
        }

        public String getStopTag() {
            return myStopTag;
        }

        @Override
        public int hashCode() {
            return myStopTag.hashCode();
        }

        public void setLat(double theLat) {
            myLat = theLat;
        }

        public void setLon(double theLon) {
            myLon = theLon;
        }

        public void setRouteTag(String theRouteTag) {
            myRouteTag = theRouteTag;
        }

        public void setStopTag(String theStopTag) {
            myStopTag = theStopTag;
        }

    }

}
