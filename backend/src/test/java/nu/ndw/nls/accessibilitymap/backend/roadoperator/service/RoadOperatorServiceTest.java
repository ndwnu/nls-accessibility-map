package nu.ndw.nls.accessibilitymap.backend.roadoperator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.controllers.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.RoadOperatorRepository;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

@ExtendWith(MockitoExtension.class)
class RoadOperatorServiceTest {

    private RoadOperatorService roadOperatorService;

    @Mock
    private RoadOperatorRepository roadOperatorRepository;

    @Mock
    private List<RoadOperator> roadOperators;

    @BeforeEach
    void setUp() {

        roadOperatorService = new RoadOperatorService(roadOperatorRepository);
    }

    @Test
    void findAll() {

        when(roadOperatorRepository.findAll()).thenReturn(roadOperators);

        assertThat(roadOperatorService.findAll()).isEqualTo(roadOperators);
    }

    @Test
    void annotation_service() {

        AnnotationUtil.classContainsAnnotation(
                roadOperatorService.getClass(),
                Service.class,
                annotation -> assertThat(annotation.value()).isEmpty());
    }
}