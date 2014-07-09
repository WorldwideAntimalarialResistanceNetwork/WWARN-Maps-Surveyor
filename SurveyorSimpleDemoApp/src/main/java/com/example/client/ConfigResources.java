package com.example.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 *
 * User: nigel
 * Date: 30/07/13
 * Time: 16:00
 */
public interface ConfigResources extends ClientBundle {
    ConfigResources IMPL = (ConfigResources) GWT.create(ConfigResources.class);
    @Source("resources/config.xml")
    TextResource config();

}
