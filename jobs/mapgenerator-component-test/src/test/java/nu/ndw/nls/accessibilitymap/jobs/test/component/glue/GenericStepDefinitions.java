package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven.MavenDriver;

@RequiredArgsConstructor
public class GenericStepDefinitions {

    private final MavenDriver mavenDriver;


    @Given("buildProject")
    public void buildProject() {

        mavenDriver.buildRelatedProjectToComponentTest();
    }
}
