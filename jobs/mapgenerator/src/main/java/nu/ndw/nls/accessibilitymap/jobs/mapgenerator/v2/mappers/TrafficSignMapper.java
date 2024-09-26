package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services.TextSignFilterService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
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
                .iconUri(URI.create(trafficSignGeoJsonDto.getProperties().getImageUrl()))
                .build();
    }

    private String findWindowTimes(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        // @todo: replace getText() with getOpeningHours() when this text field is in use
        return textSignFilterService.findFirstWindowTimeTextSign(trafficSignGeoJsonDto.getProperties().getTextSigns())
                .map(TextSignDto::getText)
                .orElseThrow(() -> new IllegalStateException("Failed to find window time text sign for traffic sign by "
                        + "id: " + trafficSignGeoJsonDto.getId()));
    }
}
