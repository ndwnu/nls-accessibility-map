package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MissingRoadSectionProvider {

    @Timed(value = "accessibilitymap.accessibility.calculateMissingRoadSections")
    @SuppressWarnings("java:S5612")
    public Collection<RoadSection> findAll(
            NetworkData networkData,
            Integer municipalityId,
            Collection<RoadSection> knownRoadSections,
            boolean missingRoadSectionsAreAccessible,
            BBox searchArea) {

        List<AccessibilityNwbRoadSection> roadSections = getAllRoadSections(networkData, municipalityId);

        Map<Long, List<RoadSection>> roadSectionsById = knownRoadSections.stream()
                .collect(Collectors.groupingBy(RoadSection::getId));

        Map<Long, List<AccessibilityNwbRoadSection>> allNwbRoadSectionById = roadSections.stream()
                .collect(Collectors.groupingBy(AccessibilityNwbRoadSection::roadSectionId));

        SetView<Long> missingRoadSectionIds = Sets.difference(allNwbRoadSectionById.keySet(), roadSectionsById.keySet());

        AtomicInteger roadSectionFragmentIdSupplier = newRoadSectionFragmentIdSupplier(knownRoadSections);
        AtomicInteger directionIdSupplier = newDirectionIdSupplier(knownRoadSections);

        return missingRoadSectionIds.stream()
                .map(allNwbRoadSectionById::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(accessibilityRoadSection -> {
                    LineString geometry = accessibilityRoadSection.geometry();
                    if (!searchArea.intersects(PointList.from(geometry))) {
                        return null;
                    }

                    RoadSection roadSection = RoadSection.builder()
                            .id(accessibilityRoadSection.roadSectionId())
                            .functionalRoadClass(accessibilityRoadSection.functionalRoadClass())
                            .build();
                    RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                            .id(roadSectionFragmentIdSupplier.getAndIncrement())
                            .roadSection(roadSection)
                            .build();

                    if (accessibilityRoadSection.forwardAccessible()) {
                        roadSectionFragment.setForwardSegment(
                                buildDirection(
                                        Direction.FORWARD,
                                        directionIdSupplier.getAndIncrement(),
                                        geometry,
                                        roadSectionFragment,
                                        missingRoadSectionsAreAccessible));
                    }
                    if (accessibilityRoadSection.backwardAccessible()) {
                        roadSectionFragment.setBackwardSegment(
                                buildDirection(
                                        Direction.BACKWARD,
                                        directionIdSupplier.getAndIncrement(),
                                        geometry.reverse(),
                                        roadSectionFragment,
                                        missingRoadSectionsAreAccessible));
                    }
                    roadSection.setRoadSectionFragments(List.of(roadSectionFragment));
                    return roadSection;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AccessibilityNwbRoadSection> getAllRoadSections(NetworkData networkData, Integer municipalityId) {

        if (Objects.isNull(municipalityId)) {
            return networkData.getNwbData().findAllAccessibilityNwbRoadSections();
        } else {
            return networkData.getNwbData().findAllAccessibilityNwbRoadSectionByMunicipalityId(
                    municipalityId);
        }
    }

    private static AtomicInteger newRoadSectionFragmentIdSupplier(
            Collection<RoadSection> accessibleRoadSectionsWithoutAppliedRestrictions) {

        AtomicInteger idSequenceSupplier = new AtomicInteger(accessibleRoadSectionsWithoutAppliedRestrictions.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .map(RoadSectionFragment::getId)
                .max(Integer::compareTo)
                .orElse(1));

        idSequenceSupplier.getAndIncrement();
        return idSequenceSupplier;
    }

    private static AtomicInteger newDirectionIdSupplier(
            Collection<RoadSection> accessibleRoadSectionsWithoutAppliedRestrictions) {

        AtomicInteger idSequenceSupplier = new AtomicInteger(accessibleRoadSectionsWithoutAppliedRestrictions.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .map(DirectionalSegment::getId)
                .max(Integer::compareTo)
                .orElse(1));

        idSequenceSupplier.getAndIncrement();
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
