package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "liblouis-hyphenation-tables",
	service = { LibhyphenTablePath.class },
	property = {
		"identifier:String=http://www.liblouis.org/tables/",
		"path:String=/tables",
		"includes:String=hyph_*.dic"
	}
)
public class LiblouisHyphenationTables extends LibhyphenTablePath {
	
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, LiblouisHyphenationTables.class);
	}
}
