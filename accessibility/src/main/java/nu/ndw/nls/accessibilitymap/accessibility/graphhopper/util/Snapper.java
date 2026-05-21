package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.EdgeAccessHandler.CAR_ACCESSIBLE_ROADS;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.PointMatchService;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Snapper {

    private final PointMatchService pointMatchService;

    public Optional<Snap> snapLocation(NetworkData networkData, Location location) {

        if (Objects.isNull(location)) {
            return Optional.empty();
        }

        NetworkGraphHopper networkGraphHopper = networkData.getNetworkGraphHopper();
        return pointMatchService.match(networkGraphHopper, location.point())
                .map(CandidateMatch::getSnappedPoint)
                .filter(Geometry::isValid)
                .map(snappedPoint -> networkGraphHopper.getLocationIndex().findClosest(
                        snappedPoint.getY(),
                        snappedPoint.getX(),
                        edgeIteratorState -> locationIsCarAccessible(networkData, edgeIteratorState)));
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
            EdgeIteratorState edgeIteratorState
    ) {

        int encodedRoadSectionId = edgeIteratorState.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
        return encodedRoadSectionId == restriction.roadSectionId();
    }

    private static boolean locationIsCarAccessible(
            NetworkData networkData, EdgeIteratorState edgeIteratorState
    ) {
        EncodingManager encodingManager = networkData.getNetworkGraphHopper().getEncodingManager();
        CarriagewayTypeCode carriagewayTypeCode = networkData.findCarriageWayTypeCodeByRoadSectionId(edgeIteratorState.get(encodingManager.getIntEncodedValue(
                        WAY_ID_KEY)))
                .orElseThrow(() -> new IllegalStateException(
                        "Road section not found for link id: " + edgeIteratorState.get(encodingManager.getIntEncodedValue(WAY_ID_KEY))));
        return CAR_ACCESSIBLE_ROADS.contains(carriagewayTypeCode);
    }
}
