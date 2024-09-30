package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers;

import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services.TextSignFilterService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignMapper {

    private final TextSignFilterService textSignFilterService;

    public TrafficSign mapFromTrafficSignGeoJsonDto(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        return TrafficSign.builder()
                .trafficSignType(TrafficSignType.valueOf(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                .windowTimes(findWindowTimes(trafficSignGeoJsonDto))
                .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                .build();
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
