package ca.wimsc.client.common.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.wimsc.client.common.model.FavouriteStop;
import ca.wimsc.client.common.model.IModelListenerSync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.resources.FavouritesResources;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;
import ca.wimsc.client.common.widgets.HoverImage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class FavouriteStopGrid extends Grid implements IClosable {

    private IClosable myContainer;
    private MyModelListener myModelListener;
    private boolean myShowDeleteButton = true;
    private boolean myShowSelectedStop = true;
	private boolean myShowFavButton = false;

    /**
     * Constructor to use if this grid doesn't need to notify anyone when a stop is selected
     */
    public FavouriteStopGrid() {
        this(null);
    }

    /**
     * Constructor
     */
    public FavouriteStopGrid(boolean theShowDeleteButton) {
        myShowDeleteButton = theShowDeleteButton;
        
        addStyleName("favouriteStopGrid");
    }

    /**
     * Constructor
     * 
     * @param theContainer
     *            The container to close if/when a stop is selected
     */
    public FavouriteStopGrid(IClosable theContainer) {
        myModelListener = new MyModelListener();
        myContainer = theContainer;

        addStyleName("favouriteStopGrid");

        Model.INSTANCE.addFavouriteStopListener(myModelListener);
        Model.INSTANCE.addPropertyChangeListener(Model.SELECTED_FAVOURITE_PROPERTY, myModelListener);
    }

    @Override
    public void closeNow() {
        Model.INSTANCE.removeFavouriteStopListener(myModelListener);
    }

    public void setShowSelectedStop(boolean theShowSelectedStop) {
        myShowSelectedStop = theShowSelectedStop;
    }

    public void updateFavourites() {
        List<FavouriteStop> favouriteStops = Model.INSTANCE.getRecentOrFavouriteStops();

        updateFavourites(favouriteStops);

    }

    public boolean isEmpty() {
        return getRowCount() == 0;
    }
    
    public void updateFavourites(List<FavouriteStop> favouriteStops) {

        List<FavouriteStop> displayRows = new ArrayList<FavouriteStop>();
        for (int i = 0; i < favouriteStops.size() && i < 5; i++) {
            
            final FavouriteStop nextFav = favouriteStops.get(i);
            Set<String> selectedStopTags = Model.INSTANCE.getSelectedStopTags();
            boolean stopSelected = selectedStopTags != null && selectedStopTags.contains(nextFav.getStopTag());
            
            if (stopSelected && !myShowSelectedStop) {
                continue;
            }
            
            displayRows.add(nextFav);
        }
        
        resize(displayRows.size(), 3);

        for (int i = 0; i < displayRows.size() && i < 5; i++) {
            
            final FavouriteStop nextFav = displayRows.get(i);
            String labelString = (nextFav.toTitle());

            FavouritesResources res = FavouritesResources.INSTANCE;

            int col = 0;
            
            if (myShowFavButton) {
                ImageResource favHeartRes = nextFav.isPinned() ? res.favHeartOn() : res.favHeart();
                Image favHeartImage = new HoverImage(favHeartRes, res.favHeartHover());
                favHeartImage.setTitle("Click here to add this stop to your \"favourites\".");
                favHeartImage.addClickHandler(new MyHeartClickHandler(nextFav));
            	setWidget(i, col++, favHeartImage);
            }
            
            if (myShowDeleteButton) {
                Image favDelImage = new HoverImage(res.favDel(), res.favDelHover());
                favDelImage.addClickHandler(new MyDelClickHandler(nextFav));
                setWidget(i, col++, favDelImage);
            }

            if (Model.INSTANCE.getSelectedStopTags() != null && Model.INSTANCE.getSelectedStopTags().contains(nextFav.getStopTag())) {
                Label link = new Label(labelString);
                link.addStyleName("favouriteLinkSelected");
                setWidget(i, col++, link);
            } else {
                Anchor link = new Anchor(labelString);
                link.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent theEvent) {
                        if (myContainer != null) {
                            myContainer.closeNow();
                        }
                        HistoryUtil.setStop(nextFav.getRouteTag(), nextFav.getStopTag());
                    }
                });
                setWidget(i, col++, link);
            }

        }
    }

    public class MyDelClickHandler implements ClickHandler {

        private FavouriteStop myFavourite;

        public MyDelClickHandler(FavouriteStop theFav) {
            myFavourite = theFav;
        }

        @Override
        public void onClick(ClickEvent theEvent) {
            if (!Window.confirm("Really remove the stop: " + (myFavourite.toTitle()))) {
                return;
            }

            Model.INSTANCE.removeRecentStop(myFavourite);
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
