package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Mapper turns the list of {@link DirectionalRoadSectionAndTrafficSign} into {@link DirectionalRoadSectionAndTrafficSignGroupedById},
 * which is basically a grouped by roadSectionId and result in backward and forward direction
 */
@Component
public class EnrichedRoadSectionMapper {

    public List<DirectionalRoadSectionAndTrafficSignGroupedById> map(
            List<DirectionalRoadSectionAndTrafficSign> directionalRoadSectionAndTrafficSigns) {

        Map<Long, Map<Direction, List<DirectionalRoadSectionAndTrafficSign>>>
                groupedByIdAndForward = directionalRoadSectionAndTrafficSigns.stream()
                .collect(Collectors.groupingBy(r -> r.getRoadSection().getNwbRoadSectionId(),
                         Collectors.groupingBy(r1 -> r1.getRoadSection().getDirection())));

        return groupedByIdAndForward.values()
                .stream()
                .map(r -> DirectionalRoadSectionAndTrafficSignGroupedById.builder()
                        .forward(getByDirection(r, Direction.FORWARD))
                        .backward(getByDirection(r, Direction.BACKWARD))
                        .build())
                .toList();
    }

    private DirectionalRoadSectionAndTrafficSign getByDirection(
            Map<Direction, List<DirectionalRoadSectionAndTrafficSign>> map, Direction direction) {

        List<DirectionalRoadSectionAndTrafficSign> roadSectionAndTrafficSigns = map.get(direction);

        if (CollectionUtils.isEmpty(roadSectionAndTrafficSigns)) {
            return null;
        }

        return roadSectionAndTrafficSigns.stream().findFirst().orElse(null);

    }

}
