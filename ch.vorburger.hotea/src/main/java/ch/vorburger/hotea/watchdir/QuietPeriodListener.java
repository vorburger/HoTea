/**
 * Copyright (C) 2016 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import java.nio.file.Path;

import ch.vorburger.hotea.watchdir.DirectoryWatcher.ChangeKind;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

/**
 * Listener which only notifies by delegating to another wrapped Listener after a certain quiet period.
 *
 * @author Michael Vorburger
 */
public class QuietPeriodListener implements Listener {

    protected final Listener delegate;
    private final ExceptionHandler exceptionHandler;
    protected final long quietPeriodInMS;

    protected Thread thread;
    protected volatile boolean sleepAgain;

    protected QuietPeriodListener(long quietPeriodInMS, Listener listenerToWrap, ExceptionHandler exceptionHandler) {
        this.quietPeriodInMS = quietPeriodInMS;
        this.delegate = listenerToWrap;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public synchronized void onChange(Path path, ChangeKind changeKind) throws Throwable {
        if (thread != null && thread.isAlive()) {
            sleepAgain = true;
            //System.out.println("sleepAgain = true");
        } else {
            Runnable r = () -> {
                try {
                    do {
                        sleepAgain = false;
                        //System.out.println("sleepAgain = false");
                        Thread.sleep(quietPeriodInMS);
                    } while (sleepAgain);
                    delegate.onChange(path, changeKind);
                } catch (Throwable e) {
                    exceptionHandler.onException(e);
                }
            };
            thread = new Thread(r, QuietPeriodListener.class.getName());
            thread.setDaemon(true);
            thread.start();
        }
    }
}
