package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaxNwbVersionTrackerTest {

    private static final String LOCALDATE_STRING_EXAMPLE_NEWER = "2023-11-01";
    private static final LocalDate LOCAL_DATE_EXAMPLE_NEWER = LocalDate.parse(LOCALDATE_STRING_EXAMPLE_NEWER);


    private static final String LOCALDATE_STRING_EXAMPLE_NEWEST = "2023-12-01";
    private static final LocalDate LOCAL_DATE_EXAMPLE_NEWEST = LocalDate.parse(LOCALDATE_STRING_EXAMPLE_NEWEST);

    @Mock
    private TrafficSignJsonDtoV3 trafficSignJsonDtoV3;
    @Mock
    private LocationJsonDtoV3 locationJsonDtoV3;
    @Mock
    private RoadJsonDtoV3 roadJsonDtoV3;


    @Test
    void getMaxNwbReferenceDate_ok_defaultMinDate() {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();
        assertEquals(LocalDate.MIN, maxNwbVersionTracker.getMaxNwbReferenceDate());
    }

    @Test
    void getMaxNwbReferenceDate_ok_nullValuesIgnored() {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        when(trafficSignJsonDtoV3.getLocation()).thenReturn(locationJsonDtoV3);
        when(locationJsonDtoV3.getRoad()).thenReturn(roadJsonDtoV3);
        when(roadJsonDtoV3.getNwbVersion()).thenReturn(null);

        assertEquals(trafficSignJsonDtoV3, maxNwbVersionTracker.updateMaxNwbVersionAndContinue(trafficSignJsonDtoV3));

        assertEquals(LocalDate.MIN, maxNwbVersionTracker.getMaxNwbReferenceDate());
    }

    @Test
    void getMaxNwbReferenceDate_ok_dateNewerThanMin() {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        when(trafficSignJsonDtoV3.getLocation()).thenReturn(locationJsonDtoV3);
        when(locationJsonDtoV3.getRoad()).thenReturn(roadJsonDtoV3);

        when(roadJsonDtoV3.getNwbVersion()).thenReturn(LOCALDATE_STRING_EXAMPLE_NEWER);
        assertEquals(trafficSignJsonDtoV3, maxNwbVersionTracker.updateMaxNwbVersionAndContinue(trafficSignJsonDtoV3));

        assertEquals(LOCAL_DATE_EXAMPLE_NEWER, maxNwbVersionTracker.getMaxNwbReferenceDate());
    }


    @Test
    void getMaxNwbReferenceDate_ok_dateNewestNewerThanNewerAndMin() {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        when(trafficSignJsonDtoV3.getLocation()).thenReturn(locationJsonDtoV3);
        when(locationJsonDtoV3.getRoad()).thenReturn(roadJsonDtoV3);

        when(roadJsonDtoV3.getNwbVersion()).thenReturn(LOCALDATE_STRING_EXAMPLE_NEWEST);
        assertEquals(trafficSignJsonDtoV3, maxNwbVersionTracker.updateMaxNwbVersionAndContinue(trafficSignJsonDtoV3));

        when(roadJsonDtoV3.getNwbVersion()).thenReturn(LOCALDATE_STRING_EXAMPLE_NEWER);
        assertEquals(trafficSignJsonDtoV3, maxNwbVersionTracker.updateMaxNwbVersionAndContinue(trafficSignJsonDtoV3));

        assertEquals(LOCAL_DATE_EXAMPLE_NEWEST, maxNwbVersionTracker.getMaxNwbReferenceDate());
    }



}