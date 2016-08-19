package org.daisy.pipeline.braille.maven.plugin;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.DirectoryScanner;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.calabash.Calabash;
		
/**
 * @goal xprocdoc
 */
public class XProcDocMojo extends AbstractMojo {
	
	/**
	 * @parameter expression="${project.basedir}/src/main/resources"
	 * @required
	 */
	private File sourceDirectory;
	
	/**
	 * @parameter
	 */
	private String includes;
	private final String defaultIncludes = "xml/**/*.xpl";
	
	/**
	 * @parameter expression="${project.build.directory}/generated-resources/doc"
	 * @required
	 */
	private File outputDirectory;
	
	public void execute() throws MojoFailureException {
		if (!sourceDirectory.exists()) {
			getLog().info("Directory " + sourceDirectory + " does not exist. Skipping xprocdoc goal.");
			return; }
		XProcEngine engine = new Calabash();
		List<String> source = new ArrayList<String>();
		if (includes == null)
			includes = defaultIncludes;
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(sourceDirectory);
		scanner.setIncludes(includes.replace(" ", "").split(","));
		scanner.scan();
		for (String f : scanner.getIncludedFiles())
			source.add(asURI(new File(sourceDirectory, f)).toASCIIString());
		outputDirectory.mkdirs();
		try {
			engine.run(asURI(XProcDocMojo.class.getResource("/xprocdoc/xprocdoc.xpl")).toASCIIString(),
			           ImmutableMap.of("source", source),
			           null,
			           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
			                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
			           null); }
		catch (XProcExecutionException e) {
			throw new MojoFailureException(e.getMessage()); }
	}
	
	public static URI asURI(Object o) {
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
}
