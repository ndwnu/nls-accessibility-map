package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.issue.IssueDriver;

@Slf4j
@RequiredArgsConstructor
public class IssueExportStepDefinitions {

    private final IssueDriver issueDriver;

    @Given("with issues sent to issue api")
    public void issueApi() {
        issueDriver.stubIssueApiRequest();
    }

    @Then("we expect {int} issues to be created")
    public void issuesShouldBeCreated(int number) {
        assertThat(issueDriver.getNumberOfIssuesCreated()).isEqualTo(number);
    }
}
