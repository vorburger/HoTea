/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import ch.vorburger.hotea.util.LoggingExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

/**
 * Builder for DirectoryWatcher.
 * 
 * @author Michael Vorburger
 */
public class DirectoryWatcherBuilder {

	protected Path dir;
	protected Listener listener;
	protected ExceptionHandler exceptionHandler = new LoggingExceptionHandler();
	
	public DirectoryWatcherBuilder dir(File dir) {
		if (this.dir != null)
			throw new IllegalStateException("dir already set");
		this.dir = dir.toPath();
		return this;
	}

	public DirectoryWatcherBuilder dir(Path dir) {
		if (this.dir != null)
			throw new IllegalStateException("dir already set");
		this.dir = dir;
		return this;
	}
	
	public DirectoryWatcherBuilder listener(Listener listener) {
		if (this.listener != null)
			throw new IllegalStateException("listener already set");
		this.listener = listener;
		return this;
	}

	public DirectoryWatcherBuilder exceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}
	
	public DirectoryWatcher build() throws IOException {
		return new DirectoryWatcherImpl(dir, listener, exceptionHandler);
	}

}
