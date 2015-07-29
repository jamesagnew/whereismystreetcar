package ca.wimsc.client.mobile;

import java.util.List;

import ca.wimsc.client.common.map.BaseMapOuterPanel;
import ca.wimsc.client.common.map.FavouritesGrid;
import ca.wimsc.client.common.map.FavouritesGrid.IFavouriteActionHandler;
import ca.wimsc.client.common.model.Favourite;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.top.BaseTopPanel;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.widgets.HtmlBr;
import ca.wimsc.client.common.widgets.HtmlH1;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class MobileChooseFavPanel extends AbstractDropDownMenu implements IClosable, ResizeHandler {

    public class MyFavouriteActionHandler implements IFavouriteActionHandler {

		@Override
		public void edit(Favourite theFavourite) {
			myTopPanel.showChooseRoutePanel(theFavourite);
		}

	}

	private FavouritesGrid myFavouriteStopGrid;
    private HandlerRegistration myResizeRegistration;
	private MapTopPanelMobile myTopPanel;

    public MobileChooseFavPanel(final MapTopPanelMobile theTopPanel, BaseMapOuterPanel theContainer) {
        super(theContainer);
        myTopPanel = theTopPanel;
        
        init();
    }

    @Override
    public void closeNow() {
        myResizeRegistration.removeHandler();

        if (myFavouriteStopGrid != null) {
            myFavouriteStopGrid.closeNow();
        }
    }

    private void init() {
        FlowPanel chooserContainer = new FlowPanel();
        getScrollPanel().add(chooserContainer);

        myResizeRegistration = Window.addResizeHandler(this);

        ClickHandler addFavClickHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				myTopPanel.showChooseRoutePanel(true);
			}
		};
        
        // Favourite Stops

        List<Favourite> favouriteStops = Model.INSTANCE.getFavourites();
        if (favouriteStops.size() > 0) {

            HtmlH1 recentFavLabel = new HtmlH1("Favourite");
            chooserContainer.add(recentFavLabel);

            Label label = new Label("These are your favourites. You may click the 'X' to remove one, or maybe ");
            label.addStyleName("inlineLabel");
			chooserContainer.add(label);

			label = new Label("add another.");
            label.addStyleName("inlineLinkLabel");
            label.addClickHandler(addFavClickHandler);
			chooserContainer.add(label);

			chooserContainer.add(new HtmlBr());
			
            myFavouriteStopGrid = new FavouritesGrid(new MyFavouriteActionHandler());
            myFavouriteStopGrid.updateFavourites();
            chooserContainer.add(myFavouriteStopGrid);

        } else {

            Label label = new Label("You have not added any favourites yet. ");
            label.addStyleName("inlineLabel");
			chooserContainer.add(label);

            label = new Label("Add one now");
            label.addStyleName("inlineLinkLabel");
            label.addClickHandler(addFavClickHandler);
			chooserContainer.add(label);
			
			chooserContainer.add(new HtmlBr());
			
        }

//        List<FavouriteStop> recentStops = Model.INSTANCE.getRecentStops();
//
//        HtmlH1 recentFavLabel = new HtmlH1("Recent");
//        chooserContainer.add(recentFavLabel);
//
//        
//        myFavouriteStopGrid = new FavouriteStopGrid(theCloseHandler);
//        myFavouriteStopGrid.setShowSelectedStop(true);
//        myFavouriteStopGrid.updateFavourites(recentStops);
//        chooserContainer.add(myFavouriteStopGrid);

        setPopupPosition(BaseTopPanel.SPINNER_WIDTH, BaseTopPanel.TOP_PANEL_HEIGHT - 4);
        show();
    }

    @Override
    public void onResize(ResizeEvent theEvent) {
        resizeMe();
    }

}
