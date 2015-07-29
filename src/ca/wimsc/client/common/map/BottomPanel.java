package ca.wimsc.client.common.map;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.wimsc.client.common.model.MapDataController;
import ca.wimsc.client.common.util.Common;
import ca.wimsc.client.common.util.IAsyncListener;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class BottomPanel extends DockLayoutPanel implements IAsyncListener {

    private static final int MODE_WIDTH = 50;
    public static final int BOTTOM_PANEL_HEIGHT = 21;
    private Label myUpdateLabel;

    public BottomPanel() {
        super(Unit.PX);

        addStyleName("bottomPanel");
        
        MapDataController.INSTANCE.addAsyncListeners(this);

        String hostname = Location.getHostName();
        String port = Location.getPort();

        StringBuilder prefix = new StringBuilder("http://");
        prefix.append(hostname);
        if (port != null && !"".equals(port) && !"80".equals(port)) {
            prefix.append(":");
            prefix.append(port);
        }

        Map<String, List<String>> params = new HashMap<String, List<String>>(Location.getParameterMap());
        params.put("mobile", Arrays.asList(new String[] { "1" }));
        boolean first = true;
        for (String nextKey : params.keySet()) {
            for (String nextValue : params.get(nextKey)) {
                if (first) {
                    first = false;
                    prefix.append("?");
                } else {
                    prefix.append("&");
                }
                prefix.append(nextKey).append("=").append(nextValue);
            }
        }

        prefix.append(Location.getHash());
        String mobileLink = prefix.toString();
        String normalLink = mobileLink.replace("mobile=1", "mobile=0");
        String htmlLink = mobileLink.replace("mobile=1", "mobile=2");

        if (Common.isRunningMobile()) {

            Label mobile = new Label("Mobile");
            addWest(mobile, MODE_WIDTH);

            Anchor normal = new Anchor("Normal", normalLink);
            addWest(normal, MODE_WIDTH);

        } else {

            Anchor mobile = new Anchor("Mobile", mobileLink);
            addWest(mobile, MODE_WIDTH);

            Label normal = new Label("Normal");
            addWest(normal, MODE_WIDTH);

        }

        Anchor mobile = new Anchor("HTML", htmlLink);
        addWest(mobile, MODE_WIDTH);

        if (Common.isRunningMobile() == false) {

            HTML openSourceLink = new HTML("This project is <a href='http://code.google.com/p/whereismystreetcar/'>Open Source</a>");
            openSourceLink.addStyleName("openSourceLink");
            addEast(openSourceLink, 180);

            addEast(new HTML(
                    "<iframe src=\"http://www.facebook.com/plugins/like.php?href=http%3A%2F%2Fwww.facebook.com%2Fpages%2FWhere-is-my-Streetcar%2F120199051373266&amp;layout=button_count&amp;show_faces=true&amp;width=80&amp;action=like&amp;font=trebuchet+ms&amp;colorscheme=light&amp;height=21\" scrolling=\"no\" frameborder=\"0\" style=\"border:none; overflow:hidden; width:80px; height:21px;\" allowTransparency=\"true\"></iframe>"),
                    80);

            addEast(new HTML("<a href='http://www.facebook.com/pages/Where-is-my-Streetcar/120199051373266' target='_blank'><img src='images/facebook.jpg' border='0' style='margin-top: 4px;'/></a>"), 15);
            
            addEast(new HTML(
            "<a href='http://twitter.com/wimsc' target='_blank'><img src='images/twitter.gif' height='14' width='14' border='0' style='margin-top: 4px;'/></a>"),
            17);
            
        }

        myUpdateLabel = new Label();
        myUpdateLabel.addStyleName("onTop");
        myUpdateLabel.addStyleName("bottomUpdateLabel");
        addWest(myUpdateLabel, 106);

    }

    @Override
    public void startLoading() {
        // nothing
    }

    @Override
    public void finishedLoading() {
        myUpdateLabel.setText("Updated: " + DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT).format(new Date()));
    }

}
