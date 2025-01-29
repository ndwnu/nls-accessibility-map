package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSignMapper {

    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(
            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
            IntegerSequenceSupplier integerSequenceSupplier) {

        try {
            TrafficSignType type = TrafficSignType.fromRvvCode(trafficSignGeoJsonDto.getProperties().getRvvCode());
            return Optional.of(TrafficSign.builder()
                    .id(integerSequenceSupplier.next())
                    .externalId(trafficSignGeoJsonDto.getId().toString())
                    .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                    .trafficSignType(type)
                    .direction(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()))
                    .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                    .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude())
                    .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude())
                    .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                    .textSigns(trafficSignGeoJsonDto.getProperties().getTextSigns())
                    .blackCode(mapToBlackCode(trafficSignGeoJsonDto, type))
                    .build());
        } catch (RuntimeException exception) {
            log.warn("Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto, exception);
            return Optional.empty();
        }
    }

    private static Direction createDirection(DirectionType drivingDirection) {

        return switch (drivingDirection) {
            case FORTH -> Direction.FORWARD;
            case BACK -> Direction.BACKWARD;
            default -> throw new IllegalArgumentException(
                    "Driving direction '%s' could not be mapped.".formatted(drivingDirection));
        };
    }

    private static URI createIconUri(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getImageUrl())) {
            return null;
        }

        return URI.create(trafficSignPropertiesDto.getImageUrl());
    }

    private Double mapToBlackCode(TrafficSignGeoJsonDto trafficSignGeoJsonDto, TrafficSignType type) {

        String blackCode = trafficSignGeoJsonDto.getProperties().getBlackCode();
        if (List.of(C17, C18, C19, C20, C21).contains(type) && Strings.isEmpty(blackCode)) {
            throw new IllegalStateException(
                    "Traffic sign with id '%s' is not containing a black code but that is required for type '%s'".formatted(
                            trafficSignGeoJsonDto.getId(), type));
        }

        if (Strings.isEmpty(blackCode)) {
            return null;
        }
        try {
            return Double.parseDouble(blackCode.replace(",", "."));
        } catch (NumberFormatException ignored) {
            log.warn("Unprocessable value {} for traffic sign with id {} and RVV code {} on road section {}",
                    blackCode,
                    trafficSignGeoJsonDto.getId(),
                    trafficSignGeoJsonDto.getProperties().getRvvCode(),
                    trafficSignGeoJsonDto.getProperties().getRoadSectionId());
            return null;
        }
    }
}
