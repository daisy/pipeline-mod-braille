package org.daisy.pipeline.braille.liblouis.impl;

import org.daisy.pipeline.braille.liblouis.LiblouisTranslator.Typeform;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LiblouisJnaImplTest {
	
	@Test
	public void testTypeformFromInlineCSS() {
		assertEquals(Typeform.BOLD + Typeform.UNDERLINE,
		             LiblouisJnaImpl.typeformFromInlineCSS(
			             " text-decoration: underline ;font-weight: bold  ; hyphens:auto; color: #FF00FF "));
	}

	@Test
	public void testTextFromTextTransform() {
		assertEquals("IK BEN MOOS",
			LiblouisJnaImpl.textFromTextTransform("Ik ben Moos",
				" uppercase "));
		assertEquals("ik ben moos",
			LiblouisJnaImpl.textFromTextTransform("Ik ben Moos",
				" lowercase "));
		assertEquals("ik ben moos",
			LiblouisJnaImpl.textFromTextTransform("Ik ben Moos",
				" uppercase lowercase "));
		assertEquals("Ik ben Moos",
			LiblouisJnaImpl.textFromTextTransform("Ik ben Moos",
				" foo bar "));
	}
	
	@Test
	public void testTypeformFromTextTransform() {
		assertEquals(Typeform.BOLD + Typeform.UNDERLINE,
		             LiblouisJnaImpl.typeformFromTextTransform(" louis-bold  ital louis-under foo "));
	}
}
