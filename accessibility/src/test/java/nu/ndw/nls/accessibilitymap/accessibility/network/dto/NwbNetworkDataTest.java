package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NwbNetworkDataTest {

    private NwbNetworkData nwbNetworkData;

    @BeforeEach
    void setUp() {
        NwbData nwbData = new NwbData(1, List.of(
                AccessibilityNwbRoadSection.builder()
                        .roadSectionId(1L)
                        .fromNode(10L)
                        .toNode(20L)
                        .municipalityId(100)
                        .geometry(null)
                        .forwardAccessible(true)
                        .backwardAccessible(true)
                        .carriagewayTypeCode(CarriagewayTypeCode.HR)
                        .functionalRoadClass("1")
                        .build(),
                AccessibilityNwbRoadSection.builder()
                        .roadSectionId(2L)
                        .fromNode(20L)
                        .toNode(30L)
                        .municipalityId(200)
                        .geometry(null)
                        .forwardAccessible(true)
                        .backwardAccessible(true)
                        .carriagewayTypeCode(CarriagewayTypeCode.HR)
                        .functionalRoadClass("1")
                        .build()));

        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(1,
                List.of(new AccessibilityNwbRoadSectionUpdate(1L, false, false, CarriagewayTypeCode.FP)));

        nwbNetworkData = new NwbNetworkData(nwbData, nwbDataUpdates);
    }

    @Test
    void constructor() {
        assertThat(nwbNetworkData.getNwbVersionId()).isEqualTo(1);
        assertThat(nwbNetworkData.getNwbData().getNwbVersionId()).isEqualTo(1);
        assertThat(nwbNetworkData.getNwbDataUpdates().getNwbVersionId()).isEqualTo(1);
    }

    @Test
    void constructor_appliesUpdate() {
        AccessibilityNwbRoadSection updatedSection = nwbNetworkData.getAccessibilityNwbRoadSections().getFirst();

        assertThat(updatedSection.roadSectionId()).isEqualTo(1L);
        assertThat(updatedSection.fromNode()).isEqualTo(10L);
        assertThat(updatedSection.toNode()).isEqualTo(20L);
        assertThat(updatedSection.municipalityId()).isEqualTo(100);
        assertThat(updatedSection.forwardAccessible()).isFalse();
        assertThat(updatedSection.backwardAccessible()).isFalse();
        assertThat(updatedSection.carriagewayTypeCode()).isEqualTo(CarriagewayTypeCode.FP);
        assertThat(updatedSection.functionalRoadClass()).isEqualTo("1");
    }

    @Test
    void constructor_doesNotApplyUpdate_whenNoMatchingUpdate() {
        AccessibilityNwbRoadSection nonUpdatedSection = nwbNetworkData.getAccessibilityNwbRoadSections().get(1);

        assertThat(nonUpdatedSection.roadSectionId()).isEqualTo(2L);
        assertThat(nonUpdatedSection.fromNode()).isEqualTo(20L);
        assertThat(nonUpdatedSection.toNode()).isEqualTo(30L);
        assertThat(nonUpdatedSection.municipalityId()).isEqualTo(200);
        assertThat(nonUpdatedSection.forwardAccessible()).isTrue();
        assertThat(nonUpdatedSection.backwardAccessible()).isTrue();
        assertThat(nonUpdatedSection.carriagewayTypeCode()).isEqualTo(CarriagewayTypeCode.HR);
        assertThat(nonUpdatedSection.functionalRoadClass()).isEqualTo("1");
    }

    @Test
    void findAccessibilityNwbRoadSectionById() {
        assertThat(nwbNetworkData.findAccessibilityNwbRoadSectionById(1L))
                .isPresent()
                .get()
                .extracting(AccessibilityNwbRoadSection::roadSectionId)
                .isEqualTo(1L);
    }

    @Test
    void findAccessibilityNwbRoadSectionById_notFound() {
        assertThat(nwbNetworkData.findAccessibilityNwbRoadSectionById(999L)).isEmpty();
    }

    @Test
    void findAllAccessibilityNwbRoadSections() {
        assertThat(nwbNetworkData.findAllAccessibilityNwbRoadSections())
                .hasSize(2)
                .extracting(AccessibilityNwbRoadSection::roadSectionId)
                .containsExactly(1L, 2L);
    }

    @Test
    void findAllAccessibilityNwbRoadSectionByMunicipalityId() {
        assertThat(nwbNetworkData.findAllAccessibilityNwbRoadSectionByMunicipalityId(100))
                .hasSize(1)
                .first()
                .satisfies(section -> assertThat(section.roadSectionId()).isEqualTo(1L));
    }

    @Test
    void findAllAccessibilityNwbRoadSectionByMunicipalityId_nonMatchingMunicipalityId_returnsEmpty() {
        assertThat(nwbNetworkData.findAllAccessibilityNwbRoadSectionByMunicipalityId(999)).isEmpty();
    }
}
