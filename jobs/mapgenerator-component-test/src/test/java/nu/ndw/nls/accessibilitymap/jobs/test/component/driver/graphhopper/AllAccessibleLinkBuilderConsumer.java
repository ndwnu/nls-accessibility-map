package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import java.util.function.Consumer;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
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
