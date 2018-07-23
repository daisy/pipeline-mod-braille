package org.daisy.pipeline.braille.libhyphen.impl;

import java.util.Map;

import org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "libhyphen-libreoffice-tables",
	service = { LibhyphenTablePath.class },
	property = {
		"identifier:String=http://www.libreoffice.org/dictionaries/hyphen/",
		"path:String=/hyphen"
	}
)
public class LibreOfficeTablePath extends LibhyphenTablePath {
	
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, LibreOfficeTablePath.class);
	}
}
