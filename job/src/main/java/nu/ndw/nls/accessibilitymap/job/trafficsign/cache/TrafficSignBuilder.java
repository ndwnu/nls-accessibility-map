package nu.ndw.nls.accessibilitymap.job.trafficsign.cache;

import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.BlackCodeMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.DirectionMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.SupplementaryTrafficSignMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.TransportRestrictionMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.ZoneCodeTypeMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class TrafficSignBuilder {

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final BlackCodeMapper blackCodeMapper;

    private final ZoneCodeTypeMapper zoneCodeTypeMapper;

    private final DirectionMapper directionMapper;

    private final TransportRestrictionMapper transportRestrictionMapper;

    private final SupplementaryTrafficSignMapper supplementaryTrafficSignMapper;

    @Valid
    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(
            LineString nwbRoadSectionGeometry,
            TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json,
            AtomicInteger idSequenceSupplier) {

        try {
            if (Objects.isNull(nwbRoadSectionGeometry)) {
                throw new IllegalStateException("Traffic sign with id '%s' is missing a road section."
                        .formatted(trafficSignGeoJsonDtoV5Json.getId()));
            }

            Double fraction = trafficSignGeoJsonDtoV5Json.getProperties().getFraction();
            if (Objects.isNull(fraction)) {
                throw new IllegalStateException("Traffic sign with id '%s' is missing a fraction."
                        .formatted(trafficSignGeoJsonDtoV5Json.getId()));
            }

            if (Objects.isNull(trafficSignGeoJsonDtoV5Json.getProperties().getRoadSectionId())) {
                throw new IllegalStateException("Traffic sign with id '%s' is missing a roadSectionId."
                        .formatted(trafficSignGeoJsonDtoV5Json.getId()));
            }


            var coordinateAndBearing = fractionAndDistanceCalculator.getCoordinateAndBearing(nwbRoadSectionGeometry, fraction);

            TrafficSignType trafficSignType = TrafficSignType.fromRvvCode(trafficSignGeoJsonDtoV5Json.getProperties().getRvvCode());
            TrafficSign trafficSign = TrafficSign.builder()
                    .id(idSequenceSupplier.getAndIncrement())
                    .externalId(trafficSignGeoJsonDtoV5Json.getId().toString())
                    .roadSectionId(trafficSignGeoJsonDtoV5Json.getProperties().getRoadSectionId())
                    .trafficSignType(trafficSignType)
                    .direction(directionMapper.map(trafficSignGeoJsonDtoV5Json.getProperties().getDrivingDirection()))
                    .fraction(fraction)
                    //In GeoJSON, a Point's coordinates are always [longitude, latitude] (X, Y),
                    .latitude(trafficSignGeoJsonDtoV5Json.getGeometry().getCoordinates().getLast())
                    .longitude(trafficSignGeoJsonDtoV5Json.getGeometry().getCoordinates().getFirst())
                    .zoneCodeType(zoneCodeTypeMapper.map(trafficSignGeoJsonDtoV5Json.getProperties().getZoneCode()))
                    .trafficRegulationOrderId(trafficSignGeoJsonDtoV5Json.getProperties().getTrafficOrderId())
                    .blackCode(blackCodeMapper.map(trafficSignGeoJsonDtoV5Json, trafficSignType))
                    .networkSnappedLatitude(coordinateAndBearing.coordinate().getY())
                    .networkSnappedLongitude(coordinateAndBearing.coordinate().getX())
                    .supplementaryTrafficSigns(trafficSignGeoJsonDtoV5Json.getProperties().getSupplementarySigns()
                            .stream()
                            .map(supplementaryTrafficSignMapper::map)
                            .toList())
                    .build();

            ConditionsDtoV5Json conditions = trafficSignGeoJsonDtoV5Json.getProperties().getConditions();

            TransportRestrictions transportRestrictions = transportRestrictionMapper.map(conditions,
                    trafficSignGeoJsonDtoV5Json.getProperties().getTrafficOrderId());

            return Optional.of(trafficSign.withTransportRestrictions(transportRestrictions));
        } catch (RuntimeException exception) {
            log.debug(
                    "Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDtoV5Json.getId(), trafficSignGeoJsonDtoV5Json, exception);
            return Optional.empty();
        }
    }

}
