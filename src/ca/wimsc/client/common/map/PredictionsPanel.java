package ca.wimsc.client.common.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.NumberAndTimestamp;
import ca.wimsc.client.common.model.NumbersAndTimestamps;
import ca.wimsc.client.common.model.Prediction;
import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.resources.VehicleMarkers;
import ca.wimsc.client.common.util.Vehicles;
import ca.wimsc.client.common.util.Vehicles.VehicleTypeEnum;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays all of the upcoming predictions for a given stop, with nice graphics to indicate vehicle type, speed info,
 * etc.
 */
public class PredictionsPanel extends FlowPanel {

	private int myTopWidgetCount;


	/**
	 * Constructor
	 */
	public PredictionsPanel() {
		addStyleName("predictionsPanel");
	}


	/**
	 * Must be called immediately after constructor
	 */
	public void addWidgetToTop(Widget theWidget) {
		myTopWidgetCount++;
		add(theWidget);
	}


	public void removeWidgetFromTop(Widget theWidget) {
		if (remove(theWidget)) {
			myTopWidgetCount++;
		}
	}


	/**
	 * Update the view with the contents of multiple prediction lists, which will be grouped together
	 */
	public void updateList(Collection<PredictionsList> theValues) {

		List<Prediction> predictions = new ArrayList<Prediction>();
		for (PredictionsList predictionsList : theValues) {
			predictions.addAll(predictionsList.getPredictions());
		}
		Collections.sort(predictions);

		doUpdateList(predictions, theValues.size() > 1);

	}


	/**
	 * Update the view with the contents of a single list of predictions
	 */
	public void updateList(PredictionsList theResult) {
		doUpdateList(theResult.getPredictions(), false);
	}


	private void doUpdateList(List<Prediction> theResult, boolean theResultsAreForMultipleStops) {
		while (getWidgetCount() > myTopWidgetCount) {
			remove(myTopWidgetCount);
		}

		if (theResult == null || theResult.size() == 0) {
			add(new HTML("Sorry, no predictions available for this stop right now."));
		}

		String previousRouteAndStopTag = null;

		for (Prediction next : theResult) {
			int timeMins = (int) Math.floor(next.getSeconds() / 60);
			NumbersAndTimestamps speed = next.getSpeed();

			StringBuilder text = new StringBuilder();

			// Add speed chart
			if (speed != null && speed.getValues().size() > 1) {

				text.append("<img height='27' align='right' width='60' style='margin-top: 2px;' src='");
				// text.append("http://chart.apis.google.com/chart?chxr=0,0,46&chxs=0,676767,0,0,l,676767&chxt=y&chs=50x15&cht=ls&chco=3D7930&chd=t:");
				text.append("http://chart.apis.google.com/chart");
				// text.append("?chxs=0,676767,12.5,0,l,676767");

				text.append("?chxt=r");
				text.append("&chs=60x27");
				text.append("&cht=ls");
				text.append("&chco=3D7930");
				text.append("&chg=-1,-1,1,1");
				text.append("&chls=2,4,0");
				text.append("&chm=B,C5D4B5BB,0,0,0");
				text.append("&chd=t:");

				int maxValue = -1;
				for (NumberAndTimestamp numberAndTimestamp : speed.getValues()) {
					if (maxValue > -1) {
						text.append(",");
					}

					int number = numberAndTimestamp.getNumber();
					if (number > maxValue) {
						maxValue = number;
					}
					text.append(number);

				}

				text.append("&chxl=0:|km%2Fh|");
				text.append(maxValue > 20 ? maxValue : 20);
				text.append("&chxp=0,0,");
				text.append(maxValue > 20 ? maxValue : 20);

				text.append("&chds=0,");
				text.append(maxValue > 20 ? maxValue : 20);
				text.append("&chxr=0,0,");
				text.append(maxValue > 20 ? maxValue : 20);

				// text.append("&chds=0,");
				// text.append(maxValue);
				// text.append("&chls=1&chma=|0,5&chm=B,C5D4B5BB,0,0,0");
				text.append("'/>");
			}

			if (theResultsAreForMultipleStops) {
				String nextRouteAndStop = next.getPredictionsList().getRouteTag() + next.getPredictionsList().getStopTag();
				if (previousRouteAndStopTag == null || !previousRouteAndStopTag.equals(nextRouteAndStop)) {

					text.append(next.getPredictionsList().getRouteTag());

					StopListForRoute stopListForRoute = Model.INSTANCE.getStopListForRoute(next.getPredictionsList().getRouteTag());
					
					String uiDirectionName = stopListForRoute.getUiDirectionNameForStopTag(next.getClosestStopTag());
					if (uiDirectionName != null) {
						text.append(" ");
						text.append(uiDirectionName);
					}
					
					Stop stop = stopListForRoute.getStopTagToStop().get(next.getPredictionsList().getStopTag());
					
					if (stop != null) {
						text.append(": ");
						text.append(stop.getTitle());
					}

					text.append("<br>");

					previousRouteAndStopTag = nextRouteAndStop;
				}
			}

			text.append("<b>");
			text.append(timeMins);
			text.append(" min");

			if (timeMins != 1) {
				text.append('s');
			}

			text.append("</b>");

			// Add speed
			// if (speed != null && !speed.isEmpty()) {
			// Integer currentSpeed = next.getCurrentSpeed();
			// if (currentSpeed != null) {
			// text.append(" @ ");
			// text.append(currentSpeed);
			// text.append("km/h");
			// }
			// }

			StopList stopList = null;
			StopListForRoute stopListForRoute = Model.INSTANCE.getStopListForRoute(next.getPredictionsList().getRouteTag());
			if (stopListForRoute != null) {
				String vehicleDirectionTag = next.getVehicleDirectionTag();

				// text.append("<!-- dir: " + vehicleDirectionTag + "-->");

				stopList = stopListForRoute.getUiOrNonUiStopListForDirectionTag(vehicleDirectionTag);
			}

			if (next.getClosestStopTag() != null) {

				if (stopList != null) {
					// text.append("<!-- Have stop list -->");

					Stop closestStop = stopList.findFirstStopWithTag(next.getClosestStopTag());
					if (closestStop != null) {
						text.append(", Currently at ");
						text.append(closestStop.getTitle());
					}
				}
			}

			String vehicleTag = Vehicles.getVehicleLink(next.getVehicleId());
			if (vehicleTag != null) {
				text.append(" (").append(vehicleTag).append(") ");
			}

			if (next.getHeadway() != null) {
				text.append("<br><span class='predictionsHeadway'>");
				int mins = next.getHeadway() / 60;
				text.append(mins);
				text.append(" min");
				if (mins != 1) {
					text.append('s');
				}
				text.append(" between vehicles</span>");
			}

			if (stopList != null && stopList.isShortTurn()) {
				text.append("<br><span class='predictionShortTurn'>Short turning: ");
				text.append(stopList.getTitle());
				text.append("</span>");
			}

			FlowPanel nextPredictionContainer = new FlowPanel();
			nextPredictionContainer.addStyleName("predictionRowContainer");
			add(nextPredictionContainer);

			Image image;
			VehicleTypeEnum vehicleType = Vehicles.getVehicleType(next.getVehicleId());
			switch (vehicleType) {
			case STREETCAR:
				image = new Image(VehicleMarkers.INSTANCE.streetcarIcon());
				break;
			default:
				image = new Image(VehicleMarkers.INSTANCE.busIcon());
				break;
			}
			image.addStyleName("predictionVehicleImage");
			nextPredictionContainer.add(image);

			text.append("<br clear='all'/>");

			HTML w = new HTML(text.toString());
			w.addStyleName("predictionText");
			nextPredictionContainer.add(w);

		}
	}
}
