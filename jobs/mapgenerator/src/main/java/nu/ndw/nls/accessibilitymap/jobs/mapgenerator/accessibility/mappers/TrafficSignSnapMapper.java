package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.constants.SRID;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrafficSignSnapMapper {

    private final NwbRoadSectionCrudService roadSectionService;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final NetworkMetaDataService networkMetaDataService;

    private final CrsTransformer crsTransformer;

    private final NetworkGraphHopper networkGraphHopper;

    public List<TrafficSignSnap> map(List<TrafficSign> trafficSigns, boolean includeOnlyTimeWindowedSigns) {

        return trafficSigns.stream()
                .filter(trafficSign -> applyTimeWindowedSignFilter(includeOnlyTimeWindowedSigns, trafficSign))
                .map(trafficSign -> findClosestSnapOnNetwork(trafficSign)
                        .map(snap -> TrafficSignSnap
                                .builder()
                                .trafficSign(trafficSign)
                                .snap(snap)
                                .build()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Snap> findClosestSnapOnNetwork(TrafficSign trafficSign) {

        int nwbVersion = networkMetaDataService.loadMetaData().nwbVersion();

        Optional<NwbRoadSectionDto> foundRoadSection = roadSectionService
                .findById(new Id(nwbVersion, trafficSign.roadSectionId()));

        if (foundRoadSection.isPresent()) {
            return foundRoadSection
                    .map(roadSectionDto -> mapToCoordinateAndBearing(trafficSign, roadSectionDto))
                    .map(coordinateAndBearing -> buildSnap(trafficSign, coordinateAndBearing, nwbVersion));
        } else {
            log.warn("No road section present for traffic sign id {} with road section id {} for nwb map version {} "
                            + "in the NWB road section database",
                    trafficSign.externalId(), trafficSign.roadSectionId(), nwbVersion);
            return Optional.empty();
        }
    }

    private CoordinateAndBearing mapToCoordinateAndBearing(TrafficSign trafficSign, NwbRoadSectionDto roadSectionDto) {

        LineString lineStringWgs84 = (LineString) crsTransformer.transformFromRdNewToWgs84(
                roadSectionDto.getGeometry());
        lineStringWgs84.setSRID(SRID.WGS84.value);

        return fractionAndDistanceCalculator.getCoordinateAndBearing(
                lineStringWgs84, trafficSign.fraction());
    }

    private Snap buildSnap(TrafficSign trafficSign, CoordinateAndBearing coordinateAndBearing, int nwbVersion) {

        Snap snap = networkGraphHopper.getLocationIndex()
                .findClosest(
                        coordinateAndBearing.coordinate().getY(), // Latitude
                        coordinateAndBearing.coordinate().getX(), // Longitude
                        edgeIteratorState -> trafficSignMatchesEdge(trafficSign, edgeIteratorState)
                );

        if (!snap.isValid()) {
            log.warn(
                    "No road section present for traffic sign id {} " +
                            "with road section id {} " +
                            "in nwb map version {} " +
                            "on graphhopper network",
                    trafficSign.externalId(), trafficSign.roadSectionId(), nwbVersion);
        }

        return snap.isValid() ? snap : null;
    }

    /**
     * Tries to match the traffic sign with the edge. They must have the same road section id. If not it should not be
     * linking up. This seems strange at first, but sometimes it happens that a traffic sign is linked to a cycling
     * lane. If we one hava a graph that contains only road sections accessible by motorized vehicles it will be linked
     * to the closest road and thus will be placed incorrectly. That is why we check this.
     *
     * @param trafficSign       - The traffic sign to compare to
     * @param edgeIteratorState - The edge that it should be linked up with.
     * @return - True if they match, false if they don't.
     */
    private boolean trafficSignMatchesEdge(TrafficSign trafficSign, EdgeIteratorState edgeIteratorState) {

        return getLinkId(edgeIteratorState) == trafficSign.roadSectionId();
    }

    private boolean applyTimeWindowedSignFilter(boolean includeOnlyTimeWindowedSigns, TrafficSign trafficSign) {

        return includeOnlyTimeWindowedSigns && trafficSign.hasTimeWindowedSign();
    }

    private int getLinkId(EdgeIteratorState edge) {

        return edge.get(networkGraphHopper.getEncodingManager().getIntEncodedValue(WAY_ID_KEY));
    }
}
