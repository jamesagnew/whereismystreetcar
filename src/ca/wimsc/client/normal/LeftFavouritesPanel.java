package ca.wimsc.client.normal;

import ca.wimsc.client.common.map.FavouritesGrid;
import ca.wimsc.client.common.map.FavouritesGrid.IFavouriteActionHandler;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.util.IClosable;
import ca.wimsc.client.common.util.IPropertyChangeListener;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LeftFavouritesPanel extends FlowPanel implements IClosable {

    private final class MyFavouriteListPropertyListener implements IPropertyChangeListener {
		@Override
		public void propertyChanged(String thePropertyName, Object theOldValue, Object theNewValue) {
			redraw();
		}
	}


	private Label myFavInstructionsLabel;
    private FavouritesGrid myFavGrid;
	private MyFavouriteListPropertyListener myFavouriteListPropertyListener;

    public LeftFavouritesPanel(IFavouriteActionHandler theFavouriteActionHandler) {
        
        myFavInstructionsLabel = new Label();
        myFavInstructionsLabel.addStyleName("leftInstructionsLabel");
        add(myFavInstructionsLabel);
        
        myFavGrid = new FavouritesGrid(theFavouriteActionHandler);
        add(myFavGrid);
        
        myFavGrid.updateFavourites();
        redraw();
        
        myFavouriteListPropertyListener = new MyFavouriteListPropertyListener();
		Model.INSTANCE.addPropertyChangeListener(Model.FAVOURITE_LIST_PROPERTY, myFavouriteListPropertyListener);
        
    }
    
    
    private void redraw() {

        if (myFavGrid.isEmpty()) {
            myFavInstructionsLabel.setText("You don't have any favourites yet. Click the \"Choose Stops\" tab above to pick a different route or stop.");                
            myFavInstructionsLabel.setVisible(true);                
        } else {
            myFavInstructionsLabel.setVisible(false);                
        }
        
    }


	@Override
	public void closeNow() {
		Model.INSTANCE.removePropertyChangeListener(Model.FAVOURITE_LIST_PROPERTY, myFavouriteListPropertyListener);
	}
    
    
}
