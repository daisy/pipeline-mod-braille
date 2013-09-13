package org.daisy.pipeline.braille;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Booleans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Utilities {
	
	public static interface Function0<T> {
		public T apply();
	}
	
	public static interface Function2<T1,T2,T3> {
		public T3 apply(T1 object1, T2 object2);
	}
	
	public static class Pair<T1,T2> {
		public final T1 _1;
		public final T2 _2;
		public Pair(T1 _1, T2 _2) {
			this._1 = _1;
			this._2 = _2;
		}
	}
	
	public static abstract class Functions {
		
		public static Function0<Void> noOp = new Function0<Void> () {
			public Void apply() { return null; }};
	}
	
	public static abstract class Predicates {
		
		private static Predicate<String> matchesRegexPattern(final Pattern pattern) {
			return new Predicate<String>() {
				public boolean apply(String s) {
					return pattern.matcher(s).matches(); }};
		}
		
		public static Predicate<String> matchesRegexPattern(final String pattern) {
			return matchesRegexPattern(Pattern.compile(pattern));
		}
		
		public static Predicate<String> matchesGlobPattern(final String pattern) {
			return matchesRegexPattern(GlobPattern.compile(pattern));
		}
	}
	
	public static abstract class Iterators {
		
		public static <T1,T2> T1 fold(Iterator<T2> iterator, Function2<? super T1,? super T2,? extends T1> function, T1 seed) {
			T1 result = seed;
			while(iterator.hasNext()) result = function.apply(result, iterator.next());
			return result;
		}
		
		public static<T> T reduce(Iterator<T> iterator, Function2<? super T,? super T,? extends T> function) {
			T seed = iterator.next();
			return Iterators.<T,T>fold(iterator, function, seed);
		}
		
		public static <T> Pair<Collection<T>,Collection<T>> partition(Iterator<T> iterator, Predicate<? super T> predicate) {
			Multimap<Boolean,T> map = Multimaps.index(iterator, com.google.common.base.Functions.forPredicate(predicate));
			return new Pair<Collection<T>,Collection<T>>(map.get(true), map.get(false));
		}
	}

	public static abstract class OS {
		
		public static enum Family {
			LINUX ("linux"),
			MACOSX ("macosx"),
			WINDOWS ("windows");
			private final String name;
			private Family(String name) { this.name = name; }
			public String toString() { return name; }
		}
		
		public static Family getFamily() {
			String name = System.getProperty("os.name").toLowerCase();
			if (name.startsWith("windows"))
				return Family.WINDOWS;
			else if (name.startsWith("mac os x"))
				return Family.MACOSX;
			else if (name.startsWith("linux"))
				return Family.LINUX;
			else
				throw new RuntimeException("Unsupported OS: " + name);
		}
		
		public static boolean isWindows() {
			return getFamily() == Family.WINDOWS;
		}
		
		public static boolean isMacOSX() {
			return getFamily() == Family.MACOSX;
		}
		
		public static String getArch() {
			return System.getProperty("os.arch").toLowerCase();
		}
		
		public static boolean is64Bit() {
			return getArch().equals("amd64") || getArch().equals("x86_64");
		}
	}
	
	public static abstract class Strings {
		
		@SuppressWarnings("unchecked")
		public static String join(Iterator<? extends Object> strings, final String separator) {
			if (!strings.hasNext()) return "";
			String seed = strings.next().toString();
			return Iterators.<String,Object>fold(
				(Iterator<Object>)strings,
				new Function2<String,Object,String>() {
					public String apply(String s1, Object s2) {
						return s1 + separator + String.valueOf(s2); }},
				seed);
		}
		
		public static String join(Iterable<?> strings, final String separator) {
			return join(strings.iterator(), separator);
		}
		
		public static String join(Object[] strings, String separator) {
			return join(Arrays.asList(strings), separator);
		}
		
		public static String normalizeSpace(Object object) {
			return String.valueOf(object).replaceAll("\\s+", " ").trim();
		}
		
		public static Pair<String,boolean[]> extractHyphens(String string, char hyphen) {
			StringBuffer unhyphenatedString = new StringBuffer();
			List<Boolean> hyphens = new ArrayList<Boolean>();
			boolean seenHyphen = false;
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c == hyphen)
					seenHyphen = true;
				else {
					unhyphenatedString.append(c);
					hyphens.add(seenHyphen);
					seenHyphen = false; }}
			hyphens.remove(0);
			return new Pair<String,boolean[]>(unhyphenatedString.toString(), Booleans.toArray(hyphens));
		}
		
		public static String insertHyphens(String string, boolean hyphens[], char hyphen) {
			if (string.equals("")) return "";
			if (hyphens.length != string.length()-1)
				throw new RuntimeException("hyphens.length must be equal to string.length() - 1");
			StringBuffer hyphenatedString = new StringBuffer();
			int i; for (i = 0; i < hyphens.length; i++) {
				hyphenatedString.append(string.charAt(i));
				if (hyphens[i])
					hyphenatedString.append(hyphen); }
			hyphenatedString.append(string.charAt(i));
			return hyphenatedString.toString();
		}
	}
	
	public static abstract class Files {
		
		public static File asFile(Object o) {
			try {
				if (o instanceof String)
					return asFile(asURL(o));
				if (o instanceof File)
					return (File)o;
				if (o instanceof URL)
					return asFile(asURI(o));
				if (o instanceof URI)
					return new File((URI)o); }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to File: " + o);
		}
		
		@SuppressWarnings("deprecation")
		public static URL asURL(Object o) {
			try {
				if (o instanceof String)
					return new URL((String)o);
				if (o instanceof File)
					return asURL(asURI(o));
				if (o instanceof URL)
					return (URL)o;
				if (o instanceof URI)
					return new URL(URLDecoder.decode(o.toString())); }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to URL: " + o);
		}
		
		public static URI asURI(Object o) {
			try {
				if (o instanceof String)
					return asURI(asURL(o));
				if (o instanceof File)
					return ((File)o).toURI();
				if (o instanceof URL) {
					URL url = (URL)o;
					return new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()); }
				if (o instanceof URI)
					return (URI)o; }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to URI: " + o);
		}
		
		public static boolean isAbsoluteFile(Object o) {
			if (o instanceof String)
				return ((String)o).startsWith("file:");
			try { asFile(o); }
			catch (Exception e) { return false; }
			return true;
		}
		
		public static String fileName(Object o) {
			if (o instanceof File)
				return ((File)o).getName();
			String file = asURL(o).getFile();
			return file.substring(file.lastIndexOf('/')+1);
		}
		
		public static URL resolve(Object base, Object url) {
			if (url instanceof URI)
				return asURL(asURI(base).resolve((URI)url));
			if (url instanceof String) {
				try { return new URL(asURL(base), url.toString()); }
				catch (MalformedURLException e) { throw new RuntimeException(e); }}
			return asURL(url);
		}
		
		@SuppressWarnings("deprecation")
		public static String relativize(Object base, Object url) {
			return URLDecoder.decode(asURI(base).relativize(asURI(url)).toString());
		}
		
		public static boolean unpack(URL url, File file) {
			if (file.exists()) return false;
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream writer = new FileOutputStream(file);
				url.openConnection();
				InputStream reader = url.openStream();
				byte[] buffer = new byte[153600];
				int bytesRead = 0;
				while ((bytesRead = reader.read(buffer)) > 0) {
					writer.write(buffer, 0, bytesRead);
					buffer = new byte[153600]; }
				writer.close();
				reader.close();
				logger.debug("Unpacking file {} ...", file.getName());
				return true; }
			catch (Exception e) {
				throw new RuntimeException(
						"Exception occured during unpacking of file '" + file.getName() + "'", e); }
		}
		
		public static Iterable<File> unpack(Iterator<URL> urls, File directory) {
			if (!directory.exists()) directory.mkdirs();
			Collection<File> files = new ArrayList<File>();
			while(urls.hasNext()) {
				URL url = urls.next();
				File file = new File(directory.getAbsolutePath(), fileName(url));
				if (unpack(url, file)) files.add(file); }
			return files;
		}
		
		public static void chmod775(File file) {
			if (OS.isWindows()) return;
			try {
				Runtime.getRuntime().exec(new String[] { "chmod", "775", file.getAbsolutePath() }).waitFor();
				logger.debug("Chmodding file {} ...", file.getName());}
			catch (Exception e) {
				throw new RuntimeException(
						"Exception occured during chmodding of file '" + file.getName() + "'", e); }
		}
	}
	
	public static abstract class Locales {
		
		public static Locale parseLocale(String locale) {
			StringTokenizer parser = new StringTokenizer(locale, "-_");
			if (parser.hasMoreTokens()) {
				String lang = parser.nextToken();
				if (parser.hasMoreTokens()) {
					String country = parser.nextToken();
					if (parser.hasMoreTokens()) {
						String variant = parser.nextToken();
						return new Locale(lang, country, variant); }
					else
						return new Locale(lang, country); }
				else
					return new Locale(lang); }
			else
				throw new IllegalArgumentException("Locale '" + locale + "' could not be parsed");
		}
	}
	
	/*
	 * Naive implementation of glob syntax. Assumes the pattern is valid.
	 * @see http://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
	 */
	private static abstract class GlobPattern {
		
		private static Pattern compile(String pattern) {
			return new RegexBuilder(pattern).build();
		}
		
		private static class RegexBuilder {
			
			private final StringCharacterIterator glob;
			private boolean inClass;
			private boolean inGroup;
			
			private RegexBuilder(String globPattern) {
				glob = new StringCharacterIterator(globPattern);
			}
			
			private Pattern build() {
				StringBuilder regex = new StringBuilder();
				regex.append('^');
				inClass = false;
				inGroup = false;
				for (char c = glob.first(); c != CharacterIterator.DONE; c = glob.next()) {
					if (!inClass) {
						if (c == '[') {
							regex.append("[[^/]&&[");
							if (lookAhead() == '!')
								regex.append(glob.next());
							if (lookAhead() == '^')
								regex.append("\\" + glob.next());
							inClass = true; }
						else if (c == '{' && !inGroup) {
							regex.append("(?:(?:");
							inGroup = true; }
						else if (c == '}' && inGroup) {
							regex.append("))");
							inGroup = false; }
						else if (c == ',' && inGroup)
							regex.append(")|(?:");
						else if (c == '*' && lookAhead() == '*') {
							regex.append(".*");
							glob.next(); }
						else if (c == '*')
							regex.append("[^/]*");
						else if (c == '?')
							regex.append("[^/]");
						else if (".^$+]|()".indexOf(c) > -1)
							regex.append("\\" + c);
						else
							regex.append(c); }
					else {
						if (c == ']') {
							regex.append("]]");
							inClass = false; }
						else if (c == '&' && lookAhead() == '&')
							regex.append("\\" + c + glob.next());
						else if (c == '\\')
							regex.append("\\" + c);
						else
							regex.append(c); }}
				regex.append('$');
				return Pattern.compile(regex.toString());
			}
			
			private char lookAhead() {
				char la = glob.next();
				glob.previous();
				return la;
			}
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Utilities.class);
	
}