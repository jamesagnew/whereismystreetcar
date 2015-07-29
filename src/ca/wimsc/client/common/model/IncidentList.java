package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class IncidentList implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private List<IncidentBean> myIncidents;

	/**
	 * @param theIncidents the incidents to set
	 */
	public void setIncidents(List<IncidentBean> theIncidents) {
		this.myIncidents = theIncidents;
	}

	/**
	 * @return the incidents
	 */
	public List<IncidentBean> getIncidents() {
		return myIncidents;
	}
	
}
