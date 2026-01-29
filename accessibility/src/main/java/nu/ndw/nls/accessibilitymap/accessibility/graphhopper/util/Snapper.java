package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.PointMatchService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Snapper {

    private final PointMatchService pointMatchService;

    public Optional<Snap> snapLocation(NetworkGraphHopper networkGraphHopper, Location location) {

        if (Objects.isNull(location)) {
            return Optional.empty();
        }

        return pointMatchService.match(networkGraphHopper, location.point())
                .map(CandidateMatch::getSnappedPoint)
                .filter(Geometry::isValid)
                .map(snappedPoint -> networkGraphHopper.getLocationIndex().findClosest(
                        snappedPoint.getY(),
                        snappedPoint.getX(),
                        EdgeFilter.ALL_EDGES));
    }

    public Optional<Snap> snapRestriction(NetworkGraphHopper networkGraphHopper, Restriction restriction) {
        Snap snap = networkGraphHopper.getLocationIndex()
                .findClosest(
                        restriction.networkSnappedLatitude(),
                        restriction.networkSnappedLongitude(),
                        edgeIteratorState -> restrictionMatchesEdge(restriction, networkGraphHopper.getEncodingManager(), edgeIteratorState)
                );
        if (!snap.isValid()) {
            log.debug(
                    "No road section present for restriction '{}' that could be linked to the nwb map in the Graph Hopper network.",
                    restriction);
        }

        return snap.isValid() ? Optional.of(snap) : Optional.empty();
    }

    private static boolean restrictionMatchesEdge(
            Restriction restriction,
            EncodingManager encodingManager,
            EdgeIteratorState edgeIteratorState) {

        int encodedRoadSectionId = edgeIteratorState.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
        return encodedRoadSectionId == restriction.roadSectionId();
    }
}
