package com.example.client.impl;

import com.example.client.ConfigResources;
import org.wwarn.surveyor.client.mvp.ConfigLoader;

/**
 * Created by nigelthomas on 04/04/2014.
 */
public class ClientBundleConfigLoader implements ConfigLoader {

    public String getXMLConfig() {
        return ConfigResources.IMPL.config().getText();
    }

}
