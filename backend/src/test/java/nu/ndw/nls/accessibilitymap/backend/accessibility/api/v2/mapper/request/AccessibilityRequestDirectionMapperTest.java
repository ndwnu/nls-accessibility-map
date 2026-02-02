package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestDirectionMapperTest {

    private AccessibilityRequestDirectionMapper accessibilityRequestDirectionMapper;

    @BeforeEach
    void setUp() {

        accessibilityRequestDirectionMapper = new AccessibilityRequestDirectionMapper();
    }

    @ParameterizedTest
    @EnumSource(DirectionJson.class)
    void map(DirectionJson directionJson) {

        assertThat(accessibilityRequestDirectionMapper.map(directionJson)).isEqualTo(
                switch (directionJson) {
                    case FORWARD -> Direction.FORWARD;
                    case BACKWARD -> Direction.BACKWARD;
                });
    }
}
