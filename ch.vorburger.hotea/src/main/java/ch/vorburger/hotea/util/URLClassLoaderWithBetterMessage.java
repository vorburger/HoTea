/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * An URLClassLoader which includes all the URLs it searched in its Exceptions.
 *
 * @author Michael Vorburger
 */
public class URLClassLoaderWithBetterMessage extends URLClassLoader {

    protected String allMyURLs;

    public URLClassLoaderWithBetterMessage(URL[] urls) {
        super(urls);
    }

    public URLClassLoaderWithBetterMessage(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public URLClassLoaderWithBetterMessage(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            String originalMessage = e.getMessage();
            String newMessage = originalMessage + getAllMyURLs();
            ClassNotFoundException newClassNotFoundException = new ClassNotFoundException(newMessage);
            if (e.getCause() != null) {
                newClassNotFoundException.initCause(e.getCause());
            }
            throw newClassNotFoundException;
        } catch (LinkageError e) {
            String originalMessage = e.getMessage();
            String newMessage = originalMessage + getAllMyURLs();
            LinkageError newLinkageError = new LinkageError(newMessage);
            if (e.getCause() != null) {
                newLinkageError.initCause(e.getCause());
            }
            throw newLinkageError;
        }
    }

    protected String getAllMyURLs() {
        if (allMyURLs == null) {
            StringBuilder allMyURLsBuilder = new StringBuilder();
            URL[] urls = getURLs();
            for (URL url : urls) {
                allMyURLsBuilder.append('\n');
                allMyURLsBuilder.append(url.toString());
            }
            allMyURLs = allMyURLsBuilder.toString();
        }
        return allMyURLs;
    }
}
