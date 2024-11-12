package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.dto.EdgeAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignToEdgeAttributeMapperTest {

    private TrafficSignToEdgeAttributeMapper trafficSignToEdgeAttributeMapper;

    @Mock
    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {
        trafficSignToEdgeAttributeMapper = new TrafficSignToEdgeAttributeMapper();
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void mapToEdgeAttribute_ok(TrafficSignType trafficSignType, String key, boolean hasTimeWindowedSign, Object value) {

        when(trafficSign.trafficSignType()).thenReturn(trafficSignType);
        when(trafficSign.hasTimeWindowedSign()).thenReturn(hasTimeWindowedSign);

        EdgeAttribute edgeAttribute = trafficSignToEdgeAttributeMapper.mapToEdgeAttribute(trafficSign);

        assertThat(edgeAttribute).isEqualTo(EdgeAttribute.builder()
                .key(key)
                .value(value)
                .build());
    }

    @Test
    void mapToEdgeAttribute_ok_allTrafficSignsInTestCase() {

        assertThat(provideTestData().map(a -> a.get()[0])
                .collect(Collectors.toSet()))
                .withFailMessage("Missing traffic sign mapping in TrafficSignToEdgeAttributeMapper or in testcase.")
                .hasSize(TrafficSignType.values().length);
    }

    private static Stream<Arguments> provideTestData() {

        return Stream.of(
                Arguments.of(TrafficSignType.C6, CAR_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C6, CAR_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C7, HGV_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C7, HGV_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED, true, true)
        );
    }
}
