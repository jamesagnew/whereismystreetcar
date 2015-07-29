package ca.wimsc.client.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.wimsc.client.common.model.NearbyStop;
import ca.wimsc.client.common.util.HistoryUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

public class NearbyStopSelectionPanel extends FormPanel {

    private FlexTable myGrid;
    private String myOpenDirectionTag = null;
    private String myOpenRouteTag = null;
    private Integer myOpenRowIndex = null;
    private List<RadioButton> myRadioButtons = new ArrayList<RadioButton>();
    private List<NearbyStop> myResults;

    public NearbyStopSelectionPanel() {
        myGrid = new FlexTable();
        this.add(myGrid);
    }

    /**
     * Sets or updates the results displayed in this table
     */
    public void setResults(List<NearbyStop> theResults) {
        myResults = new ArrayList<NearbyStop>(theResults);
        Collections.sort(myResults, new MyNearbyStopRouteAndDirectionComparator());

        while (myGrid.getRowCount() > 0) {
            myGrid.removeRow(0);
        }
        
        int nextRow = 0;
        String lastRouteTag = null;
        String lastDirTag = null;
        myRadioButtons.clear();
        for (NearbyStop nearbyStop : myResults) {
            boolean sameRoute = nearbyStop.getRouteTag().equals(lastRouteTag);
            if (sameRoute) {
                if (nearbyStop.getDirectionTag().equals(lastDirTag)) {
                    continue;
                }
            }

            if (!sameRoute) {
                Label routeLabel = new Label(nearbyStop.getRouteTitle());
                routeLabel.addStyleName("nearbySelectorRoute");
                myGrid.setWidget(nextRow, 0, routeLabel);
            }

            lastRouteTag = nearbyStop.getRouteTag();
            lastDirTag = nearbyStop.getDirectionTag();

            RadioButton radioButton = new RadioButton("routeDir", nearbyStop.getDirectionTitle());
            myRadioButtons.add(radioButton);
            radioButton.addValueChangeHandler(new MyRadioButtonValueChangeHandler(nextRow, lastRouteTag, lastDirTag));
            myGrid.setWidget(nextRow, 1, radioButton);

            if (lastDirTag.equals(myOpenDirectionTag) && lastRouteTag.equals(myOpenRouteTag)) {
                radioButton.setValue(true, false);
                showResultsRow(nextRow, lastRouteTag, lastDirTag);
                nextRow++;
            }
            
            nextRow++;
        }

    }

    private final class MyNearbyStopRouteAndDirectionComparator implements Comparator<NearbyStop> {
        @Override
        public int compare(NearbyStop theO1, NearbyStop theO2) {
            int retVal = theO1.getRouteTag().compareTo(theO2.getRouteTag());
            if (retVal == 0) {
                retVal = theO1.getDirectionTitle().compareTo(theO2.getDirectionTitle());
            }
            return retVal;
        }
    }

    public class MyRadioButtonValueChangeHandler implements ValueChangeHandler<Boolean> {

        private String myDirTag;
        private String myRouteTag;
        private int myRowIndex;

        public MyRadioButtonValueChangeHandler(int theRowIndex, String theRouteTag, String theDirTag) {
            myRowIndex = theRowIndex;
            myRouteTag = theRouteTag;
            myDirTag = theDirTag;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
            if (theEvent.getValue() != Boolean.TRUE) {
                return;
            }

            for (RadioButton next : myRadioButtons) {
                if (next != theEvent.getSource()) {
                    next.setValue(false, false);
                }
            }

            if (myOpenRowIndex != null) {
                myGrid.removeRow(myOpenRowIndex);
            }
            
            int rowIndex = myRowIndex;
            String dirTag = myDirTag;
            String routeTag = myRouteTag;

            showResultsRow(rowIndex, routeTag, dirTag);
            
        }


    }

    private void showResultsRow(int theRowIndex, String theRouteTag, String theDirTag) {
        myOpenRowIndex = theRowIndex + 1;
        myOpenDirectionTag = theDirTag;
        myOpenRouteTag = theRouteTag;
        
        myGrid.insertRow(myOpenRowIndex);
        
        myGrid.getFlexCellFormatter().setColSpan(myOpenRowIndex, 0, 2);
        myGrid.getFlexCellFormatter().setHorizontalAlignment(myOpenRowIndex, 0, HasHorizontalAlignment.ALIGN_CENTER);
        
        FlowPanel panel = new FlowPanel();
        myGrid.setWidget(myOpenRowIndex, 0, panel);
        
        for (final NearbyStop next : myResults) {
            if (next.getRouteTag().equals(myOpenRouteTag)) {
                if (next.getDirectionTag().equals(myOpenDirectionTag)) {
                    
                	Hyperlink label = new Hyperlink(next.getTitle(), HistoryUtil.getTokenForNewStop(next.getRouteTag(), next.getStopTag()));
                    label.addStyleName("nearbySelectorStop");
					panel.add(label);
                }
            }
        }
    }
    
}
