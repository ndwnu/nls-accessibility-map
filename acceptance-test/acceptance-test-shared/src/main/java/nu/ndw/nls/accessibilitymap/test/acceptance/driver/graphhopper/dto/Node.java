package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

@Builder
@Getter
public final class Node {

    private final long id;

    private final double latitude;

    private final double longitude;

    @Default
    private final List<Link> links = new ArrayList<>();

    public Coordinate getLatLongAsCoordinate() {
        return new Coordinate(longitude, latitude);
    }

    public void addLink(Link link) {

        links.add(link);
    }

    public List<Link> getCommonLinks(Node otherNode) {

        List<Link> linksCopy = new ArrayList<>(otherNode.links);
        linksCopy.retainAll(links);

        return linksCopy;
    }
}
