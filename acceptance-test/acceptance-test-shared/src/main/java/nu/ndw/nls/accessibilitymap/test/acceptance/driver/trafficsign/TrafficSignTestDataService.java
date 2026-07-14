package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers.ConditionPropertiesDtoV5JsonMapper;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers.TextSignDtoV5JsonMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignTestDataService {

    private final ConditionPropertiesDtoV5JsonMapper conditionPropertiesDtoV5JsonMapper;

    private final TextSignDtoV5JsonMapper textSignDtoV5JsonMapper;

    @SuppressWarnings("java:S109")
    public TrafficSignGeoJsonDtoV5Json createTrafficSignGeoJsonDto(TrafficSign trafficSign) {

        return TrafficSignGeoJsonDtoV5Json.builder()
                .id(UUID.fromString(trafficSign.id()))
                .geometry(new PointJson().type("point").coordinates(List.of(trafficSign.location().x, trafficSign.location().y)))
                .properties(TrafficSignPropertiesDtoV5Json.builder()
                        .fraction(trafficSign.fraction())
                        .blackCode(trafficSign.blackCode())
                        .rvvCode(trafficSign.rvvCode())
                        .drivingDirection(trafficSign.directionType())
                        .roadSectionId(Math.toIntExact(trafficSign.roadSectionId()))
                        .trafficOrderId(trafficSign.regulationOrderId())
                        .supplementarySigns(mapTextSignDto(trafficSign.supplementaryTrafficSigns()))
                        .conditions(ConditionsDtoV5Json.builder()
                                .restrictions(conditionPropertiesDtoV5JsonMapper.map(trafficSign.restrictions()))
                                .exemptions(mapTrafficSignConditions(trafficSign.exemptions()))
                                .build())
                        .build())
                .build();
    }

    private List<ConditionPropertiesDtoV5Json> mapTrafficSignConditions(List<TrafficSignCondition> trafficSignConditions) {
        if (trafficSignConditions == null) {
            return Collections.emptyList();
        }

        return trafficSignConditions
                .stream()
                .map(conditionPropertiesDtoV5JsonMapper::map)
                .toList();
    }

    private List<TextSignDtoV5Json> mapTextSignDto(List<SupplementaryTrafficSign> supplementaryTrafficSigns) {
        if (supplementaryTrafficSigns == null) {
            return Collections.emptyList();
        }

        return supplementaryTrafficSigns
                .stream()
                .map(textSignDtoV5JsonMapper::map)
                .toList();
    }

}
