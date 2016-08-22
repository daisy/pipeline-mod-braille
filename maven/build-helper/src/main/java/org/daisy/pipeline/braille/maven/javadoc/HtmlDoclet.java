package org.daisy.pipeline.braille.maven.javadoc;

// http://www.oracle.com/technetwork/java/javase/documentation/index-137483.html#sourcedoclet
// -> downloaded openjdk source from http://download.java.net/openjdk/jdk7/

import com.sun.javadoc.*;

public class HtmlDoclet {
	
	public static int optionLength(String option) {
		return org.daisy.pipeline.braille.maven.javadoc.com.sun.tools.doclets.formats.html.HtmlDoclet.optionLength(option);
	}
	
	public static boolean start(RootDoc root) {
		return org.daisy.pipeline.braille.maven.javadoc.com.sun.tools.doclets.formats.html.HtmlDoclet.start(root);
	}
	
	public static boolean validOptions(String[][] options,
	                                   DocErrorReporter reporter) {
		return org.daisy.pipeline.braille.maven.javadoc.com.sun.tools.doclets.formats.html.HtmlDoclet.validOptions(options, reporter);
	}
	
	public static LanguageVersion languageVersion() {
		return org.daisy.pipeline.braille.maven.javadoc.com.sun.tools.doclets.formats.html.HtmlDoclet.languageVersion();
	}
}
