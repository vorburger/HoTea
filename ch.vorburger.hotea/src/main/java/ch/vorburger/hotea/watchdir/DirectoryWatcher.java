/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import java.io.Closeable;
import java.nio.file.Path;

/**
 * Watch a directory and be notified on your Listener on changes in it.
 * 
 * @author Michael Vorburger
 */
public interface DirectoryWatcher extends Closeable {

    public enum ChangeKind {
        MODIFIED, DELETED
    }

    interface Listener {
        /**
         * Listener for file change notifications.
         * @param path Path to what caused the change. Note that when watching directory trees, we get a notification of one file (or new/deleted directory) causing it, not the registered root directory.
         * @throws Throwable if anything went wrong
         */
        void onChange(Path path, ChangeKind changeKind) throws Throwable;
    }

    interface ExceptionHandler {
        void onException(Throwable t);
    }

    @Override String toString();

    @Override void close(); // do NOT throws (IO)Exception
}
