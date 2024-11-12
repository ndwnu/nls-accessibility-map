package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalSegmentAccessibleComparatorTest {

    private DirectionalSegmentAccessibleComparator directionalSegmentAccessibleComparator;

    @BeforeEach
    void setUp() {

        directionalSegmentAccessibleComparator = new DirectionalSegmentAccessibleComparator();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, 0
            true, false, -1
            false, true, 1
            false, false, 0
            """)
    void compare_ok(boolean directionalSegment1Accessible, boolean directionalSegment2Accessible, int expectedResult) {

        DirectionalSegment directionalSegment1 = DirectionalSegment.builder()
                .accessible(directionalSegment1Accessible)
                .build();

        DirectionalSegment directionalSegment2 = DirectionalSegment.builder()
                .accessible(directionalSegment2Accessible)
                .build();

        assertThat(directionalSegmentAccessibleComparator.compare(directionalSegment1, directionalSegment2)).isEqualTo(expectedResult);
    }
}
