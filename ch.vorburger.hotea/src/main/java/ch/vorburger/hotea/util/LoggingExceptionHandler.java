/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.util;

import ch.vorburger.hotea.HotClassLoader.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ExceptionHandler which logs to slfj4j.
 *
 * @author Michael Vorburger
 */
public class LoggingExceptionHandler implements ExceptionHandler {
    private final static Logger log = LoggerFactory.getLogger(LoggingExceptionHandler.class);

    @Override
    public void onException(Throwable t) {
        log.error("Oopsy daisy", t);
    }

}
