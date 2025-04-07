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

@Service
@RequiredArgsConstructor
@Slf4j
public class NwbRoadSectionSnapService {

    private final NwbRoadSectionCrudService roadSectionService;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;
    private final NetworkMetaDataService networkMetaDataService;
    private final CrsTransformer crsTransformer;

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
