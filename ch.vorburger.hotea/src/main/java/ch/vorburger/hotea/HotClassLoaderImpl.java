package ch.vorburger.hotea;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.hotea.util.URLClassLoaderWithBetterMessage;
import ch.vorburger.hotea.watchdir.DirectoryWatcher;
import ch.vorburger.hotea.watchdir.DirectoryWatcherBuilder;

// intentionally package local, for now
class HotClassLoaderImpl implements HotClassLoader {
	private final static Logger log = LoggerFactory.getLogger(HotClassLoaderImpl.class);

	private final URL[] urls;	
	private final List<HotClassLoader.Listener> listeners;
	private final ExceptionHandler exceptionHandler;
	private final List<DirectoryWatcher> watchers;
	private final ClassLoader parentClassLoader;

	private URLClassLoader currentClassLoader;

	protected HotClassLoaderImpl(List<Path> classpathEntries, ClassLoader parentClassLoader, List<HotClassLoader.Listener> listeners, ExceptionHandler exceptionHandler) throws IOException {
		this.listeners = listeners;
		this.exceptionHandler = exceptionHandler;
		this.parentClassLoader = parentClassLoader;
		
		this.urls = getURLs(classpathEntries);
		this.currentClassLoader = getNewClassLoader();
		this.watchers = setUpFileChangeListeners(classpathEntries);

		notifyListeners();
	}

	@Override
	public ClassLoader getCurrentClassLoader() {
		return currentClassLoader;
	}

	@Override
	public void close() {
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
		if (parentClassLoader == null)
			return new URLClassLoaderWithBetterMessage(urls); // URLClassLoader.newInstance(urls);
		else 
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
		
		DirectoryWatcher.Listener dirListener = (directory, changeKind) -> {
			URLClassLoader oldClassLoader = currentClassLoader;
			this.currentClassLoader = getNewClassLoader();
			oldClassLoader.close();
			notifyListeners();
		};
		
		ExceptionHandler exH = t -> {
			exceptionHandler.onException(t);
		};
		
		for (Path filePath : classpathEntries) {
			// Let's only watch directories, not JARs
			if (!filePath.toFile().isDirectory())
				continue;
			
			newWatchers.add(new DirectoryWatcherBuilder().dir(filePath).listener(dirListener).exceptionHandler(exH).build());
		}
		newWatchers.trimToSize();
		return newWatchers;
	}

	// Duh, what a mess this is in Java! ;) Check out:
	//  * https://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html
	//  * http://stackoverflow.com/questions/18520972/converting-java-file-url-to-file-path-platform-independent-including-u
	//  * http://stackoverflow.com/questions/2166039/java-how-to-get-a-file-from-an-escaped-url
	//  * https://wiki.eclipse.org/Eclipse/UNC_Paths
// Since passing List<Path> instead of List<URL> into setUpFileChangeListeners we don't actually need this anymore
//	protected Optional<Path> getPathFromURL(URL url) {
//		if (!"file".equalsIgnoreCase(url.getProtocol()))
//			return Optional.empty();
//		try {
//			Path path = Paths.get(url.toURI());
//			return Optional.ofNullable(path);
//		} catch (URISyntaxException e) {
//			return Optional.empty();
//		}
//	}

	protected URL getURLFromPath(Path path) throws IllegalArgumentException {
		URL url;
		try {
			url = path.toUri().toURL();
		} catch (MalformedURLException e) {
			// This .. "normally" should never happen.
			throw new IllegalArgumentException("URL canont be created from Path due to MalformedURLException: " + path.toString(), e);
		}
				
		// Just double checking, better safe than sorry
		if (path.toFile().isDirectory())
			if (!url.toExternalForm().endsWith("/"))
				throw new IllegalArgumentException("URL created from Path which is a directory does not end with slash as required by the URLClassLoader: " + path.toString());
		
		return url;
	}

}
