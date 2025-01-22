package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.nwb.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionMetaData;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionMetaDataMapperTest {

    private NwbRoadSectionDto nwbRoadSectionDto;

    @BeforeEach
    void setUp() {

        nwbRoadSectionDto = NwbRoadSectionDto.builder()
                .validFrom(LocalDate.of(2023, 10, 13))
                .municipalityName("municipalityName")
                .roadName("roadName")
                .roadNameSource("roadNameSource")
                .townName("townName")
                .build();
    }

    @Test
    void name() {

        RoadSectionMetaData roadSectionMetaData = new RoadSectionMetaDataMapper().map(nwbRoadSectionDto);

        assertThat(roadSectionMetaData.validFrom()).isEqualTo(nwbRoadSectionDto.getValidFrom());
        assertThat(roadSectionMetaData.municipalityName()).isEqualTo(nwbRoadSectionDto.getMunicipalityName());
        assertThat(roadSectionMetaData.name()).isEqualTo(nwbRoadSectionDto.getRoadName());
        assertThat(roadSectionMetaData.nameSource()).isEqualTo(nwbRoadSectionDto.getRoadNameSource());
        assertThat(roadSectionMetaData.townName()).isEqualTo(nwbRoadSectionDto.getTownName());
    }
}
