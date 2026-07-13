package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json.SignCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SupplementaryTrafficSignTypeMapperTest {

    private final SupplementaryTrafficSignTypeMapper supplementaryTrafficSignTypeMapper = new SupplementaryTrafficSignTypeMapper();

    @ParameterizedTest
    @EnumSource(SignCodeEnum.class)
    void map(SignCodeEnum signCodeEnum) {
        assertThat(supplementaryTrafficSignTypeMapper.map(signCodeEnum)).isEqualTo(SupplementarySignType.valueOf(String.valueOf(signCodeEnum)));
    }

    @Test
    void map_null() {
        assertThat(supplementaryTrafficSignTypeMapper.map(null)).isNull();
    }
}