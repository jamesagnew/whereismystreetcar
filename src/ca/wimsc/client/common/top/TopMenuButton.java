package ca.wimsc.client.common.top;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;

public class TopMenuButton extends LayoutPanel {

	private HTML myLabel;
	private Image myImage;
	private boolean myOpened;

	public TopMenuButton() {
		this(true);
	}

	public TopMenuButton(boolean theShowDropdownButton) {
		this(theShowDropdownButton, "topMenuLink");
	}

	public TopMenuButton(boolean theShowDropdownButton, String theLinkStyle) {
		this((theShowDropdownButton ? "images/menu_button.png" : null), theLinkStyle);
	}

	public TopMenuButton(String theImageUrl, String theLinkStyle) {
		addStyleName("topMenuButton");

		myLabel = new HTML();
		myLabel.addStyleName(theLinkStyle);
		add(myLabel);
		setWidgetHorizontalPosition(myLabel, Alignment.BEGIN);

		if (theImageUrl != null) {
			myImage = new Image(theImageUrl);
			myImage.setVisible(false);
			add(myImage);
			setWidgetHorizontalPosition(myImage, Alignment.BEGIN);
		}
	}

	public void setOpened() {
		if (myOpened) {
			return;
		}

		addStyleName("topMenuButtonSelected");
		myLabel.removeStyleName("topMenuLink");
		myLabel.addStyleName("topMenuLinkOpened");

		myOpened = true;
	}

	public void setClosed() {
		if (!myOpened) {
			return;
		}

		removeStyleName("topMenuButtonSelected");
		myLabel.removeStyleName("topMenuLinkOpened");
		myLabel.addStyleName("topMenuLink");

		myOpened = false;
	}

	public void addClickHandler(ClickHandler theClickHandler) {
		myLabel.addClickHandler(theClickHandler);
		if (myImage != null) {
			myImage.addClickHandler(theClickHandler);
		}
	}

	public void setMenuHtml(final String theMenuText) {
		assert getParent() != null;
		int offsetWidth = getOffsetWidth();
		
		if (offsetWidth == 0) {
			new Timer() {
				@Override
				public void run() {
					setMenuHtml(theMenuText);
				}}.schedule(500);
		}

		myLabel.setHTML(theMenuText);

		if (myImage != null) {
			setWidgetLeftWidth(myLabel, 15, Unit.PX, offsetWidth - 14, Unit.PX);

			setWidgetLeftWidth(myImage, 0, Unit.PX, 14, Unit.PX);
			setWidgetTopHeight(myImage, 10, Unit.PX, 11, Unit.PX);
			myImage.setVisible(true);
		} else {

			setWidgetLeftWidth(myLabel, 0, Unit.PX, offsetWidth, Unit.PX);

		}

	}

}
