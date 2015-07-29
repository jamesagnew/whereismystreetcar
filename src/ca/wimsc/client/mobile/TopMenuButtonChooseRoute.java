package ca.wimsc.client.mobile;

import java.util.ArrayList;

import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.top.TopMenuButton;

public class TopMenuButtonChooseRoute extends TopMenuButton {

	public static final double WIDTH = 100;

	public void updateText(boolean theOpen) {
		StringBuilder b = new StringBuilder();

		if (theOpen) {

			b.append("Choose<br>Route and Stop...");

		} else {

			Favourite fav = Model.INSTANCE.getCurrentFavourite();
			if (fav != null) {

				b.append("Showing:<br>");
				b.append(fav.getName());

			} else {

				ArrayList<Route> selectedRoutes = Model.INSTANCE.getSelectedRoutes();
				ArrayList<Stop> selectedStops = Model.INSTANCE.getSelectedStops();

				if (selectedRoutes == null) {
					selectedRoutes = new ArrayList<Route>();
				}
				if (selectedStops == null) {
					selectedStops = new ArrayList<Stop>();
				}
				
				if (selectedStops.size() > 0) {
					if (selectedStops.size() == 1) {
						Stop stop = selectedStops.get(0);
						Route route = Model.INSTANCE.getRouteForStopTag(stop.getStopTag());
						b.append(route.getTitle());
						b.append("<br>");
						b.append(stop.getTitle());
					} else if (selectedStops.size() == 2) {
						Stop stop0 = selectedStops.get(0);
						Stop stop1 = selectedStops.get(1);
						b.append(stop0.getTitle());
						b.append("<br>");
						b.append(stop1.getTitle());
					} else {
						Stop stop0 = selectedStops.get(0);
						b.append(stop0.getTitle());
						b.append("<br>And ");
						b.append(selectedStops.size() - 1);
						b.append(" more");
					}
				} else {
					if (selectedRoutes.size() == 1) {
						b.append(selectedRoutes.get(0).getTitle());
					} else if (selectedRoutes.size() == 2) {
						b.append(selectedRoutes.get(0).getTitle());
						b.append("<br>");
						b.append(selectedRoutes.get(1).getTitle());
					} else {
						b.append("Showing ");
						b.append(selectedRoutes.size());
						b.append(" route");
						if (selectedRoutes.size() > 1) {
							b.append("s");
						}
					}
				}

			}
		}

		setMenuHtml(b.toString());

	}
}
