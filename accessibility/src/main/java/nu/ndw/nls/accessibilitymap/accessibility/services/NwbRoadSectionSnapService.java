package nu.ndw.nls.accessibilitymap.accessibility.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.constants.SRID;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

/**
 * Service responsible for snapping traffic signs to road sections within a given NWB map version.
 * The service retrieves road section information from a database, calculates the coordinate
 * and bearing for a traffic sign based on its location on the road section, and performs
 * necessary transformations between coordinate reference systems.
 *
 * Dependencies used in this class:
 * - {@link NwbRoadSectionCrudService}: For interacting with the NWB road section database.
 * - {@link FractionAndDistanceCalculator}: For determining the specific coordinates and bearing
 *   of a traffic sign based on its fractional position along a road geometry.
 * - {@link NetworkMetaDataService}: For retrieving metadata about the NWB map, including the map version.
 * - {@link CrsTransformer}: For transforming road geometry between different coordinate reference systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NwbRoadSectionSnapService {

    private final NwbRoadSectionCrudService roadSectionService;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;
    private final NetworkMetaDataService networkMetaDataService;
    private final CrsTransformer crsTransformer;

    /**
     * Snaps a traffic sign to its corresponding road section, calculating its coordinates
     * and bearing based on its fractional position along the road geometry.
     *
     * @param trafficSign the traffic sign to be snapped, containing its metadata
     *                    and positional data such as the road section ID and fraction.
     * @return an {@link Optional} containing the calculated {@link CoordinateAndBearing}
     *         if the corresponding road section is found; otherwise, {@link Optional#empty()}.
     */
    public Optional<CoordinateAndBearing> snapTrafficSign(TrafficSign trafficSign) {
        int nwbVersion = networkMetaDataService.loadMetaData().nwbVersion();

        Optional<NwbRoadSectionDto> foundRoadSection = roadSectionService
                .findById(new Id(nwbVersion, trafficSign.roadSectionId()));

        if (foundRoadSection.isPresent()) {
            return foundRoadSection
                    .map(roadSectionDto -> mapToCoordinateAndBearing(trafficSign, roadSectionDto));

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
}
