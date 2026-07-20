package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.ZoneCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class ZoneCodeTypeMapper {

    public ZoneCodeType map(ZoneCodeEnum zoneCodeEnum) {

        if (Objects.isNull(zoneCodeEnum)) {
            return null;
        }

        return switch (zoneCodeEnum) {
            case END -> ZoneCodeType.END;
            case BEGIN -> ZoneCodeType.START;
            case REPEAT -> ZoneCodeType.REPEAT;
            case UNKNOWN -> ZoneCodeType.UNKNOWN;
        };
    }
}
