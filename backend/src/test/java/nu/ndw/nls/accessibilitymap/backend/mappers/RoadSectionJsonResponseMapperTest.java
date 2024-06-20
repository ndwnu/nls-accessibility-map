package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import org.junit.jupiter.api.Test;

class RoadSectionJsonResponseMapperTest {

    private static final int ID_1 = 1;
    private static final int ID_2 = 2;
    private final RoadSectionJsonResponseMapper roadSectionJsonResponseMapper = new RoadSectionJsonResponseMapper();

    @Test
    void mapToRoadSectionsJson_ok() {
        assertEquals(new RoadSectionJson().roadSectionId(ID_1).forwardAccessible(true).backwardAccessible(false),
                roadSectionJsonResponseMapper.mapToRoadSection(new RoadSection(ID_1, null, true, false)));

        assertEquals(new RoadSectionJson().roadSectionId(ID_2).forwardAccessible(false).backwardAccessible(null),
                roadSectionJsonResponseMapper.mapToRoadSection(new RoadSection(ID_2, null, false, null)));
    }
}
