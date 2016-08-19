package org.daisy.pipeline.braille.maven.plugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.trans.XPathException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.DirectoryScanner;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.calabash.Calabash;
		
/**
 * @goal htmlize-sources
 */
public class HtmlizeSourcesMojo extends AbstractMojo {
	
	/**
	 * @parameter expression="${project.basedir}/src/main/resources"
	 * @required
	 */
	private File sourceDirectory;
	
	/**
	 * @parameter
	 */
	private String includes;
	private final String defaultIncludes = "**/*.java,**/*.css,**/*.xpl,**/*.xsl,**/catalog.xml";
	
	/**
	 * @parameter expression="${project.build.directory}/generated-resources/doc"
	 * @required
	 */
	private File outputDirectory;
	
	public void execute() throws MojoFailureException {
		try {
			final XProcEngine engine = new Calabash();
			List<File> sources = new ArrayList<File>();
			if (includes == null)
				includes = defaultIncludes;
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(sourceDirectory);
			scanner.setIncludes(includes.replaceAll("\\s", "").split(",(?![^{]*})"));
			scanner.scan();
			for (String f : scanner.getIncludedFiles())
				sources.add(new File(sourceDirectory, f));
			final Map<FilenameFilter,Htmlizer> htmlizers = new HashMap<FilenameFilter,Htmlizer>();
			htmlizers.put(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.equals("catalog.xml") && dir.getName().equals("META-INF"); }},
				new Htmlizer() {
					public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
						List<String> sourcesAsURIs = new ArrayList<String>();
						for (File f : sources)
							sourcesAsURIs.add(asURI(f).toASCIIString());
							engine.run(asURI(XProcDocMojo.class.getResource("/htmlize-sources/htmlize-catalog.xpl")).toASCIIString(),
							           ImmutableMap.of("sources", sourcesAsURIs),
							           null,
							           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
							                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
							           null);
					}
				}
			);
			htmlizers.put(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".xpl"); }},
				new Htmlizer() {
					public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
						List<String> sourcesAsURIs = new ArrayList<String>();
						for (File f : sources)
							sourcesAsURIs.add(asURI(f).toASCIIString());
							engine.run(asURI(XProcDocMojo.class.getResource("/htmlize-sources/htmlize-xproc.xpl")).toASCIIString(),
							           ImmutableMap.of("sources", sourcesAsURIs),
							           null,
							           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
							                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
							           null);
					}
				}
			);
			htmlizers.put(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".xsl"); }},
				new Htmlizer() {
					public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
						List<String> sourcesAsURIs = new ArrayList<String>();
						for (File f : sources)
							sourcesAsURIs.add(asURI(f).toASCIIString());
							engine.run(asURI(XProcDocMojo.class.getResource("/htmlize-sources/htmlize-xslt.xpl")).toASCIIString(),
							           ImmutableMap.of("sources", sourcesAsURIs),
							           null,
							           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
							                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
							           null);
					}
				}
			);
			htmlizers.put(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".utb")
							|| name.endsWith(".uti")
							|| name.endsWith(".ctb")
							|| name.endsWith(".cti")
							|| name.endsWith(".dis")
							|| name.endsWith(".dic")
							|| dir.getName().equals("manifest"); }},
				new Htmlizer() {
					public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) {
						for (File f : sources) {
							try {
								File outputFile = new File(outputDirectory, relativize(asURI(sourceDirectory), asURI(f)) + ".html");
								BufferedReader reader = new BufferedReader(new FileReader(f));
								Serializer serializer = new Processor(false).newSerializer(outputFile);
								serializer.setOutputProperty(Serializer.Property.METHOD, "xhtml");
								serializer.setOutputProperty(Serializer.Property.VERSION, "1.1");
								serializer.setOutputProperty(Serializer.Property.INDENT, "no");
								XMLStreamWriter writer = serializer.getXMLStreamWriter();
								writer.writeStartElement("html");
								writer.writeStartElement("head");
								writer.writeAttribute("prefix", "dp2: http://www.daisy.org/ns/pipeline/");
								writer.writeStartElement("link");
								writer.writeAttribute("rev", "dp2:doc");
								writer.writeAttribute("href", relativize(asURI(outputFile), asURI(f)).toASCIIString());
								writer.writeEndElement();
								writer.writeStartElement("link");
								writer.writeAttribute("rel", "rdf:type");
								writer.writeAttribute("href", "http://www.daisy.org/ns/pipeline/source");
								writer.writeEndElement();
								writer.writeStartElement("style");
								writer.writeAttribute("type", "text/css");
								writer.writeCharacters(".code { white-space: pre; font-family: monospace; }\n" +
								                       ".code-liblouis-comment { color: #aaaaaa; }\n" +
								                       ".code-liblouis-feature-key { font-weight: bold; color: #ccaa00; }\n" +
								                       ".code-liblouis-feature-value { font-weight: bold; color: #aa3311; }");
								writer.writeEndElement();
								writer.writeEndElement();
								writer.writeStartElement("body");
								writer.writeStartElement("div");
								writer.writeAttribute("class", "code");
								boolean inHeader = true;
								Pattern featureLine = Pattern.compile("(#\\+\\s*)([0-9A-Za-z_-]+)(\\s*:\\s*)([0-9A-Za-z_-]+)(\\s*)");
								Pattern commentLine = Pattern.compile("\\s*#.*");
								Pattern includeLine = Pattern.compile("(include\\s*)([^\\s]+)(\\s*)");
								String line;
								while ((line = reader.readLine()) != null) {
									if (line.length() == 0) {
										writer.writeCharacters("\n");
										continue; }
									if (inHeader) {
										Matcher m = featureLine.matcher(line);
										if (m.matches()) {
											writer.writeStartElement("span");
											writer.writeAttribute("class", "code-liblouis-comment");
											writer.writeCharacters(m.group(1));
											writer.writeStartElement("span");
											writer.writeAttribute("class", "code-liblouis-feature-key");
											writer.writeCharacters(m.group(2));
											writer.writeEndElement();
											writer.writeCharacters(m.group(3));
											writer.writeStartElement("span");
											writer.writeAttribute("class", "code-liblouis-feature-value");
											writer.writeCharacters(m.group(4));
											writer.writeEndElement();
											writer.writeCharacters(m.group(5));
											writer.writeEndElement();
											writer.writeCharacters("\n");
											continue; }
										else
											inHeader = false; }
									if (commentLine.matcher(line).matches()) {
										writer.writeStartElement("span");
										writer.writeAttribute("class", "code-liblouis-comment");
										writer.writeCharacters(line);
										writer.writeEndElement(); }
									else {
										Matcher m = includeLine.matcher(line);
										if (m.matches()) {
											writer.writeCharacters(m.group(1));
											writer.writeStartElement("a");
											writer.writeAttribute("class", "source");
											writer.writeAttribute("href", m.group(2));
											writer.writeCharacters(m.group(2));
											writer.writeEndElement();
											writer.writeCharacters(m.group(3)); }
										else
											writer.writeCharacters(line); }
									writer.writeCharacters("\n");
								}
								reader.close();
								writer.writeEndElement();
								writer.writeEndElement();
								writer.writeEndElement();
								writer.close();
							} catch (Exception e) {
								throw new RuntimeException("Error processing file " + f, e);
							}
						}
					}
				}
			);
			Multimap<Htmlizer,File> index = Multimaps.index(
				sources,
				new Function<File,Htmlizer>() {
					public Htmlizer apply(File f) {
						for (Map.Entry<FilenameFilter,Htmlizer> kv : htmlizers.entrySet())
							if (kv.getKey().accept(f.getParentFile(), f.getName()))
								return kv.getValue();
						throw new RuntimeException("File type of " + f + " not recognized."); }});
			outputDirectory.mkdirs();
			for (Map.Entry<Htmlizer,Collection<File>> kv : index.asMap().entrySet())
				kv.getKey().run(kv.getValue(), sourceDirectory, outputDirectory);
		} catch (Throwable e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	private static interface Htmlizer {
		public void run(Iterable<File> files, File sourceDirectory, File outputDirectory);
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
