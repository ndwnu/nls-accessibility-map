package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import org.locationtech.jts.geom.Coordinate;

@Builder
@Getter
public final class Node {

    private final long id;

    private final Coordinate coordinate;

    @Default
    private final List<AccessibilityLink> links = new ArrayList<>();

    public void addLink(AccessibilityLink link) {

        links.add(link);
    }

    public List<AccessibilityLink> getCommonLinks(Node otherNode) {

        List<AccessibilityLink> linksCopy = new ArrayList<>(otherNode.links);
        linksCopy.retainAll(links);

        return linksCopy;
    }
}
