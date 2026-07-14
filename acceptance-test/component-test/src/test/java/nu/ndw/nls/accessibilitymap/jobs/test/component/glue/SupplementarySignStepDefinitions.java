package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.SupplementaryTrafficSignDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.SupplementaryTrafficSign;

@RequiredArgsConstructor
public class SupplementarySignStepDefinitions {

    private final SupplementaryTrafficSignDriver supplementaryTrafficSignDriver;

    @Given("with supplementary traffic sign(s)")
    public void withSupplementaryTrafficSigns(List<SupplementaryTrafficSign> supplementaryTrafficSigns) {
        supplementaryTrafficSignDriver.addTraficSignConditions(supplementaryTrafficSigns);
    }
}
