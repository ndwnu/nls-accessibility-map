package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_1;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_2;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import org.junit.jupiter.api.Test;

class RoadSectionJsonResponseMapperTest {

    private final RoadSectionJsonResponseMapper roadSectionJsonResponseMapper = new RoadSectionJsonResponseMapper();

    @Test
    void mapToRoadSectionsJson_ok() {
        assertEquals(new RoadSectionJson().roadSectionId(ID_1).forwardAccessible(true).backwardAccessible(false),
                roadSectionJsonResponseMapper.mapToRoadSection(new RoadSection(ID_1, true, false)));

        assertEquals(new RoadSectionJson().roadSectionId(ID_2).forwardAccessible(false).backwardAccessible(null),
                roadSectionJsonResponseMapper.mapToRoadSection(new RoadSection(ID_2, false, null)));
    }
}
