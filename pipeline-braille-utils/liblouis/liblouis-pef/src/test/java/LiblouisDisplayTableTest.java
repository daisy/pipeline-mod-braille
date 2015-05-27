import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.Table;

import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Provider.DispatchingProvider;
import org.daisy.pipeline.braille.pef.TableProvider;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.bundlesAndDependencies;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.forThisPlatform;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiblouisDisplayTableTest {
	
	@Test
	public void testDisplayTableProvider() {
		Provider<String,Table> tableProvider = getProvider(TableProvider.class);
		BrailleConverter converter = tableProvider.get("(liblouis-table:'foobar.dis')").iterator().next().newBrailleConverter();
		assertEquals("⠋⠕⠕⠃⠁⠗", converter.toBraille("foobar"));
		assertEquals("foobar", converter.toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			logbackBundles(),
			felixDeclarativeServices(),
			domTraversalPackage(),
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("net.java.dev.jna").artifactId("jna").versionAsInProject(),
			mavenBundle().groupId("org.liblouis").artifactId("liblouis-java").versionAsInProject(),
			mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.antlr-runtime").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("jstyleparser").versionAsInProject(),
			mavenBundle().groupId("org.daisy.braille").artifactId("brailleUtils-core").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("jing").versionAsInProject(),
			bundlesAndDependencies("org.daisy.pipeline.calabash-adapter"),
			brailleModule("common-utils"),
			brailleModule("css-core"),
			brailleModule("pef-core"),
			brailleModule("liblouis-core"),
			forThisPlatform(brailleModule("liblouis-native")),
			thisBundle("org.daisy.pipeline.modules.braille", "liblouis-pef"),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/table_paths/"),
			junitBundles()
		);
	}
	
	@Inject
	private BundleContext context;
	
	private <T> Provider<String,T> getProvider(Class<? extends Provider<String,T>> providerClass) {
		List<Provider<String,T>> providers = new ArrayList<Provider<String,T>>();
		try {
			for (ServiceReference<? extends Provider<String,T>> ref : context.getServiceReferences(providerClass, null))
				providers.add(context.getService(ref)); }
		catch (InvalidSyntaxException e) {
			throw new RuntimeException(e); }
		return DispatchingProvider.<String,T>newInstance(providers);
	}
}
