package ca.wimsc.client.common.map.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.RoutePath;
import ca.wimsc.client.common.model.RoutePathElement;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.SpeedUtil;
import ca.wimsc.client.common.widgets.google.Polyline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.base.HasPoint;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.event.Event;
import com.google.gwt.maps.client.event.EventCallback;
import com.google.gwt.maps.client.event.HasMapsEventListener;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.Timer;

/**
 * Shows a line containing the route, colouring it to match the speed of the currently selected direction
 */
public class SpeedRouteLayer extends AbstractMapOverlayLayer {

	private Set<String> myCurrentlySelectedRouteTags = Collections.emptySet();
	private MapWidget myMap;
	private IPropertyChangeListener myPropertyChangeListener;
	private Map<String, MyRoutePathListener> myRoutePathListeners = new HashMap<String, SpeedRouteLayer.MyRoutePathListener>();
	private Map<String, List<Polyline>> myRoutePolylines = new HashMap<String, List<Polyline>>();
	private HasMapsEventListener myZoomChangedEventListener;

	
	public SpeedRouteLayer(boolean theBothDirections) {
	}

	@Override
	public void destroy() {
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
		for (Iterator<Map.Entry<String, List<Polyline>>> iter = myRoutePolylines.entrySet().iterator(); iter.hasNext();) {
			Entry<String, List<Polyline>> next = iter.next();
			for (Polyline nextPl : next.getValue()) {
				nextPl.setMap(null);
			}
			iter.remove();
		}

		Event.removeListener(myZoomChangedEventListener);
	}


	@Override
	public void initialize(MapWidget theMap) {
		myMap = theMap;

		myPropertyChangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
				redraw();
			}
		};
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myPropertyChangeListener);
		redraw();

		myZoomChangedEventListener = Event.addListener(myMap.getMap(), "zoom_changed", new MyZoomChangedEventListener());

	}


	private void redraw() {

		Set<String> selectedRoute = Model.INSTANCE.getSelectedRouteTags();
		if (selectedRoute == null) {
			return;
		}

		if (selectedRoute.equals(myCurrentlySelectedRouteTags)) {
			return;
		}

		for (String next : myCurrentlySelectedRouteTags) {

			if (selectedRoute.contains(next) == false) {
				List<Polyline> list = myRoutePolylines.get(next);
				if (list != null) {
					for (Polyline nextPl : list) {
						nextPl.setMap(null);
					}
					list.clear();
				}
			}

			Model.INSTANCE.removeRoutePathListener(next, myRoutePathListeners.get(next));
		}

		myCurrentlySelectedRouteTags = selectedRoute;

		for (String next : myCurrentlySelectedRouteTags) {
			MyRoutePathListener listener = new MyRoutePathListener(next);
			myRoutePathListeners.put(next, listener);
			Model.INSTANCE.addRoutePathListener(next, listener);
		}

	}


	private final class MyRoutePathListener implements IModelListenerAsync<RoutePath> {
		private boolean myHaveOutstandingLoad;
		private double myOffset;
		private final String myRoute;
		private int myWeight;


		private MyRoutePathListener(String theRoute) {
			myRoute = theRoute;
		}


		private void addToMap(Polyline currentRoutePolyLine, List<HasLatLng> path, int theNextSpeed) {
			if (currentRoutePolyLine == null) {
				return;
			}

			PolylineOptions options = new PolylineOptions();

			String strokeColour = SpeedUtil.toSpeedHexColour(theNextSpeed);
			options.setStrokeColor(strokeColour);
			options.setStrokeOpacity(1.0);
			options.setStrokeWeight(myWeight);

			options.setPath(path);
			currentRoutePolyLine.setOptions(options);
			currentRoutePolyLine.setMap(myMap.getMap());
		}


		private void drawRoute(RoutePath theObject, int directionIndex) {
			Polyline currentRoutePolyLine = null;
			List<HasLatLng> path = null;
			String directionTag = theObject.getDirectionTags().get(directionIndex);
			StopListForRoute stopListForRoute = Model.INSTANCE.getStopListForRoute(myRoute);
			StopList stopList = stopListForRoute.getUiOrNonUiStopListForDirectionTag(directionTag);
			if (stopList == null) {
				GWT.log("ERROR: Unknown direction tag: " + directionTag);
				return;
			}
			
			DirectionEnum direction = DirectionEnum.fromNameOrTitle(stopList.getName());
			
			int directionModifier = 0;
			switch (direction) {
			case NORTHBOUND:
			case EASTBOUND:
				directionModifier = 0;
				break;
			case SOUTHBOUND:
			case WESTBOUND:
				directionModifier = 180;
				break;
			}

			int previousSpeed = -999;
			int nextSpeed = 0;
			// int currentSegmentSpeed = 0;

			List<Polyline> routePolylines = myRoutePolylines.get(myRoute);
			int pathIndex = routePolylines.size();

			List<RoutePathElement> elems = theObject.getRoutePathElements();
			for (RoutePathElement nextElement : elems) {

				nextSpeed = nextElement.getSpeedInKmhDirectionWithDefault(directionIndex, 20);
				int speedDelta = Math.abs(nextSpeed - previousSpeed);

				if (speedDelta >= 3 || nextElement.isNewPathEntry()) {

					addToMap(currentRoutePolyLine, path, previousSpeed);

					previousSpeed = nextSpeed;

					if (routePolylines.size() <= pathIndex) {
						currentRoutePolyLine = new Polyline();
						routePolylines.add(currentRoutePolyLine);
					} else {
						currentRoutePolyLine = routePolylines.get(pathIndex);
					}

					pathIndex++;

					if (path == null || path.size() == 0 || nextElement.isNewPathEntry()) {
						path = new ArrayList<HasLatLng>();
					} else {
						path = new ArrayList<HasLatLng>(path.subList(path.size() - 1, path.size()));
					}

				}

				HasLatLng nextLatLng = nextElement.toLatLng();
				int nextHeadingDegrees = nextElement.getHeadingToNextStop();
				nextHeadingDegrees = (nextHeadingDegrees + directionModifier) % 360;

				double nextHeadingRads = Math.toRadians(nextHeadingDegrees);
				double sinHeading = Math.sin(nextHeadingRads);
				double cosHeading = Math.cos(nextHeadingRads);

				// double offset = 0.001;
				double offsetX = (cosHeading * myOffset);
				double offsetY = (sinHeading * myOffset);

				HasPoint point = myMap.getMap().getProjection().fromLatLngToPoint(nextLatLng);
				point = new Point(point.getX() + offsetX, point.getY() + offsetY);
				// point = new Point(point.getX() + 0.001, point.getY() + 0.001);
				nextLatLng = myMap.getMap().getProjection().fromPointToLatLng(point);

				path.add(nextLatLng);

			}

			addToMap(currentRoutePolyLine, path, nextSpeed);
		}


		@Override
		public void objectLoaded(final RoutePath theObject, final boolean theRequiredAsyncLoad) {
			if (theObject.getLastUpdatedTimestamp() == null || theObject.getRoutePathElements() == null) {
				// This shouldn't generally happen.. But if a route hasn't had the
				// activity monitor service examine it yet, then it might
				return;
			}

			if (myHaveOutstandingLoad) {
				return;
			}

			/*
			 * If there are no bounds defined, we probably received the data for the route before the data for the
			 * streetcar locations, which are the ones used to set the bounds, so let's wait a bit
			 */
			JavaScriptObject boundsJso = myMap.getMap().getBounds().getJso();
			if (boundsJso == null) {
				myHaveOutstandingLoad = true;
				GWT.log("Bounds is null, waiting a bit before drawing speed layer");
				new Timer() {

					@Override
					public void run() {
						myHaveOutstandingLoad = false;
						objectLoaded(theObject, theRequiredAsyncLoad);
					}
				}.schedule(750);
				return;
			}

			processRoutePath(theObject);

		}


		private void processRoutePath(RoutePath theObject) {
			// Which direction(s) to show?
			List<String> directionTags = theObject.getDirectionTags();
			Set<String> stopTags = Model.INSTANCE.getSelectedStopTagsForRoute(myRoute);
			List<Integer> selectedDirectionIndexes = new ArrayList<Integer>();
			for (String nextStopTag : stopTags) {
				String nextDirectionTag = Model.INSTANCE.getSelectedStopDirectionTag(nextStopTag);
				int nextIndex = directionTags.indexOf(nextDirectionTag);
				if (nextIndex != -1) {
					selectedDirectionIndexes.add(nextIndex);
				}
			}
			if (selectedDirectionIndexes.isEmpty()) {
				selectedDirectionIndexes.add(0);
				selectedDirectionIndexes.add(1);
			}

			List<Polyline> routePolylines = myRoutePolylines.get(myRoute);
			if (routePolylines == null) {
				routePolylines = new ArrayList<Polyline>();
				myRoutePolylines.put(myRoute, routePolylines);
			}

			for (int i = 0; i < routePolylines.size(); i++) {
				Polyline next = routePolylines.get(i);
				next.setMap(null);
			}
			routePolylines.clear();

			// Calculate offset between the two directions
			HasLatLngBounds currentBounds = myMap.getMap().getBounds();
			HasLatLng currentSw = currentBounds.getSouthWest();
			HasLatLng currentNe = currentBounds.getNorthEast();
			HasPoint sw = myMap.getMap().getProjection().fromLatLngToPoint(currentSw);
			HasPoint ne = myMap.getMap().getProjection().fromLatLngToPoint(currentNe);
			double xDelta = Math.abs(sw.getX() - ne.getX());
			double mapWidth = myMap.getOffsetWidth();
			double pixelWidth = xDelta / mapWidth;
			
			int zoom = myMap.getMap().getZoom();
			if (zoom < 13) {
				myOffset = pixelWidth * 2;
				myWeight = 2;
			} else if (zoom < 15) {
				myOffset = pixelWidth * 3;
				myWeight = 3;
			} else if (zoom <= 16) {
				myOffset = pixelWidth * 4;
				myWeight = 4;
			} else {
				myOffset = pixelWidth * 5;
				myWeight = 5;
			}
			
			GWT.log("Zoom: " + zoom);
			GWT.log("SW: x=" + sw.getX() + ", y=" + sw.getY());
			GWT.log("NE: x=" + ne.getX() + ", y=" + ne.getY());

			drawRoute(theObject, 0);
			drawRoute(theObject, 1);
		}


		@Override
		public void startLoadingObject() {
			// nothing
		}
	}


	/**
	 * @author James
	 * 
	 */
	public class MyZoomChangedEventListener extends EventCallback {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void callback() {
			if (myCurrentlySelectedRouteTags != null) {
				new Timer() {
					@Override
					public void run() {
						for (String next : myCurrentlySelectedRouteTags) {
							RoutePath nextRoutePath = Model.INSTANCE.getRoutePath(next);
							if (nextRoutePath == null) {
								continue;
							}
							
							MyRoutePathListener listener = myRoutePathListeners.get(next);
							if (listener == null) {
								return;
							}
							
							listener.objectLoaded(nextRoutePath, false);
							
						}
					}}.schedule(750);
			}
		}

	}

}
