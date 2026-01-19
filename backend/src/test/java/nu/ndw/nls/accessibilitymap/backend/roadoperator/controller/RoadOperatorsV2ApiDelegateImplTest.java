package nu.ndw.nls.accessibilitymap.backend.roadoperator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadOperatorJson;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.controller.mapper.RoadOperatorMapperV2;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.service.RoadOperatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RoadOperatorsV2ApiDelegateImplTest {

    @Mock
    private RoadOperatorService roadOperatorRepository;

    @Mock
    private RoadOperatorMapperV2 roadOperatorMapper;

    @Mock
    private List<RoadOperator> roadOperators;

    @Mock
    private List<RoadOperatorJson> mappedRoadOperators;

    private RoadOperatorsV2ApiDelegateImpl roadOperatorsApiDelegate;

    @BeforeEach
    void setUp() {

        roadOperatorsApiDelegate = new RoadOperatorsV2ApiDelegateImpl(roadOperatorRepository, roadOperatorMapper);
    }

    @Test
    void getRoadOperators() {

        when(roadOperatorRepository.findAll()).thenReturn(roadOperators);
        when(roadOperatorMapper.map(roadOperators)).thenReturn(mappedRoadOperators);
        var response = roadOperatorsApiDelegate.getRoadOperators();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mappedRoadOperators);
    }
}
