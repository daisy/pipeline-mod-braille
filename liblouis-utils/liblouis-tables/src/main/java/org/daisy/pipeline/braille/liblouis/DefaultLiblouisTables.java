package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

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
	@Override
	public void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		super.activate(context, properties);
	}
}
