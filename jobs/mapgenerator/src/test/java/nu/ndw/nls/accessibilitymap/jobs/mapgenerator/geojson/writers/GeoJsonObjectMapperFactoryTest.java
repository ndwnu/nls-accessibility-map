package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoJsonObjectMapperFactoryTest {

    private GeoJsonObjectMapperFactory geoJsonObjectMapperFactory;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @BeforeEach
    void setUp() {

        geoJsonObjectMapperFactory = new GeoJsonObjectMapperFactory();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true
            false
            """)
    void create(boolean prettyPrint) throws IOException {

        when(generateConfiguration.prettyPrintJson()).thenReturn(prettyPrint);

        ObjectMapper geoJsonObjectMapper = geoJsonObjectMapperFactory.create(generateConfiguration);
        String writtenValue = geoJsonObjectMapper.writeValueAsString(
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