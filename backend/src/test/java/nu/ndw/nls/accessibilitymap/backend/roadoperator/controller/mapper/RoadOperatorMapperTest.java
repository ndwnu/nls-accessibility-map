package nu.ndw.nls.accessibilitymap.backend.roadoperator.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RoadOperatorJson.RoadOperatorTypeEnum;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@ExtendWith(MockitoExtension.class)
class RoadOperatorMapperTest {

    private RoadOperatorMapper roadOperatorMapper;

    @BeforeEach
    void setUp() {

        roadOperatorMapper = new RoadOperatorMapper();
    }

    @ParameterizedTest
    @EnumSource(RoadOperatorTypeEnum.class)
    void map(RoadOperatorTypeEnum roadOperatorType) {

        List<RoadOperatorJson> roadOperators = roadOperatorMapper.map(List.of(
                RoadOperator.builder()
                        .roadOperatorCode("DR")
                        .roadOperatorType(roadOperatorType)
                        .roadOperatorName("Drenthe")
                        .municipalityId("123")
                        .requestExemptionUrl(URI.create(
                                "https://www.noord-holland.nl/Loket/Producten_en_Diensten/Producten_op_alfabet/O/Ontheffing_van_verkeersregels_op_een_provinciale_weg_aanvragen_Vrijstelling_RVV_1990"))
                        .build()));

        assertThat(roadOperators).containsExactlyInAnyOrderElementsOf(List.of(
                RoadOperatorJson.builder()
                        .roadOperatorCode("DR")
                        .roadOperatorType(RoadOperatorJson.RoadOperatorTypeEnum.valueOf(roadOperatorType.name()))
                        .roadOperatorName("Drenthe")
                        .municipalityId("123")
                        .requestExemptionUrl(
                                "https://www.noord-holland.nl/Loket/Producten_en_Diensten/Producten_op_alfabet/O/Ontheffing_van_verkeersregels_op_een_provinciale_weg_aanvragen_Vrijstelling_RVV_1990")
                        .build()
        ));
    }

    @Test
    void map_requestExemptionUrl_null() {

        List<RoadOperatorJson> roadOperators = roadOperatorMapper.map(List.of(
                RoadOperator.builder()
                        .roadOperatorCode("DR")
                        .roadOperatorType(RoadOperatorTypeEnum.MUNICIPALITY)
                        .roadOperatorName("Drenthe")
                        .municipalityId("123")
                        .requestExemptionUrl(null)
                        .build()));

        assertThat(roadOperators).containsExactlyInAnyOrderElementsOf(List.of(
                RoadOperatorJson.builder()
                        .roadOperatorCode("DR")
                        .roadOperatorType(RoadOperatorJson.RoadOperatorTypeEnum.MUNICIPALITY)
                        .roadOperatorName("Drenthe")
                        .municipalityId("123")
                        .requestExemptionUrl(null)
                        .build()
        ));
    }

    @Test
    void annotation_component() {

        AnnotationUtil.classContainsAnnotation(
                roadOperatorMapper.getClass(),
                Component.class,
                annotation -> assertThat(annotation.value()).isEmpty());
    }
}
