package org.wwarn.surveyor.server.core;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FileChangeMonitorTest {

    private FileChangeMonitor fileChangeMonitor;
    private Path tempFile;
    public static boolean hasBeenCalled = false;
    public FileChangeMonitorTest that = this;
    private CountDownLatch fileChangedEvent;
    private CountDownLatch fileListenerLoadEvent;

    @Before
    public void setUp() throws Exception {
        tempFile = Files.createTempFile("tmp", ".txt");
        fileChangedEvent = new CountDownLatch(1);
        fileListenerLoadEvent = new CountDownLatch(1);
        fileChangeMonitor = FileChangeMonitor.getInstance();
        fileChangeMonitor.init(tempFile, fileListenerLoadEvent, fileChangedEvent);
        registerObserver();
    }

    private void simulateFileWrites() throws IOException {
        try(
            final FileWriter fileWriter = new FileWriter(tempFile.toFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(tempFile.toFile());
        ){
            printWriter.write("test");
            System.out.println("test");
        }

    }

    private void registerObserver() {
        fileChangeMonitor.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                assertNotNull(o);
                assertNotNull(arg);
                hasBeenCalled = true;
            }
        });
    }

//    @Test
//    public void testFileChangeEvent() throws InterruptedException, IOException {
//        final int timeoutInMilliseconds = 15 * 1000;
//        fileListenerLoadEvent.await(timeoutInMilliseconds, TimeUnit.MILLISECONDS); // wait until file listener is setup
//        simulateFileWrites(); // write to file
//        fileChangedEvent.await(timeoutInMilliseconds, TimeUnit.MILLISECONDS); // wait for thread to detect change
//        assertTrue(hasBeenCalled);
//    }
}

