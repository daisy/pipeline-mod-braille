package org.daisy.pipeline.liblouis.internal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.daisy.pipeline.liblouis.Liblouis;
import org.daisy.pipeline.liblouis.LiblouisTableRegistry;
import org.daisy.pipeline.liblouis.Utilities.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiblouisJnaImpl implements Liblouis {

	private final Iterable<URL> jarURLs;
	private final File nativeDirectory;
	private final LiblouisTableRegistry tableRegistry;
	private Constructor<?> Translator;
	private Method translate;
	private Method getBraille;
	private boolean loaded = false;
	
	public LiblouisJnaImpl(Iterable<URL> jarURLs, Iterable<URL> nativeURLs, File unpackDirectory, LiblouisTableRegistry tableRegistry) {
		this.jarURLs = jarURLs;
		Iterator<URL> nativeURLsIterator = nativeURLs.iterator();
		if (!nativeURLsIterator.hasNext())
			throw new IllegalArgumentException("Argument nativeURLs must not be empty");
		for (File file : Files.unpack(nativeURLsIterator, unpackDirectory))
			if (!file.getName().endsWith(".dll")) Files.chmod775(file);
		nativeDirectory = unpackDirectory;
		this.tableRegistry = tableRegistry;
	}
	
	public LiblouisJnaImpl load() {
		if (!loaded) {
			try {
				ClassLoader classLoader = new LiblouisJnaClassLoader(jarURLs, nativeDirectory);
				Class<?> TranslatorClass = classLoader.loadClass("org.liblouis.Translator");
				Class<?> TranslationResultClass = classLoader.loadClass("org.liblouis.TranslationResult");
				Translator = TranslatorClass.getConstructor(String.class);
				translate = TranslatorClass.getMethod("translate", String.class);
				getBraille = TranslationResultClass.getMethod("getBraille");
				logger.debug("Loading liblouis service: version {}", TranslatorClass.getMethod("version").invoke(null)); }
			catch (Exception e) {
				logger.error("Could not load liblouis service");
				throw new RuntimeException("Could not load liblouis service", e); }
			loaded = true; }
		return this;
	}
	
	public void unload() {
		if (!loaded) return;
		Translator = null;
		translate = null;
		getBraille = null;
		translatorCache.clear();
		System.gc();
		loaded = false;
	}
	
	public String translate(String tables, String text) {
		if (!loaded) load();
		try {
			tables = tableRegistry.resolveTableURL(tables);
			text = squeeze(text);
			return (String)getBraille.invoke(translate.invoke(getTranslator(tables), text)); }
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause()); }
		catch (Exception e) {
			logger.error("Error during liblouis translation");
			throw new RuntimeException(e); }
	}
	
	private Map<String,Object> translatorCache = new HashMap<String,Object>();

	private Object getTranslator(String tables) {
		try {
			Object translator = translatorCache.get(tables);
			if (translator == null) {
				translator = Translator.newInstance(tables);
				translatorCache.put(tables, translator); }
			return translator; }
		catch (Exception e) {
			throw new RuntimeException(e); }
	}

	private static String squeeze(final String in) {
		return in.replaceAll("(?:\\p{Z}|\\s)+", " ");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisJnaImpl.class);
}