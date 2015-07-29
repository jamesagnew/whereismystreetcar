package ca.wimsc.client.common.map.layers;

import java.util.Date;
import java.util.Set;

import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.MostRecentTweets;
import ca.wimsc.client.common.model.ShowTweetsMode;
import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.DateUtil;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the most recent tweet for a particular route in a single line
 */
public class RecentTweetsFooterLayer extends FlowPanel implements IMapFooterLayer {

	protected static final String HASHTAG_TTCU = "#ttcu";

	private Image myCategoryIcon;
	private MostRecentTweets myCurrentResults;
	private Set<String> myCurrentRouteTag;
	private boolean myDestroyed;
	private MySelectedRoutePropertyChangeListener mySelectedRoutePropertyChangeListener;
	private HTML myTextLabel;
	private FlowPanel myTweetsPanel;
	private RepeatingCommand myScheduledUpdateTask;


	/**
	 * Constructor
	 */
	public RecentTweetsFooterLayer() {

		myCategoryIcon = new Image("images/twitter_icon.png");
		myCategoryIcon.addStyleName("twitterCategoryIcon");
		myCategoryIcon.setVisible(false);
		add(myCategoryIcon);

		myTweetsPanel = new FlowPanel();
		add(myTweetsPanel);

		mySelectedRoutePropertyChangeListener = new MySelectedRoutePropertyChangeListener();
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, mySelectedRoutePropertyChangeListener);

		myScheduledUpdateTask = new RepeatingCommand() {

			@Override
			public boolean execute() {
				if (!myDestroyed) {
					updateShowingTweet();
				}
				return !myDestroyed;
			}
		};
		Scheduler.get().scheduleFixedDelay(myScheduledUpdateTask, 60 * 60 * 1000);

		updateCurrentRouteTag();

	}


	/**
	 * Clean up
	 */
	@Override
	public void closeNow() {
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_ROUTE_PROPERTY, mySelectedRoutePropertyChangeListener);
		myDestroyed = true;
	}


	/**
	 * Create an inline label displaying the category of the current tweet (e.g. #ttcu)
	 */
	protected Widget createCategoryLabel() {
		// Anchor categoryLabel = new Anchor(HASHTAG_TTCU, "http://ttcupdates.com/", "_blank");
		// categoryLabel.addStyleName("twitterCategoryLabel");
		// return categoryLabel;
		return null;
	}


	protected MostRecentTweets getCurrentResults() {
		return myCurrentResults;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFooterHeight() {
		return 20;
	}


	/**
	 * For normal mode, hard code (that rhymes!)
	 */
	protected ShowTweetsMode getShowTweetsMode() {
		return ShowTweetsMode.SHOW_ONE;
	}


	protected FlowPanel getTweetsPanel() {
		return myTweetsPanel;
	}


	protected void onDoneAddingTweet() {
		// nothing, may be overridden
	}


	protected void redrawContents() {
		myTweetsPanel.clear();

		ShowTweetsMode showTweetsMode = getShowTweetsMode();

		if (myCurrentResults != null && myCurrentResults.getMostRecent() != null) {
			myCategoryIcon.setVisible(true);
			addStyleName("twitterPanel");

			try {
				Tweet firstTweet = myCurrentResults.getMostRecent();

				Date createdAt = firstTweet.getCreatedAtDate();
				if (createdAt == null) {
					return;
				}

				Widget categoryLabel = createCategoryLabel();
				if (categoryLabel != null) {
					myTweetsPanel.add(categoryLabel);
				}

				Label timeElapsedLabel = new Label(DateUtil.formatTimeElapsed(createdAt) + " ago ");
				timeElapsedLabel.addStyleName("twitterTimeElapsedLabel");
				myTweetsPanel.add(timeElapsedLabel);

				if (!showTweetsMode.equals(ShowTweetsMode.SHORT)) {

					Label nameLabel = new Label("@" + firstTweet.getFromUser() + " ");
					nameLabel.addStyleName("twitterNameLabel");
					myTweetsPanel.add(nameLabel);

					String tweetText = firstTweet.getText().replace(HASHTAG_TTCU, "");
					String anchorTweetText = StringUtil.addAnchorTagsAroundLinks(tweetText);

					if (myTextLabel == null) {
						myTextLabel = new HTML(anchorTweetText);
						myTextLabel.addStyleName("twitterTextLabel");
					}

					myTextLabel.setTitle(tweetText);
					myTweetsPanel.add(myTextLabel);

				}

				onDoneAddingTweet();

			} catch (Exception e) {
				GWT.log("Problem adding Tweet panel: ", e);
			}
		} else {

			myCategoryIcon.setVisible(false);
			removeStyleName("twitterPanel");

		}
	}


	private void updateCurrentRouteTag() {
		Set<String> newValue = Model.INSTANCE.getSelectedRouteTags();
		if (newValue == null) {
			return;
		}

		if (myCurrentRouteTag == null || !myCurrentRouteTag.equals(newValue)) {
			myCurrentRouteTag = (Set<String>) newValue;
			updateShowingTweet();
		}
	}


	/**
	 * Update the showing tweet on this panel
	 */
	public void updateShowingTweet() {
		if (myCurrentRouteTag != null && myCurrentRouteTag.size() > 0) {

			Common.SC_SVC_TWIT.getMostRecentTweetForRoutes(myCurrentRouteTag, new AsyncCallback<MostRecentTweets>() {

				@Override
				public void onSuccess(MostRecentTweets theResult) {
					myCurrentResults = theResult;
					redrawContents();
				}


				@Override
				public void onFailure(Throwable theCaught) {
					Common.report(Common.CLIENT_LOGGING_HANDLER, theCaught);
				}
			});

		}

	}


	private final class MySelectedRoutePropertyChangeListener implements IPropertyChangeListener {
		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			if (thePropertyName != Model.SELECTED_ROUTE_PROPERTY) {
				return;
			}

			updateCurrentRouteTag();
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getBottomIndex() {
		return 0;
	}

}
