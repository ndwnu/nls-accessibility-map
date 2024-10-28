package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import org.locationtech.jts.geom.LineString;

@Builder
@Getter
public class Link {

    private AccessibilityLink accessibilityLink;

    private LineString rijksDiehoekLineString;

    private LineString wgs84LineString;
}
