/**
 * 
 */
package ca.wimsc.client.common.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.NumberAndTimestamp;
import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.util.ColourUtil;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.DateUtil;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.SetUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author James
 * 
 */
public class StatsPanel extends FlowPanel implements IClosable {

	private static final DateTimeFormat ourTimeFormat = DateTimeFormat.getFormat("HH'%3A'mm");
	private Image myAverageSpeedImg;
	private boolean myClosed;
	private IPropertyChangeListener myRoutechangeListener;

	private Image myVehicleCountImg;
	private int myLineGraphWidth;
	private Set<String> myRoutes;
	private Button myAllStreetcarsButton;
	private int myLineGraphHeight = 150;
	private Image myLegendImg1;
	private Image myLegendImg2;


	/**
	 * Constructor with default widths
	 */
	public StatsPanel() {
		this(300);
	}


	/**
	 * Constructor
	 */
	public StatsPanel(int theLineGraphWidth) {
		myLineGraphWidth = theLineGraphWidth;

		myRoutechangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
				myRoutes = Model.INSTANCE.getSelectedRouteTags();
				redraw();
			}
		};
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myRoutechangeListener);

		myLegendImg1 = new Image();
		this.add(myLegendImg1);

		myLegendImg2 = new Image();
		this.add(myLegendImg2);

		myVehicleCountImg = new Image();
		this.add(myVehicleCountImg);

		myAverageSpeedImg = new Image();
		this.add(myAverageSpeedImg);

		myRoutes = Model.INSTANCE.getSelectedRouteTags();
		redraw();

		RepeatingCommand repeatCommand = new RepeatingCommand() {

			@Override
			public boolean execute() {
				if (!myClosed) {
					redraw();
				}
				return !myClosed;
			}
		};
		Scheduler.get().scheduleFixedDelay(repeatCommand, 30 * 60 * 1000);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeNow() {
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, myRoutechangeListener);
		myClosed = true;
	}


	private void createXLineChart(Map<String, NumbersAndTimestamps> theResult, String title, Image image, boolean theIncludeLegend) {
		if (theResult.isEmpty()) {
			image.getElement().getStyle().setDisplay(Display.NONE);
			return;
		} else {
			image.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		}

		List<String> routeTags = new ArrayList<String>(theResult.keySet());
		Collections.sort(routeTags);

		// Sort values by timestamp then by route.. This makes it nice for graphing
		Map<Date, Map<String, Integer>> datesToRouteTagsToCounts = new HashMap<Date, Map<String, Integer>>();
		int highestValue = 0;
		for (String nextRouteTag : routeTags) {
			NumbersAndTimestamps nextTimestamps = theResult.get(nextRouteTag);

			int nextHighest = nextTimestamps.getHighest();
			if (nextHighest > highestValue) {
				highestValue = nextHighest;
			}

			for (NumberAndTimestamp nextValue : nextTimestamps.getValues()) {

				Date nextTimestamp = nextValue.getTimestamp();
				Integer nextCount = nextValue.getNumber();

				if (!datesToRouteTagsToCounts.containsKey(nextTimestamp)) {
					datesToRouteTagsToCounts.put(nextTimestamp, new HashMap<String, Integer>());
				}
				datesToRouteTagsToCounts.get(nextTimestamp).put(nextRouteTag, nextCount);

			}
		}

		ArrayList<Date> dates = new ArrayList<Date>(datesToRouteTagsToCounts.keySet());
		Collections.sort(dates);

		int size = dates.size();
		int numXlabels = 4; // how many labels on the X axis

		if (size < numXlabels) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("http://chart.apis.google.com/chart");
		// 12%3A00
		/*
		 * ?chxl=0:|12%3A00|1%3A00|3%3A00 &chxp=0,0,2,5
		 */

		// Chart type
		builder.append("?cht=lc");

		// Title
		builder.append("&chtt=" + title);
		builder.append("&chts=000000,10.4");

		// Line colour
		builder.append("&chco=");
		for (int i = 0; i < routeTags.size(); i++) {
			if (i > 0) {
				builder.append(",");
			}
			builder.append(ColourUtil.getColourNameWithoutHash(i));
		}

		// Axes
		builder.append("&chxt=x,y");
		builder.append("&chxl=0:");

		// Labels text on the X axis
		int intervalSize = size / numXlabels;
		for (int i = 0; i < (numXlabels - 1); i++) {
			Date next = dates.get(i * intervalSize);
			builder.append("|").append(ourTimeFormat.format(next));
		}

		// Final label text on the X axis
		Date finalTimestamp = dates.get(size - 1);
		if ((System.currentTimeMillis() - finalTimestamp.getTime()) > DateUtil.HALF_AN_HOUR_IN_MILLIS) {
			builder.append("|").append(ourTimeFormat.format(finalTimestamp));
		} else {
			builder.append("|Now");
		}

		// Label values for the X axis
		builder.append("&chxp=0");
		intervalSize = size / (numXlabels - 1);
		for (int i = 0; i < (numXlabels - 1); i++) {
			builder.append(",").append(i * intervalSize);
		}
		builder.append(",").append(size - 1);

		// Axis label ranges
		int highest = highestValue + 2;
		builder.append("&chxr=0,0,").append(size).append("|1,0,").append(highest);

		// Graph size
		builder.append("&chs=").append(myLineGraphWidth).append("x").append(myLineGraphHeight);

		// Labels
		if (theIncludeLegend) {
			builder.append("&chdl=");
			for (String nextRouteTag : routeTags) {
				if (nextRouteTag != routeTags.get(0)) {
					builder.append("|");
				}
				builder.append(nextRouteTag);
			}
		}

		// Data
		builder.append("&chds=0,").append(highest);
		builder.append("&chd=t:");

		for (String nextRouteTag : routeTags) {
			if (nextRouteTag != routeTags.get(0)) {
				builder.append("|");
			}

			Integer previous = Integer.valueOf(0);
			for (Date nextDate : dates) {
				if (nextDate != dates.get(0)) {
					builder.append(",");
				}

				Integer nextCount = datesToRouteTagsToCounts.get(nextDate).get(nextRouteTag);
				if (nextCount == null) {
					nextCount = previous;
				}
				builder.append(nextCount);

				previous = nextCount;
			}

		}

		// Legend at left
		builder.append("&chdlp=l");

		image.setUrl(builder.toString());
		image.setPixelSize(myLineGraphWidth, myLineGraphHeight);
	}


	private void createLegend(Image theImage, List<String> theValues, int theOffset) {
		theImage.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

		List<String> values;
		if (theOffset > 0) {
			int size = theValues.size();
			values = theValues.subList(theOffset, size);
		} else {
			values = theValues.subList(0, -theOffset);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("http://chart.apis.google.com/chart");
		// 12%3A00
		/*
		 * ?chxl=0:|12%3A00|1%3A00|3%3A00 &chxp=0,0,2,5
		 */

		// Chart type
		builder.append("?cht=lc");

		// Line colour
		builder.append("&chco=");
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				builder.append(",");
			}
			builder.append(ColourUtil.getColourNameWithoutHash(i + (theOffset > 0 ? theOffset : 0)));
		}

		builder.append("&cht=ls");
		
		// Graph size
		int width = 50;
		builder.append("&chs=").append(width).append("x").append(myLineGraphHeight);

		// Labels
		builder.append("&chdl=");
		for (String nextRouteTag : values) {
			if (nextRouteTag != values.get(0)) {
				builder.append("|");
			}
			builder.append(nextRouteTag);
		}

		// Legend at left
		builder.append("&chdlp=l");

		theImage.setUrl(builder.toString());
		theImage.setPixelSize(width, myLineGraphHeight);

	}


	private void handleAverageSpeedResults(Map<String, NumbersAndTimestamps> theResult) {
		String title = "Average+Speed+kmh";
		Image image = myAverageSpeedImg;
		createXLineChart(theResult, title, image, false);
	}


	private void handleVehicleCountResults(Map<String, NumbersAndTimestamps> theResult, boolean theAddLegend) {
		String title = "Vehicle+Count";
		Image image = myVehicleCountImg;
		createXLineChart(theResult, title, image, theAddLegend);
	}


	protected void redraw() {

		if (myRoutes == null || myRoutes.isEmpty()) {
			return;
		}

		if (myRoutes.size() > 8) {
			List<String> routes = SetUtil.toList(myRoutes);
			Collections.sort(routes);
			
			int count = routes.size() / 2;
			createLegend(myLegendImg1, routes, -count);
			createLegend(myLegendImg2, routes, count);
		} else {
			myLegendImg1.getElement().getStyle().setDisplay(Display.NONE);
			myLegendImg2.getElement().getStyle().setDisplay(Display.NONE);
		}

		Common.SC_SVC_STAT.getRouteVehicleCountsOverLast24Hours(myRoutes, new AsyncCallback<Map<String, NumbersAndTimestamps>>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Common.report(Common.CLIENT_LOGGING_HANDLER, theCaught);
			}


			@Override
			public void onSuccess(Map<String, NumbersAndTimestamps> theResult) {
				handleVehicleCountResults(theResult, !myLegendImg1.isVisible());
			}
		});

		Common.SC_SVC_STAT.getRouteAverageSpeedsOverLast24Hours(myRoutes, new AsyncCallback<Map<String, NumbersAndTimestamps>>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Common.report(Common.CLIENT_LOGGING_HANDLER, theCaught);
			}


			@Override
			public void onSuccess(Map<String, NumbersAndTimestamps> theResult) {
				handleAverageSpeedResults(theResult);
			}
		});

	}

	
	public class MyAllStreetcarsButton extends Button {
		
		public MyAllStreetcarsButton() {
			
			setText("All Streetcars");
			setStyleName("statsAllStreetcarsButton");
			addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent theEvent) {
					Model.INSTANCE.getRouteList(new IModelListenerAsync<RouteList>() {

						@Override
						public void startLoadingObject() {
							// nothing
						}


						@Override
						public void objectLoaded(RouteList theObject, boolean theRequiredAsyncLoad) {
							SetUtil.IPredicate<String> predicate = new SetUtil.IPredicate<String>() {

								@Override
								public boolean matches(String theObject) {
									return theObject.startsWith("5");
								}
							};
							myRoutes = SetUtil.toSet(predicate, theObject.getRouteTags());
							redraw();
						}
					});
				}
			});
			
		}
		
	}
	
	public Button createAllStreetcarsButton() {
		return new MyAllStreetcarsButton();
	}
	
}
