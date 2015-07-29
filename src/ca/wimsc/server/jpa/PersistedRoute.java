package ca.wimsc.server.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.model.RoutePath;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

@Entity(name = "ROUTE")
public class PersistedRoute implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger ourLog = Logger.getLogger(PersistedRoute.class.getName());

	/**
	 * Stored using a marshalled form to keep lots of data in one place
	 */
	@Column(name = "AVG_RTE_SPEED")
	private Text myAverageRouteSpeed;

	@Column(name = "LAST_VEH_REF_TS")
	private long myLastVehicleRefreshTimestamp;

	@Column(name = "RTE_ENDS")
	private Set<GeoPt> myRouteEndpoints;

	private transient RoutePath myRoutePath;

	/**
	 * Stored using a marshalled form to keep lots of data in one place
	 */
	@Column(name = "ROUTE_PATH")
	private Text myRoutePathString;

	@Id
	@Column(name = "ROUTE_TAG")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key myRouteTag;

	private transient ArrayList<PersistedVehicle> myUnmarshalledVehicles;

	@Column(name = "VEHICLES")
	private Text myVehicles;

	/**
	 * Stored using a marshalled form to keep lots of data in one place
	 */
	@Column(name = "VEH_IN_SVC")
	private Text myVehiclesInService;


	public NumbersAndTimestamps getAverageRouteSpeed() {
		if (myAverageRouteSpeed != null) {
			return new NumbersAndTimestamps(myAverageRouteSpeed.getValue());
		} else {
			return new NumbersAndTimestamps();
		}
	}


	public long getLastVehicleRefreshTimestamp() {
		return myLastVehicleRefreshTimestamp;
	}


	public Set<GeoPt> getRouteEndpoints() {
		return myRouteEndpoints;
	}


	/**
	 * @return the routePath
	 */
	public RoutePath getRoutePath() {
		if (myRoutePath == null) {
			try {
			myRoutePath = new RoutePath(getRoutePathString());
			} catch (NumberFormatException e) {
				ourLog.warning("Failed to parse route path. String was: " + getRoutePathString());
				throw e;
			}
		}
		return myRoutePath;
	}


	/**
	 * @return Returns the marshalled form of the route path
	 */
	public String getRoutePathString() {
		return myRoutePathString != null ? myRoutePathString.getValue() : null;
	}


	public String getRouteTag() {
		return myRouteTag.getName();
	}


	public PersistedVehicle getVehicle(String theTag) {

		// Make sure myUnmarshalledVehicles is set
		getVehicles();

		// Binary search
		int low = 0;
		int high = myUnmarshalledVehicles.size() - 1;
		int middle;

		while (low <= high) {
			middle = (low + high) / 2;

			int compareTo = myUnmarshalledVehicles.get(middle).getVehicleTag().compareTo(theTag);
			if (compareTo < 0) {
				low = middle + 1;
			} else if (compareTo > 0) {
				high = middle - 1;
			} else {
				return myUnmarshalledVehicles.get(middle);
			}
		}

		return null;
	}


	public List<PersistedVehicle> getVehicles() {
		if (myUnmarshalledVehicles != null) {
			return myUnmarshalledVehicles;
		}

		ArrayList<PersistedVehicle> retVal = new ArrayList<PersistedVehicle>();

		if (myVehicles != null && myVehicles.getValue().length() > 0) {
			String[] split = myVehicles.getValue().split("~");
			for (String string : split) {
				retVal.add(PersistedVehicle.unmarshall(string));
			}

		}

		myUnmarshalledVehicles = retVal;
		return retVal;

	}


	public NumbersAndTimestamps getVehiclesInService() {
		if (myVehiclesInService == null) {
			return new NumbersAndTimestamps();
		}
		return new NumbersAndTimestamps(myVehiclesInService.getValue());
	}


	public void setAverageRouteSpeed(NumbersAndTimestamps theAverageRouteSpeed) {
		myAverageRouteSpeed = new Text(theAverageRouteSpeed.marshall());
	}


	public void setLastVehicleRefreshTimestamp(long theLastVehicleRefreshTimestamp) {
		myLastVehicleRefreshTimestamp = theLastVehicleRefreshTimestamp;
	}


	public void setRouteEndpoints(Set<GeoPt> theRouteEndpoints) {
		myRouteEndpoints = theRouteEndpoints;
	}


	/**
	 * @param theRoutePath
	 *            the routePath to set
	 */
	public void setRoutePath(RoutePath theRoutePath) {
		myRoutePath = theRoutePath;
		myRoutePathString = new Text(theRoutePath.marshall());
	}


	public void setRouteTag(String theRouteTag) {
		myRouteTag = KeyFactory.createKey(PersistedRoute.class.getSimpleName(), theRouteTag);
	}


	public void setVehicles(Collection<PersistedVehicle> theVehicles) {
		// We'll marshall these in order so we can binary search for them later
		ArrayList<PersistedVehicle> vehicles = new ArrayList<PersistedVehicle>(theVehicles);
		Collections.sort(vehicles, new Comparator<PersistedVehicle>() {

			@Override
			public int compare(PersistedVehicle theO1, PersistedVehicle theO2) {
				return theO1.getVehicleTag().compareTo(theO2.getVehicleTag());
			}
		});

		myUnmarshalledVehicles = vehicles;

		StringBuilder b = new StringBuilder();
		for (PersistedVehicle next : vehicles) {
			if (b.length() > 0) {
				b.append("~");
			}
			b.append(PersistedVehicle.marshall(next));
		}

		myVehicles = new Text(b.toString());
	}


	public void setVehiclesInService(NumbersAndTimestamps theVehiclesInService) {
		myVehiclesInService = new Text(theVehiclesInService.marshall());
	}
}
