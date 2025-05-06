package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportTypeTest {

    @ParameterizedTest
    @EnumSource(value = TransportType.class)
    void allExcept(TransportType excludingTransportType) {

        Set<TransportType> expectedTransportTypes = Stream.of(TransportType.values())
                .filter(transportType -> excludingTransportType != transportType)
                .collect(Collectors.toSet());

        assertThat(TransportType.allExcept(excludingTransportType)).doesNotContain(excludingTransportType);
        assertThat(TransportType.allExcept(excludingTransportType)).containsAll(expectedTransportTypes);
    }

    @Test
    void allExcept() {
        Set<TransportType> expectedTransportTypes = Stream.of(TransportType.values())
                .filter(transportType -> TransportType.CAR != transportType)
                .filter(transportType -> TransportType.BUS != transportType)
                .collect(Collectors.toSet());

        assertThat(TransportType.allExcept(TransportType.CAR, TransportType.BUS)).doesNotContain(TransportType.CAR, TransportType.BUS);
        assertThat(TransportType.allExcept(TransportType.CAR, TransportType.BUS)).containsAll(expectedTransportTypes);
    }
}