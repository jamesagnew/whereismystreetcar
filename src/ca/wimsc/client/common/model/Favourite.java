package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Favourite implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private HasLatLngBounds myBounds;
	private transient String myHistoryToken;
	private String myId;
	private String myName;
	private List<String> myRouteTags;
	private boolean myShowPredictionsOnly;
	private List<String> myStopTags;

	@Override
	public boolean equals(Object theObj) {
		Favourite obj = (Favourite) theObj;
		return obj.getId().equals(myId);
	}

	public HasLatLngBounds getBounds() {
		return myBounds;
	}

	public String getHistoryToken() {
		if (myHistoryToken == null) {
			myHistoryToken = HistoryUtil.getTokenForFavourite(this);
		}
		return myHistoryToken;
	}

	public String getId() {
		return myId;
	}

	public String getName() {
		return myName;
	}

	public List<String> getRouteTags() {
		if (myRouteTags == null) {
			myRouteTags = new ArrayList<String>();
		}
		return myRouteTags;
	}

	public List<String> getStopTags() {
		if (myStopTags == null) {
			myStopTags = new ArrayList<String>();
		}
		return myStopTags;
	}

	@Override
	public int hashCode() {
		return myId != null ? myId.hashCode() : 1;
	}

	public boolean isShowPredictionsOnly() {
		return myShowPredictionsOnly;
	}

	public boolean matches(Collection<String> theRouteTags, Collection<String> theStopTags) {
		if (theRouteTags == null || theStopTags == null) {
			return false;
		}
		
		List<String> routeTags = getRouteTags();
		if (theRouteTags.size() != routeTags.size()) {
			return false;
		}
		
		List<String> stopTags = getStopTags();
		if (theStopTags.size() != stopTags.size()) {
			return false;
		}
		
		if (!routeTags.containsAll(theRouteTags)) {
			return false;
		}
		
		if (!stopTags.containsAll(theStopTags)) {
			return false;
		}
		return true;
	}

	public void setBounds(HasLatLngBounds theBounds) {
		myHistoryToken = null;
		myBounds = theBounds;
	}

	public void setId(String theId) {
		assert StringUtil.isNotBlank(theId);

		myHistoryToken = null;
		myId = theId;
	}

	public void setName(String theName) {
		assert StringUtil.isNotBlank(theName);

		myHistoryToken = null;
		myName = theName;
	}

	public void setRouteTags(List<String> theRouteTags) {
		assert theRouteTags != null && theRouteTags.size() > 0;

		myHistoryToken = null;
		myRouteTags = theRouteTags;
	}

	public void setShowPredictionsOnly(boolean theShowPredictionsOnly) {
		myHistoryToken = null;
		myShowPredictionsOnly = theShowPredictionsOnly;
	}

	public void setStopTags(List<String> theStopTags) {
		assert theStopTags != null;

		myHistoryToken = null;
		myStopTags = theStopTags;
	}

}
