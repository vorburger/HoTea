/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.tests;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoaderBuilder;
import ch.vorburger.hotea.tests.util.AssertableExceptionHandler;

import com.google.common.io.Files;

/**
 * Tests for HotClassLoaderBuilder.
 * 
 * @author Michael Vorburger
 */
public class HotClassLoaderTest {

	volatile int i;
	SomeInterface someAPI;
	AssertableExceptionHandler assertableExceptionHandler = new AssertableExceptionHandler();

//	@BeforeClass
//	static public void configureSlf4jSimpleShowAllLogs() {
//		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
//	}
	
	@Test public void testLoadBinaryJavaClassNotOnClasspath() throws Exception {
		File targetClassFile = copyClassFile("SomeInterfaceImpl-hello_world.class");
		try(HotClassLoader hcl = new HotClassLoaderBuilder().addClasspathEntry(new File("target/tests/hot/classes")).build()) {
			ClassLoader classLoader = hcl.getCurrentClassLoader();
			@SuppressWarnings("unchecked") Class<SomeInterface> klass = (Class<SomeInterface>) classLoader.loadClass("ch.vorburger.hotea.tests.notoncp.SomeInterfaceImpl");
			SomeInterface someAPI = klass.newInstance();
			assertTrue(targetClassFile.delete());
			assertEquals("hello, world", someAPI.whatup());
		}
	}

	private File copyClassFile(String sourceClassFileName) throws IOException {
		File targetDir = new File("target/tests/hot/classes/ch/vorburger/hotea/tests/notoncp/");
		File targetClassFile = new File(targetDir, "SomeInterfaceImpl.class");
		targetDir.mkdirs();
		Files.copy(new File("src/test/resources/SomeInterfaceImpl/" + sourceClassFileName), targetClassFile);
		return targetClassFile;
	}

	@Ignore // too slow and unreliable
	@Test public void testChangeJavaBinaryClassNotOnClasspath100x() throws Throwable {
		for (int n = 0; n < 100; n++) {
			testChangeJavaBinaryClassNotOnClasspath();
		}
	}

	@Test public void testChangeJavaBinaryClassNotOnClasspath() throws Throwable {
		i = 0;
		copyClassFile("SomeInterfaceImpl-hello_world.class");

		// The ClassLoader returned here is not (yet) interesting - its another one with the same version of the Class as above
		try (HotClassLoader hcl = new HotClassLoaderBuilder().addClasspathEntry(new File("target/tests/hot/classes")).addListener(newClassLoader -> {
			@SuppressWarnings("unchecked") Class<SomeInterface> klass = (Class<SomeInterface>) newClassLoader.loadClass("ch.vorburger.hotea.tests.notoncp.SomeInterfaceImpl");
			someAPI = klass.newInstance();
			++i;
		}).setListenerExceptionHandler(assertableExceptionHandler).build()) {

			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			ClassLoader firstClassLoader = hcl.getCurrentClassLoader();
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			
			// Now replace the class file with another implementation
			File newTargetClassFile = copyClassFile("SomeInterfaceImpl-world_hello.class");
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			
			// Wait for (2nd!) reload
			await().atMost(1, SECONDS).until(() -> i, greaterThanOrEqualTo(2));
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
	
			ClassLoader secondClassLoader = hcl.getCurrentClassLoader();
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			assertFalse(firstClassLoader.equals(secondClassLoader));
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			
			assertEquals("world, hello!", someAPI.whatup());
			assertableExceptionHandler.assertNoErrorInTheBackgroundThread();
			assertTrue(newTargetClassFile.delete());
			// Note that now we expect a problem - we won't be able to reload the Class that was just deleted
			assertableExceptionHandler.assertErrorCaughtFromTheBackgroundThread();
		}
	}

	@Test public void testHotClassLoaderBuilderListenerFailure() throws Throwable {
		try (HotClassLoader hcl = new HotClassLoaderBuilder().addClasspathEntry(new File(".")).addListener(newClassLoader -> {
			fail("duh!");
		}).setListenerExceptionHandler(assertableExceptionHandler).build()) {
			assertableExceptionHandler.assertErrorMessageCaughtFromTheBackgroundThreadContains("duh!");
		}
	}
	
	@Test public void testExceptionMessageContainsPath() throws Exception {
		try (HotClassLoader hcl = new HotClassLoaderBuilder().addClasspathEntry(new File(".")).build()) {
			ClassLoader classLoader = hcl.getCurrentClassLoader();
			try {
				classLoader.loadClass("Class.that.does.not.exist");
				fail("Should not have worked!");
			} catch (ClassNotFoundException e) {
				assertTrue(e.getMessage(), e.getMessage().contains("Class.that.does.not.exist"));
				assertTrue(e.getMessage(), e.getMessage().contains("."));
			}
		}
	}
	
/*
    TODO testChangeJavaSourceNotOnClasspath, when ch.vorburger.hotea.compilewatchedfiles is implemented

		String src = "ch/vorburger/hotea/tests/notoncp/SomeInterfaceImplNotOnCP.java";
		File sourceFileOriginal = new File("src/test/resources/" + src);
		String sourceText = Files.toString(sourceFileOriginal, Charsets.US_ASCII);
		String srcPath = "target/tests/srcToCompileOnTheFly/";
		File sourceFileCopy = new File(srcPath + src);;
		sourceFileCopy.getParentFile().mkdirs();
		Files.write(sourceText, sourceFileCopy, Charsets.US_ASCII);				

		sourceText = sourceText.replace("hello, world", "world, hello!");
		Files.write(sourceText, sourceFileCopy, Charsets.US_ASCII);
 
	private void reset(String sourceText, File sourceFile) throws IOException {
		sourceText = sourceText.replace("world, hello!", "hello, world");
		Files.write(sourceText, sourceFile, Charsets.US_ASCII);		
	}
*/

}
