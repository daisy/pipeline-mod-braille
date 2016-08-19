package org.daisy.pipeline.braille.maven.plugin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Optional;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.trans.XPathException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.DirectoryScanner;

/**
 * @goal index-liblouis-tables
 */
public class IndexLiblouisTablesMojo extends AbstractMojo {
	
	/**
	 * @parameter
	 * @required
	 */
	private File sourceDirectory;
	
	/**
	 * @parameter
	 */
	private String includes;
	private final String defaultIncludes = "**/*";
	
	/**
	 * @parameter
	 */
	private String excludes;
	
	/**
	 * @parameter
	 */
	private File outputFile;
	private final String defaultOutputFileName = "index.html";
	
	/**
	 * @parameter
	 * @required
	 */
	private String identifier;
	
	public void execute() throws MojoFailureException {
		try {
			if (includes == null)
				includes = defaultIncludes;
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(sourceDirectory);
			scanner.setIncludes(includes.replaceAll("\\s", "").split(",(?![^{]*})"));
			if (excludes != null)
				scanner.setExcludes(excludes.replaceAll("\\s", "").split(",(?![^{]*})"));
			scanner.scan();
			if (outputFile == null)
				outputFile = new File(sourceDirectory, defaultOutputFileName);
			Serializer serializer = new Processor(false).newSerializer(outputFile);
			XMLStreamWriter writer = serializer.getXMLStreamWriter();
			writer.writeStartElement("html");
			writer.writeAttribute("prefix", "dp2: http://www.daisy.org/ns/pipeline/");
			writer.writeStartElement("head");
			writer.writeStartElement("link");
			writer.writeAttribute("rev", "dp2:doc");
			writer.writeAttribute("href", relativize(asURI(outputFile), asURI(sourceDirectory)).toASCIIString());
			writer.writeEndElement();
			writer.writeStartElement("link");
			writer.writeAttribute("rel", "rdf:type");
			writer.writeAttribute("href", "http://www.daisy.org/ns/pipeline/apidoc");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("body");
			writer.writeStartElement("h1");
			writer.writeCharacters("Index of " + identifier);
			writer.writeEndElement();
			writer.writeStartElement("ul");
			for (String f : scanner.getIncludedFiles()) {
				String relPath = relativize(asURI(outputFile), asURI(new File(sourceDirectory, f))).toASCIIString();
				writer.writeStartElement("li");
				writer.writeAttribute("about", relPath);
				writer.writeAttribute("property", "dp2:id");
				writer.writeAttribute("content", identifier + f);
				writer.writeStartElement("a");
				writer.writeAttribute("class", "source");
				writer.writeAttribute("href", relPath);
				writer.writeCharacters(f);
				writer.writeEndElement();
				writer.writeEndElement(); }
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.close(); }
		catch (Throwable e) {
			throw new MojoFailureException(e.getMessage()); }
	}
	
	private static URI asURI(Object o) {
		try {
			if (o instanceof URI)
				return (URI)o;
			if (o instanceof File)
				return asURI(((File)o).toURI());
			if (o instanceof URL) {
				URL url = (URL)o;
				if (url.getProtocol().equals("jar"))
					return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }}
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URI: " + o);
	}
	
	private static URI relativize(URI base, URI child) {
		try {
			if (base.isOpaque() || child.isOpaque()
			    || !Optional.fromNullable(base.getScheme()).or("").equalsIgnoreCase(Optional.fromNullable(child.getScheme()).or(""))
			    || !Optional.fromNullable(base.getAuthority()).equals(Optional.fromNullable(child.getAuthority())))
				return child;
			else {
				String bp = base.normalize().getPath();
				String cp = child.normalize().getPath();
				String relativizedPath;
				if (cp.startsWith("/")) {
					String[] bpSegments = bp.split("/", -1);
					String[] cpSegments = cp.split("/", -1);
					int i = bpSegments.length - 1;
					int j = 0;
					while (i > 0) {
						if (bpSegments[j].equals(cpSegments[j])) {
							i--;
							j++; }
						else
							break; }
					relativizedPath = "";
					while (i > 0) {
						relativizedPath += "../";
						i--; }
					while (j < cpSegments.length) {
						relativizedPath += cpSegments[j] + "/";
						j++; }
					relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1); }
				else
					relativizedPath = cp;
				if (relativizedPath.isEmpty())
					relativizedPath = "./";
				return new URI(null, null, relativizedPath, child.getQuery(), child.getFragment()); }}
		catch (URISyntaxException e) {
			throw new RuntimeException(e); }
	}
}
