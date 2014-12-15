package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.util.Map;
import javax.xml.namespace.QName;

import org.daisy.pipeline.braille.common.util.Tuple3;

public interface XProcTransform extends Transform {
	
	/**
	 * Expose the {@link org.daisy.pipeline.braille.common.Transform} as an
	 * XProc step (an href to an XProc document and a step name) with a set of
	 * options. The step must accept a single document on its "source" port
	 * and produce a single document on its "result" port. Optionally, the
	 * step may produce a document on its "resource-map" output port with
	 * synchronization data between input and ouput.
	 */
	public Tuple3<URI,QName,Map<String,String>> asXProc();
	
	public interface Provider<T extends XProcTransform> extends Transform.Provider<T> {}
	
}
