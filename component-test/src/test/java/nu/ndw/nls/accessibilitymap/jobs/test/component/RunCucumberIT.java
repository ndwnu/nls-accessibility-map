package nu.ndw.nls.accessibilitymap.jobs.test.component;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import io.cucumber.spring.CucumberContextConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.EnableDockerDriver;
import nu.ndw.nls.springboot.test.component.driver.wiremock.EnableWireMockDriver;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "nu.ndw.nls.accessibilitymap.jobs.test.component, nu.ndw.nls.springboot.test.component.state")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CucumberContextConfiguration
@EnableDockerDriver
@EnableWireMockDriver
public class RunCucumberIT {

}
