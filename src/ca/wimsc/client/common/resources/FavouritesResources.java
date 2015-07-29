package ca.wimsc.client.common.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface FavouritesResources extends ClientBundle {

    public static final FavouritesResources INSTANCE = GWT.create(FavouritesResources.class);

    @Source("/images/blank_16x16.png")
    ImageResource blank();

    @Source("/images/fav_push_pin_16x16.png")
    ImageResource favHeart();

    @Source("/images/fav_push_pin_16x16_on.png")
    ImageResource favHeartOn();

    @Source("/images/fav_push_pin_16x16_hover.png")
    ImageResource favHeartHover();

    @Source("/images/fav_del_16x16.png")
    ImageResource favDel();

    @Source("/images/fav_del_16x16_hover.png")
    ImageResource favDelHover();

}
