package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public RoadSectionsJson mapToRoadSectionsJson(Set<IsochroneMatch> inaccessibleRoadSections) {

        return new RoadSectionsJson().inaccessibleRoadSections(inaccessibleRoadSections
                .stream()
                .collect(Collectors
                        .groupingBy(IsochroneMatch::getMatchedLinkId))
                .entrySet()
                .stream()
                .map(this::mapToRoadSection)
                .toList()
        );

    }

    private RoadSectionJson mapToRoadSection(Entry<Integer, List<IsochroneMatch>> isochroneMatchInBothDirections) {
        boolean backwardAccessible = isochroneMatchInBothDirections
                .getValue()
                .stream()
                .anyMatch(IsochroneMatch::isReversed);
        boolean forwardAccessible = isochroneMatchInBothDirections
                .getValue()
                .stream()
                .anyMatch(i -> !i.isReversed());
        return new RoadSectionJson()
                .roadSectionId(isochroneMatchInBothDirections.getKey())
                .backwardAccessible(backwardAccessible)
                .forwardAccessible(forwardAccessible);

    }
}
