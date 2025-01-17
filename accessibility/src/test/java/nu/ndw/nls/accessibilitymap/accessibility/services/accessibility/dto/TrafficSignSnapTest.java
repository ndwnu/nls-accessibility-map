package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto;

import com.graphhopper.storage.index.Snap;
import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignSnapTest extends ValidationTest {

    private TrafficSignSnap additionalSnap;

    @Mock
    private Snap snap;

    @BeforeEach
    void setUp() {

        additionalSnap = TrafficSignSnap.builder()
                .snap(snap)
                .build();
    }

    @Test
    void validate_ok() {

        validate(additionalSnap, List.of(), List.of());
    }

    @Test
    void validate_snap_null() {

        additionalSnap = additionalSnap.withSnap(null);
        validate(additionalSnap, List.of("snap"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return additionalSnap.getClass();
    }
}
