package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NumberAndTimestamp implements Serializable, IsSerializable, Comparable<NumberAndTimestamp> {

    private static final long serialVersionUID = 1L;

    private int myNumber;
    private Date myTimestamp;

    public NumberAndTimestamp() {
    }

    public NumberAndTimestamp(int theNumber, Date theTimestamp) {
        myNumber = theNumber;
        myTimestamp = theTimestamp;
    }

    public NumberAndTimestamp(Integer theNumber, long theTimestamp) {
        myNumber = theNumber;
        myTimestamp = new Date(theTimestamp);
    }

    @Override
    public boolean equals(Object theObj) {
        if (!(theObj instanceof NumberAndTimestamp)) {
            return false;
        }
        NumberAndTimestamp o = (NumberAndTimestamp) theObj;
        return (o.myNumber == myNumber) && (o.myTimestamp.equals(myTimestamp));
    }

    public int getNumber() {
        return myNumber;
    }

    public Date getTimestamp() {
        return myTimestamp;
    }
    
    public long getTimestampAge() {
        return System.currentTimeMillis() - myTimestamp.getTime();
    }
    
    public void setNumber(int theNumber) {
        myNumber = theNumber;
    }

    public void setTimestamp(Date theTimestamp) {
        myTimestamp = theTimestamp;
    }

    @Override
    public String toString() {
        return "NumberAndTimestamp[" + myNumber + " / " + myTimestamp + "]";
    }

    @Override
    public int compareTo(NumberAndTimestamp theO) {
        return myTimestamp.compareTo(theO.getTimestamp());
    }

}
