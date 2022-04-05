/*
 * Copyright (C) 2015 - 2022 by Michael Vorburger
 */
package ch.vorburger.hotea;

import ch.vorburger.fswatch.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.fswatch.Slf4jLoggingExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for HotClassLoader.
 *
 * @author Michael Vorburger
 */
public class HotClassLoaderBuilder {

    protected List<Path> classpathEntries = new ArrayList<>();
    protected List<HotClassLoader.Listener> listeners = new ArrayList<>();
    protected ExceptionHandler exceptionHandler = new Slf4jLoggingExceptionHandler();
    protected ClassLoader parentClassLoader;

    public HotClassLoaderBuilder addClasspathEntry(File file) {
        return addClasspathEntry(file.toPath());
    }

    public HotClassLoaderBuilder addClasspathEntry(String pathName) {
        return addClasspathEntry(new File(pathName));
    }

    public HotClassLoaderBuilder addClasspathEntry(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("Does not exist: " + path.toString());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("Cannot not read: " + path.toString());
        }
        if (!file.isFile() && !file.isDirectory()) {
            throw new IllegalArgumentException("Is neither a directory nor a file: " + path.toString());
        }

        classpathEntries.add(path);
        return this;
    }

    public HotClassLoaderBuilder addClasspathEntries(List<Path> paths) {
        for (Path path : paths) {
            addClasspathEntry(path);
        }
        return this;
    }

    public HotClassLoaderBuilder addListener(HotClassLoader.Listener listener) {
        listeners.add(listener);
        return this;
    }

    public HotClassLoaderBuilder setListenerExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public HotClassLoaderBuilder setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }

    /**
     * Builds the first hot ClassLoader.
     *
     * @return a ClassLoader. Use this to get your initial classes from, but do NOT hold on to this; replace this when the
     *             HotClassLoaderListener give you a new one!
     *
     * @throws IllegalStateException    if no classpath entry has been added (no Listener is OK - this simple means that it
     *                                  will not watch for changes and hot reload)
     * @throws IllegalArgumentException if any of the added classpath entries can not be converted to URLs as needed by the
     *                                  URLClassLoader
     * @throws IOException              if there was an IO related problem with accessing one of the classpath entry
     *                                  files/directories etc.
     */
    public HotClassLoader build() throws IllegalStateException, IllegalArgumentException, IOException {
        if (classpathEntries.isEmpty()) {
            throw new IllegalStateException("Needs add least one classpath entry added");
        }
        return new HotClassLoaderImpl(classpathEntries, parentClassLoader, listeners, exceptionHandler);
    }
}