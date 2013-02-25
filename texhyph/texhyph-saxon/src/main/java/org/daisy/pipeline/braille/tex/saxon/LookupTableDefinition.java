package org.daisy.pipeline.braille.tex.saxon;

import java.net.URL;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.braille.tex.TexHyphenatorTableLookup;
import static org.daisy.pipeline.braille.Utilities.Locales.parseLocale;

public class LookupTableDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("tex",
			"http://code.google.com/p/texhyphj/", "lookup-table");
	
	private TexHyphenatorTableLookup tableLookup = null;
	
	public void bindTableLookup(TexHyphenatorTableLookup tableLookup) {
		this.tableLookup = tableLookup;
	}
	
	public void unbindTableLookup(TexHyphenatorTableLookup tableLookup) {
		this.tableLookup = null;
	}
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 1;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 1;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.SINGLE_STRING };
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_STRING;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		
		return new ExtensionFunctionCall() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public SequenceIterator call(SequenceIterator[] arguments,
					XPathContext context) throws XPathException {
				
				String locale = ((StringValue)arguments[0].next()).getStringValue();
				URL table = tableLookup.lookup(parseLocale(locale));
				if (table != null)
					return SingletonIterator.makeIterator(new StringValue(table.toExternalForm()));
				return EmptyIterator.getInstance();
			}
			
			private static final long serialVersionUID = 1L;
		};
	}
	
	private static final long serialVersionUID = 1L;
}