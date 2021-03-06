package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.daisy.pipeline.braille.common.util.OS;

import org.osgi.service.component.ComponentContext;

public class BundledNativePath extends StandardNativePath {
	
	private static final String OS_FAMILY = "os.family";
	
	private BundledResourcePath resourcePath;
	
	protected ResourcePath delegate() {
		return resourcePath;
	}
	
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		if (properties.get(OS_FAMILY) == null
				|| properties.get(OS_FAMILY).toString().isEmpty()) {
			throw new IllegalArgumentException(OS_FAMILY + " property must not be empty"); }
		if (OS.Family.valueOf(properties.get(OS_FAMILY).toString().toUpperCase()) != OS.getFamily())
			throw new Exception(toString() + " does not work on " + OS.getFamily());
		if (properties.get(BundledResourcePath.UNPACK) != null)
			throw new IllegalArgumentException(BundledResourcePath.UNPACK + " property not supported");
		Map<Object,Object> props = new HashMap<Object,Object>(properties);
		props.remove(OS_FAMILY);
		props.put(BundledResourcePath.UNPACK, true);
		props.put(BundledResourcePath.EXECUTABLES, true);
		resourcePath = new BundledResourcePath();
		resourcePath.activate(context, props);
	}
	
	private boolean unpacked = false;
	
	@Override
	public URL resolve(URI resource) {
		URL resolved = super.resolve(resource);
		if (!unpacked && resolved != null) {
			// unpack everything (because of possible dependencies between binaries)
			resourcePath.resolve(getIdentifier());
			unpacked = true; }
		return resolved;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		return super.equals((BundledResourcePath)object);
	}
}
