package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_1;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import org.junit.jupiter.api.Test;

class ResponseMapperTest {

    private final ResponseMapper responseMapper = new ResponseMapper();

    @Test
    void mapToRoadSectionsJson_ok() {
        List<RoadSection> roadSections = List.of(
                new RoadSection(ID_1, true, false),
                new RoadSection(ID_2, false, null));

        RoadSectionsJson result = responseMapper.mapToRoadSectionsJson(roadSections);

        assertThat(result.getInaccessibleRoadSections()).isEqualTo(List.of(
                new RoadSectionJson().roadSectionId(ID_1).forwardAccessible(true).backwardAccessible(false),
                new RoadSectionJson().roadSectionId(ID_2).forwardAccessible(false).backwardAccessible(null)));
    }
}
