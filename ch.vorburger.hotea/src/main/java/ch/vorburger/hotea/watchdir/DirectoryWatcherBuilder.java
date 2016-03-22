/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import ch.vorburger.hotea.util.LoggingExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.ChangeKind;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

/**
 * Builder for DirectoryWatcher.
 * 
 * @author Michael Vorburger
 */
public class DirectoryWatcherBuilder {

	protected Path path;
	protected Listener listener;
	protected ExceptionHandler exceptionHandler = new LoggingExceptionHandler();
	protected long quietPeriodInMS = 100;

	public DirectoryWatcherBuilder path(File directory) {
		return path(directory.toPath());
	}

	public DirectoryWatcherBuilder path(Path directory) {
		if (this.path != null)
			throw new IllegalStateException("path already set");
		this.path = directory;
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
	
	public DirectoryWatcherBuilder quietPeriodInMS(long quietPeriodInMS) {
		this.quietPeriodInMS = quietPeriodInMS;
		return this;
	}
	
	public DirectoryWatcher build() throws IOException {
		check();
		if (!path.toFile().isDirectory())
			throw new IllegalStateException("When using DirectoryWatcherBuilder, set path() to a directory, not a file (use FileWatcherBuilder to watch a single file)");
		DirectoryWatcherImpl watcher = new DirectoryWatcherImpl(true, path, getQuietListener(listener), exceptionHandler);
		firstListenerNotification();
		return watcher;
	}

	// We intentionally want to first call the listener once for setup, even without any change detected
	protected void firstListenerNotification() {
		try {
			listener.onChange(path, ChangeKind.MODIFIED);
		} catch (Throwable e) {
			exceptionHandler.onException(e);
		}
	}

	protected void check() {
		if (this.path == null)
			throw new IllegalStateException("path not set");
		if (!this.path.toFile().exists())
			throw new IllegalStateException("path does not exist: " + this.path.toString());
		if (this.listener == null)
			throw new IllegalStateException("listener not set");
		if (this.exceptionHandler == null)
			throw new IllegalStateException("exceptionHandler not set");
	}

	protected Listener getQuietListener(Listener listenerToWrap) {
		return new QuietPeriodListener(quietPeriodInMS, listenerToWrap, exceptionHandler);
	}

}
