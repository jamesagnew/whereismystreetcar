package ca.wimsc.server.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import ca.wimsc.server.jpa.StopQuadrant.StopKey;

public class StopQuadrantList  implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<List<StopQuadrant>> myStopQuadrants;

    public List<StopKey> findClosestStops(final double theLat, final double theLon, int theNumToFind) {

        TreeSet<StopKey> retVal = new TreeSet<StopKey>(new QuadrantDistanceComparator(theLon, theLat));
        HashSet<String> checkedQuadrants = new HashSet<String>();

        int centerLatIndex = -1;
        int centerLonIndex = -1;

        OUTERLOOP: for (int latIndex = 0; latIndex < myStopQuadrants.size(); latIndex++) {
            List<StopQuadrant> lonRow = myStopQuadrants.get(latIndex);
            if (theLat >= lonRow.get(0).getLatMin()) {
                if (theLat < lonRow.get(0).getLatMax()) {

                    // Found the row, now find the column
                    for (int lonIndex = 0; lonIndex < lonRow.size(); lonIndex++) {

                        StopQuadrant nextQuadrant = lonRow.get(lonIndex);
                        if (theLon >= nextQuadrant.getLonMin()) {
                            if (theLon < nextQuadrant.getLonMax()) {
                                centerLatIndex = latIndex;
                                centerLonIndex = lonIndex;
                                break OUTERLOOP;
                            }
                        }

                    }

                }
            }
        }

        if (centerLatIndex != -1) {
            findClosestStops(retVal, centerLatIndex, centerLonIndex, 0, theNumToFind, checkedQuadrants);
        }

        List<StopKey> arrayList = new ArrayList<StopKey>(retVal);
        if (arrayList.size() > theNumToFind) {
            arrayList =  arrayList.subList(0, theNumToFind);
        }
        return arrayList;
    }

    private void findClosestStops(TreeSet<StopKey> theRetVal, int theCenterLatIndex, int theCenterLonIndex, int theRadiatingSize, int theNumWanted, HashSet<String> theCheckedQuadrants) {

//        System.out.println("Searching with radiating size " + theRadiatingSize + " - Curently have " + theRetVal.size());

        int startLatIndex = theCenterLatIndex - theRadiatingSize;
        if (startLatIndex < 0) {
            startLatIndex = 0;
        }
        for (int latIndex = startLatIndex; (latIndex <= (theCenterLatIndex + theRadiatingSize)) && (latIndex < myStopQuadrants.size()); latIndex++) {

            int startLonIndex = theCenterLonIndex - theRadiatingSize;
            if (startLonIndex < 0) {
                startLonIndex = 0;
            }
            for (int lonIndex = startLonIndex; (lonIndex <= (theCenterLonIndex + theRadiatingSize)) && (lonIndex < myStopQuadrants.get(0).size()); lonIndex++) {

                StopQuadrant list = myStopQuadrants.get(latIndex).get(lonIndex);
                if (theCheckedQuadrants.contains(list.getId())) {
                    continue;
                }
                
                theCheckedQuadrants.add(list.getId());
                theRetVal.addAll(list.getStops());

                // Keep looking outward if we haven't found enough
                if (theRetVal.size() < theNumWanted) {
                    findClosestStops(theRetVal, theCenterLatIndex, theCenterLonIndex, theRadiatingSize + 1, theNumWanted, theCheckedQuadrants);
                }

            }

        }

    }

    public List<List<StopQuadrant>> getStopQuadrants() {
        return myStopQuadrants;
    }

    public void setStopQuadrants(List<List<StopQuadrant>> theStopQuadrants) {
        myStopQuadrants = theStopQuadrants;
    }

    public static final class QuadrantDistanceComparator implements Comparator<StopKey> {
        public static long ourNumCalcs = 0;
        private final double myLat;
        private final double myLon;

        private QuadrantDistanceComparator(double theLon, double theLat) {
            myLon = theLon;
            myLat = theLat;
        }

        @Override
        public int compare(StopKey theO1, StopKey theO2) {
            ourNumCalcs++;

            double delta = theO1.distanceFrom(myLat, myLon) - theO2.distanceFrom(myLat, myLon);
            if (delta < 0) {
                return -1;
            } else if (delta > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
