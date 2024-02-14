package nu.ndw.nls.accessibilitymap.trafficsignclient.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.utils.MaxEventTimestampTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaxEventTimestampTrackerTest {

    @Mock
    private TrafficSignJsonDtoV3 trafficSignJsonDtoV3;

    @Test
    void getMaxEventTimestamp_ok_initialValueMinimum() {
        MaxEventTimestampTracker maxEventTimestampTracker = new MaxEventTimestampTracker();
        assertEquals(Instant.MIN, maxEventTimestampTracker.getMaxEventTimestamp());
    }
    @Test
    void getMaxEventTimestamp_ok_nowAlwaysMoreThanMin() {
        Instant now = Instant.now();

        when(trafficSignJsonDtoV3.getPublicationTimestamp()).thenReturn(now);

        MaxEventTimestampTracker maxEventTimestampTracker = new MaxEventTimestampTracker();
        assertEquals(trafficSignJsonDtoV3,
                maxEventTimestampTracker.updateMaxEventTimeStampAndContinue(trafficSignJsonDtoV3));
        assertEquals(now, maxEventTimestampTracker.getMaxEventTimestamp());
    }

    @Test
    void getMaxEventTimestamp_ok_maxEvenMoreThanMinAndNow() {
        Instant now = Instant.now();

        MaxEventTimestampTracker maxEventTimestampTracker = new MaxEventTimestampTracker();

        when(trafficSignJsonDtoV3.getPublicationTimestamp()).thenReturn(Instant.MAX);
        assertEquals(trafficSignJsonDtoV3,
                maxEventTimestampTracker.updateMaxEventTimeStampAndContinue(trafficSignJsonDtoV3));

        when(trafficSignJsonDtoV3.getPublicationTimestamp()).thenReturn(now);
        assertEquals(trafficSignJsonDtoV3,
                maxEventTimestampTracker.updateMaxEventTimeStampAndContinue(trafficSignJsonDtoV3));

        assertEquals(Instant.MAX, maxEventTimestampTracker.getMaxEventTimestamp());
    }

}