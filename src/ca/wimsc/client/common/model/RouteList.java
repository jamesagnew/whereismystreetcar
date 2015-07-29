package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RouteList implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private List<Route> myList;

	private boolean myFullyLoaded;

    public List<Route> getList() {
        return myList;
    }

    public void setList(List<Route> theList) {
        myList = theList;
    }

    public Route getRoute(String theRouteId) {
        for (Route next : myList) {
            if (theRouteId.equals(next.getTag())) {
                return next;
            }
        }
        return null;
    }

	/**
	 * @return
	 */
	public Set<String> getRouteTags() {
		Set<String> retVal = new HashSet<String>();
        for (Route next : myList) {
        	retVal.add(next.getTag());
        }
		return retVal;
	}
	
	public boolean hasRoute(String theString) {
		return getRoute(theString) != null;
	}

	/**
	 * @param theFullyLoaded
	 */
	public void setFullyLoaded(boolean theFullyLoaded) {
		myFullyLoaded = theFullyLoaded;
	}

	/**
	 * @return the fullyLoaded
	 */
	public boolean isFullyLoaded() {
		return myFullyLoaded;
	}
    
}
