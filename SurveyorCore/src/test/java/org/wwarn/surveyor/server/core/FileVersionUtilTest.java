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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Basic test classes for file version
 */
public class FileVersionUtilTest {
    FileVersionUtil fileVersionUtil;

    @Before
    public void setUp() throws Exception {
        fileVersionUtil = new FileVersionUtil();
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() throws Exception {
        fileVersionUtil.calculateVersionFrom(null);
    }

    @Test
    public void testEmptyFile() throws Exception {
        final Path emptyFile = Files.createTempFile("emptyFile", ".txt");
        final File file = emptyFile.toFile();
        final String version = fileVersionUtil.calculateVersionFrom(file);
        assertNotNull(version);
        assertEquals("D41D8CD98F00B204E9800998ECF8427E",version);
        file.deleteOnExit();
    }

//    @Test
//    public void testFiftyMegFile() throws Exception {
//        final File file = new File("path to a big file");
//        final String version = fileVersionUtil.calculateVersionFrom(file);
//        assertNotNull(version);
//        assertEquals("F4B8B98AEFC2D73E42746EC1AB409448",version);
//    }
}