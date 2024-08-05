package nu.ndw.nls.accessibilitymap.backend.mappers;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionJsonResponseMapper {

    public RoadSectionJson mapToRoadSection(RoadSection roadSection) {
        return new RoadSectionJson()
                .roadSectionId(roadSection.getRoadSectionId())
                .backwardAccessible(roadSection.getBackwardAccessible())
                .forwardAccessible(roadSection.getForwardAccessible());
    }
}
