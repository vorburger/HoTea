/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.vorburger.hotea.util.LoggingExceptionHandler;

/**
 * Builder for HotClassLoader.
 * 
 * @author Michael Vorburger
 */
public class HotClassLoaderBuilder {

	protected List<Path> classpathEntries = new ArrayList<>();
	protected List<HotClassLoader.Listener> listeners = new ArrayList<>();
	protected HotClassLoader.ExceptionHandler exceptionHandler = new LoggingExceptionHandler();
	protected ClassLoader parentClassLoader;
	
	public HotClassLoaderBuilder addClasspathEntry(File path) {
		classpathEntries.add(path.toPath());
		return this;
	}

	public HotClassLoaderBuilder addClasspathEntry(Path path) {
		classpathEntries.add(path);
		return this;
	}

	public HotClassLoaderBuilder addListener(HotClassLoader.Listener listener) {
		listeners.add(listener);
		return this;
	}

	public HotClassLoaderBuilder setListenerExceptionHandler(HotClassLoader.ExceptionHandler exceptionHandler) {
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
	 * @return a ClassLoader.  Use this to get your initial classes from, but do NOT hold on to this; replace this when the HotClassLoaderListener give you a new one!
	 * 
	 * @throws IllegalStateException if no classpath entry has been added (no Listener is OK - this simple means that it will not watch for changes and hot reload)
	 * @throws IllegalArgumentException if any of the added classpath entries can not be converted to URLs as needed by the URLClassLoader
	 * @throws IOException if there was an IO related problem with accessing one of the classpath entry files/directories etc.  
	 */
	public HotClassLoader build() throws IllegalStateException, IllegalArgumentException, IOException {
		return new HotClassLoaderImpl(classpathEntries, parentClassLoader, listeners, exceptionHandler);
	}

}