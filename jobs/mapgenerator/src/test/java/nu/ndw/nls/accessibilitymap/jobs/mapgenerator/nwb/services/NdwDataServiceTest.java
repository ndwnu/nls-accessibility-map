package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionMetaData;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.mappers.RoadSectionMetaDataMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NdwDataServiceTest {

    private NdwDataService ndwDataService;

    @Mock
    private NwbRoadSectionCrudService nwbRoadSectionCrudService;

    @Mock
    private RoadSectionMetaDataMapper roadSectionMetaDataMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto1;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto2;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto3;

    @Mock
    private RoadSectionMetaData roadSectionMetaData1;

    @Mock
    private RoadSectionMetaData roadSectionMetaData2;

    @Mock
    private RoadSectionMetaData roadSectionMetaData3;

    private Accessibility accessibility;

    @BeforeEach
    void setUp() {

        accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(List.of(buildRoadSection(1)))
                .accessibleRoadSectionsWithAppliedRestrictions(List.of(buildRoadSection(2)))
                .combinedAccessibility(List.of(buildRoadSection(3)))
                .build();
        ndwDataService = new NdwDataService(nwbRoadSectionCrudService, roadSectionMetaDataMapper);
    }

    private RoadSection buildRoadSection(long id) {
        return RoadSection.builder()
                .id(id)
                .build();
    }

    @Test
    void addNwbDataToAccessibility_ok() {

        when(nwbRoadSectionCrudService.findById(new Id(1, 1))).thenReturn(Optional.of(nwbRoadSectionDto1));
        when(nwbRoadSectionCrudService.findById(new Id(1, 2))).thenReturn(Optional.of(nwbRoadSectionDto2));
        when(nwbRoadSectionCrudService.findById(new Id(1, 3))).thenReturn(Optional.of(nwbRoadSectionDto3));

        when(roadSectionMetaDataMapper.map(nwbRoadSectionDto1)).thenReturn(roadSectionMetaData1);
        when(roadSectionMetaDataMapper.map(nwbRoadSectionDto2)).thenReturn(roadSectionMetaData2);
        when(roadSectionMetaDataMapper.map(nwbRoadSectionDto3)).thenReturn(roadSectionMetaData3);

        ndwDataService.addNwbDataToAccessibility(accessibility, 1);

        assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions().stream().findFirst().get().getMetaData()).isEqualTo(
                roadSectionMetaData1);
        assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions().stream().findFirst().get().getMetaData()).isEqualTo(
                roadSectionMetaData2);
        assertThat(accessibility.combinedAccessibility().stream().findFirst().get().getMetaData()).isEqualTo(roadSectionMetaData3);
    }

    @Test
    void addNwbDataToAccessibility_illegalArgumentException() {

        when(nwbRoadSectionCrudService.findById(any())).thenReturn(Optional.empty());

        assertThat(catchThrowable(() -> ndwDataService.addNwbDataToAccessibility(accessibility, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to find road section with id '%s' and by version '%s'".formatted(3, 1));
    }
}
