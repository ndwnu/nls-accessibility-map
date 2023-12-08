package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public RoadSectionsJson mapToRoadSectionsJson(List<RoadSection> inaccessibleRoadSections) {
        return new RoadSectionsJson().inaccessibleRoadSections(
                inaccessibleRoadSections.stream()
                        .map(this::mapToRoadSection)
                        .toList());
    }

    private RoadSectionJson mapToRoadSection(RoadSection roadSection) {
        return new RoadSectionJson()
                .roadSectionId(roadSection.getRoadSectionId())
                .backwardAccessible(roadSection.getBackwardAccessible())
                .forwardAccessible(roadSection.getForwardAccessible());
    }
}
