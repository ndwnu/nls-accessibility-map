package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.mappers;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7C;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.CAR_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_AXLE_LOAD;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_HEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_LENGTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_WIDTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAILER_ACCESS_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.dto.EdgeAttribute;
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
    void mapToEdgeAttribute(TrafficSignType trafficSignType, String key, boolean hasTimeWindowedSign, Object value) {

        when(trafficSign.trafficSignType()).thenReturn(trafficSignType);
        if (hasTimeWindowedSign) {
            when(trafficSign.hasTimeWindowedSign()).thenReturn(true);
        }
        if (List.of(C17, C18, C19, C20, C21).contains(trafficSignType)) {
            when(trafficSign.blackCode()).thenReturn((Double) value);
        }
        EdgeAttribute edgeAttribute = trafficSignToEdgeAttributeMapper.mapToEdgeAttribute(trafficSign);

        assertThat(edgeAttribute).isEqualTo(EdgeAttribute.builder()
                .key(key)
                .value(value)
                .build());
    }

    @Test
    void mapToEdgeAttribute_allTrafficSignsInTestCase() {

        Set<TrafficSignType> trafficSignTypeSet = provideTestData().map(a -> a.get()[0])
                .map(TrafficSignType.class::cast)
                .collect(Collectors.toSet());

        Set<TrafficSignType> missingTrafficSignTypes = Sets.difference(
                Arrays.stream(TrafficSignType.values())
                        //Ignoring these because we will support them in the new api version
                        .filter(trafficSignType -> List.of(C1, C7C, C22).contains(trafficSignType))
                        .collect(Collectors.toSet()), trafficSignTypeSet);

        assertThat(missingTrafficSignTypes)
                .withFailMessage("Missing traffic sign mapping in vehiclePropertiesMapper or in testcase %s", missingTrafficSignTypes)
                .isEmpty();
    }

    private static Stream<Arguments> provideTestData() {

        return Stream.of(
                Arguments.of(TrafficSignType.C6, CAR_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C6, CAR_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C7, HGV_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C7, HGV_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C7A, BUS_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C7B, HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C10, TRAILER_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C12, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN, false, true),
                Arguments.of(TrafficSignType.C22C, LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED, true, true),
                Arguments.of(C17, MAX_LENGTH, false, 20D),
                Arguments.of(C18, MAX_WIDTH, false, 6D),
                Arguments.of(C19, MAX_HEIGHT, false, 10D),
                Arguments.of(C20, MAX_AXLE_LOAD, false, 5D),
                Arguments.of(C21, MAX_WEIGHT, false, 2D)
        );
    }
}
