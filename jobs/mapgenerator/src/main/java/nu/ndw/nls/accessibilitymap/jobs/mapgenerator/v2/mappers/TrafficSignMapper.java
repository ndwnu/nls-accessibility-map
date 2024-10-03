package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services.TextSignFilterService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficSignMapper {

    private final TextSignFilterService textSignFilterService;

    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        if(Objects.isNull(trafficSignGeoJsonDto.getProperties().getFraction())) {
            log.warn("Traffic sign with id '{}' is incomplete", trafficSignGeoJsonDto.getId());
            return Optional.empty();
        }
        return Optional.of(TrafficSign.builder()
                .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                .trafficSignType(TrafficSignType.valueOf(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                .windowTimes(findWindowTimes(trafficSignGeoJsonDto))
                .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude())
                .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude())
                .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                .build());
    }

    private URI createIconUri(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getImageUrl())) {
            return null;
        }

        return URI.create(trafficSignPropertiesDto.getImageUrl());
    }

    private String findWindowTimes(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        // @todo: replace getText() with getOpeningHours() when this text field is in use
        return textSignFilterService.findFirstWindowTimeTextSign(trafficSignGeoJsonDto.getProperties().getTextSigns())
                .map(TextSignDto::getText)
                .orElse(null);
//                .orElseThrow(() -> new IllegalStateException("Failed to find window time text sign for traffic sign by "
//                        + "id: " + trafficSignGeoJsonDto.getId()));
    }
}
