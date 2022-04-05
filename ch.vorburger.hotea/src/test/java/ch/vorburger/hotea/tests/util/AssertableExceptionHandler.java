package ch.vorburger.hotea.tests.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.hotea.HotClassLoader.ExceptionHandler;
import ch.vorburger.hotea.util.LoggingExceptionHandler;

/**
 * ExceptionHandler useful in multi-threaded tests.
 *
 * @author Michael Vorburger
 */
public class AssertableExceptionHandler extends LoggingExceptionHandler implements ExceptionHandler {
    private final static Logger log = LoggerFactory.getLogger(AssertableExceptionHandler.class);

    // This stuff isn't 100% concurrency kosher, but "good enough" to cover the tests..
    private Object lockObject = new Object();
    private volatile Throwable lastThrowable = null;

    @Override
    public void onException(Throwable t) {
        synchronized (lockObject) {
            super.onException(t);
            if (lastThrowable == null) // don't overwrite, if we already have one (that hasn't been checked yet)
                lastThrowable = t;
            else
                log.error("There is already a previous lastThrowable which hasn't been asserted, yet; so ignoring this", t);
        }
    }

    public void assertNoErrorInTheBackgroundThread() throws Throwable {
        Thread.yield();
        Thread.sleep(100); // slow!
        synchronized (lockObject) {
            if (lastThrowable != null) {
                Throwable theThrowable = lastThrowable;
                lastThrowable = null;
                // NOT just throw throwable (this gives us more information via two stack traces)
                throw new AssertionError("Failed to assert that no error occured in the background Thread (see nested cause)", theThrowable);
            }
        }
    }

    public void assertErrorCaughtFromTheBackgroundThread() throws InterruptedException {
        Thread.yield();
        Thread.sleep(100);
        synchronized (lockObject) {
            assertNotNull("Expected an error occuring in the background thread (but there wasn't)", lastThrowable);
            lastThrowable = null;
        }
    }

    public void assertErrorMessageCaughtFromTheBackgroundThreadContains(String message) throws InterruptedException {
        Thread.yield();
        Thread.sleep(100);
        synchronized (lockObject) {
            assertNotNull("Expected an error occuring in the background thread (but there wasn't)", lastThrowable);
            assertTrue(lastThrowable.getMessage(), lastThrowable.getMessage().contains(message));
            lastThrowable = null;
        }
    }

}
