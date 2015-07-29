package ca.wimsc.client;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.map.BaseOuterPanel;
import ca.wimsc.client.common.systemmap.SystemMapOuterPanel;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Whereismystreetcar implements EntryPoint {

	private BaseOuterPanel myCurrentView;

	private void initView() {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void onUncaughtException(Throwable theE) {
				Common.handleUnexpectedError(theE);
			}

		});

		updateCurrentView();

		History.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				updateCurrentView();
			}
		});

		// Add event for tracking
		if (HistoryUtil.isShowSystemMap()) {
			Common.trackGoogleAnalyticsEvent("Launch", "LaunchSystemMap");
		} else if (HistoryUtil.isCurrentlyShowingFavourite()) {
			Common.trackGoogleAnalyticsEvent("Launch", "LaunchFavourite");
		} else if (HistoryUtil.hasRouteOrStopToken()){
			Common.trackGoogleAnalyticsEvent("Launch", "LaunchRouteAndStopUrl");
		} else {
			Common.trackGoogleAnalyticsEvent("Launch", "LaunchNew");
		}

		// Common.report("/clientLoggingHandler", new Exception());

	}

	private void updateCurrentView() {
		if (HistoryUtil.isShowSystemMap()) {
			if (myCurrentView == null || !(myCurrentView instanceof SystemMapOuterPanel)) {
				if (myCurrentView != null) {
					myCurrentView.closeNow();
				}

				myCurrentView = GWT.create(SystemMapOuterPanel.class);
				RootLayoutPanel.get().clear();
				RootLayoutPanel.get().add(myCurrentView);
			}
		} else {
			if (myCurrentView == null || !(myCurrentView instanceof BaseMapOuterPanel)) {
				if (myCurrentView != null) {
					myCurrentView.closeNow();
				}

				myCurrentView = GWT.create(BaseMapOuterPanel.class);
				RootLayoutPanel.get().clear();
				RootLayoutPanel.get().add(myCurrentView);
				((BaseMapOuterPanel) myCurrentView).init();
			}
		}

	}

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		initView();

		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				Common.trackGoogleAnalytics(theEvent.getValue());
			}
		});

		new Timer() {

			@Override
			public void run() {
				Window.Location.reload();
			}
		}.schedule(12 * 60 * 60 * 1000);

		// Initial track of analytics
		Common.trackGoogleAnalytics(History.getToken());

	}

}
