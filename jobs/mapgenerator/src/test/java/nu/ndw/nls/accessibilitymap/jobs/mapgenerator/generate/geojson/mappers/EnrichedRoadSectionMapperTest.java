package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import org.junit.jupiter.api.Test;

class EnrichedRoadSectionMapperTest {


    private final EnrichedRoadSectionMapper mapper = new EnrichedRoadSectionMapper();

    @Test
    void map_ok() {

        DirectionalRoadSectionAndTrafficSign BF = create(2, Direction.FORWARD);
        DirectionalRoadSectionAndTrafficSign AF = create(1, Direction.FORWARD);
        DirectionalRoadSectionAndTrafficSign BB = create(2, Direction.BACKWARD);
        DirectionalRoadSectionAndTrafficSign AB = create(1, Direction.BACKWARD);
        // Only backward
        DirectionalRoadSectionAndTrafficSign CB = create(3, Direction.BACKWARD);
        // Only forward
        DirectionalRoadSectionAndTrafficSign DF = create(4, Direction.FORWARD);

        assertThat(mapper.map(List.of(BF, AF, BB, AB, CB, DF))).isEqualTo(List.of(
                createEnriched(AB, AF),
                createEnriched(BB, BF),
                createEnriched(CB, null),
                createEnriched(null, DF)));
    }

    private DirectionalRoadSectionAndTrafficSignGroupedById createEnriched(DirectionalRoadSectionAndTrafficSign backward,
            DirectionalRoadSectionAndTrafficSign forward) {
        return DirectionalRoadSectionAndTrafficSignGroupedById.builder()
                    .backward(backward)
                    .forward(forward)
                .build();
    }

    private DirectionalRoadSectionAndTrafficSign create(long nwbRoadSectionId, Direction direction) {
        return DirectionalRoadSectionAndTrafficSign.builder()
                .roadSection(DirectionalRoadSection.builder()
                        .nwbRoadSectionId(nwbRoadSectionId)
                        .direction(direction)
                        .build())
                .build();
    }
}