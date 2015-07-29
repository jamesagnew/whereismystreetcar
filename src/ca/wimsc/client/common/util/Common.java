package ca.wimsc.client.common.util;

import java.util.Date;

import ca.wimsc.client.common.rpc.IGetMapDataService;
import ca.wimsc.client.common.rpc.IGetMapDataServiceAsync;
import ca.wimsc.client.common.rpc.IGetNearbyStopsService;
import ca.wimsc.client.common.rpc.IGetNearbyStopsServiceAsync;
import ca.wimsc.client.common.rpc.IGetRouteConfigService;
import ca.wimsc.client.common.rpc.IGetRouteConfigServiceAsync;
import ca.wimsc.client.common.rpc.IGetStatisticsService;
import ca.wimsc.client.common.rpc.IGetStatisticsServiceAsync;
import ca.wimsc.client.common.rpc.IGetTwitterService;
import ca.wimsc.client.common.rpc.IGetTwitterServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class Common {

	public static final String CLIENT_LOGGING_HANDLER = "/clientLoggingHandler";

	static Date ourInitTime = new Date();
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public static final IGetTwitterServiceAsync SC_SVC_TWIT = GWT.create(IGetTwitterService.class);
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public static final IGetStatisticsServiceAsync SC_SVC_STAT = GWT.create(IGetStatisticsService.class);
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public static final IGetRouteConfigServiceAsync SC_SVC_GRC = GWT.create(IGetRouteConfigService.class);
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public static final IGetNearbyStopsServiceAsync SC_SVC_GNB = GWT.create(IGetNearbyStopsService.class);
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public static final IGetMapDataServiceAsync SC_SVC_GMD = GWT.create(IGetMapDataService.class);

	/**
	 * Add a history token using analytics
	 */
	public static void trackGoogleAnalytics(String theHistoryToken) {
		theHistoryToken = Window.Location.getQueryString() + "#" + theHistoryToken;
		if (GWT.isScript()) {
			doTrackGoogleAnalytics(theHistoryToken);
		} else {
			GWT.log("New history token: " + theHistoryToken);
		}
	}

	public static void trackGoogleAnalyticsEvent(String theCategory, String theAction) {
		doTrackGoogleAnalyticsEvent(theCategory, theAction);
	}

	
	/**
	 * Add a history token using analytics
	 */
	static native void doTrackGoogleAnalyticsEvent(String theCategory, String theAction) /*-{
		try {
		  $wnd._gaq.push(['_trackEvent', theCategory, theAction]);
		} catch(err) {
		}
	}-*/;

	/**
	 * Are we in mobile mode, as determined by a javascript var set by the JSP which loads this page
	 */
	public static native boolean isRunningMobile()/*-{
		return $wnd.runningMobile;
	}-*/;

	/**
	 * Add a history token using analytics
	 */
	static native void doTrackGoogleAnalytics(String historyToken) /*-{
		try {
		  $wnd._gaq.push(['_setAccount', 'UA-19092728-1']);
		  $wnd._gaq.push(['_trackPageview', historyToken]);

		//        // setup tracking object with account
		//        var pageTracker = $wnd._gat._getTracker("UA-19092728-1");
		//        pageTracker._setRemoteServerMode();
		//        // turn on anchor observing
		//        pageTracker._setAllowAnchor(true)
		//        // send event to google server
		//        pageTracker._trackPageview(historyToken);

		} catch(err) {
		    //window.alert("Problem: " + err);
		// ignore
		}
	}-*/;

	public static Date getInitTime() {
		return Common.ourInitTime;
	}

	public static void handleUnexpectedError(Throwable theE) {
		GWT.log("Error", theE);
		if (theE instanceof UmbrellaException) {
			for (Throwable e : ((UmbrellaException) theE).getCauses()) {
				GWT.log("Error", e);
			}
		}

		StringBuilder b = new StringBuilder();
		int count = 0;
		for (StackTraceElement e : theE.getStackTrace()) {
			b.append(e.getMethodName());
			b.append("\n");
			if (count++ > 5) {
				break;
			}
		}

		Window.alert("Sorry! We just hit an unexpected error. If this keeps happeneing, please let us know so that we can fix it. Message: " + theE.toString() + "\n\nTrace: "
				+ b.toString());

		if (theE instanceof UmbrellaException) {
			// Umbrella exception isn't helpful, so report the cause
			for (Throwable nextCause : ((UmbrellaException) theE).getCauses()) {
				report(CLIENT_LOGGING_HANDLER, nextCause);
				break;
			}
		} else {
			report(CLIENT_LOGGING_HANDLER, theE);
		}

	}

	/**
	 * Duplicated from GWT itself, but with logging added
	 */
	public static boolean report(String url, Throwable t) {
		try {
			XMLHttpRequest xhr = XMLHttpRequest.create();
			xhr.open("POST", url);
			xhr.send(buildPayload(t));
			return true;
		} catch (Throwable t2) {
			GWT.log("Failed to report error: ", t2);
			return false;
		}
	}

	/**
	 * Visible for testing.
	 */
	static String buildPayload(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"strongName\" : ");
		sb.append('\'').append((GWT.getPermutationStrongName())).append('\'');
		sb.append(",\"message\" : ");
		sb.append('\'').append(jsonEscape(t.toString())).append('\'');

		sb.append(",\"stackTrace\" : [");
		boolean needsComma = false;
		for (StackTraceElement e : t.getStackTrace()) {
			if (needsComma) {
				sb.append(",");
			} else {
				needsComma = true;
			}

			sb.append('\'').append((e.getMethodName())).append('\'');
		}
		sb.append("]}");

		return sb.toString();
	}

	private static String jsonEscape(String theString) {
		if (theString == null) {
			return null;
		}
		return SafeHtmlUtils.htmlEscape(theString);
	}

}
