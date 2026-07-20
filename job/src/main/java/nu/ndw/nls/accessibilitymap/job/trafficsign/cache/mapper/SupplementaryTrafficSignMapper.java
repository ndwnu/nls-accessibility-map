package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplementaryTrafficSignMapper {

    private final SupplementaryTrafficSignTypeMapper supplementaryTrafficSignTypeMapper;

    public SupplementaryTrafficSign map(TextSignDtoV5Json textSignDtoV5Json) {
        return SupplementaryTrafficSign.builder()
                .type(supplementaryTrafficSignTypeMapper.map(textSignDtoV5Json.getSignCode()))
                .text(textSignDtoV5Json.getText())
                .build();
    }
}
