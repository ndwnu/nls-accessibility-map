package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class GeoJsonMapperFactoryTest {

    private GeoJsonMapperFactory geoJsonMapperFactory;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @BeforeEach
    void setUp() {

        geoJsonMapperFactory = new GeoJsonMapperFactory();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true
            false
            """)
    void create(boolean prettyPrint) throws IOException {

        when(generateConfiguration.prettyPrintJson()).thenReturn(prettyPrint);

        JsonMapper geoJsonMapper = geoJsonMapperFactory.create(generateConfiguration);
        String writtenValue = geoJsonMapper.writeValueAsString(
                TestDto.builder()
                        .field1("value1")
                        .field2(null)
                        .build());

        if (prettyPrint) {
            assertThat(writtenValue.lines().count()).isGreaterThan(1);
        } else {
            assertThat(writtenValue.lines().count()).isOne();
        }

        assertThatJson(writtenValue)
                .isEqualTo("""
                        {
                            "field1":"value1"
                        }
                        """);
    }

    @Builder
    private record TestDto(
            String field1,
            String field2
    ) {

    }
}
