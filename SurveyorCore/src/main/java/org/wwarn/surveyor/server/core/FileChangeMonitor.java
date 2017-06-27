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

import com.google.gwt.core.shared.GWT;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 * Listen for changes in local indexed file and inform parent class of changes
 * Takes a while to setup file changed listener
 */
public class FileChangeMonitor extends Observable {
    private static Logger logger = Logger.getLogger("SurveyorCore.FileChangeMonitor");

    private WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    private Path monitoredFile;
    private FileChangeMonitor fileChangeMonitor = this;
    private boolean trace = false;

    protected FileChangeMonitor() {
    }

    protected static class Loader{
        static FileChangeMonitor INSTANCE = new FileChangeMonitor();
    }

    @Override
    public synchronized void addObserver(Observer o) {
        if(watcher == null) {
            logger.log(INFO, "FileChangeMonitor::addObserver + init/initSynchronous not yet called - ensure it is called with a path to file to observer, or there will be nothing to observe!");
        }
        super.addObserver(o);
    }

    /**
     * Thread safe lazy instantiation done by JVM, no explicit synchronisation
     * @return
     */
    public static FileChangeMonitor getInstance(){
        return Loader.INSTANCE;
    }

    public void init(Path monitoredFile) throws IOException {
        initNewThread(monitoredFile, new CountDownLatch(1), new CountDownLatch(1));
    }

    /**
     * Added countdown latches as a synchronization aid to allow better unit testing
     * Allows one or more threads to wait until a set of operations being performed in other threads completes,
     * @param monitoredFile
     * @param start calling start.await() waits till file listner is active and ready
     * @param stop calling stop.await() allows calling code to wait until a fileChangedEvent is processed
     * @throws IOException
     */
    protected void initNewThread(Path monitoredFile, CountDownLatch start, CountDownLatch stop) throws IOException {
        final Runnable watcher = initializeWatcherWithDirectory(monitoredFile, start, stop);
        final Thread thread = new Thread(watcher);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * A blocking method to start begin the monitoring of a directory, only exists on thread interrupt
     * @param monitoredFile
     * @throws IOException
     */
    public void initSynchronous(Path monitoredFile) throws IOException {
        final Runnable watcher = initializeWatcherWithDirectory(monitoredFile, new CountDownLatch(1), new CountDownLatch(1));
        watcher.run();
    }


    private Runnable initializeWatcherWithDirectory(Path monitoredFile, CountDownLatch start, CountDownLatch stop) throws IOException {
        if (!Files.isRegularFile(monitoredFile)) {
            throw new IllegalArgumentException("Input of type File expected");
        }
        final FileSystem fileSystem = FileSystems.getDefault();
        watcher = fileSystem.newWatchService();
        this.monitoredFile = monitoredFile;
        final Path monitoredFileParentDirectory = monitoredFile.getParent();
        register(monitoredFileParentDirectory);
        final Runnable watcher = new Watcher(start, stop);
        return watcher;
    }


    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
//        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        WatchKey key = dir.register(watcher, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE}, SensitivityWatchEventModifier.HIGH);
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
            //loop forever or until thread interrupted
            while(!Thread.currentThread().isInterrupted()){
                // wait for key to be signalled

                logger.log(INFO,"Watcher::processEvents"+ "Started the long blocking call");
                WatchKey key = watcher.take(); /* This call is blocking until events are present
                This can take a while complete,
                hence startSignal given only after this is loaded*/
                startSignal.countDown();
                logger.log(INFO,"Watcher::processEvents" + "Finished the long blocking call");


                Path dir = keys.get(key);
                if (dir == null) {
                    final String warningmsg = "WatchKey not recognized!!";
                    logger.log(SEVERE,"Watcher::processEvents", warningmsg);
                    throw new IllegalStateException(warningmsg);
                }

                // poll for file system events on the WatchKey
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

                    if(Files.exists(child) && Files.isSameFile(child, monitoredFile)){
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
                logger.log(INFO,"Watcher::run", "Entered run state");
                processEvents();
            } catch (IOException e) {
                logger.log(SEVERE,"Watcher::run I/O failure while call to processEvents", e);
                throw new IllegalStateException(e);
            } catch (InterruptedException e) {
                logger.log(SEVERE,"Watcher::run Threat interrupted exception", e);
                throw new IllegalStateException(e);
            }
        }
    }
}
