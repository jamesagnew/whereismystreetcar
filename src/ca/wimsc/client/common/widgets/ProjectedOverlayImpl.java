package ca.wimsc.client.common.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.jsio.client.Binding;
import com.google.gwt.jsio.client.Constructor;
import com.google.gwt.jsio.client.JSFlyweightWrapper;

public interface ProjectedOverlayImpl extends JSFlyweightWrapper {

    ProjectedOverlayImpl impl = GWT.create(ProjectedOverlayImpl.class);

    @Constructor("$wnd.ProjectedOverlay")
    JavaScriptObject construct(JavaScriptObject map, JavaScriptObject bounds);

    @Binding
    void bind(JavaScriptObject jso, ProjectedOverlay theProjectedOverlay);

    void setMap(JavaScriptObject jso, JavaScriptObject map);
    
    JavaScriptObject getMap(JavaScriptObject jso);
    
    void kill(JavaScriptObject theJso);

    void redraw(JavaScriptObject theJso);
    
}
