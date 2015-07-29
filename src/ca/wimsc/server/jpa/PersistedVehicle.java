package ca.wimsc.server.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class PersistedVehicle implements Serializable {

    private static final String MARSHALL_SEP = "`";

    private static final long serialVersionUID = 1L;

    @Column(name = "AT_EXTREMITY")
    private boolean myAtExtremity;

    @Column(name = "LOC0", nullable = true, length = 100)
    private String myLocation0;

    @Column(name = "LOC1", nullable = true, length = 100)
    private String myLocation1;

    @Column(name = "LOC2", nullable = true, length = 100)
    private String myLocation2;

    @Column(name = "LOC3", nullable = true, length = 100)
    private String myLocation3;

    @Column(name = "LOC4", nullable = true, length = 100)
    private String myLocation4;

    @Column(name = "LOC5", nullable = true, length = 100)
    private String myLocation5;

    @Column(name = "LOC6", nullable = true, length = 100)
    private String myLocation6;

    @Column(name = "LOC7", nullable = true, length = 100)
    private String myLocation7;

    @Column(name = "LOC8", nullable = true, length = 100)
    private String myLocation8;

    @Column(name = "LOC9", nullable = true, length = 100)
    private String myLocation9;

    @Column(name = "MR_INCIDENT_ACTIVE")
    private boolean myMostRecentIncidentActive;

    @Column(name = "MR_INCIDENT")
    private Long myMostRecentIncidentId;

    private transient List<PersistedVehiclePosition> myPositions;

    @Id
    @Column(name = "VEHICLE_TAG")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key myVehicleTag;


    public Long getMostRecentIncidentId() {
        return myMostRecentIncidentId;
    }

    public PersistedVehiclePosition getMostRecentPosition() {
    	if (myPositions != null) {
    		if (myPositions.isEmpty()) {
    			return null;
    		} else {
    			return myPositions.get(myPositions.size() - 1);
    		}
    	}
        if (myLocation9 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation9));
        }
        if (myLocation8 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation8));
        }
        if (myLocation7 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation7));
        }
        if (myLocation6 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation6));
        }
        if (myLocation5 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation5));
        }
        if (myLocation4 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation4));
        }
        if (myLocation3 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation3));
        }
        if (myLocation2 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation2));
        }
        if (myLocation1 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation1));
        }
        if (myLocation0 != null) {
            return (PersistedVehiclePosition.unmarshall(myLocation0));
        }
        return null;
    }

    public List<PersistedVehiclePosition> getPositions() {
        if (myPositions != null) {
            return myPositions;
        }

        ArrayList<PersistedVehiclePosition> retVal = new ArrayList<PersistedVehiclePosition>();
        if (myLocation0 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation0));
        }
        if (myLocation1 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation1));
        }
        if (myLocation2 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation2));
        }
        if (myLocation3 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation3));
        }
        if (myLocation4 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation4));
        }
        if (myLocation5 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation5));
        }
        if (myLocation6 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation6));
        }
        if (myLocation7 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation7));
        }
        if (myLocation8 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation8));
        }
        if (myLocation9 != null) {
            retVal.add(PersistedVehiclePosition.unmarshall(myLocation9));
        }
        return retVal;
    }

    public String getVehicleTag() {
        return myVehicleTag.getName();
    }

    public boolean isAtExtremity() {
        return myAtExtremity;
    }

    public boolean isMostRecentIncidentActive() {
        return myMostRecentIncidentActive;
    }

    public void setAtExtremity(boolean theAtExtremity) {
        myAtExtremity = theAtExtremity;
    }


    public void setMostRecentIncidentActive(boolean theMostRecentIncidentActive) {
        myMostRecentIncidentActive = theMostRecentIncidentActive;
    }

    public void setMostRecentIncidentId(Long theMostRecentIncidentId) {
        myMostRecentIncidentId = theMostRecentIncidentId;
    }

    public void setPositions(List<PersistedVehiclePosition> thePositions) {
        assert !thePositions.contains(null);
        List<PersistedVehiclePosition> positionsCopy = new ArrayList<PersistedVehiclePosition>(thePositions);

        while (thePositions.size() < 10) {
            thePositions.add(null);
        }
        if (thePositions.size() > 10) {
            thePositions = thePositions.subList(thePositions.size() - 10, thePositions.size());
        }

        myLocation0 = PersistedVehiclePosition.marshall(thePositions.get(0));
        myLocation1 = PersistedVehiclePosition.marshall(thePositions.get(1));
        myLocation2 = PersistedVehiclePosition.marshall(thePositions.get(2));
        myLocation3 = PersistedVehiclePosition.marshall(thePositions.get(3));
        myLocation4 = PersistedVehiclePosition.marshall(thePositions.get(4));
        myLocation5 = PersistedVehiclePosition.marshall(thePositions.get(5));
        myLocation6 = PersistedVehiclePosition.marshall(thePositions.get(6));
        myLocation7 = PersistedVehiclePosition.marshall(thePositions.get(7));
        myLocation8 = PersistedVehiclePosition.marshall(thePositions.get(8));
        myLocation9 = PersistedVehiclePosition.marshall(thePositions.get(9));

        myPositions = positionsCopy;

    }

    public void setVehicleTag(String theVehicleTag) {
        myVehicleTag = KeyFactory.createKey(PersistedVehicle.class.getSimpleName(), theVehicleTag);
    }

    public static String marshall(PersistedVehicle theVehicle) {
        StringBuilder retVal = new StringBuilder();

        retVal.append(serialVersionUID);
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.getVehicleTag()));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myAtExtremity));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myMostRecentIncidentActive));
        retVal.append(MARSHALL_SEP);
        if (theVehicle.myMostRecentIncidentId != null) {
            retVal.append(formatForMarshalling(Long.toString(theVehicle.myMostRecentIncidentId, Character.MAX_RADIX)));
        }
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation0));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation1));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation2));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation3));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation4));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation5));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation6));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation7));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation8));
        retVal.append(MARSHALL_SEP);
        retVal.append(formatForMarshalling(theVehicle.myLocation9));

        return retVal.toString();
    }

    public static PersistedVehicle unmarshall(String theVehicle) {
        String[] split = theVehicle.split(MARSHALL_SEP);

        PersistedVehicle retVal = new PersistedVehicle();
        retVal.setVehicleTag(split[1]);
        retVal.setAtExtremity(Boolean.parseBoolean(split[2]));
        retVal.setMostRecentIncidentActive(Boolean.parseBoolean(split[3]));

        if (split[4].length() > 0) {
            try {
                retVal.setMostRecentIncidentId(Long.parseLong(split[4], Character.MAX_RADIX));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (split.length > 5) {
            retVal.myLocation0 = split[5];
        }
        if (split.length > 6) {
            retVal.myLocation1 = split[6];
        }
        if (split.length > 7) {
            retVal.myLocation2 = split[7];
        }
        if (split.length > 8) {
            retVal.myLocation3 = split[8];
        }
        if (split.length > 9) {
            retVal.myLocation4 = split[9];
        }
        if (split.length > 10) {
            retVal.myLocation5 = split[10];
        }
        if (split.length > 11) {
            retVal.myLocation6 = split[11];
        }
        if (split.length > 12) {
            retVal.myLocation7 = split[12];
        }
        if (split.length > 13) {
            retVal.myLocation8 = split[13];
        }
        if (split.length > 14) {
            retVal.myLocation9 = split[14];
        }

        return retVal;

    }

    private static Object formatForMarshalling(Object theObject) {
        if (theObject == null) {
            return "";
        }
        String retVal = theObject.toString().replace(MARSHALL_SEP, "");
        return retVal;
    }

}
