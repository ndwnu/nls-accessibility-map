package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public record Assertions(
        @NotNull GenericAssertion genericAssertion,
        @NotNull List<Section> sections) {

}
