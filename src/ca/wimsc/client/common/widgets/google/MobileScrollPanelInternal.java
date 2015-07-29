package ca.wimsc.client.common.widgets.google;

/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A panel implementation that behaves like a {@link com.google.gwt.user.client.ui.ScrollPanel ScrollPanel} by default,
 * but switches to a manual drag-scroll implementation on browsers that support touch events.
 * 
 * TODO(jgw): Implement scroll events. TODO(jgw): This is widgetry that doesn't belong in this package. TODO(jgw):
 * Consider rolling it directly into ScrollPanel. Maybe.
 */
class MobileScrollPanelInternal extends SimplePanel implements RequiresResize {

    private Element container;
    private Scroller scroller;

    public MobileScrollPanelInternal() {
        container = Document.get().createDivElement();
        getElement().appendChild(container);
        getElement().getStyle().setOverflow(Overflow.AUTO);

        // Only turn on the touch-scroll implementation if we're on a touch device.
        if (TouchHandler.supportsTouch()) {
            scroller = new Scroller(this, getElement(), container);
            scroller.setMomentum(true);
        }
    }

    @Override
	public void onResize() {
    }

    public HandlerRegistration addScrollHandler(ScrollHandler handler) {
        // TODO: this isn't the right way to do this, should just have scroller fire a dom event on me
        if (scroller != null) {
            scroller.addScrollHandler(handler);
        }
        
        return addDomHandler(handler, ScrollEvent.getType());
    }

    public boolean hasContentAbove() {
//        if (scroller != null) {
//            return scroller.getContentOffsetX() > 0;
//        } else {
            return getElement().getScrollTop() > 0;
//        }
    }

    public boolean hasContentBelow() {
        int scrollTop = getScrollTop();
        int contentHeight = container.getOffsetHeight();
        int widgetHeight = getOffsetHeight();

        int diff = contentHeight - scrollTop;
        return diff > widgetHeight;
    }

    /**
     * Sets the horizontal scroll position.
     * 
     * @param left
     *            the horizontal scroll position, in pixels
     */
    public void setScrollLeft(int left) {
//        if (scroller != null) {
//            scroller.setContentOffset(left, scroller.getContentOffsetY());
//        } else {
            getElement().setScrollLeft(left);
//        }
    }

    /**
     * Sets the vertical scroll position.
     * 
     * @param top
     *            the vertical scroll position, in pixels
     */
    public void setScrollTop(int top) {
//        if (scroller != null) {
//            scroller.setContentOffset(scroller.getContentOffsetX(), top);
//        } else {
            getElement().setScrollTop(top);
//        }
    }

    public int getScrollTop() {
//        if (scroller != null) {
//            return (int) scroller.getContentOffsetY();
//        } else {
            return getElement().getScrollTop();
//        }
    }

    @Override
    protected com.google.gwt.user.client.Element getContainerElement() {
        return container.cast();
    }

    public void ensureVerticalPointsShowing(int theYtop, int theYbottom) {
        if (getScrollTop() > theYtop) {
            setScrollTop(theYtop);
        }
        
        int scrollTop = getScrollTop();
        int widgetHeight = getOffsetHeight();
        int bottomShowing = widgetHeight + scrollTop;
        if (bottomShowing < theYbottom) {
            scrollTop = theYbottom - widgetHeight;
            setScrollTop(scrollTop);
        }
    }
}