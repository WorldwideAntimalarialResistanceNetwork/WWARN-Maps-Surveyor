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

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Listen for changes in local indexed file and inform parent class of changes
 * Takes a while to setup file changed listener
 */
public class FileChangeMonitor extends Observable {
    private WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    private Path monitoredFile;
    private boolean trace = false;
    private FileChangeMonitor fileChangeMonitor = this;

    private FileChangeMonitor() {
    }

    private static class Loader{
        static FileChangeMonitor INSTANCE = new FileChangeMonitor();
    }

    /**
     * Thread safe lazy instantiation done by JVM, no explicit synchronisation
     * @return
     */
    public static FileChangeMonitor getInstance(){
        return Loader.INSTANCE;
    }

    public void init(Path monitoredFile) throws IOException {
        init(monitoredFile, new CountDownLatch(1), new CountDownLatch(1));
    }

    /**
     * Added countdown latches as a synchronization aid to allow better unit testing
     * Allows one or more threads to wait until a set of operations being performed in other threads completes,
     * @param monitoredFile
     * @param start calling start.await() waits till file listner is active and ready
     * @param stop calling stop.await() allows calling code to wait until a fileChangedEvent is processed
     * @throws IOException
     */
    protected void init(Path monitoredFile, CountDownLatch start, CountDownLatch stop) throws IOException {

        if(!Files.isRegularFile(monitoredFile)) {
            throw new IllegalArgumentException("Input of type File expected");
        }
//        start.countDown(); // start countdown
        final FileSystem fileSystem = FileSystems.getDefault();
        watcher = fileSystem.newWatchService();
        this.monitoredFile = monitoredFile;
        final Path monitoredFileParentDirectory = monitoredFile.getParent();
        register(monitoredFileParentDirectory);
        final Thread thread = new Thread(new Watcher(start, stop));
        thread.setDaemon(false);
        thread.start();
    }


    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }


    class Watcher implements Runnable {

        private final CountDownLatch stopSignal;
        private final CountDownLatch startSignal;
        public Watcher(CountDownLatch start, CountDownLatch stop) {
            startSignal = start;
            stopSignal = stop;
        }
        void processEvents() throws IOException, InterruptedException {

            while(!Thread.currentThread().isInterrupted()){
                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take(); /* This can take a while complete,
                    hence startSignal given only after this is loaded*/
                    startSignal.countDown();
                } catch (InterruptedException x) {
                    throw x;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    throw new IllegalStateException("WatchKey not recognized!!");
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    // TBD - provide example of how OVERFLOW event is handled
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    if(Files.isSameFile(child, monitoredFile)){
                        //set event change
                        fileChangeMonitor.setChanged();
                        fileChangeMonitor.notifyObservers(event);
                        stopSignal.countDown();
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        }
        @Override
        public void run() {
            try {
                processEvents();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
