/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.tests;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.vorburger.hotea.tests.util.AssertableExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher;
import ch.vorburger.hotea.watchdir.DirectoryWatcherBuilder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Tests for DirectoryWatcher.
 * 
 * @author Michael Vorburger
 */
public class DirectoryWatcherTest {

	AssertableExceptionHandler assertableExceptionHandler = new AssertableExceptionHandler();
	boolean changed = false;
	
	@BeforeClass
	static public void configureSlf4jSimpleShowAllLogs() {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
	}
	
	@Test
	public void testDirectoryWatcher() throws Throwable {
		final File dir = new File("target/tests/DirectoryWatcherTest/some/sub/directory");
		dir.mkdirs();
		final File subDir = new File(dir.getParentFile(), "another");
		File newFile = new File(subDir, "yo.txt");
		newFile.delete();
		subDir.delete();
		
		try (DirectoryWatcher dw = new DirectoryWatcherBuilder()
				.dir(dir.getParentFile().getParentFile()).listener((p, c) -> {
						assertFalse(changed); // We want this to only be called once
						changed = true;
					}).exceptionHandler(assertableExceptionHandler).build()) {

			// Note we're creating another new sub-directory (because we want to
			// test that not only existing but also new directories are scanned)
			assertTrue(subDir.mkdirs());
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();

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
		final File testFile = new File("target/tests/DirectoryWatcherTest/someFile");
		testFile.delete();
		final File dir = testFile.getParentFile();
		dir.mkdirs();
		try (DirectoryWatcher dw = new DirectoryWatcherBuilder().dir(dir)
				.listener((p, c) -> {
					fail("duh!");
				}).exceptionHandler(assertableExceptionHandler).build()) {
			
			Files.write("yo", testFile, Charsets.US_ASCII);
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
		}
	}
}
