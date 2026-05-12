package nu.ndw.nls.accessibilitymap.accessibility.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.EdgeAccessHandler.CAR_ACCESSIBLE_ROADS;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.graphhopper.util.shapes.BBox;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.repository.NwbRoadSectionGeometryRepository;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MissingRoadSectionProvider {

    private final NwbRoadSectionGeometryRepository roadSectionGeometryRepository;

    @Timed(value = "accessibilitymap.accessibility.calculateMissingRoadSections")
    @SuppressWarnings("java:S5612")
    public Collection<RoadSection> findAll(
            NetworkData networkData,
            Integer municipalityId,
            Collection<RoadSection> knownRoadSections,
            boolean missingRoadSectionsAreAccessible,
            BBox searchArea
    ) {

        Map<Long, List<RoadSection>> roadSectionsById = knownRoadSections.stream()
                .collect(Collectors.groupingBy(RoadSection::getId));

        Map<Long, LineString> roadSectionGeometriesByArea = roadSectionGeometryRepository.findGeometriesByArea(networkData.getNwbVersion(),
                searchArea, CAR_ACCESSIBLE_ROADS);

        SetView<Long> missingRoadSectionIds = Sets.difference(roadSectionGeometriesByArea.keySet(), roadSectionsById.keySet());

        AtomicInteger roadSectionFragmentIdSupplier = newRoadSectionFragmentIdSupplier(knownRoadSections);
        AtomicInteger directionIdSupplier = newDirectionIdSupplier(knownRoadSections);

        return missingRoadSectionIds.stream()
                .map(networkData.getNwbData()::findAccessibilityNwbRoadSectionById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(accessibilityRoadSection -> {

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
                                        roadSectionGeometriesByArea.get(accessibilityRoadSection.roadSectionId()),
                                        roadSectionFragment,
                                        missingRoadSectionsAreAccessible));
                    }
                    if (accessibilityRoadSection.backwardAccessible()) {
                        roadSectionFragment.setBackwardSegment(
                                buildDirection(
                                        Direction.BACKWARD,
                                        directionIdSupplier.getAndIncrement(),
                                        roadSectionGeometriesByArea.get(accessibilityRoadSection.roadSectionId()),
                                        roadSectionFragment,
                                        missingRoadSectionsAreAccessible));
                    }
                    roadSection.setRoadSectionFragments(List.of(roadSectionFragment));
                    return roadSection;
                })
                .toList();
    }

    private static AtomicInteger newRoadSectionFragmentIdSupplier(
            Collection<RoadSection> accessibleRoadSectionsWithoutAppliedRestrictions
    ) {

        AtomicInteger idSequenceSupplier = new AtomicInteger(accessibleRoadSectionsWithoutAppliedRestrictions.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .map(RoadSectionFragment::getId)
                .max(Integer::compareTo)
                .orElse(1));

        idSequenceSupplier.getAndIncrement();
        return idSequenceSupplier;
    }

    private static AtomicInteger newDirectionIdSupplier(
            Collection<RoadSection> accessibleRoadSectionsWithoutAppliedRestrictions
    ) {

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
            boolean isAccessible
    ) {

        return DirectionalSegment.builder()
                .id(id)
                .roadSectionFragment(roadSectionFragment)
                .accessible(isAccessible)
                .direction(forward)
                .lineString(accessibilityRoadSection)
                .build();
    }
}
