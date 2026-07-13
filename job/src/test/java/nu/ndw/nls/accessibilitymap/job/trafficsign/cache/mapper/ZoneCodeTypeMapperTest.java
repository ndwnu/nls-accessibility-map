package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.ZoneCodeEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneCodeTypeMapperTest {

    private final ZoneCodeTypeMapper zoneCodeTypeMapper = new ZoneCodeTypeMapper();

    @ParameterizedTest
    @CsvSource(textBlock = """
                        null, null
                        END, END
                        BEGIN, START
                        REPEAT, REPEAT
                        UNKNOWN, UNKNOWN
                        """, nullValues = "null")
    void map(ZoneCodeEnum inputZoneCodeEnum, ZoneCodeType expectedZoneCodeType) {
        assertThat(zoneCodeTypeMapper.map(inputZoneCodeEnum)).isEqualTo(expectedZoneCodeType);
    }
}