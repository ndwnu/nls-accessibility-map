package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.response.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
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
