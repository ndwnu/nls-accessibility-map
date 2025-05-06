package nu.ndw.nls.accessibilitymap.accessibility.services;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityRoadSectionsService;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MissingRoadSectionProvider {

    private final AccessibilityRoadSectionsService accessibilityRoadSectionsService;

    private final GraphhopperMetaData graphhopperMetaData;

    public Collection<RoadSection> get(
            Integer municipalityId,
            Collection<RoadSection> knownRoadSections,
            boolean missingRoadsSectionAreAccessible) {

        return calculateMissingRoadSections(municipalityId, knownRoadSections, missingRoadsSectionAreAccessible);
    }

    private List<RoadSection> calculateMissingRoadSections(
            Integer municipalityId,
            Collection<RoadSection> knownRoadSections,
            boolean isAccessible) {

        var roadSections = getAllRoadSections(municipalityId);

        var roadSectionsById = knownRoadSections.stream()
                .collect(Collectors.groupingBy(RoadSection::getId));

        Map<Long, List<AccessibilityNwbRoadSection>> allNwbRoadSectionById = roadSections.stream()
                .collect(Collectors.groupingBy(AccessibilityNwbRoadSection::getRoadSectionId));

        SetView<Long> missingRoadSectionIds = Sets.difference(allNwbRoadSectionById.keySet(), roadSectionsById.keySet());

        var roadSectionFragmentIdSupplier = newRoadSectionFragmentIdSupplier(knownRoadSections);
        var directionIdSupplier = newDirectionIdSupplier(knownRoadSections);

        return missingRoadSectionIds.stream()
                .map(allNwbRoadSectionById::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(accessibilityRoadSection -> {
                    var roadSection = RoadSection.builder()
                            .id(accessibilityRoadSection.getRoadSectionId())
                            .build();
                    var roadSectionFragment = RoadSectionFragment.builder()
                            .id(roadSectionFragmentIdSupplier.next())
                            .roadSection(roadSection)
                            .build();

                    LineString geometry = accessibilityRoadSection.getGeometry();
                    if (accessibilityRoadSection.isForwardAccessible()) {
                        roadSectionFragment.setForwardSegment(
                                buildDirection(Direction.FORWARD, directionIdSupplier.next(), geometry, roadSectionFragment, isAccessible));
                    }
                    if (accessibilityRoadSection.isBackwardAccessible()) {
                        roadSectionFragment.setBackwardSegment(
                                buildDirection(
                                        Direction.BACKWARD,
                                        directionIdSupplier.next(),
                                        geometry.reverse(),
                                        roadSectionFragment,
                                        isAccessible));
                    }
                    roadSection.setRoadSectionFragments(List.of(roadSectionFragment));
                    return roadSection;
                })
                .toList();
    }

    private List<AccessibilityNwbRoadSection> getAllRoadSections(Integer municipalityId) {
        if (Objects.isNull(municipalityId)) {
            return accessibilityRoadSectionsService.getRoadSections(graphhopperMetaData.nwbVersion());
        } else {
            return accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(
                    graphhopperMetaData.nwbVersion(),
                    municipalityId);
        }
    }

    private static IntegerSequenceSupplier newRoadSectionFragmentIdSupplier(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions) {

        IntegerSequenceSupplier idSequenceSupplier = new IntegerSequenceSupplier(accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .map(RoadSectionFragment::getId)
                .max(Integer::compareTo)
                .orElse(1));

        idSequenceSupplier.next();
        return idSequenceSupplier;
    }

    private static IntegerSequenceSupplier newDirectionIdSupplier(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions) {

        IntegerSequenceSupplier idSequenceSupplier = new IntegerSequenceSupplier(accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .map(DirectionalSegment::getId)
                .max(Integer::compareTo)
                .orElse(1));

        idSequenceSupplier.next();
        return idSequenceSupplier;
    }

    private static DirectionalSegment buildDirection(
            Direction forward,
            int id,
            LineString accessibilityRoadSection,
            RoadSectionFragment roadSectionFragment,
            boolean isAccessible) {

        return DirectionalSegment.builder()
                .id(id)
                .roadSectionFragment(roadSectionFragment)
                .accessible(isAccessible)
                .direction(forward)
                .lineString(accessibilityRoadSection)
                .build();
    }
}
