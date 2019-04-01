package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.Hyphenator;

public class DefaultLineBreakerTest {
	
	@Test
	public void testNonStandardLineBreaking() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		assertEquals(
			"BUSS\n" +
			"TOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 4));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 5));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 6));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 7));
	}
	
	@Test
	public void testFullLine() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		assertEquals(
			"ABCDEF",
			fillLines(translator.lineBreakingFromStyledText().transform(text("abcdef")), 6));
		assertEquals(
			"ABCDE ",
			fillLines(translator.lineBreakingFromStyledText().transform(text("abcde  ")), 6));
		assertEquals(
			"ABCDEF",
			fillLines(translator.lineBreakingFromStyledText().transform(text("abcdef ")), 6));
		assertEquals(
			"ABCDEF",
			fillLines(translator.lineBreakingFromStyledText().transform(text("abcdef ")), 6));
		{
			BrailleTranslator.LineIterator lines = translator.lineBreakingFromStyledText().transform(text("abcdef"));
			assertEquals("", lines.nextTranslatedRow(3, false));
			assertEquals("ABCDEF", lines.nextTranslatedRow(100, false));
		}
		{
			BrailleTranslator.LineIterator lines = translator.lineBreakingFromStyledText().transform(text("abcdef"));
			assertEquals("", lines.nextTranslatedRow(0, false));
			assertEquals("ABCDEF", lines.nextTranslatedRow(100, false));
		}
	}
	
	@Test
	public void testProhibitBreak() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		{
			List<BrailleTranslator.LineIterator> segments = new ArrayList<>(); {
				segments.add(translator.lineBreakingFromStyledText().transform(text("abc\u2060")));
				segments.add(translator.lineBreakingFromStyledText().transform(text("def")));
			}
			assertEquals(
				"ABCDEF",
				fillLines(segments.iterator(), 9));
		}
		{
			List<BrailleTranslator.LineIterator> segments = new ArrayList<>(); {
				segments.add(translator.lineBreakingFromStyledText().transform(text("abc def\u2060")));
				segments.add(translator.lineBreakingFromStyledText().transform(text("ghi")));
			}
			assertEquals(
				"ABC\n" +
				"DEFGHI",
				fillLines(segments.iterator(), 9));
		}
		{
			List<BrailleTranslator.LineIterator> segments = new ArrayList<>(); {
				segments.add(translator.lineBreakingFromStyledText().transform(text("abc def\u2060")));
				segments.add(translator.lineBreakingFromStyledText().transform(text("ghi")));
			}
			assertEquals(
				"ABC DEFGHI",
				fillLines(segments.iterator(), 10));
		}
		{
			List<BrailleTranslator.LineIterator> segments = new ArrayList<>(); {
				segments.add(translator.lineBreakingFromStyledText().transform(text("abc def  \u2060")));
				segments.add(translator.lineBreakingFromStyledText().transform(text("ghi")));
			}
			assertEquals(
				"ABC DEF\n" +
				" GHI",
				fillLines(segments.iterator(), 11));
		}
		{
			List<BrailleTranslator.LineIterator> segments = new ArrayList<>(); {
				segments.add(translator.lineBreakingFromStyledText().transform(text("abc def  \u2060")));
				segments.add(translator.lineBreakingFromStyledText().transform(text("ghi")));
			}
			assertEquals(
				"ABC DEF  GHI",
				fillLines(segments.iterator(), 12));
		}
	}
	
	@Test
	public void testEmpty() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		assertEquals(
			" ",
			fillLines(translator.lineBreakingFromStyledText().transform(text("   ")), 10));
		assertEquals(
			"",
			fillLines(translator.lineBreakingFromStyledText().transform(text("\u200B")), 10));
	}
	
	@Test
	public void testHardLineBreak() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		assertEquals(
			"ABC\n" +
			"DEF",
			fillLines(translator.lineBreakingFromStyledText().transform(text("abc\u2028def")), 10));
	}
	
	@Test
	public void testDisallowHyphenation() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		BrailleTranslator.LineIterator i
			= translator.lineBreakingFromStyledText().transform(text("abc­def abc­def abc­def abc­def"));
		assertEquals("ABCDEF ABC-", i.nextTranslatedRow(12, true, false));
		assertEquals("DEF",         i.nextTranslatedRow(6,  true, false));
		assertEquals("ABCDEF",      i.nextTranslatedRow(12, true, true));
		assertEquals("ABC-",        i.nextTranslatedRow(5,  true, false));
		assertEquals("DEF",         i.nextTranslatedRow(5,  true, false));
		assertFalse(i.hasNext());
	}
	
	private static class TestHyphenator extends AbstractHyphenator {
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private static final FullHyphenator fullHyphenator = new FullHyphenator() {
			public String transform(String text) {
				if (text.contains("busstopp"))
					throw new RuntimeException("text contains non-standard break points");
				else
					return text;
			}
			public String[] transform(String[] text) {
				String[] r = new String[text.length];
				for (int i = 0; i < r.length; i++)
					r[i] = transform(text[i]);
				return r;
			}
		};
		
		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}
		
		private final LineBreaker lineBreaker = new AbstractHyphenator.util.DefaultLineBreaker() {
			protected Break breakWord(String word, int limit, boolean force) {
				if (limit >= 4 && word.equals("busstopp"))
					return new Break("bussstopp", 4, true);
				else if (limit >= word.length())
					return new Break(word, word.length(), false);
				else if (force)
					return new Break(word, limit, false);
				else
					return new Break(word, 0, false);
			}
		};
	}
	
	private static class TestTranslator extends AbstractBrailleTranslator {
		
		private final Hyphenator hyphenator;
		
		private TestTranslator(Hyphenator hyphenator) {
			this.hyphenator = hyphenator;
		}
		
		private final static Pattern WORD_SPLITTER = Pattern.compile("[\\x20\t\\n\\r\\u2800\\xA0]+");
		
		private final LineBreakingFromStyledText lineBreaker = new AbstractBrailleTranslator.util.DefaultLineBreaker(' ', '-', null) {
			protected BrailleStream translateAndHyphenate(final Iterable<CSSStyledText> styledText) {
				return new BrailleStream() {
					int pos = 0;
					String text; {
						text = "";
						for (CSSStyledText t : styledText)
							text += t.getText();
						if (text.replaceAll("[\u00ad\u200b]","").isEmpty())
							text = "";
					}
					public boolean hasNext() {
						return pos < text.length();
					}
					public String next(int limit, boolean force, boolean allowHyphens) {
						String next = "";
						int start = pos;
						int end = text.length();
						int available = limit;
						if (end - start <= available) {
							next = text.substring(start);
							pos = end; }
						else {
							try {
								next = hyphenator.asFullHyphenator().transform(text.substring(pos));
								pos = end; }
							catch (Exception e) {
								Matcher m = WORD_SPLITTER.matcher(text.substring(pos));
								boolean foundSpace;
								while ((foundSpace = m.find()) || pos < end) {
									int wordStart = pos;
									int wordEnd = foundSpace ? start + m.start() : end;
									if (wordEnd > wordStart) {
										String word = text.substring(wordStart, wordEnd);
										if (word.length() <= available) {
											next += word;
											available -= word.length();
											pos += word.length(); }
										else if (available <= 0)
											break;
										else {
											Hyphenator.LineIterator lines = hyphenator.asLineBreaker().transform(word);
											String line = lines.nextLine(available, force, allowHyphens);
											if (line.length() == available && lines.lineHasHyphen()) {
												lines.reset();
												line = lines.nextLine(available - 1, force, allowHyphens); }
											if (line.length() > 0) {
												next += line;
												if (lines.lineHasHyphen())
													next += "\u00ad";
												pos += line.length();
												text = text.substring(0, pos) + lines.remainder(); }
											break; }}
									if (foundSpace) {
										String space = text.substring(pos, start + m.end());
										next += space;
										available -= space.length();
										pos = space.length(); }}}}
						return translate(next);
					}
					public Character peek() {
						return text.charAt(pos);
					}
					public String remainder() {
						return translate(text.substring(pos));
					}
					@Override
					public Object clone() {
						try {
							return super.clone();
						} catch (CloneNotSupportedException e) {
							throw new InternalError("coding error");
						}
					}
					private String translate(String s) {
						return s.toUpperCase();
					}
				};
			}
		};
		
		@Override
		public LineBreakingFromStyledText lineBreakingFromStyledText() {
			return lineBreaker;
		}
	}
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}
	
	private static String fillLines(BrailleTranslator.LineIterator lines, int width) {
		StringBuilder sb = new StringBuilder();
		while (lines.hasNext()) {
			sb.append(lines.nextTranslatedRow(width, true));
			if (lines.hasNext())
				sb.append('\n'); }
		return sb.toString();
	}
	
	private static String fillLines(Iterator<BrailleTranslator.LineIterator> segments, int width) {
		return fillLines(aggregate(segments), width);
	}
	
	private static BrailleTranslator.LineIterator aggregate(final Iterator<BrailleTranslator.LineIterator> segmentsIterator) {
		return new BrailleTranslator.LineIterator() {
			
			private List<BrailleTranslator.LineIterator> segments = newArrayList(segmentsIterator);
			
			public boolean hasNext() {
				while (true) {
					if (segments.isEmpty())
						return false;
					if (segments.get(0).hasNext())
						return true;
					segments = segments.subList(1, segments.size());
				}
			}
			
			public String nextTranslatedRow(int limit, boolean force, boolean allowHyphens) {
				String row = fillLine(segments, limit, false, allowHyphens);
				if (force && row.isEmpty())
					row = fillLine(segments, limit, true, allowHyphens);
				return row;
			}
			
			private String fillLine(List<BrailleTranslator.LineIterator> segments, int width, boolean force, boolean allowHyphens) {
				if (!segments.get(0).hasNext()) {
					segments.remove(0);
					return fillLine(segments, width, force, allowHyphens);
				}
				if (segments.get(0).countRemaining() < width
				    || (segments.get(0).countRemaining() == width && segments.size() == 1)) {
					String line = segments.get(0).getTranslatedRemainder();
					if (segments.size() == 1) {
						segments.remove(0);
						return line;
					}
					String s = fillLine(segments.subList(1, segments.size()), width - line.length(), force, allowHyphens);
					if (!s.isEmpty()) {
						segments.remove(0);
						return line + s;
					}
				}
				return segments.get(0).nextTranslatedRow(width, force, allowHyphens);
			}
			
			public BrailleTranslator.LineIterator copy() {
				List<BrailleTranslator.LineIterator> segmentsCopy = new ArrayList<>(); {
					for (BrailleTranslator.LineIterator s : segments) {
						segmentsCopy.add(s.copy());
					}
				}
				return aggregate(segmentsCopy.iterator());
			}
			
			public String getTranslatedRemainder()       { throw new UnsupportedOperationException(); }
			public int countRemaining()                  { throw new UnsupportedOperationException(); }
			public boolean supportsMetric(String metric) { throw new UnsupportedOperationException(); }
			public double getMetric(String metric)       { throw new UnsupportedOperationException(); }
		};
	}
}
