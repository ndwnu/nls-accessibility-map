package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json.SignCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class SupplementaryTrafficSignTypeMapper {

    //@todo: implement this properly
    public SupplementarySignType map(SignCodeEnum signCodeEnum) {
        return SupplementarySignType.valueOf(String.valueOf(signCodeEnum));
    }

}
