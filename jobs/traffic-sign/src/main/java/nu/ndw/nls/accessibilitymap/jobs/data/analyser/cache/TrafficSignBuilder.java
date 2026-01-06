package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.NwbRoadSectionSnapService;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper.BlackCodeMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class TrafficSignBuilder {

    private final TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    private final NwbRoadSectionSnapService nwbRoadSectionSnapService;

    private final BlackCodeMapper blackCodeMapper;

    @Valid
    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(
            LineString nwbRoadSectionGeometry,
            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
            AtomicInteger idSequenceSupplier) {

        try {
            if (Objects.isNull(nwbRoadSectionGeometry)) {
                throw new IllegalStateException("Traffic sign with id '%s' is missing a road section."
                        .formatted(trafficSignGeoJsonDto.getId()));
            }

            Double fraction = trafficSignGeoJsonDto.getProperties().getFraction();
            if (Objects.isNull(fraction)) {
                throw new IllegalStateException("Traffic sign with id '%s' is missing a fraction."
                        .formatted(trafficSignGeoJsonDto.getId()));
            }
            CoordinateAndBearing coordinateAndBearing = nwbRoadSectionSnapService.snapToLine(nwbRoadSectionGeometry, fraction);

            TrafficSignType type = TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode());
            TrafficSign trafficSign = TrafficSign.builder()
                    .id(idSequenceSupplier.getAndIncrement())
                    .externalId(trafficSignGeoJsonDto.getId().toString())
                    .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                    .trafficSignType(type)
                    .direction(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()))
                    .fraction(fraction)
                    //In GeoJSON, a Point's coordinates are always [longitude, latitude] (X, Y),
                    .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getFirst())
                    .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLast())
                    .iconUri(createUri(trafficSignGeoJsonDto.getProperties().getImageUrl()))
                    .textSigns(trafficSignGeoJsonDto.getProperties().getTextSigns())
                    .zoneCodeType(mapZoneCodeType(trafficSignGeoJsonDto))
                    .trafficRegulationOrderId(trafficSignGeoJsonDto.getProperties().getTrafficOrderUrl())
                    .blackCode(blackCodeMapper.map(trafficSignGeoJsonDto, type))
                    .networkSnappedLatitude(coordinateAndBearing.coordinate().getY())
                    .networkSnappedLongitude(coordinateAndBearing.coordinate().getX())
                    .build();

            return Optional.of(trafficSign.withRestrictions(trafficSignRestrictionsBuilder.buildFor(trafficSign)));
        } catch (RuntimeException exception) {
            log.info("Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto, exception);
            return Optional.empty();
        }
    }

    private static ZoneCodeType mapZoneCodeType(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        if (Objects.isNull(trafficSignGeoJsonDto.getProperties().getZoneCode())) {
            return null;
        }
        return switch (trafficSignGeoJsonDto.getProperties().getZoneCode()) {
            case "ZE" -> ZoneCodeType.END;
            case "ZB" -> ZoneCodeType.START;
            case "ZH" -> ZoneCodeType.REPEAT;
            case "ZO" -> ZoneCodeType.UNKNOWN;
            default -> throw new IllegalArgumentException("Unknown zone code '%s'"
                    .formatted(trafficSignGeoJsonDto.getProperties().getZoneCode()));
        };
    }

    private static Direction createDirection(DirectionType drivingDirection) {

        return switch (drivingDirection) {
            case FORTH -> Direction.FORWARD;
            case BACK -> Direction.BACKWARD;
            default -> throw new IllegalArgumentException(
                    "Driving direction '%s' could not be mapped.".formatted(drivingDirection));
        };
    }

    private static URI createUri(String value) {

        if (Objects.isNull(value)) {
            return null;
        }

        return URI.create(value);
    }
}
