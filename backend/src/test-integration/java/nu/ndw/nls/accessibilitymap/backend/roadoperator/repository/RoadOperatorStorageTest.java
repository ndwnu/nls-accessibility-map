package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson.RoadOperatorTypeEnum;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.controllers.dto.RoadOperator;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.annotation.Validated;

@SpringBootTest(classes = RoadOperatorStorage.class)
@EnableConfigurationProperties
@ExtendWith(MockitoExtension.class)
class RoadOperatorStorageTest {

    @Autowired
    private RoadOperatorStorage roadOperatorStorage;

    @Test
    void loadFromConfigFile_ok() {

        assertThat(roadOperatorStorage).isNotNull();

        List<RoadOperator> roadOperators = roadOperatorStorage.getRoadOperators();

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

    @Test
    void annotation_fieldValidationPropegation_roadOperators() {

        AnnotationUtil.getAnnotationForField(
                RoadOperatorStorage.class,
                Valid.class,
                "RoadOperators");
    }

    @Test
    void annotation_classValidated() {

        AnnotationUtil.classContainsAnnotation(
                RoadOperatorStorage.class,
                Validated.class,
                annotation -> assertThat(annotation).isNotNull());
    }
}