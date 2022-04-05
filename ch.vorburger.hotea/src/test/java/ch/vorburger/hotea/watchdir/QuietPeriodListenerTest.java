/**
 * Copyright (C) 2016 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

import ch.vorburger.hotea.tests.util.AssertableExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;

public class QuietPeriodListenerTest {

    AssertableExceptionHandler assertableExceptionHandler;
    volatile boolean notified;
    
    @Test
    public void testQuietPeriodListener() throws Throwable {
        assertableExceptionHandler = new AssertableExceptionHandler();
        Listener originalListener = (path, changeKind) -> {
            assertFalse(notified); // We want this to only be called once
            notified = true;
        };

        Listener quietListener = new QuietPeriodListener(100, originalListener, assertableExceptionHandler);
        
        notified = false;
        quietListener.onChange(null, null);
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        await().atMost(1, SECONDS).until(() -> notified, is(true));
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        
        notified = false;
        quietListener.onChange(null, null);
        quietListener.onChange(null, null);
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        await().atMost(1, SECONDS).until(() -> notified, is(true));
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

        
        notified = false;
        quietListener.onChange(null, null);
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        await().atMost(1, SECONDS).until(() -> notified, is(true));
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

        Thread.sleep(500);
        
        notified = false;
        quietListener.onChange(null, null);
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        await().atMost(1, SECONDS).until(() -> notified, is(true));
        assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
    }

}
