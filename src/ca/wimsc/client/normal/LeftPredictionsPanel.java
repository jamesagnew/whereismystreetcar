package ca.wimsc.client.normal;

import ca.wimsc.client.common.map.PredictionsPanel;
import ca.wimsc.client.common.model.IModelListenerAsync;
import ca.wimsc.client.common.model.Model;
import ca.wimsc.client.common.model.PredictionsList;

import com.google.gwt.user.client.ui.ScrollPanel;

public class LeftPredictionsPanel extends ScrollPanel {

    private IModelListenerAsync<PredictionsList> myModelListenerAsync;
    private PredictionsPanel myPredictionsPanel;
    private String myStopTag;
    
    /**
     * Constructor
     */
    public LeftPredictionsPanel(String theStopTag) {
        myPredictionsPanel = new PredictionsPanel();
        setWidget(myPredictionsPanel);

        myStopTag = theStopTag;
        
        myModelListenerAsync = new MyModelListenerAsync();
        
        Model.INSTANCE.addPredictionListListener(myStopTag, myModelListenerAsync);
        
    }

    public void destroy() {
        Model.INSTANCE.removePredictionListListener(myStopTag, myModelListenerAsync);
    }
    
    private final class MyModelListenerAsync implements IModelListenerAsync<PredictionsList> {
        @Override
        public void objectLoaded(PredictionsList theObject, boolean theRequiredAsyncLoad) {
            myPredictionsPanel.updateList(theObject);
        }

        @Override
        public void startLoadingObject() {
            // nothing
        }
    }
    
}
