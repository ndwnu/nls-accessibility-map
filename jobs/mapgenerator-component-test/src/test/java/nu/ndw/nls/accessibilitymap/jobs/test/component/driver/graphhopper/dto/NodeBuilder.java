package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto;

import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.utils.LongSequenceSupplier;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

@Component
public class NodeBuilder {

    private final LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

    public Node build(double x, double y) {

        return Node.builder()
                .id(longSequenceSupplier.next())
                .coordinate(new Coordinate(x, y))
                .build();
    }
}
