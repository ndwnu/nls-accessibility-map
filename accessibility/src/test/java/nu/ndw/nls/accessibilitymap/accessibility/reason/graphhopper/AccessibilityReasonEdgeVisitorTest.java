package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.MaximumRestrictionReducer;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonEdgeVisitorTest {

    private AccessibilityReasonEdgeVisitor accessibilityReasonEdgeVisitor;

    private AccessibilityReasons accessibilityReasons;

    private Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducers;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private IntEncodedValue intEncodedValue;

    @BeforeEach
    void setUp() {

        accessibilityRestrictionReducers = Map.of(MaximumRestriction.class, new MaximumRestrictionReducer());
    }

    @ParameterizedTest
    @EnumSource(value = Direction.class, names = {"FORWARD", "BACKWARD"})
    void calculateRestrictions(Direction direction) {

        var reason1 = createReason("100", TrafficSignType.C18, 1, direction, RestrictionType.VEHICLE_WIDTH, 10D);
        var reason2 = createReason("101", TrafficSignType.C18, 1, direction, RestrictionType.VEHICLE_WIDTH, 3D);
        var reason3 = createReason("102", TrafficSignType.C17, 1, direction, RestrictionType.VEHICLE_LENGTH, 4D);
        var reason4 = createReason("103", TrafficSignType.C18, 2, direction, RestrictionType.VEHICLE_WIDTH, 5D);

        accessibilityReasons = new AccessibilityReasons(List.of(reason1, reason2, reason3, reason4));
        accessibilityReasonEdgeVisitor = new AccessibilityReasonEdgeVisitor(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue))
                .thenReturn(1)
                .thenReturn(2);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState))
                .thenReturn(direction == Direction.BACKWARD)
                .thenReturn(direction == Direction.BACKWARD);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 2, 1);
        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 3, 2);

        accessibilityReasonEdgeVisitor.finish();
        List<AccessibilityReason> reasons = accessibilityReasonEdgeVisitor.getAccessibilityReasonList()
                .stream()
                .sorted(Comparator.comparing(AccessibilityReason::trafficSignExternalId))
                .toList();

        assertThat(reasons).hasSize(2);
        assertReason(reasons.getFirst(), reason2);
        assertReason(reasons.get(1), reason3);
    }


    @Test
    void calculateRestrictions_noReasonForDirection() {

        var reason1 = createReason("100", TrafficSignType.C18, 1, Direction.FORWARD, RestrictionType.VEHICLE_WIDTH, 10D);

        accessibilityReasons = new AccessibilityReasons(List.of(reason1));
        accessibilityReasonEdgeVisitor = new AccessibilityReasonEdgeVisitor(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(1);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState)).thenReturn(true);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 2, 1);
        accessibilityReasonEdgeVisitor.finish();
        assertThat(accessibilityReasonEdgeVisitor.getAccessibilityReasonList()).isEmpty();
    }

    @Test
    void calculateRestrictions_noReasonForRoadSectionId() {

        var reason1 = createReason("100", TrafficSignType.C18, 1, Direction.FORWARD, RestrictionType.VEHICLE_WIDTH, 10D);

        accessibilityReasons = new AccessibilityReasons(List.of(reason1));
        accessibilityReasonEdgeVisitor = new AccessibilityReasonEdgeVisitor(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(2);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState)).thenReturn(false);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 2, 1);
        accessibilityReasonEdgeVisitor.finish();
        assertThat(accessibilityReasonEdgeVisitor.getAccessibilityReasonList()).isEmpty();
    }

    @Test
    void calculateRestrictions_unknownRestrictionType() {

        var reason1 = AccessibilityReason.builder()
                .direction(Direction.FORWARD)
                .trafficSignExternalId("100")
                .trafficSignType(TrafficSignType.C18)
                .roadSectionId(1)
                .restrictions(new ArrayList<>())
                .build();

        reason1.mergeRestrictions(List.of(FuelTypeRestriction.builder()
                .value(Set.of(FuelType.ETHANOL))
                .accessibilityReason(reason1)
                .build()));

        accessibilityReasons = new AccessibilityReasons(List.of(reason1));
        accessibilityReasonEdgeVisitor = new AccessibilityReasonEdgeVisitor(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(1);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState)).thenReturn(false);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 2, 1);
        assertThat(catchThrowable(() -> accessibilityReasonEdgeVisitor.finish()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown restriction type FUEL_TYPE");
        assertThat(accessibilityReasonEdgeVisitor.getAccessibilityReasonList()).isEmpty();
    }

    private static void assertReason(AccessibilityReason actualReason, AccessibilityReason expectedReason) {

        assertThat(actualReason.direction()).isEqualTo(expectedReason.direction());
        assertThat(actualReason.roadSectionId()).isEqualTo(expectedReason.roadSectionId());
        assertThat(actualReason.trafficSignExternalId()).isEqualTo(expectedReason.trafficSignExternalId());
        assertThat(actualReason.trafficSignType()).isEqualTo(expectedReason.trafficSignType());
        assertThat(actualReason.restrictions()).isEqualTo(expectedReason.restrictions());
    }

    private static AccessibilityReason createReason(
            String trafficSignExternalId,
            TrafficSignType trafficSignType,
            int roadSectionId,
            Direction direction,
            RestrictionType restrictionType,
            double restrictionValue) {

        var accessibilityReason = AccessibilityReason.builder()
                .direction(direction)
                .trafficSignExternalId(trafficSignExternalId)
                .trafficSignType(trafficSignType)
                .roadSectionId(roadSectionId)
                .restrictions(new ArrayList<>())
                .build();

        accessibilityReason.mergeRestrictions(List.of(MaximumRestriction.builder()
                .restrictionType(restrictionType)
                .value(Maximum.builder().value(restrictionValue).build())
                .accessibilityReason(accessibilityReason)
                .build()));

        return accessibilityReason;
    }
}