package io.github.ctgnz.yamlflock.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * The JUnit Platform Suite entrypoint Surefire discovers, running every {@code .feature} file under {@code src/test/resources/features} against the step definitions in this
 * package.
 * <p>
 * These scenarios are the library's specification rather than a regression net over it. The behaviour being described - which properties are written in flow style and which in
 * block - was never exercised by the two projects this emitter came from, so there is no prior output to preserve. What the features say is therefore what the library does, and
 * the printer is written to satisfy them rather than the other way round.
 * <p>
 * {@link SelectClasspathResource} rather than {@code @SelectPackage("features")}: discovery logs a warning suggesting the latter, but against this project's classpath layout it
 * finds nothing at all. The same trap is documented in foxglove, which is where this suite's shape comes from.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.github.ctgnz.yamlflock.cucumber")
public class RunCucumberTest {
}
