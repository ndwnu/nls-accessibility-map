package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson.RoadOperatorTypeEnum;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RoadOperatorRepository.class)
@EnableConfigurationProperties
@ExtendWith(MockitoExtension.class)
class RoadOperatorRepositoryTest {

    @Autowired
    private RoadOperatorRepository roadOperatorRepository;

    @Test
    void loadFromConfigFile_ok() {

        assertThat(roadOperatorRepository).isNotNull();

        List<RoadOperator> roadOperators = roadOperatorRepository.findAll();

        assertThat(roadOperators).containsExactlyInAnyOrderElementsOf(List.of(
                RoadOperator.builder()
                        .roadOperatorCode("WS14")
                        .roadOperatorType(RoadOperatorTypeEnum.WATER_AUTHORITY)
                        .roadOperatorName("Waterschap De Hollandse Delta")
                        .municipalityId(null)
                        .requestExemptionUrl(URI.create("https://www.wshd.nl/verkeersontheffingen"))
                        .build(),
                RoadOperator.builder()
                        .roadOperatorCode("OI")
                        .roadOperatorType(RoadOperatorTypeEnum.OTHER)
                        .roadOperatorName("Overige instanties")
                        .municipalityId(null)
                        .requestExemptionUrl(null)
                        .build(),
                RoadOperator.builder()
                        .roadOperatorCode("Rijk")
                        .roadOperatorType(RoadOperatorTypeEnum.STATE)
                        .roadOperatorName("Het Rijk")
                        .municipalityId(null)
                        .requestExemptionUrl(null)
                        .build(),
                RoadOperator.builder()
                        .roadOperatorCode("DR")
                        .roadOperatorType(RoadOperatorTypeEnum.PROVINCE)
                        .roadOperatorName("Drenthe")
                        .municipalityId(null)
                        .requestExemptionUrl(URI.create(
                                "https://www.noord-holland.nl/Loket/Producten_en_Diensten/Producten_op_alfabet/O/Ontheffing_van_verkeersregels_op_een_provinciale_weg_aanvragen_Vrijstelling_RVV_1990"))
                        .build(),
                RoadOperator.builder()
                        .roadOperatorCode("1952")
                        .roadOperatorType(RoadOperatorTypeEnum.MUNICIPALITY)
                        .roadOperatorName("Midden-Groningen")
                        .municipalityId("GM1952")
                        .requestExemptionUrl(null)
                        .build()
        ));
    }
}