package nu.ndw.nls.accessibilitymap.backend.municipality.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nu.ndw.nls.accessibilitymap.accessibility.municipality.mappers.MunicipalityIdMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityIdMapperTest {

    @InjectMocks
    private MunicipalityIdMapper municipalityIdMapper;

    @Test
    void map_ok() {
        assertEquals(344, municipalityIdMapper.map("GM0344"));
    }

    @Test
    void map_fail_invalidId() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> municipalityIdMapper.map("INVALID_ID_FORMAT"));

        assertEquals("Incorrect municipalityId INVALID_ID_FORMAT", illegalStateException.getMessage());
    }
}