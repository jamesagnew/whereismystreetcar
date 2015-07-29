package ca.wimsc.client.common.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Direction implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;
    private String myName;
    private String myTag;

    public String getName() {
        return myName;
    }

    public String getTag() {
        return myTag;
    }

    public void setName(String theName) {
        myName = theName;
    }

    public void setTag(String theTag) {
        myTag = theTag;
    }

}
