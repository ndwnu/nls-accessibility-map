package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.dto.RoadOperator;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Repository;

@ExtendWith(MockitoExtension.class)
class RoadOperatorRepositoryTest {

    private RoadOperatorRepository roadOperatorRepository;

    @Mock
    private RoadOperatorStorage roadOperatorStorage;

    @Mock
    private List<RoadOperator> roadOperators;

    @BeforeEach
    void setUp() {

        roadOperatorRepository = new RoadOperatorRepository(roadOperatorStorage);
    }

    @Test
    void findAll() {

        when(roadOperatorStorage.getRoadOperators()).thenReturn(roadOperators);

        assertThat(roadOperatorRepository.findAll()).isEqualTo(roadOperators);
    }

    @Test
    void annotation_repository() {

        AnnotationUtil.classContainsAnnotation(
                roadOperatorRepository.getClass(),
                Repository.class,
                annotation -> assertThat(annotation.value()).isEmpty());
    }
}