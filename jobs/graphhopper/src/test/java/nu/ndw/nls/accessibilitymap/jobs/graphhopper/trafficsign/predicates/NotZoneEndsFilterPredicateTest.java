package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotZoneEndsFilterPredicateTest {

    private final NotZoneEndsFilterPredicate notZoneEndsFilterPredicate = new NotZoneEndsFilterPredicate();

    @Mock
    private TrafficSignJsonDtoV3 trafficSignJsonDtoV3;

    @Test
    void test_ok_includingZoneBegins() {
        when(trafficSignJsonDtoV3.getZoneCode()).thenReturn(ZoneCode.BEGIN);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDtoV3));
        verify(trafficSignJsonDtoV3).getZoneCode();
    }

    @Test
    void test_ok_includingNull() {
        when(trafficSignJsonDtoV3.getZoneCode()).thenReturn(null);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDtoV3));
        verify(trafficSignJsonDtoV3).getZoneCode();
    }

    @Test
    void test_ok_excludingZoneEnds() {
        when(trafficSignJsonDtoV3.getZoneCode()).thenReturn(ZoneCode.END);
        assertFalse(notZoneEndsFilterPredicate.test(trafficSignJsonDtoV3));
        verify(trafficSignJsonDtoV3).getZoneCode();
    }
}