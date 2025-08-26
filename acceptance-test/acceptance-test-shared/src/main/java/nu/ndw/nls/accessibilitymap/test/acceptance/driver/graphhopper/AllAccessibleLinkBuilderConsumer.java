package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import java.util.function.Consumer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;

public class AllAccessibleLinkBuilderConsumer implements Consumer<AccessibilityLinkBuilder> {

    @Override
    public void accept(AccessibilityLinkBuilder accessibilityLinkBuilder) {

        accessibilityLinkBuilder.accessibility(allAccessible());
    }

    private static DirectionalDto<Boolean> allAccessible() {
        return DirectionalDto.<Boolean>builder()
                .forward(true)
                .reverse(true)
                .build();
    }
}
