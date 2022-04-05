/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea;

import ch.vorburger.hotea.watchdir.DirectoryWatcher;
import java.io.Closeable;
import java.util.List;

/**
 * Hot Chai Tea Class Loader.
 *
 * @author Michael Vorburger
 */
public interface HotClassLoader extends Closeable {

    /**
     * Do NOT "keep" the ClassLoader returned by this without handling its
     * dynamic replacement through a HotClassLoaderListener - it will keep
     * changing!
     */
    ClassLoader getCurrentClassLoader();

    List<HotClassLoader.Listener> getListeners();

    interface Listener {

        /**
         * Notification of new ClassLoader availability event.
         *
         * @param newClassLoader
         * @throws Throwable if anything went wrong - the caller will log this
         */
        void onReload(ClassLoader newClassLoader) throws Throwable;
    }

    interface ExceptionHandler extends DirectoryWatcher.ExceptionHandler {
         @Override void onException(Throwable t);
    }

    @Override void close(); // do NOT throws (IO)Exception

}
