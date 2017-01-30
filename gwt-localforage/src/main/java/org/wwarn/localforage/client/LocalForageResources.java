package org.wwarn.localforage.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface LocalForageResources extends ClientBundle {

	public LocalForageResources INSTANCE = GWT.create(LocalForageResources.class);
	
	@Source("localforage.min.js")
	TextResource js();

}

