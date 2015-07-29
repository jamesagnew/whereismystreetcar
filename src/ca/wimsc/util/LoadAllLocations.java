package ca.wimsc.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.HashMapCache;
import ca.wimsc.server.jpa.StopQuadrant;
import ca.wimsc.server.jpa.StopQuadrant.StopKey;
import ca.wimsc.server.jpa.StopQuadrantList;
import ca.wimsc.server.svc.INextbusFetcherService;
import ca.wimsc.server.svc.NextbusFetcherServiceImpl;
import ca.wimsc.server.xml.route.Body;
import ca.wimsc.server.xml.route.Body.Route.Stop;

public class LoadAllLocations {

	private static final Logger logger = Logger.getLogger(LoadAllLocations.class.getName());


	/**
	 * @param args
	 * @throws FailureException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws FailureException, MalformedURLException, IOException {
		// INextbusFetcherService fetcher = new NextbusFetcherServiceImpl(new HashMapCache());
		//
		// List<StopKey> closest = null;
		// for (int i = 15; i < 100; i += 5) {
		// logger.info("*** Starting increment " + i + ".");
		//
		// List<List<StopQuadrant>> quadrants = fetcher.loadAllStopQuadrants(i);
		//
		// StopQuadrantList list = new StopQuadrantList();
		// list.setStopQuadrants(quadrants);
		//
		// long start = System.currentTimeMillis();
		//
		// for (int n = 0; n < 10; n++) {
		// StopQuadrantList.QuadrantDistanceComparator.ourNumCalcs = 0;
		// closest = list.findClosestStops(43.6413501, -79.4152394, 10);
		// // closest = list.findClosestStops(0, 0, 10);
		// }
		//
		// long delay = System.currentTimeMillis() - start;
		//
		// logger.info("Done finding " + closest.size() + " nearby stops took " + delay + "ms and "
		// + StopQuadrantList.QuadrantDistanceComparator.ourNumCalcs + " calculations");
		//
		// }
		//
		// // for (StopKey stopKey : closest) {
		// // logger.info(" * found " + stopKey.getStopTag() + " for " + stopKey.getRouteTag());
		// // }

		Writer w = new FileWriter("oldStopTags.properties");

		String[] routes = { "501", "502", "503", "504", "505", "506", "508", "509", "510", "511", "512" };

		for (String theRoute : routes) {

			URL routeUrl = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=ttc&r=" + theRoute + "&verbose");
			InputStream content = routeUrl.openStream();
			Body newRoute = JAXB.unmarshal(content, Body.class);

			routeUrl = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=ttc-manual&r=" + theRoute + "&verbose");
			content = routeUrl.openStream();
			Body oldRoute = JAXB.unmarshal(content, Body.class);

			for (Stop oldStop : oldRoute.getRoute().getStop()) {

				String foundTag = null;
				String foundTag2 = null;
				double foundDistance = Double.MAX_VALUE;
				for (Stop newStop : newRoute.getRoute().getStop()) {

					if (newStop.getStopId() != null && newStop.getStopId().equals(oldStop.getStopId())) {
						foundTag2 = newStop.getTag();
					}

				}

				if (foundTag2 != null) {
					w.append(oldStop.getTag() + "=" + foundTag2 + "  " + "\n");
				}
			}
		}

		w.close();
	}

}
