package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LocalDateVersionMapperTest {

    private final LocalDateVersionMapper localDateVersionMapper = new LocalDateVersionMapper();

    @Test
    void map() {
        assertEquals(20241231, localDateVersionMapper.map(LocalDate.of(2024, 12, 31)));
    }
}