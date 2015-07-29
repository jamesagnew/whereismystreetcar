package ca.wimsc.client.common.map;

import java.util.ArrayList;
import java.util.List;

import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.FavouriteStop;
import ca.wimsc.client.common.model.IModelListenerSync;
import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.resources.FavouritesResources;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.widgets.HoverImage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Small table displaying the user's favourites
 */
public class FavouritesGrid extends Grid implements IClosable {

	private IFavouriteActionHandler myFavouriteActionHandler;
	private MyModelListener myModelListener = new MyModelListener();
	private boolean myShowDeleteButton = true;
	private boolean myShowSelectedStop = true;
	
	/**
	 * Constructor
	 */
	public FavouritesGrid(IFavouriteActionHandler theFavouriteActionHandler) {
		myFavouriteActionHandler = theFavouriteActionHandler;
		
		addStyleName("favouriteStopGrid");

		Model.INSTANCE.addFavouriteStopListener(myModelListener);
		Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_FAVOURITE_PROPERTY, myModelListener);
		Model.INSTANCE.addPropertyChangeListener(Model.FAVOURITE_LIST_PROPERTY, myModelListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeNow() {
		Model.INSTANCE.removeFavouriteStopListener(myModelListener);
		Model.INSTANCE.removePropertyChangeListener(Model.SELECTED_FAVOURITE_PROPERTY, myModelListener);
		Model.INSTANCE.removePropertyChangeListener(Model.FAVOURITE_LIST_PROPERTY, myModelListener);
	}

	public boolean isEmpty() {
		return getRowCount() == 0;
	}

//	public void setShowSelectedStop(boolean theShowSelectedStop) {
//		myShowSelectedStop = theShowSelectedStop;
//	}

	public void updateFavourites() {
		List<Favourite> favouriteStops = Model.INSTANCE.getFavourites();

		updateFavourites(favouriteStops);

	}

	public void updateFavourites(List<Favourite> theFavourites) {

		List<Favourite> displayRows = new ArrayList<Favourite>();
		Favourite selectedFav = Model.INSTANCE.getCurrentFavourite();
		for (int i = 0; i < theFavourites.size() && i < 5; i++) {

			final Favourite nextFav = theFavourites.get(i);
			boolean stopSelected = selectedFav != null && selectedFav.equals(nextFav);

			if (stopSelected && !myShowSelectedStop) {
				continue;
			}

			displayRows.add(nextFav);
		}

		resize(displayRows.size(), 3);

		for (int i = 0; i < displayRows.size() && i < 5; i++) {

			final Favourite nextFav = displayRows.get(i);
			String labelString = (nextFav.getName());

			FavouritesResources res = FavouritesResources.INSTANCE;

			int col = 0;

			if (myShowDeleteButton) {
				Image favDelImage = new HoverImage(res.favDel(), res.favDelHover());
				favDelImage.addClickHandler(new MyDelClickHandler(nextFav));
				setWidget(i, col++, favDelImage);
			}

			Widget link;
			if (Model.INSTANCE.getCurrentFavourite() != null && Model.INSTANCE.getCurrentFavourite().equals(nextFav)) {
				link = new Label(labelString);
				link.addStyleName("favouriteLinkSelected");
			} else {
				link = new Hyperlink(labelString, nextFav.getHistoryToken());
				link.addStyleName("favouriteLink");
			}
			
			FlowPanel titlePanel = new FlowPanel();
			titlePanel.add(link);
			
			Label editLink = new Label("(Edit)");
			editLink.addStyleName("hyperlinkSmall");
			editLink.addClickHandler(new MyEditLinkClickHandler(nextFav));
			titlePanel.add(editLink);
			
			setWidget(i, col++, titlePanel);
			

		}
	}

	public interface IFavouriteActionHandler{
		void edit(Favourite theFavourite);
	}

	public class MyDelClickHandler implements ClickHandler {

		private Favourite myFavourite;

		public MyDelClickHandler(Favourite theNextFav) {
			myFavourite = theNextFav;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			if (!Window.confirm("Really remove the stop: " + (myFavourite.getName()))) {
				return;
			}

			MapDataController.INSTANCE.removeFavourite(myFavourite);
		}

	}

	public class MyEditLinkClickHandler implements ClickHandler {

		private Favourite myFav;

		public MyEditLinkClickHandler(Favourite theFav) {
			myFav = theFav;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			myFavouriteActionHandler.edit(myFav);
		}

	}

	public class MyHeartClickHandler implements ClickHandler {

		private FavouriteStop myFavourite;

		public MyHeartClickHandler(FavouriteStop theFav) {
			myFavourite = theFav;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			String name = Window.prompt("Choose a name to remember this stop by? (E.g. Home, Work)", (myFavourite.toTitle()));
			if (name == null || name.trim().length() == 0) {
				return;
			}
			myFavourite.setPinned(true);
			myFavourite.setAssignedName(name);
			Model.INSTANCE.addOrUpdateFavouriteStop(myFavourite);
		}

	}

	
	private final class MyModelListener implements IModelListenerSync<List<FavouriteStop>>, IPropertyChangeListener {
		@Override
		public void objectLoaded(List<FavouriteStop> theObject) {
			updateFavourites();
		}

		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			updateFavourites();
		}
	}
}
