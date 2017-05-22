package org.wwarn.surveyor.client.bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Created by suay on 25/03/14.
 */
public interface Resources extends ClientBundle{
        public static final Resources INSTANCE =  GWT.create(Resources.class);

        @Source("img/ajax-loader.gif") public ImageResource loader();
        @Source("img/feedback.png") public ImageResource feedback();

}
