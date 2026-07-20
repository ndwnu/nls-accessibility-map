package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json.SignCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplementaryTrafficSignMapperTest {

    private static final String TEXT = "text";

    @Mock
    private SupplementaryTrafficSignTypeMapper supplementaryTrafficSignTypeMapper;

    @InjectMocks
    private SupplementaryTrafficSignMapper supplementaryTrafficSignMapper;

    @Mock
    private SignCodeEnum signCodeEnum;

    @Mock
    private SupplementarySignType supplementarySignType;

    @Test
    void map() {

        when(supplementaryTrafficSignTypeMapper.map(signCodeEnum)).thenReturn(supplementarySignType);

        SupplementaryTrafficSign trafficSignMappingResult = supplementaryTrafficSignMapper.map(TextSignDtoV5Json.builder()
                .text(TEXT)
                .signCode(signCodeEnum)
                .build());

        assertThat(trafficSignMappingResult.text()).isEqualTo(TEXT);
        assertThat(trafficSignMappingResult.type()).isEqualTo(supplementarySignType);
    }
}

