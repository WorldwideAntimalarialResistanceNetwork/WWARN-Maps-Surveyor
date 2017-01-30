package org.wwarn.surveyor.client.util;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2015 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.QueryResult;
import org.wwarn.localforage.client.LocalForage;
import org.wwarn.localforage.client.LocalForageCallback;

import java.util.Objects;

/**
 * Utility class for offline storage, only works in recent browser, that support GetRandomValues, see isRecentBrowser check.
 */
public class OfflineStorageUtil<T> {
    private final String uniqueKey;
    private final Class<T> clazz;
    private static LocalForage offlineDataStore = new LocalForage();
    private SerializationUtil serializationUtil = new SerializationUtil();

    public static native boolean isRecentBrowser()/*-{
        // ios version 7 seems to use a buggy or slow localStorage implementation works fine in 6, 5.1, 8
        if (/iP(hone|od|ad)/.test($wnd.navigator.platform)) {
            // supports iOS 2.0 and later: <http://bit.ly/TJjs1V>
            if($wnd.MSStream){
                // may detect some windows phones as well
                // https://msdn.microsoft.com/en-us/library/hh869301(v=vs.85).aspx
                return false;
            }
            var v = ($wnd.navigator.appVersion).match(/OS (\d+)_(\d+)_?(\d+)?/);
            var test = [parseInt(v[1], 10), parseInt(v[2], 10), parseInt(v[3] || 0, 10)];
            if (test[0] == 7) {
                return true;
            }
        }

        // We know that serialisation is slow in ie10 or below, too slow to be usable, so we disable this
        // We use GetRandomValues http://caniuse.com/#feat=getrandomvalues to determine if browser is recent,
        // as it is only support in IE 11 and above
        if (typeof $wnd.crypto === "undefined" || $wnd.crypto === null || typeof $wnd.crypto.getRandomValues === "undefined") {
            return false;
        }
        return true;
    }-*/;

    public OfflineStorageUtil(Class<T> clazz,String uniqueKey) {
        if(StringUtils.isEmpty(uniqueKey)){
            throw new IllegalArgumentException("key must be set");
        }
        Objects.requireNonNull(clazz);
        this.clazz = clazz;
        this.uniqueKey = uniqueKey;
    }

    public void removeItem(String key, final Runnable onComplete){
        offlineDataStore.removeItem(key, new LocalForageCallback() {
            @Override
            public void onComplete(boolean error, Object o) {
                if(error){ throw new IllegalStateException("unexpected error while removing time");}
                onComplete.run();
            }
        });
    }

    public void fetch(final AsyncCommand asyncCommand) {
        if(!isRecentBrowser()){  asyncCommand.failure(); return; }
        // using schema uniqueID to fetch queryResult
        offlineDataStore.getItem(uniqueKey, new LocalForageCallback<String>() {
            @Override
            public void onComplete(boolean error, String storedString) {
                if (!error && storedString != null) {
                    final T queryResult = serializationUtil.deserialize(clazz, storedString);
                    if(queryResult != null) {
                        asyncCommand.success(queryResult); return;
                    }
                }
                asyncCommand.failure(); return;
            }
        });
    }

    public void store(@NotNull final T object, final AsyncCommand asyncCommand) {
        if(!isRecentBrowser()) return;
        // using schema uniqueID to fetch queryResult
        Objects.requireNonNull(object);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                final String queryResultSerialisedString = serializationUtil.serialize(clazz, object); // can be very slow esp in IE!
                scheduleStorageOfItem(queryResultSerialisedString, asyncCommand, object);
            }
        });
    }

    private void scheduleStorageOfItem(final String queryResultSerialisedString, final AsyncCommand asyncCommand, @NotNull final T item) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                offlineDataStore.setItem(uniqueKey, queryResultSerialisedString, new LocalForageCallback<String>() {
                    @Override
                    public void onComplete(boolean error, String storedString) {
                        if (!error && storedString != null) {
                            asyncCommand.success(item);
                        } else asyncCommand.failure();
                    }
                });
            }
        });
    }

    public interface AsyncCommand<T> {
        /**
         * Success only if result was found
         * @param objectToStore
         */
        void success(@NotNull T objectToStore);

        /**
         * Failure if object not found or was null
         */
        void failure();
    }
}
