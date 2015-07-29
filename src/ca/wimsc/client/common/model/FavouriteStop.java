package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FavouriteStop extends NearbyStop implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;
    public static final int NUM_SERIALIZED_PARTS = 4;

    private Date myLastAccess;
    private String myAssignedName;
    private boolean myPinned;

    public FavouriteStop() {
        // nothing
    }

    public FavouriteStop(Stop theClone) {
        super(theClone);
    }

    public String toTitle() {
        StringBuilder label = new StringBuilder();
        if (getAssignedName() != null && getAssignedName().trim().length() > 0) {
            label.append(getAssignedName());
        } else {
            label.append(getRouteTag()).append(" ");
            label.append(getDirectionTitle().charAt(0)).append(" ");
            label.append(getTitle());
        }
        String labelString = label.toString();
        return labelString;
    }
    
    /**
     * Should the stop be removed from the list if it isn't used for a while
     */
    public boolean isPinned() {
        return myPinned;
    }

    /**
     * Should the stop be removed from the list if it isn't used for a while
     */
    public void setPinned(boolean thePinned) {
        myPinned = thePinned;
    }

    @Override
    public List<String> deserializeFromString(String theString) {
        List<String> parts = super.deserializeFromString(theString);

        try {
            myLastAccess = new Date(Long.parseLong(parts.get(1)));
        } catch (Exception e) {
            myLastAccess = new Date(0L);
        }

        myAssignedName = parts.get(2);

        myPinned = Boolean.parseBoolean(parts.get(3));

        if (parts.size() > NUM_SERIALIZED_PARTS) {
            return parts.subList(NUM_SERIALIZED_PARTS, parts.size());
        } else {
            return null;
        }

    }

    @Override
    public void serializeToString(StringBuilder theBuilder) {
        super.serializeToString(theBuilder);

        theBuilder.append(SER_DELIM);
        theBuilder.append(serialVersionUID);
        theBuilder.append(SER_DELIM);
        theBuilder.append(myLastAccess.getTime());
        theBuilder.append(SER_DELIM);

        if (myAssignedName != null) {
            theBuilder.append(myAssignedName);
        } else {
            theBuilder.append("");
        }
        theBuilder.append(SER_DELIM);
        theBuilder.append(myPinned);

    }

    public Date getLastAccess() {
        return myLastAccess;
    }

    public void setLastAccess(Date theLastAccess) {
        myLastAccess = theLastAccess;
    }

    public String getAssignedName() {
        return myAssignedName;
    }

    public void setAssignedName(String theAssignedName) {
        myAssignedName = theAssignedName;
    }

    public String asHistoryToken() {
        return HistoryUtil.getTokenForNewStop(getRouteTag(), getStopTag());
    }

}
