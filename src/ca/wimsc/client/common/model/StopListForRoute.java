/**
 * 
 */
package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes all stops on a given route
 */
public class StopListForRoute implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private List<StopList> myNonUiStopLists;
	private String myRouteTag;
	private Map<String, Stop> myStopTagToStop = new HashMap<String, Stop>();
	private List<StopList> myUiStopLists;
	private Map<String, StopList> myDirectionTagToStopList = new HashMap<String, StopList>();
	
	/**
	 * Constructor
	 */
	public StopListForRoute() {
		// nothing
	}


	/**
	 * Constructor
	 */
	public StopListForRoute(String theRouteTag) {
		myUiStopLists = new ArrayList<StopList>();
		myNonUiStopLists = new ArrayList<StopList>();
		myRouteTag = theRouteTag;
	}


	public void addNonUiStopList(StopList theStopList) {
		theStopList.setStopListForRoute(this);
		myNonUiStopLists.add(theStopList);
		myDirectionTagToStopList.put(theStopList.getTag(), theStopList);
	}


	public void addUiStopList(StopList theStopList) {
		theStopList.setStopListForRoute(this);
		myUiStopLists.add(theStopList);
		myDirectionTagToStopList.put(theStopList.getTag(), theStopList);
	}


	/**
	 * @return the nonUiStopLists
	 */
	public List<StopList> getNonUiStopLists() {
		return myNonUiStopLists;
	}


	/**
	 * @return the routeTag
	 */
	public String getRouteTag() {
		return myRouteTag;
	}


	/**
	 * @return the stopTagToStop
	 */
	public Map<String, Stop> getStopTagToStop() {
		return myStopTagToStop;
	}


	/**
	 * @return the uiStopLists
	 */
	public List<StopList> getUiStopLists() {
		return myUiStopLists;
	}


	/**
	 * @param theRouteTag
	 *            the routeTag to set
	 */
	public void setRouteTag(String theRouteTag) {
		myRouteTag = theRouteTag;
	}



	/**
	 * @param theStopTagToStop the stopTagToStop to set
	 */
	public void setStopTagToStop(Map<String, Stop> theStopTagToStop) {
		myStopTagToStop = theStopTagToStop;
	}


	public StopList getUiOrNonUiStopListForDirectionTag(String theDirTag) {
		return myDirectionTagToStopList.get(theDirTag);
	}


	public StopList getUiStopListWithMatchingName(String theName) {
		assert StringUtil.isNotBlank(theName);
		
		for (StopList next : myUiStopLists) {
			if (next.getName().equals(theName)) {
				return next;
			}
		}
		
		return null;
	}

	private transient Map<String, String> myUiDirectionTagEquivalentToDirecitonTag;

	/**
	 * @param theDirectionTag
	 * @return
	 */
	public String getUiDirectionTagEquivalentToDirecitonTag(String theDirectionTag) {
		assert StringUtil.isNotBlank(theDirectionTag);
		
		if (myUiDirectionTagEquivalentToDirecitonTag == null) {
			myUiDirectionTagEquivalentToDirecitonTag = new HashMap<String, String>();
		}
		String retVal = myUiDirectionTagEquivalentToDirecitonTag.get(theDirectionTag);
		if (retVal != null) {
			return retVal;
		}
		
		DirectionEnum dir = null;
		for (StopList next : myNonUiStopLists) {
			if (next.getTag().equals(theDirectionTag)) {
				dir = next.getDirectionEnum();
			}
		}
		
		if (dir == null) {
			myUiDirectionTagEquivalentToDirecitonTag.put(theDirectionTag, theDirectionTag);
			return theDirectionTag;
		}
		
		for (StopList next : myUiStopLists) {
			if (next.getDirectionEnum() == dir) {
				retVal = next.getTag();
				myUiDirectionTagEquivalentToDirecitonTag.put(theDirectionTag, retVal);
				return retVal;
			}
		}
		
		myUiDirectionTagEquivalentToDirecitonTag.put(theDirectionTag, theDirectionTag);
		return theDirectionTag;
	}


	/**
	 * @param theClosestStopTag
	 * @return
	 */
	public String getUiDirectionNameForStopTag(String theClosestStopTag) {
		for (StopList next : myUiStopLists) {
			if (next.getStopTags().contains(theClosestStopTag)) {
				return next.getTitle();
			}
		}
		return null;
	}

}
