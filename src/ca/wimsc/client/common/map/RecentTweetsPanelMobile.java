package ca.wimsc.client.common.map;

import ca.wimsc.client.common.map.layers.RecentTweetsFooterLayer;
import ca.wimsc.client.common.model.MostRecentTweets;
import ca.wimsc.client.common.model.ShowTweetsMode;
import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.DateUtil;
import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

public class RecentTweetsPanelMobile extends RecentTweetsFooterLayer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Label createCategoryLabel() {
		Label categoryLabel = new Label(HASHTAG_TTCU);
		categoryLabel.addStyleName("twitterCategoryLabel");
		return categoryLabel;
	}

	private Label myHideShowTweetsLabel;
	private boolean myForceCurrentMode;


	/**
	 * Constructor
	 */
	public RecentTweetsPanelMobile() {
		super();
	}


	@Override
	protected ShowTweetsMode getShowTweetsMode() {
		ShowTweetsMode retVal = HistoryUtil.getShowTweetsMode();

		/*
		 * If the first tweet is old, default to hidden
		 */
		MostRecentTweets res = getCurrentResults();
		if (res != null && !myForceCurrentMode) {
			Tweet mostRecent = res.getMostRecent();
			if (mostRecent != null) {
				long age = DateUtil.getTimeElapsedInSeconds(mostRecent.getCreatedAtDate());
				if (age > (8 * 60 * 60)) {
					retVal = ShowTweetsMode.SHORT;
				}

			}
		}

		return retVal;
	}


	@Override
	protected void onDoneAddingTweet() {

		myHideShowTweetsLabel = new Label();
		myHideShowTweetsLabel.addStyleName("twitterControlAnchor");
		getTweetsPanel().insert(myHideShowTweetsLabel, 0);

		ShowTweetsMode stm = getShowTweetsMode();
		if (stm == ShowTweetsMode.SHOW_ONE) {
			myHideShowTweetsLabel.setText("Hide");
			myHideShowTweetsLabel.addStyleName("twitterControlAnchorOpen");
			myHideShowTweetsLabel.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent theEvent) {
					HistoryUtil.setShowTweetsMode(ShowTweetsMode.SHORT);
					redrawContents();
					Common.trackGoogleAnalyticsEvent("Config", "HideTweets");
					myForceCurrentMode = true;
				}
			});
		} else {
			myHideShowTweetsLabel.setText("Show");
			myHideShowTweetsLabel.removeStyleName("twitterControlAnchorOpen");
			myHideShowTweetsLabel.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent theEvent) {
					HistoryUtil.setShowTweetsMode(ShowTweetsMode.SHOW_ONE);
					redrawContents();
					Common.trackGoogleAnalyticsEvent("Config", "ShowTweets");
					myForceCurrentMode = true;
				}
			});
		}

	}

}
