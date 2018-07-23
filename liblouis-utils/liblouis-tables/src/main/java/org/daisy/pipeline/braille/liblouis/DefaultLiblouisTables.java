package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "default-liblouis-tables",
	service = { LiblouisTablePath.class },
	property = {
		"identifier:String=http://www.liblouis.org/tables/",
		"path:String=/tables"
	}
)
public class DefaultLiblouisTables extends LiblouisTablePath {
	
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, DefaultLiblouisTables.class);
	}
}
