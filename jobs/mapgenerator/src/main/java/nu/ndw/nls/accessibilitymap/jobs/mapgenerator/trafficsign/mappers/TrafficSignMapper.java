package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.mappers;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSignMapper {

    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(
            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
            IntegerSequenceSupplier integerSequenceSupplier) {

        try {
            return Optional.of(TrafficSign.builder()
                    .id(integerSequenceSupplier.next())
                    .externalId(trafficSignGeoJsonDto.getId().toString())
                    .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                    .trafficSignType(TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                    .direction(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()))
                    .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                    .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude())
                    .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude())
                    .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                    .textSigns(trafficSignGeoJsonDto.getProperties().getTextSigns())
                    .blackCode(mapToBlackCode(trafficSignGeoJsonDto.getProperties()))
                    .build());
        } catch (Exception exception) {
            log.warn("Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto, exception);
            return Optional.empty();
        }
    }

    private Direction createDirection(DirectionType drivingDirection) {

        return switch (drivingDirection) {
            case FORTH -> Direction.FORWARD;
            case BACK -> Direction.BACKWARD;
            default -> throw new IllegalArgumentException(
                    "Driving direction '%s' could not be mapped.".formatted(drivingDirection));
        };
    }

    private URI createIconUri(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getImageUrl())) {
            return null;
        }

        return URI.create(trafficSignPropertiesDto.getImageUrl());
    }

    private Double mapToBlackCode(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getBlackCode())) {
            return null;
        }
        try {
            return Double.parseDouble(trafficSignPropertiesDto.getBlackCode().replace(",", "."));
        } catch (NumberFormatException ignored) {
            log.debug("Unprocessable value {} for traffic sign with RVV code {} on road section {}",
                    trafficSignPropertiesDto.getBlackCode(), trafficSignPropertiesDto.getRvvCode(),
                    trafficSignPropertiesDto.getRoadSectionId());
            return null;
        }
    }
}
