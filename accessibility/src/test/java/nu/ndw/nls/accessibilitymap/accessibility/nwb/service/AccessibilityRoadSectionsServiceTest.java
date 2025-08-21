package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRoadSectionsServiceTest {

    private static final int VERSION = 20241231;

    private static final int MUNICIPALITY_ID = 1234;

    private AccessibilityRoadSectionsService accessibilityRoadSectionsService;

    @Mock
    private NwbRoadSectionService nwbRoadSectionService;

    @Mock
    private AccessibilityNwbRoadSectionMapper accessibleRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private AccessibilityNwbRoadSection accessibilityRoadSection;

    @BeforeEach
    void setUp() {

        accessibilityRoadSectionsService = new AccessibilityRoadSectionsService(nwbRoadSectionService, accessibleRoadSectionMapper);
    }

    @Test
    void getRoadSections() {

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.emptySet())).thenReturn(Stream.of(nwbRoadSectionDto));
        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSections(VERSION));

        // Second time to test if the response is cached
        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSections(VERSION));

        // Response should be cached by municipality id, actual database call should only occur once
        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.emptySet());
    }

    @Test
    void getRoadSections_threadSafetyCheck() throws InterruptedException, ExecutionException, TimeoutException {

        var start = new CountDownLatch(1);

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.emptySet()))
                .thenAnswer(
                        invocation -> {
                            start.await(1, java.util.concurrent.TimeUnit.SECONDS);
                            return Stream.of(nwbRoadSectionDto);
                        })
                .thenAnswer(
                        invocation -> {
                            start.await(1, java.util.concurrent.TimeUnit.SECONDS);
                            Thread.sleep(100);
                            return Stream.of(nwbRoadSectionDto);
                        });

        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            ArrayList<Future<List<AccessibilityNwbRoadSection>>> futures = new ArrayList<>();

            futures.add(pool.submit(() -> accessibilityRoadSectionsService.getRoadSections(VERSION)));
            futures.add(pool.submit(() -> accessibilityRoadSectionsService.getRoadSections(VERSION)));

            start.countDown();

            for (var future : futures) {
                List<AccessibilityNwbRoadSection> result = future.get(1, java.util.concurrent.TimeUnit.SECONDS);
                assertThat(result).isEqualTo(List.of(accessibilityRoadSection));
            }
        }

        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.emptySet());
    }

    @Test
    void getRoadSectionsByMunicipalityId() {

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID))).thenReturn(Stream.of(nwbRoadSectionDto));
        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID));

        // Second time to test if the response is cached
        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID));

        // Response should be cached by municipality id, actual database call should only occur once
        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID));
    }

    @Test
    void getRoadSectionsByMunicipalityId_threadSafetyCheck() throws InterruptedException, ExecutionException, TimeoutException {

        var start = new CountDownLatch(1);

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID)))
                .thenAnswer(
                        invocation -> {
                            start.await(1, java.util.concurrent.TimeUnit.SECONDS);
                            return Stream.of(nwbRoadSectionDto);
                        })
                .thenAnswer(
                        invocation -> {
                            start.await(1, java.util.concurrent.TimeUnit.SECONDS);
                            Thread.sleep(100);
                            return Stream.of(nwbRoadSectionDto);
                        });
        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            ArrayList<Future<List<AccessibilityNwbRoadSection>>> futures = new ArrayList<>();

            futures.add(pool.submit(() -> accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID)));
            futures.add(pool.submit(() -> accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID)));

            start.countDown();

            for (var future : futures) {
                List<AccessibilityNwbRoadSection> result = future.get(2, java.util.concurrent.TimeUnit.SECONDS);
                assertThat(result).isEqualTo(List.of(accessibilityRoadSection));
            }
        }

        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID));
    }
}