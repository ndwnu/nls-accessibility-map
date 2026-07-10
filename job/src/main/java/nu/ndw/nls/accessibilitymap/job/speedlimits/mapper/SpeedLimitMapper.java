package nu.ndw.nls.accessibilitymap.job.speedlimits.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionDirectionalSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import org.springframework.stereotype.Component;

@Component
public class SpeedLimitMapper {

    public SpeedLimit map(
            RoadSectionSpeedLimitJson roadSectionSpeedLimitJson,
            RoadSectionDirectionalSpeedLimitJson roadSectionDirectionalSpeedLimitJson) {
        return new SpeedLimit(
                Math.toIntExact(roadSectionSpeedLimitJson.getNwbRoadSectionId()),
                Direction.valueOf(roadSectionDirectionalSpeedLimitJson.getDirection().name()),
                roadSectionDirectionalSpeedLimitJson.getAverageSpeedLimit());
    }
}
