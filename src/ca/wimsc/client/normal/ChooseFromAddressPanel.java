package ca.wimsc.client.normal;

import java.util.List;

import ca.wimsc.client.common.map.layers.SelectedStopLayer;
import ca.wimsc.client.common.model.NearbyStop;
import ca.wimsc.client.common.model.NearbyStopList;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.GeocoderUtil;
import ca.wimsc.client.common.util.HistoryUtil;
import ca.wimsc.client.common.widgets.HtmlBr;
import ca.wimsc.client.common.widgets.HtmlH1;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.geocoder.Geocoder;
import com.google.gwt.maps.client.geocoder.GeocoderCallback;
import com.google.gwt.maps.client.geocoder.GeocoderRequest;
import com.google.gwt.maps.client.geocoder.HasGeocoderResult;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ChooseFromAddressPanel extends ScrollPanel {

    private FlowPanel myPotentialMatchesPanel;
    private String myCurrentAddressText;
    private MapOuterPanelNormal myContainerPanel;

    public ChooseFromAddressPanel(MapOuterPanelNormal theContainerPanel) {
        myContainerPanel = theContainerPanel;
        
        myPotentialMatchesPanel = new FlowPanel();
        this.add(myPotentialMatchesPanel);

        // myAddressLabel = new Label();
    }

    public void setAddressText(String theAddressText) {
        if (theAddressText.toLowerCase().indexOf("toront") == -1) {
            theAddressText += ", Toronto ON";
        }

        if (myCurrentAddressText != null && myCurrentAddressText.equals(theAddressText)) {
            return;
        }
        myCurrentAddressText = theAddressText;

        myPotentialMatchesPanel.clear();

        myPotentialMatchesPanel.add(new HtmlH1("Choose an Address: " + theAddressText));

        final Label loadingLabel = new Label("Loading...");
        myPotentialMatchesPanel.add(loadingLabel);
        
        final FlowPanel responsesPanel = new FlowPanel();
        myPotentialMatchesPanel.add(responsesPanel);

        final FlexTable responsesGrid = new FlexTable();
        responsesPanel.add(responsesGrid);

        GeocoderRequest request = new GeocoderRequest();
        request.setAddress(theAddressText);
        request.setLatLng(new LatLng(43.716589d, -79.340686d));
        // request.setLanguage("en");

        GeocoderCallback callback = new GeocoderCallback() {

            @Override
            public void callback(List<HasGeocoderResult> theResponses, String theStatus) {
                myPotentialMatchesPanel.remove(loadingLabel);
                GWT.log("Geocoder returned status: " + theStatus);

                if (!"OK".equals(theStatus)) {
                    Window.alert("Google maps returned status: " + theStatus);
                    return;
                }
                
                int row = 0;
                for (final HasGeocoderResult nextResult : theResponses) {

                    // Map
                    MapOptions options = new MapOptions();
                    options.setCenter(nextResult.getGeometry().getLocation());
                    options.setZoom(13);
                    options.setMapTypeControl(false);
                    options.setNavigationControl(false);
                    options.setMapTypeId(new MapTypeId().getRoadmap());
                    options.setDraggable(false);
                    options.setScrollwheel(false);

                    MapWidget mapWidget = new MapWidget(options);
                    mapWidget.setPixelSize(100, 100);

                    MarkerOptions moptions = new MarkerOptions();
                    moptions.setPosition(nextResult.getGeometry().getLocation());
                    moptions.setClickable(true);
                    moptions.setVisible(true);
                    // moptions.setIcon(IconFactory.getMarkerImage(DirectionEnum.WESTBOUND));
                    Marker marker = new Marker(moptions);
                    marker.setMap(mapWidget.getMap());

                    responsesGrid.setWidget(row, 0, mapWidget);
                    // mapWidget.fitBounds(nextResult.getGeometry().getViewport());

                    // Label
                    HTML nextAddressLabel = new HTML();
                    final String addressDesc = GeocoderUtil.toTextDescription(nextResult);
                    nextAddressLabel.setHTML(addressDesc);

                    FlowPanel flowPanel = new FlowPanel();
                    flowPanel.add(nextAddressLabel);
                    flowPanel.add(new HtmlBr());

                    // Button
                    final Button button = new Button("Find Stops Near Here");
                    button.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent theEvent) {
                            button.setText("Loading...");
                            button.setEnabled(false);
                            findStopsNear(addressDesc, nextResult.getGeometry().getLocation());
                        }

                    });
                    flowPanel.add(button);

                    responsesGrid.setWidget(row, 1, flowPanel);

                    responsesGrid.getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
                    responsesGrid.getFlexCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
                    responsesGrid.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
                    responsesGrid.getFlexCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);

                    row++;
                }
            }

        };

        GWT.log("Invoking geocoder for: " + theAddressText);
        new Geocoder().geocode(request, callback);

    }

    private void findStopsNear(final String theAddressDesc, final HasLatLng theLocation) {

        Common.SC_SVC_GNB.getNearbyStops(theAddressDesc, theLocation.getLatitude(), theLocation.getLongitude(), 10, new AsyncCallback<NearbyStopList>() {

            @Override
            public void onSuccess(NearbyStopList theResult) {
                myPotentialMatchesPanel.clear();
                myPotentialMatchesPanel.add(new HtmlH1("Stops near: " + theAddressDesc));

                final FlowPanel responsesPanel = new FlowPanel();
                myPotentialMatchesPanel.add(responsesPanel);

                final FlexTable responsesGrid = new FlexTable();
                responsesPanel.add(responsesGrid);

                int row = 0;
                for (final NearbyStop nextStop : theResult.getNearbyStops()) {

                    // Map
                    MapOptions options = new MapOptions();
                    options.setCenter(nextStop.getLocation());
                    options.setZoom(13);
                    options.setMapTypeControl(false);
                    options.setNavigationControl(false);
                    options.setMapTypeId(new MapTypeId().getRoadmap());
                    options.setDraggable(false);
                    options.setScrollwheel(false);

                    MapWidget mapWidget = new MapWidget(options);
                    mapWidget.setPixelSize(100, 100);

                    MarkerOptions moptions = new MarkerOptions();
                    moptions.setPosition(nextStop.getLocation());
                    moptions.setClickable(true);
                    moptions.setVisible(true);
                    moptions.setIcon(SelectedStopLayer.createStopMarkerImage());
                    Marker marker = new Marker(moptions);
                    marker.setMap(mapWidget.getMap());

                    responsesGrid.setWidget(row, 0, mapWidget);

                    int dist = (int) (nextStop.distanceFromInKms(theLocation.getLatitude(), theLocation.getLongitude()) * 1000);
                    
                    // Label
                    HTML nextAddressLabel = new HTML();
                    StringBuilder addressDescBuilder = new StringBuilder();
                    addressDescBuilder.append(nextStop.getRouteTitle()).append(" ");
                    addressDescBuilder.append(nextStop.getDirectionTitle()).append("<br>");
                    addressDescBuilder.append(nextStop.getTitle());
                    final String addressDesc = addressDescBuilder.toString() + ", " + dist + "m away";
                    nextAddressLabel.setHTML(addressDesc);

                    FlowPanel flowPanel = new FlowPanel();
                    flowPanel.add(nextAddressLabel);
                    flowPanel.add(new HtmlBr());

                    // Button
                    Button button = new Button("Choose Stop");
                    button.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent theEvent) {
                            myContainerPanel.showMap();
                            HistoryUtil.setStop(nextStop.getRouteTag(), nextStop.getStopTag());
                            
                            /* 
                             * TODO: For unknown reasons, having a bunch of small maps on the screen
                             * like we did above seems to cause the main map to go haywire once it's
                             * back on the screen. It seems like it it keeping the tiny viewport of the
                             * little windows we show on this UI, so I'm guessing there is some resource
                             * that isn't being cleaned up.. So for now we just reload the page. Hopefully
                             * one day Google will update the official GWT v3 maps project and this will
                             * go away. 
                             */
                            Window.Location.reload();
                        }

                    });
                    flowPanel.add(button);

                    responsesGrid.setWidget(row, 1, flowPanel);

                    responsesGrid.getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
                    responsesGrid.getFlexCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
                    responsesGrid.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
                    responsesGrid.getFlexCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);

                    row++;
                }
            }

            @Override
            public void onFailure(Throwable theCaught) {
                GWT.log("Failed to find nearby stops", theCaught);
                Window.alert("Failed to find nearby stops" + theCaught.getMessage());
            }
        });

    }

}
