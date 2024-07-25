package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphhopperVersionMapperTest {

    private final GraphhopperVersionMapper graphhopperVersionMapper = new GraphhopperVersionMapper();

    @Mock
    private NetworkGraphHopper networkGraphHopper;



    @Test
    void map_ok() {
        when(networkGraphHopper.getDataDate()).thenReturn(LocalDate.of(2024, 12, 31).atStartOfDay().toInstant(
                ZoneOffset.UTC));

        assertEquals(20241231, graphhopperVersionMapper.map(networkGraphHopper));
    }
}