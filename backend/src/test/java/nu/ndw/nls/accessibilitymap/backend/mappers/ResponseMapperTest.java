package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponseMapperTest {

    private static final int ID = 1;
    private static final int ID_2 = 2;

    private ResponseMapper responseMapper;

    @BeforeEach
    void setUp() {
        responseMapper = new ResponseMapper();
    }

    @Test
    void mapToRoadSectionsJson_ok_twoDirections() {
        IsochroneMatch isochroneMatchReversed = IsochroneMatch
                .builder()
                .matchedLinkId(ID)
                .reversed(true)
                .build();
        IsochroneMatch isochroneMatch = IsochroneMatch
                .builder()
                .matchedLinkId(ID)
                .build();
        Set<IsochroneMatch> matches = Set.of(isochroneMatchReversed, isochroneMatch);

        RoadSectionsJson result = responseMapper.mapToRoadSectionsJson(matches);

        assertThat(result.getInaccessibleRoadSections()).hasSize(1);
        assertThat(result.getInaccessibleRoadSections().get(0).getRoadSectionId())
                .isEqualTo(ID);
        assertThat(result.getInaccessibleRoadSections().get(0).getBackwardAccessible())
                .isEqualTo(true);
        assertThat(result.getInaccessibleRoadSections().get(0).getForwardAccessible())
                .isEqualTo(true);
    }

    @Test
    void mapToRoadSectionsJson_ok_oneDirection() {
        IsochroneMatch isochroneMatchReversed = IsochroneMatch
                .builder()
                .matchedLinkId(ID)
                .reversed(true)
                .build();
        Set<IsochroneMatch> matches = Set.of(isochroneMatchReversed);

        RoadSectionsJson result = responseMapper.mapToRoadSectionsJson(matches);

        assertThat(result.getInaccessibleRoadSections()).hasSize(1);
        assertThat(result.getInaccessibleRoadSections().get(0).getRoadSectionId())
                .isEqualTo(ID);
        assertThat(result.getInaccessibleRoadSections().get(0).getBackwardAccessible())
                .isEqualTo(true);
        assertThat(result.getInaccessibleRoadSections().get(0).getForwardAccessible())
                .isEqualTo(false);
    }

    @Test
    void mapToRoadSectionsJson_ok_sort() {
        IsochroneMatch isochroneMatch1 = IsochroneMatch
                .builder()
                .matchedLinkId(ID)
                .build();
        IsochroneMatch isochroneMatch2 = IsochroneMatch
                .builder()
                .matchedLinkId(ID_2)
                .build();
        Set<IsochroneMatch> matches = Set.of(isochroneMatch2, isochroneMatch1);

        RoadSectionsJson result = responseMapper.mapToRoadSectionsJson(matches);

        assertThat(result.getInaccessibleRoadSections()).hasSize(2);
        assertThat(result.getInaccessibleRoadSections().get(0).getRoadSectionId())
                .isEqualTo(ID);
        assertThat(result.getInaccessibleRoadSections().get(1).getRoadSectionId())
                .isEqualTo(ID_2);
    }
}
