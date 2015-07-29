package ca.wimsc.client.common.systemmap;

import ca.wimsc.client.common.widgets.ProjectedOverlay;

import com.google.gwt.maps.client.HasMap;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;

public class TtcSystemMapOverlay extends ProjectedOverlay {

    public TtcSystemMapOverlay(HasMap theHasMap) {
        super(theHasMap, createBounds());
    }

    public static HasLatLngBounds createBounds() {
        LatLng sw = new LatLng(43.5734, -79.6387);
        LatLng ne = new LatLng(43.8518, -79.1114); // 12341
        LatLngBounds bounds = new LatLngBounds(sw, ne);
        return bounds;
    }

}
