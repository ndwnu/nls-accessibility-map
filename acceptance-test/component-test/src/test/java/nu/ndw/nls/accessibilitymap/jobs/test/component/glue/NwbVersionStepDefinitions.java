package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityVersionSupplier;
import nu.ndw.nls.db.nwb.jooq.repositories.JooqNwbVersionCrudRepository;

@RequiredArgsConstructor
public class NwbVersionStepDefinitions {

    private final JooqNwbVersionCrudRepository jooqNwbVersionCrudRepository;

    @Given("an nwb version {word}")
    public void anNwbVersion(String version) {
        jooqNwbVersionCrudRepository.insert(AccessibilityVersionSupplier.create(version));
    }
}
