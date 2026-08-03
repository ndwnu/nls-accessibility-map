package nu.ndw.nls.accessibilitymap.accessibility.service.debug.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import tools.jackson.databind.json.JsonMapper;

class GeoJsonBoundingServiceTest {

    private final GeoJsonBoundingService geoJsonBoundingService = new GeoJsonBoundingService(new JsonMapper(), new GeometryFactoryWgs84());

    @Test
    @SneakyThrows
    void boundTargetFileToBoundingBoxFile() {

        File sourceForBounds = getResourceAsFile("sourceForBounds");
        File sourceToBound = getResourceAsFile("sourceToBound");
        Path outputFile = Files.createTempFile("expectedResult", "geojson");

        geoJsonBoundingService.boundTargetFileToBoundingBoxFile(sourceForBounds, sourceToBound, outputFile.toFile());

        File expectedResult = getResourceAsFile("expectedResult");
        String expectedResultGeoJson = Files.readString(expectedResult.toPath(), StandardCharsets.UTF_8);

        String resultGeoJson = Files.readString(outputFile, StandardCharsets.UTF_8);

        JSONAssert.assertEquals(resultGeoJson, expectedResultGeoJson, false);
    }

    @Test
    @SneakyThrows
    void boundTargetFileToBoundingBoxFile_unknownType() {
        File sourceForBounds = getResourceAsFile("sourceForBounds");
        File sourceToBound = getResourceAsFile("sourceToBoundWithUnknownTypeMultiPoint");

        assertThatThrownBy(() -> geoJsonBoundingService.boundTargetFileToBoundingBoxFile(
                sourceForBounds,
                sourceToBound,
                null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported geometry type: MultiPoint");
    }

    private File getResourceAsFile(String resourcePart) {
        String resourcePath = "boundingbox/%s.geojson".formatted(resourcePart);
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Failed to find resource file: " + resourcePath);
        }
        return new File(resource.getFile());
    }
}