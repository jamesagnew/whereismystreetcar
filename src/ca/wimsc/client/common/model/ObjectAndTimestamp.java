package ca.wimsc.client.common.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ObjectAndTimestamp<T> implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private long myTimestamp;
    private T myObject;

    public ObjectAndTimestamp() {
    }

    public ObjectAndTimestamp(T theObject, long theTimestamp) {
        myTimestamp = theTimestamp;
        myObject = theObject;
    }

    @Override
    public boolean equals(Object theObj) {
        if (!(theObj instanceof ObjectAndTimestamp)) {
            return false;
        }
        ObjectAndTimestamp<?> o = (ObjectAndTimestamp<?>) theObj;
        return (o.myTimestamp == myTimestamp) && (o.myObject.equals(myObject));
    }

    public long getTimestamp() {
        return myTimestamp;
    }

    public T getObject() {
        return myObject;
    }

    public void setTimestamp(long theNumber) {
        myTimestamp = theNumber;
    }

    public void setObject(T theObject) {
        myObject = theObject;
    }

    @Override
    public String toString() {
        return "ObjectAndTimestamp[" + myTimestamp + " / " + myObject + "]";
    }

    public long getTimestampAge() {
        return System.currentTimeMillis() - myTimestamp;
    }

}
