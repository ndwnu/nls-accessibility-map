package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.mapper.RoadOperatorMapper;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.service.RoadOperatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RoadOperatorsApiDelegateImplTest {

    @Mock
    private RoadOperatorService roadOperatorRepository;

    @Mock
    private RoadOperatorMapper roadOperatorMapper;

    @Mock
    private List<RoadOperator> roadOperators;
    @Mock
    private List<RoadOperatorJson> mappedRoadOperators;

    private RoadOperatorsApiDelegateImpl roadOperatorsApiDelegate;

    @BeforeEach
    void setUp() {

        roadOperatorsApiDelegate = new RoadOperatorsApiDelegateImpl(roadOperatorRepository, roadOperatorMapper);
    }

    @Test
    void getRoadOperators() {

        when(roadOperatorRepository.findAll()).thenReturn(roadOperators);
        when(roadOperatorMapper.map(roadOperators)).thenReturn(mappedRoadOperators);
        ResponseEntity<List<RoadOperatorJson>> response = roadOperatorsApiDelegate.getRoadOperators();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mappedRoadOperators);
    }
}