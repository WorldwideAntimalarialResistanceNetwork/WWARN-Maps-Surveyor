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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.seanchenxi.gwt.storage.client.serializer.StorageSerializer;

/**
 * Supports serialisation of GWT Java objects to string and vice versa
 * Uses gwt-storage codebase to facilitate this, reuses GWT RPC mechanism
 */
public class SerializationUtil {

    private static final StorageSerializer storageSerializer = GWT.create(StorageSerializer.class);

    public <T> String serialize(Class<T> aClass, T object1){
        String seralisedOutput = "";
        try {
            seralisedOutput = storageSerializer.serialize(aClass, object1);
        } catch (SerializationException e) {
            throw new IllegalStateException("unable to serialise", e);
        }
        return seralisedOutput;
    }

    /**
     * Returns an object from a serialised string
     * @param clazz Class.name value
     * @param stringContainingSerialisedInput
     * @param <T>
     * @return
     */
    public <T> T deserialize(Class<T> clazz, String stringContainingSerialisedInput) {
        T instance;
        try {
            instance = storageSerializer.deserialize(clazz, stringContainingSerialisedInput);
        } catch (SerializationException e) {
            throw new IllegalStateException("unable to serialise", e);
        }
        return instance;
    }
}
