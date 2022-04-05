/**
 * Copyright (C) 2016 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import ch.vorburger.hotea.watchdir.DirectoryWatcher.ChangeKind;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

/**
 * Builder which watches one single file for changes.
 *
 * @author Michael Vorburger
 */
public class FileWatcherBuilder extends DirectoryWatcherBuilder {

    @Override public FileWatcherBuilder path(File fileNotDirectory) {
        return (FileWatcherBuilder) super.path(fileNotDirectory);
    }

    @Override public FileWatcherBuilder path(Path fileNotDirectory) {
        return (FileWatcherBuilder) super.path(fileNotDirectory);
    }

    @Override
    public DirectoryWatcher build() throws IOException {
        check();
        if (!path.toFile().isFile())
            throw new IllegalStateException("When using FileWatcherBuilder, set path() to a single file, not a directory (use DirectoryWatcherBuilder to watch a directory, and it's subdirectories)");
        // NOTE We do want to wrap the FileWatcherListener inside the QuietPeriodListener, and not the other way around!
        Listener wrap = getQuietListener(new FileWatcherListener(path, listener));
        DirectoryWatcherImpl watcher = new DirectoryWatcherImpl(false, path.getParent(), wrap, exceptionHandler);
        firstListenerNotification();
        return watcher;
    }


    protected static class FileWatcherListener implements Listener {

        private final Listener delegate;
        private Path fileToWatch;

        protected FileWatcherListener(Path fileToWatch, Listener listenerToWrap) {
            this.fileToWatch = fileToWatch;
            this.delegate = listenerToWrap;
        }

        @Override
        public void onChange(Path path, ChangeKind changeKind) throws Throwable {
            if (path.equals(fileToWatch)) {
                delegate.onChange(path, changeKind);
            }
        }
    }


}
