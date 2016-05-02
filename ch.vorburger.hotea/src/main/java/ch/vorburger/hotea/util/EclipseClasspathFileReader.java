package ch.vorburger.hotea.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads an Eclipse .classpath file's entries.
 *  
 * @author Michael Vorburger, loosely inspired by from code by Kohsuke Kawaguchi found on https://github.com/kohsuke/parse-dot-classpath/blob/master/src/org/kohsuke/pdc/ParseDotClasspath.java
 */
public class EclipseClasspathFileReader {

	// If I ever want to build something comparable for IntelliJ *.ipr files, look at https://github.com/kohsuke/parse-ipr

	protected final List<Path> paths; 
	
	public EclipseClasspathFileReader(Path eclipseDotClasspathFile) throws IllegalArgumentException {
		if (!eclipseDotClasspathFile.toString().endsWith(".classpath"))
			throw new IllegalArgumentException("Argument eclipseDotClasspathFile does not end with *.classpath: " + eclipseDotClasspathFile.toString());
		paths = new ArrayList<>();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		XMLReader parser;
		try {
			parser = spf.newSAXParser().getXMLReader();
			parser.setContentHandler(new DefaultHandler() {
				public void startElement(String uri, String localName, String qname, Attributes atts) {
					if (!localName.equals("classpathentry"))
						return; // unknown

					String kind = atts.getValue("kind");
					switch (kind) {
					case "lib":
						add(atts, eclipseDotClasspathFile);
						break;
					case "output":
						add(atts, eclipseDotClasspathFile);
						break;
					case "con":
						if (!"org.eclipse.jdt.launching.JRE_CONTAINER".equals(atts.getValue("path")))
							throw new IllegalArgumentException("Unknown classpathentry kind of kind 'con' (only org.eclipse.jdt.launching.JRE_CONTAINER accepted): " + atts);
					case "src":
						// Ignore
						break;
					default:
						throw new IllegalArgumentException("Unknown classpathentry kind: " + atts);
					}
				}
			});
			parser.parse(eclipseDotClasspathFile.toUri().toString());
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new IllegalArgumentException("Failed to read/parse this .classpath XML: " + eclipseDotClasspathFile.toString(), e);
		}
	}

	protected void add(Attributes atts, Path eclipseDotClasspathFile) {
		String pathName = atts.getValue("path");
		if (pathName == null)
			throw new IllegalArgumentException("No 'path' in: " + atts + "of: " + eclipseDotClasspathFile);
		pathName = pathName.trim();
		if (pathName.isEmpty())
			throw new IllegalArgumentException("Empty 'path' in: " + atts + "of: " + eclipseDotClasspathFile);
    	Path path = Paths.get(pathName);
		if (!path.isAbsolute()) {
	    	Path projectDirectory = eclipseDotClasspathFile.normalize().getParent();
    		path = projectDirectory.resolve(path);
		}
		path = path.normalize();
		paths.add(path);
	}

	public List<Path> getPaths() {
		return paths;
	}
}
