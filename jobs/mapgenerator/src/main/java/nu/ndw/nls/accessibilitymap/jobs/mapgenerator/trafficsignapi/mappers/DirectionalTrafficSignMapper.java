package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.TextSignFilterService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectionalTrafficSignMapper {

    private final TrafficSignApiRvvCodeMapper trafficSignApiRvvCodeMapper;

    private final TextSignFilterService textSignFilterService;

    public DirectionalTrafficSign map(TrafficSignGeoJsonDto trafficSignGeoJsonDto, Direction direction) {
        return DirectionalTrafficSign.builder()
                .nwbRoadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId())
                .trafficSignType(trafficSignApiRvvCodeMapper.map(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                .nwbFraction(trafficSignGeoJsonDto.getProperties().getFraction())
                .direction(direction)
                .windowTimes(findWindowTimes(trafficSignGeoJsonDto))
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
