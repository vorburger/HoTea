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
		 * @param directory changed directory. Note that even if a File is changed, we get a notification of its directory - not the File.
		 * @throws Throwable if anything went wrong
		 */
		void onChange(Path directory, ChangeKind changeKind) throws Throwable;
	}

	interface ExceptionHandler {
		void onException(Throwable t);
	}

	@Override String toString();

	@Override void close(); // do NOT throws (IO)Exception
}
