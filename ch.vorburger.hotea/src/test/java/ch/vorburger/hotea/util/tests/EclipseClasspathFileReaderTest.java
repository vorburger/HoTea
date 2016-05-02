package ch.vorburger.hotea.util.tests;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.io.Resources;

import ch.vorburger.hotea.util.EclipseClasspathFileReader;

public class EclipseClasspathFileReaderTest {

	@Test
	public void testGetPaths() throws URISyntaxException {
		Path classpathFile = Paths.get(Resources.getResource(".classpath").toURI());
		EclipseClasspathFileReader r = new EclipseClasspathFileReader(classpathFile);
		assertEquals(7, r.getPaths().size());
	}

}
