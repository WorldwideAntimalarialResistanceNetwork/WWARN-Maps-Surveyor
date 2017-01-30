package org.wwarn.localforage.client;

public interface LocalForageCallback<T> {
	void onComplete(boolean error, T value);
}
