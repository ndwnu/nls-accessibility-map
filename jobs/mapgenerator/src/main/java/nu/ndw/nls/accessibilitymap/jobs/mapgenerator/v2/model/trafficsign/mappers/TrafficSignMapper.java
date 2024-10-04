package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.mappers;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignDirection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficSignMapper {

    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        try {
            return Optional.of(TrafficSign.builder()
                    .id(trafficSignGeoJsonDto.getId())
                    .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                    .trafficSignType(TrafficSignType.valueOf(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                    .direction(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()))
                    .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                    .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude())
                    .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude())
                    .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                    .textSigns(trafficSignGeoJsonDto.getProperties().getTextSigns())
                    .build());
        } catch (Exception exception) {
            log.warn("Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto, exception);
            return Optional.empty();
        }
    }

    private TrafficSignDirection createDirection(DirectionType drivingDirection) {

        switch (drivingDirection) {
            case FORTH -> {
                return TrafficSignDirection.FORWARD;
            }
            case BACK -> {
                return TrafficSignDirection.BACKWARD;
            }
            case BOTH -> {
                return TrafficSignDirection.BOTH;
            }
        }

        throw new IllegalArgumentException("Driving direction '%s' could not be mapped.".formatted(drivingDirection));
    }

    private URI createIconUri(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getImageUrl())) {
            return null;
        }

        return URI.create(trafficSignPropertiesDto.getImageUrl());
    }
}
