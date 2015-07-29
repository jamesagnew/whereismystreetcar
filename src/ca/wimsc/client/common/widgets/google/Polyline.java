/**
 * 
 */
package ca.wimsc.client.common.widgets.google;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.HasMap;
import com.google.gwt.maps.client.Map;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCObject;
import com.google.gwt.maps.client.overlay.HasPolyline;
import com.google.gwt.maps.client.overlay.HasPolylineOptions;
import com.google.gwt.maps.client.overlay.impl.PolylineImpl;

/**
 * Replacement for google-maps-v3-api polyline implementation, which mistakenly stores the jso in a local variable
 * 
 * @author vinay.sekhri@gmail.com (Vinay Sekhri)
 */
public class Polyline extends MVCObject implements HasPolyline {

	public Polyline(JavaScriptObject jso) {
		super(jso);
	}


	public Polyline() {
		this(PolylineImpl.impl.construct());
	}


	public Polyline(HasPolylineOptions options) {
		this(PolylineImpl.impl.construct(options.getJso()));
	}


	@Override
	public HasMap getMap() {
		return new Map(PolylineImpl.impl.getMap(getJso()));
	}


	@Override
	public List<HasLatLng> getPath() {
		List<HasLatLng> path = new ArrayList<HasLatLng>();
		JsArray<JavaScriptObject> pathJsArr = PolylineImpl.impl.getPath(getJso());
		for (int i = 0; i < pathJsArr.length(); ++i) {
			path.add(new LatLng(pathJsArr.get(i)));
		}
		return path;
	}


	@Override
	public void setMap(HasMap map) {
		if (map != null) {
			PolylineImpl.impl.setMap(getJso(), map.getJso());
		} else {
			PolylineImpl.impl.setMap(getJso(), null);
		}
	}


	@Override
	public void setOptions(HasPolylineOptions options) {
		PolylineImpl.impl.setOptions(getJso(), options.getJso());
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setPath(List<HasLatLng> path) {
		JsArray<JavaScriptObject> pathJsArr = (JsArray<JavaScriptObject>) JavaScriptObject.createArray();
		for (HasLatLng latLng : path) {
			pathJsArr.push(latLng.getJso());
		}
		PolylineImpl.impl.setPath(getJso(), pathJsArr);
	}

}
