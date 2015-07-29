package ca.wimsc.client.common.widgets;

import com.google.gwt.user.client.ui.HTML;

/**
 * Special Label which makes onAttach and onDetach public. This is basically
 * for use as a hack to allow a GWT label to be created which is not going
 * to be added as a child of another GWT managed widget 
 */
public class NonManagedLabel extends HTML {

    @Override
    public void onAttach() {
        super.onAttach();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
