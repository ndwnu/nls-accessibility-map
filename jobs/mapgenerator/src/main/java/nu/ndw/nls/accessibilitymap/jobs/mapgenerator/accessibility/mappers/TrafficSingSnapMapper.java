package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers;

import static java.util.stream.Collectors.toCollection;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrafficSingSnapMapper {

    private final NwbRoadSectionCrudService roadSectionService;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;
    private final NetworkMetaDataService networkMetaDataService;
    private final CrsTransformer crsTransformer;
    private final NetworkGraphHopper networkGraphHopper;

    public List<TrafficSignSnap> map(List<TrafficSign> trafficSigns, AccessibilityRequest accessibilityRequest) {
        return trafficSigns
                .stream()
                .filter(trafficSign -> applyTimeWindowedSignFilter(accessibilityRequest, trafficSign))
                .map(trafficSign -> TrafficSignSnap
                        .builder()
                        .trafficSign(trafficSign)
                        .snap(findClosestPointOnNetwork(trafficSign)
                                .orElse(null))
                        .build())
                .filter(a -> Objects.nonNull(a.getSnap()))
                .collect(toCollection(ArrayList::new));
    }

    private Optional<Snap> findClosestPointOnNetwork(TrafficSign trafficSign) {
        int nwbVersion = networkMetaDataService.loadMetaData().nwbVersion();
        return getCoordinateAndBearing(
                trafficSign, nwbVersion)
                .map(c -> {
                    //Latitude is the Y axis, longitude is the X axis.
                    Snap snap = networkGraphHopper.getLocationIndex()
                            .findClosest(
                                    c.coordinate().getY(),
                                    c.coordinate().getX(),
                                    (edgeIteratorState -> linkIdIsRoadSection(trafficSign, edgeIteratorState)
                                    ));
                    if (!snap.isValid()) {
                        log.warn(
                                "No road section present for traffic sign {} road section {} in nwb version {} on graphhopper network",
                                trafficSign.externalId(), trafficSign.roadSectionId(), nwbVersion);
                    }
                    return snap.isValid() ? snap : null;
                });

    }

    private Optional<CoordinateAndBearing> getCoordinateAndBearing(TrafficSign trafficSign, int nwbVersion) {
        return Optional.ofNullable(roadSectionService.findById(
                        new Id(nwbVersion, trafficSign.roadSectionId()))
                .map(r -> mapToCoordinateAndBearing(trafficSign, r))
                .orElseGet(() -> logWarningAndReturnNull(trafficSign, nwbVersion)));
    }

    private static CoordinateAndBearing logWarningAndReturnNull(TrafficSign trafficSign, int nwbVersion) {
        log.warn("No road section present for traffic sign {} road section {} in nwb version {}",
                trafficSign.externalId(), trafficSign.roadSectionId(), nwbVersion);
        return null;
    }

    private CoordinateAndBearing mapToCoordinateAndBearing(TrafficSign trafficSign, NwbRoadSectionDto r) {
        LineString lineStringWgs84 = (LineString) crsTransformer.transformFromRdNewToWgs84(
                r.getGeometry());
        lineStringWgs84.setSRID(4326);
        return fractionAndDistanceCalculator.getCoordinateAndBearing(
                lineStringWgs84, trafficSign.fraction());
    }

    private boolean linkIdIsRoadSection(TrafficSign trafficSign, EdgeIteratorState edgeIteratorState) {
        int linkId = getLinkId(edgeIteratorState);
        return linkId == trafficSign.roadSectionId();
    }

    private boolean applyTimeWindowedSignFilter(AccessibilityRequest accessibilityRequest, TrafficSign trafficSign) {

        return accessibilityRequest.isIncludeOnlyTimeWindowedSigns() && trafficSign.hasTimeWindowedSign();
    }

    private int getLinkId(EdgeIteratorState edge) {

        return edge.get(networkGraphHopper.getEncodingManager().getIntEncodedValue(WAY_ID_KEY));
    }
}
