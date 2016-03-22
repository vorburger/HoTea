/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DirectoryWatcher based on java.nio.file.WatchService.
 * 
 * @author Michael Vorburger
 */
// intentionally package local, for now
class DirectoryWatcherImpl implements DirectoryWatcher {
	private final static Logger log = LoggerFactory.getLogger(DirectoryWatcherImpl.class);

//	protected final File dir;
	protected final Listener listener;
	protected final WatchService watcher = FileSystems.getDefault().newWatchService(); // better final, as it will be accessed by both threads (normally OK either way, but still)
	protected final Thread thread;

	/** Clients should use DirectoryWatcherBuilder */
	protected DirectoryWatcherImpl(boolean watchSubDirectories, final Path watchBasePath, final Listener listener, ExceptionHandler exceptionHandler) throws IOException {
//		this.dir = dir;
		this.listener = listener;
		if (!watchBasePath.toFile().isDirectory())
			throw new IllegalArgumentException("Not a directory: " + watchBasePath.toString());

		register(watchSubDirectories, watchBasePath);
		Runnable r = () -> {
			for (;;) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (ClosedWatchServiceException e) {
					log.debug("WatchService take() interrupted by ClosedWatchServiceException, terminating Thread (as planned).");
					return;
				} catch (InterruptedException e) {
					log.debug("Thread InterruptedException, terminating (as planned, if caused by close()).");
					return;
				}
            	Path watchKeyWatchablePath = (Path) key.watchable();
                // We have a polled event, now we traverse it and receive all the states from it
                for (WatchEvent<?> event : key.pollEvents()) {

                	Kind<?> kind = event.kind();
                	if (kind == StandardWatchEventKinds.OVERFLOW) {
                		// TODO Not sure how to correctly "handle" an Overflow.. ?
                		log.error("Received {} (TODO how to handle?)", kind.name());
                        continue;
                	}
                	
                	Path relativePath = (Path) event.context();
                	if (relativePath == null) {
                		log.error("Received {} but event.context() == null: {}", kind.name(), event.toString());
                        continue;
                	}
                	Path absolutePath = watchKeyWatchablePath.resolve(relativePath);
                	if (log.isTraceEnabled())
                		log.trace("Received {} for: {}", kind.name(), absolutePath);

					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						if (Files.isDirectory(absolutePath)) { // don't NOFOLLOW_LINKS
							try {
								register(watchSubDirectories, watchBasePath);
							} catch (IOException e) {
								exceptionHandler.onException(e);
							}
						}
					}

					if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_DELETE) {
						// To reduce notifications, only call the Listener on Modify and Delete but not Create,
						// because (on Linux at least..) every ENTRY_CREATE from new file
						// is followed by an ENTRY_MODIFY anyway.
	                	try {
							ChangeKind ourkind = kind == StandardWatchEventKinds.ENTRY_MODIFY ? ChangeKind.MODIFIED : ChangeKind.DELETED;
							listener.onChange(absolutePath, ourkind);
						} catch (Throwable e) {
							exceptionHandler.onException(e);
						}
					}
                }
                key.reset();
            }
		};
		String threadName = DirectoryWatcherImpl.class.getSimpleName() + ": " + watchBasePath.toString();
		thread = new Thread(r, threadName);
		thread.setDaemon(true);
		// Because we're catch-ing expected exceptions above, this normally
		// should never be needed, but still be better safe than sorry.. ;-)
		thread.setUncaughtExceptionHandler((t, e) -> {
			exceptionHandler.onException(e);
		}); 
		thread.start();
	}

	protected void register(boolean watchSubDirectories, final Path path) throws IOException {
		if (watchSubDirectories)
			registerAll(path);
		else
			registerOne(path);
	}
	
	protected void registerOne(final Path path) throws IOException {
		path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (log.isDebugEnabled())
        	log.debug("Registered: {}", path.toString());
	}

	// Implementation inspired by https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java, from https://docs.oracle.com/javase/tutorial/essential/io/notification.html

	protected void registerAll(final Path basePath) throws IOException {
		// register basePath directory and sub-directories
        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
            	registerOne(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

	@Override
	public void close() {
		// The order here is important - first we stop the Thread, then close the Watcher.
		thread.interrupt();
		try {
			watcher.close();
		} catch (IOException e) {
			log.error("WatchService close() failed", e);
		}
	}

	@Override
	public String toString() {
		return thread.getName();
	}

}
