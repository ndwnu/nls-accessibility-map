package nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("DataFlowIssue")
class RoadChangesTest {

    @Test
    void constructor_same_road_sections() {
        var changedRoads = List.of(
                new ChangedNwbRoadSection(1, true, true, CarriagewayTypeCode.RB),
                new ChangedNwbRoadSection(1, true, false, CarriagewayTypeCode.HR));
        var roadChanges = new RoadChanges(1, changedRoads);

        var expectedChangedRoads = List.of(new ChangedNwbRoadSection(1, true, false, CarriagewayTypeCode.HR));

        assertThat(roadChanges.getChangedNwbRoadSections()).isEqualTo(expectedChangedRoads);
    }

    @Test
    void constructor_changed_road_sections_null() {
        assertThatThrownBy(() -> new RoadChanges(1, null))
                .hasMessage("changedNwbRoadSections is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbVersionId_null() {
        assertThatThrownBy(() -> new RoadChanges(null, List.of()))
                .hasMessage("nwbVersionId is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void merge_ok() {
        var existingRoadChanges = new RoadChanges(1,
                List.of(
                        new ChangedNwbRoadSection(1, true, true, CarriagewayTypeCode.RB),
                        new ChangedNwbRoadSection(2, true, true, CarriagewayTypeCode.RB)));
        var incomingRoadChanges = new RoadChanges(1,
                List.of(
                        new ChangedNwbRoadSection(1, true, false, CarriagewayTypeCode.NRB),
                        new ChangedNwbRoadSection(3, true, false, CarriagewayTypeCode.HR)));
        var expectedRoadChanges = new RoadChanges(1,
                List.of(
                        new ChangedNwbRoadSection(1, true, false, CarriagewayTypeCode.NRB),
                        new ChangedNwbRoadSection(2, true, true, CarriagewayTypeCode.RB),
                        new ChangedNwbRoadSection(3, true, false, CarriagewayTypeCode.HR)));

        assertThat(existingRoadChanges.merge(incomingRoadChanges)).usingRecursiveComparison()
                .isEqualTo(expectedRoadChanges);
    }

    @Test
    void merge_exception() {
        var existingRoadChanges = new RoadChanges(1,
                List.of());

        var incomingRoadChanges = new RoadChanges(2,
                List.of());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> existingRoadChanges.merge(incomingRoadChanges))
                .withMessage("Cannot merge road changes with different nwbVersionId (1 vs 2)");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1,1,true
            1,2,false
            """)
    void isSameVersion(int existingVersion, int incomingVersion, boolean expectedResult) {
        var roadChanges = new RoadChanges(existingVersion, List.of());
        var incomingRoadChanges = new RoadChanges(incomingVersion, List.of());
        assertThat(roadChanges.isSameVersion(incomingRoadChanges)).isEqualTo(expectedResult);
    }
}
