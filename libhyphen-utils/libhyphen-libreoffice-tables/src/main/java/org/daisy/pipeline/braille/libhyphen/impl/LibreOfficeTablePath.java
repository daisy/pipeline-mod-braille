package org.daisy.pipeline.braille.libhyphen.impl;

import java.util.Map;

import org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

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
	@Override
	public void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		super.activate(context, properties);
	}
}
