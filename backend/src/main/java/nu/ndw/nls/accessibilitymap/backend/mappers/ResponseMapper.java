package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.Collections;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public RoadSectionsJson mapToRoadSectionsJson(Set<IsochroneMatch> inaccessibleRoadSections) {
        return new RoadSectionsJson().inaccessibleRoadSections(Collections.emptyList());
    }
}
