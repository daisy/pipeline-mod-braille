import java.io.File;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import com.google.common.base.Optional;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScriptService;

import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiTest {
	
	@Inject
	private ScriptRegistry scriptRegistry;
	
	@Inject
	private DatatypeRegistry datatypeRegistry;
	
	@Test
	public void testDatatype() {
		XProcScriptService script = scriptRegistry.getScript("script");
		XProcOptionMetadata option = script.load().getOptionMetadata(new QName("option"));
		Optional<DatatypeService> datatype = datatypeRegistry.getDatatype(option.getDatatype());
		assertTrue(datatype.isPresent());
		// TODO: implement
		//assertTrue(datatype.get().validate("one").isValid());
	}
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				logbackClassic(),
				mavenBundle("org.daisy.pipeline:modules-registry:?"),
				mavenBundle("org.daisy.pipeline:framework-core:?")
			)
		);
	}
}
