package nu.ndw.nls.accessibilitymap.backend.mappers;

import nu.ndw.nls.accessibilitymap.backend.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestV2MapperTest {

    @Mock
    private TransportTypeV2Mapper transportTypeV2Mapper;

    @InjectMocks
    private AccessibilityRequestV2Mapper accessibilityRequestV2Mapper;
    @Mock
    private VehicleArguments vehicleArguments;

    @Mock
    private Municipality municipality;


    @Test
    void mapToAccessibilityRequest_mapsFieldsCorrectly() {

    }

    @Test
    void mapToAccessibilityRequest_handlesNullValuesGracefully() {

    }
}
