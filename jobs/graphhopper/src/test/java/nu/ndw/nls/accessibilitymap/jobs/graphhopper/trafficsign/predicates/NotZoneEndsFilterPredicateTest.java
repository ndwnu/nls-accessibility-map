package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCodeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotZoneEndsFilterPredicateTest {

    @Mock
    private TrafficSignPropertiesDto trafficSignPropertiesDto;
    @Mock
    private TrafficSignGeoJsonDto trafficSignJsonDto;
    @InjectMocks
    NotZoneEndsFilterPredicate notZoneEndsFilterPredicate;

    @Test
    void test_includingZoneBegins() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn(ZoneCodeType.BEGIN.getValue());
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }

    @Test
    void test_includingNull() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn(null);
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }

    @Test
    void test_excludingZoneEnds() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn("ZE");
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertFalse(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }
}