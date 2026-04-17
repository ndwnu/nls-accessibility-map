package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.Test;

@SuppressWarnings("DataFlowIssue")
class NwbDataUpdatesTest {

    @Test
    void constructor_same_road_sections() {
        var changedRoads = List.of(
                new AccessibilityNwbRoadSectionUpdate(1, true, true, CarriagewayTypeCode.RB),
                new AccessibilityNwbRoadSectionUpdate(1, true, false, CarriagewayTypeCode.HR));
        var roadChanges = new NwbDataUpdates(1, changedRoads);

        var expectedChangedRoads = List.of(new AccessibilityNwbRoadSectionUpdate(1, true, false, CarriagewayTypeCode.HR));

        assertThat(roadChanges.getAccessibilityNwbRoadSectionUpdates()).isEqualTo(expectedChangedRoads);
    }

    @Test
    void constructor_changed_road_sections_null() {
        assertThatThrownBy(() -> new NwbDataUpdates(1, null))
                .hasMessage("accessibilityNwbRoadSectionUpdates is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbVersionId_null() {
        assertThatThrownBy(() -> new NwbDataUpdates(null, List.of()))
                .hasMessage("nwbVersionId is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void merge_ok() {
        var existingRoadChanges = new NwbDataUpdates(1,
                List.of(
                        new AccessibilityNwbRoadSectionUpdate(1, true, true, CarriagewayTypeCode.RB),
                        new AccessibilityNwbRoadSectionUpdate(2, true, true, CarriagewayTypeCode.RB)));
        var incomingRoadChanges = new NwbDataUpdates(1,
                List.of(
                        new AccessibilityNwbRoadSectionUpdate(1, true, false, CarriagewayTypeCode.NRB),
                        new AccessibilityNwbRoadSectionUpdate(3, true, false, CarriagewayTypeCode.HR)));
        var expectedRoadChanges = new NwbDataUpdates(1,
                List.of(
                        new AccessibilityNwbRoadSectionUpdate(1, true, false, CarriagewayTypeCode.NRB),
                        new AccessibilityNwbRoadSectionUpdate(2, true, true, CarriagewayTypeCode.RB),
                        new AccessibilityNwbRoadSectionUpdate(3, true, false, CarriagewayTypeCode.HR)));

        assertThat(existingRoadChanges.merge(incomingRoadChanges)).usingRecursiveComparison()
                .isEqualTo(expectedRoadChanges);
    }

    @Test
    void merge_not_same_nwbVersionId() {
        var existingRoadChanges = new NwbDataUpdates(1,
                List.of());

        var incomingRoadChanges = new NwbDataUpdates(2,
                List.of());

        assertThatThrownBy(() -> existingRoadChanges.merge(incomingRoadChanges)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot merge updates from different NWB versions");
    }
}
