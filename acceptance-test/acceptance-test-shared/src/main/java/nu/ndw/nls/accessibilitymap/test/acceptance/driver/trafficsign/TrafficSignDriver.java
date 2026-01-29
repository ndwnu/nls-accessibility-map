package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.or;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.PointTrafficSignProperties;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonFeatureCollectionDto;
import nu.ndw.nls.geojson.geometry.mappers.JtsPointJsonMapper;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Feature;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.FeatureCollection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrafficSignDriver {

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final FileService fileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JtsPointJsonMapper jtsPointJsonMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @SuppressWarnings("java:S3658")
    public void stubTrafficSignRequest(List<TrafficSignGeoJsonDto> trafficSigns) {

        try {
            writeTrafficSignsGeoJsonToDisk(trafficSigns);
            StringValuePattern[] stringValuePatterns = Arrays.stream(TrafficSignType.values())
                    .map(TrafficSignType::getRvvCode)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new)).stream()
                    .map(WireMock::equalTo).toArray(StringValuePattern[]::new);
            StringValuePattern stringValuePattern = stringValuePatterns.length == 1 ? stringValuePatterns[0] : or(stringValuePatterns);
            stubFor(
                    get(urlPathMatching(
                            "/api/rest/static-road-data/traffic-signs/v4/current-state"))
                            .withQueryParam("status", equalTo("PLACED"))
                            .withQueryParam("rvvCode", stringValuePattern)
                            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/geo+json"))
                            .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(
                                            TrafficSignGeoJsonFeatureCollectionDto.builder()
                                                    .features(trafficSigns)
                                                    .build()))));
        } catch (JsonProcessingException exception) {
            fail(exception);
        }
    }

    @SuppressWarnings("java:S3658")
    private void writeTrafficSignsGeoJsonToDisk(List<TrafficSignGeoJsonDto> trafficSigns) {

        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(trafficSigns.stream()
                        .map(trafficSign -> Feature.builder()
                                .id(idSupplier.getAndIncrement())
                                .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(
                                        trafficSign.getGeometry().getCoordinates()
                                                .getFirst(),
                                        trafficSign.getGeometry().getCoordinates()
                                                .getLast()
                                ))))
                                .properties(PointTrafficSignProperties.builder()
                                        .trafficSignId(trafficSign.getId())
                                        .roadSectionId(trafficSign.getProperties().getRoadSectionId())
                                        .fraction(trafficSign.getProperties().getFraction())
                                        .rvvCode(trafficSign.getProperties().getRvvCode())
                                        .drivingDirection(trafficSign.getProperties().getDrivingDirection().toString())
                                        .build())
                                .build())
                        .toList())
                .build();

        try {
            ObjectMapper mapper = JsonMapper.builder().build();

            fileService.writeDataToFile(
                    driverGeneralConfiguration.getDebugFolder().resolve("trafficSigns.geojson").toFile(),
                    mapper.writeValueAsString(featureCollection));
        } catch (JsonProcessingException exception) {
            fail(exception);
        }
    }
}
