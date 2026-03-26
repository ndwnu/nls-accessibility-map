package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrivingDirectionMapperTest {

    private DrivingDirectionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DrivingDirectionMapper();
    }

    @ParameterizedTest
    @CsvSource({
            "H, true, false",
            "T, false, true",
            "B, true, true",
            "O, true, true"
    })
    void map(String drivingDirection, boolean forward, boolean backward) {
       DirectionalDto<Boolean> direction =  mapper.map(drivingDirection);
       assertThat(direction.forward()).isEqualTo(forward);
       assertThat(direction.reverse()).isEqualTo(backward);
    }
}
