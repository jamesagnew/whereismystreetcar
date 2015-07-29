package ca.wimsc.client.common.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.HasMap;
import com.google.gwt.maps.client.Map;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.mvc.MVCObject;

public class ProjectedOverlay extends MVCObject {

        public ProjectedOverlay(JavaScriptObject jso) {
          super(jso);
          ProjectedOverlayImpl.impl.bind(jso, this);
        }
        
        public ProjectedOverlay(HasMap theHasMap, HasLatLngBounds theBounds) {
          this(ProjectedOverlayImpl.impl.construct(theHasMap.getJso(), theBounds.getJso()));
        }
        
        public void kill() {
            ProjectedOverlayImpl.impl.kill(getJso());
        }
        
        public HasMap getMap() {
          return new Map(ProjectedOverlayImpl.impl.getMap(getJso()));
        }
        
        public void setMap(HasMap map) {
            if (map != null) {
                ProjectedOverlayImpl.impl.setMap(getJso(), map.getJso());
            } else {
                ProjectedOverlayImpl.impl.setMap(getJso(), null);
            }
        }

        public void redraw() {
            ProjectedOverlayImpl.impl.redraw(getJso());
        }
        
}
