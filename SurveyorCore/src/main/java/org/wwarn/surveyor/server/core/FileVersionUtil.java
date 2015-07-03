package org.wwarn.surveyor.server.core;

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

import org.jetbrains.annotations.NotNull;
import org.wwarn.mapcore.client.utils.StringUtils;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Takes a file and returns a version no of the file
 * Calculate version of a file, based on stored last modified
 */
public class FileVersionUtil {

    private static class FixedSizeCache extends LinkedHashMap<String, String> {
        private static final int MAX_ENTRIES = 10;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_ENTRIES;
        }
    }

    private static FixedSizeCache fixedSizeCache = new FixedSizeCache();

    public FileVersionUtil() {
    }


    /**
     * Gets md5 hashcode from file
     * @param file
     * @return
     */
    public String calculateVersionFrom(@NotNull File file){
        Objects.requireNonNull(file, "file argument cannot be null");
        //optimise by store hash against file modified time, on subsequent calls, check file modified time first to determine file and time is present and return previous data.
        final long lastModified = file.lastModified();
        final String key = file.getAbsolutePath() + lastModified;
        final String storedHashValue = fixedSizeCache.get(key);
        if(!StringUtils.isEmpty(storedHashValue)){
            return storedHashValue;
        }
        MessageDigest md = getDigest();
        try(DigestInputStream digestInputStream = new DigestInputStream(Files.newInputStream(file.toPath()), md)){
            while(digestInputStream.read() != -1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final byte[] digest = md.digest();
        final String digestHex = convertBinaryDigestToHexStringDigest(digest);
        fixedSizeCache.put(key, digestHex);
        return digestHex;
    }

    @NotNull
    private String convertBinaryDigestToHexStringDigest(byte[] digest) {
        return (new HexBinaryAdapter()).marshal(digest);
    }

    static MessageDigest messageDigest;
    public MessageDigest getDigest() {
        try {

            if (messageDigest == null) {
                messageDigest = MessageDigest.getInstance("MD5");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return messageDigest;
    }
}
