/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import ch.vorburger.hotea.tests.util.AssertableExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher;
import ch.vorburger.hotea.watchdir.DirectoryWatcherBuilder;
import ch.vorburger.hotea.watchdir.FileWatcherBuilder;

/**
 * Tests for {@link DirectoryWatcherBuilder} and @link FileWatcherBuilder}.
 *
 * @author Michael Vorburger
 */
public class DirectoryAndFileWatcherTest {

    AssertableExceptionHandler assertableExceptionHandler;
    volatile boolean changed;

    @BeforeClass
    static public void configureSlf4jSimpleShowAllLogs() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Test
    public void testFileWatcher() throws Throwable {
        assertableExceptionHandler = new AssertableExceptionHandler();
        final File dir = new File("target/tests/FileWatcherTest/");
        final File subDir = new File(dir.getParentFile(), "subDir");
        dir.mkdirs();
        subDir.mkdirs();
        File file = new File(dir, "yo.txt");
        Files.write("yo", file, Charsets.US_ASCII);
        Files.write("bo", new File(subDir, "bo.txt"), Charsets.US_ASCII);

        changed = false;
        try (DirectoryWatcher dw = new FileWatcherBuilder()
                .path(file).listener((p, c) -> {
                        assertFalse(changed); // We want this to only be called once
                        changed = true;
                    }).exceptionHandler(assertableExceptionHandler).build()) {

            // We want it to call the listener once for setup, even without any change
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            Files.write("ho", file, Charsets.US_ASCII);
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            Files.write("do", file, Charsets.US_ASCII);
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            file.delete();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            Files.write("yo", file, Charsets.US_ASCII);
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            File anotherFile = new File(dir, "another.txt");
            Files.write("another", anotherFile, Charsets.US_ASCII);
            await().atMost(1, SECONDS).until(() -> changed, is(false));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        }
    }

    @Test
    public void testDirectoryWatcher() throws Throwable {
        assertableExceptionHandler = new AssertableExceptionHandler();
        final File dir = new File("target/tests/DirectoryWatcherTest/some/sub/directory");
        dir.mkdirs();
        final File subDir = new File(dir.getParentFile(), "another");
        File newFile = new File(subDir, "yo.txt");
        newFile.delete();
        subDir.delete();

        changed = false;
        try (DirectoryWatcher dw = new DirectoryWatcherBuilder()
                .path(dir.getParentFile().getParentFile()).listener((p, c) -> {
                        assertFalse(changed); // We want this to only be called once
                        changed = true;
                    }).exceptionHandler(assertableExceptionHandler).build()) {

            // We want it to call the listener once for setup, even without any change
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            // Note we're creating another new sub-directory (because we want to
            // test that not only existing but also new directories are scanned)
            changed = false;
            assertTrue(subDir.mkdirs());
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            Files.write("yo", newFile, Charsets.US_ASCII);
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
            await().
                // conditionEvaluationListener(new ConditionEvaluationLogger()).
                atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            Files.write("do", newFile, Charsets.US_ASCII);
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

            changed = false;
            newFile.delete();
            await().atMost(1, SECONDS).until(() -> changed, is(true));
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        }
    }

    @Test(expected=AssertionError.class)
    public void testDirectoryWatcherListenerExceptionPropagation() throws Throwable {
        assertableExceptionHandler = new AssertableExceptionHandler();
        final File testFile = new File("target/tests/DirectoryWatcherTest/someFile");
        testFile.delete();
        final File dir = testFile.getParentFile();
        dir.mkdirs();
        try (DirectoryWatcher dw = new DirectoryWatcherBuilder().path(dir)
                .quietPeriodInMS(0)
                .listener((p, c) -> {
                    fail("duh!");
                }).exceptionHandler(assertableExceptionHandler).build()) {

            Files.write("yo", testFile, Charsets.US_ASCII);
            assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
        }
    }

}
