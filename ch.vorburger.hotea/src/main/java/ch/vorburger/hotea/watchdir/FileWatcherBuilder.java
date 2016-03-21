package ch.vorburger.hotea.watchdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import ch.vorburger.hotea.watchdir.DirectoryWatcher.ChangeKind;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

public class FileWatcherBuilder extends DirectoryWatcherBuilder {

	@Override public DirectoryWatcherBuilder path(File fileNotDirectory) {
		return super.path(fileNotDirectory);
	}

	@Override public DirectoryWatcherBuilder path(Path fileNotDirectory) {
		return super.path(fileNotDirectory);
	}

	@Override
	public DirectoryWatcher build() throws IOException {
		check();
		if (!path.toFile().isFile())
			throw new IllegalStateException("When using FileWatcherBuilder, set path() to a single file, not a directory (use DirectoryWatcherBuilder to watch a directory, and it's subdirectories)");
		return new DirectoryWatcherImpl(path.getParent(), new FileWatcherListener(path, listener), exceptionHandler);
	}

	protected static class FileWatcherListener implements Listener {

		private final Listener delegate;
		private Path fileToWatch;

		public FileWatcherListener(Path fileToWatch, Listener listener) {
			this.fileToWatch = fileToWatch;
			this.delegate = listener;
		}

		@Override
		public void onChange(Path path, ChangeKind changeKind) throws Throwable {
			if (path.equals(fileToWatch)) {
				delegate.onChange(path, changeKind);
			}
		}
	}


}
