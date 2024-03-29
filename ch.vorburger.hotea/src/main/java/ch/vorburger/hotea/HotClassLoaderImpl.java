/*
 * Copyright (C) 2015 - 2022 by Michael Vorburger
 */
package ch.vorburger.hotea;

import ch.vorburger.fswatch.DirectoryWatcher;
import ch.vorburger.fswatch.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.fswatch.DirectoryWatcherBuilder;
import ch.vorburger.hotea.util.URLClassLoaderWithBetterMessage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// intentionally package local, for now
class HotClassLoaderImpl implements HotClassLoader {
    private final static Logger log = LoggerFactory.getLogger(HotClassLoaderImpl.class);

    private final URL[] urls;
    private final List<HotClassLoader.Listener> listeners;
    private final ExceptionHandler exceptionHandler;
    private final List<DirectoryWatcher> watchers;
    private final ClassLoader parentClassLoader;

    private URLClassLoader currentClassLoader;

    protected HotClassLoaderImpl(List<Path> classpathEntries, ClassLoader parentClassLoader, List<HotClassLoader.Listener> listeners,
            ExceptionHandler exceptionHandler) throws IOException {
        this.listeners = Collections.unmodifiableList(new ArrayList<>(listeners));
        this.exceptionHandler = exceptionHandler;
        this.parentClassLoader = parentClassLoader;

        urls = getURLs(classpathEntries);
        currentClassLoader = getNewClassLoader();
        watchers = setUpFileChangeListeners(classpathEntries);
    }

    @Override public ClassLoader getCurrentClassLoader() {
        return currentClassLoader;
    }

    @Override public void close() {
        // The order here is important - first we close all the Watchers, then the ClassLoader.
        for (DirectoryWatcher directoryWatcher : watchers) {
            directoryWatcher.close();
        }
        if (currentClassLoader != null) {
            try {
                currentClassLoader.close();
            } catch (IOException e) {
                log.error("close() failed due to IOException from URLClassLoader close()", e);
            }
        }
    }

    protected URL[] getURLs(List<Path> classpathEntries) {
        URL[] urls = new URL[classpathEntries.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = getURLFromPath(classpathEntries.get(i));
        }
        return urls;
    }

    protected URLClassLoader getNewClassLoader() {
        if (parentClassLoader == null) {
            return new URLClassLoaderWithBetterMessage(urls); // URLClassLoader.newInstance(urls);
        }
        return new URLClassLoaderWithBetterMessage(urls, parentClassLoader); // URLClassLoader.newInstance(urls, parentClassLoader)
    }

    protected void notifyListeners() {
        for (HotClassLoader.Listener listener : listeners) {
            try {
                listener.onReload(currentClassLoader);
            } catch (Throwable e) {
                exceptionHandler.onException(e);
            }
        }
    }

    protected List<DirectoryWatcher> setUpFileChangeListeners(List<Path> classpathEntries) throws IOException {
        ArrayList<DirectoryWatcher> newWatchers = new ArrayList<>(classpathEntries.size());

        DirectoryWatcher.Listener dirListener = (path, changeKind) -> {
            URLClassLoader oldClassLoader = currentClassLoader;
            currentClassLoader = getNewClassLoader();
            oldClassLoader.close();
            notifyListeners();
        };

        ExceptionHandler exH = t -> {
            exceptionHandler.onException(t);
        };

        for (Path filePath : classpathEntries) {
            // Let's only watch directories, not JARs
            if (!filePath.toFile().isDirectory()) {
                continue;
            }

            newWatchers.add(new DirectoryWatcherBuilder().path(filePath).listener(dirListener).exceptionHandler(exH).build());
        }
        newWatchers.trimToSize();
        return newWatchers;
    }

    protected URL getURLFromPath(Path path) throws IllegalArgumentException {
        URL url;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            // This .. "normally" should never happen.
            throw new IllegalArgumentException("URL canont be created from Path due to MalformedURLException: " + path.toString(), e);
        }

        // Just double checking, better safe than sorry
        if (path.toFile().isDirectory() && !url.toExternalForm().endsWith("/")) {
            throw new IllegalArgumentException(
                    "URL created from Path which is a directory does not end with slash as required by the URLClassLoader: "
                            + path.toString());
        }

        return url;
    }

    @Override public List<Listener> getListeners() {
        return listeners;
    }
}
