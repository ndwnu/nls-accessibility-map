package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.issue.IssueDriver;
import nu.ndw.nls.springboot.test.component.cucumber.StepArgumentParser;

@Slf4j
@RequiredArgsConstructor
public class IssueExportStepDefinitions {

    private final IssueDriver issueDriver;

    @Given("with issues sent to issue api")
    public void issueApi() {
        issueDriver.stubIssueApiRequest();
    }

    @Then("we expect the following issues to be reported")
    public void expectCreatedIssues(List<String> issueFiles) {

        issueFiles.forEach(issueDriver::verifyIssueCreated);

        issueDriver.verifyNumberOfCreatedIssues(issueFiles.size());
    }

    @Then("we expect the report to be marked as completed for trafficSignTypes {string}")
    public void reportComplete(String trafficSignTypes) {

        issueDriver.verifyReportComplete(StepArgumentParser.parseStringAsSet(trafficSignTypes));
    }
}
