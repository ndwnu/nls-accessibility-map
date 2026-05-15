package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap;

import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.LocationJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.geojson.geometry.mappers.JtsPointJsonMapper;
import nu.ndw.nls.springboot.test.await.services.AwaitService;
import nu.ndw.nls.springboot.test.await.services.predicates.AwaitResponseStatusOkPredicate;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.driver.web.AbstractWebClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Request;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Feature;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.FeatureCollection;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.PointNodeGraphProperties;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class AccessibilityMapApiClient extends AbstractWebClient {

    private static final String MEDIA_TYPE_GEOJSON = "application/geo+json";

    private static final String ADMIN_CLIENT_ID = "admin-client";

    private final AccessibilityMapApiConfiguration accessibilityMapApiConfiguration;

    private final FileService fileService;

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final KeycloakDriver keycloakDriver;

    private final AwaitService awaitService;

    private boolean apiIsStarted;

    private final JtsPointJsonMapper jtsPointJsonMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @SneakyThrows
    public Response<Void, Void> reloadCache() {
        Request<Void> request = Request.<Void>builder()
                .id("reloadNetwork")
                .method(HttpMethod.POST)
                .path("api/rest/static-road-data/accessibility-map/actuator/accessibility-map-cache-reload")
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .build();

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    @SneakyThrows
    public Response<AccessibilityRequestJson, String>
    getAccessibility(AccessibilityRequestJson accessibilityRequestJson) {
        Request<AccessibilityRequestJson> request = Request.<AccessibilityRequestJson>builder()
                .id("getAccessibility")
                .method(HttpMethod.POST)
                .path("api/rest/static-road-data/accessibility-map/v2/accessibility")
                .headers(Map.of(
                        HttpHeaders.ACCEPT_ENCODING, "gzip",
                        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ))
                .body(accessibilityRequestJson)
                .build();

        debugLogDestination(accessibilityRequestJson.getDestination(), request);

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    @SneakyThrows
    public Response<AccessibilityRequestJson, String>
    getAccessibilityGeoJson(AccessibilityRequestJson accessibilityRequestJson) {
        Request<AccessibilityRequestJson> request = Request.<AccessibilityRequestJson>builder()
                .id("getAccessibilityGeoJson")
                .method(HttpMethod.POST)
                .path("api/rest/static-road-data/accessibility-map/v2/accessibility.geojson")
                .headers(Map.of(
                        HttpHeaders.ACCEPT_ENCODING, "gzip",
                        HttpHeaders.ACCEPT, MEDIA_TYPE_GEOJSON,
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ))
                .body(accessibilityRequestJson)
                .build();

        debugLogDestination(accessibilityRequestJson.getDestination(), request);

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    public Response<AccessibilityRequestJson, String> getLastResponseForGetAccessibilityGeoJson() {
        return responseWebCache().findResponsesByFilter(
                        response ->
                                "getAccessibilityGeoJson".equals(response.request().id())
                                && response.request().method().equals(HttpMethod.POST),
                        AccessibilityRequestJson.class, String.class)
                .getLast();
    }

    @SneakyThrows
    public Response<Void, String> genericRequest(String path, String method) {
        Request<Void> request = Request.<Void>builder()
                .id("genericRequest")
                .method(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)))
                .path(path)
                .build();

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    public Response<Void, String> getLastResponseForGenericRequest() {
        return responseWebCache().findResponsesByFilter(
                        response ->
                                "genericRequest".equals(response.request().id())
                                && response.request().method().equals(HttpMethod.GET),
                        Void.class, String.class)
                .getLast();
    }

    @NotNull
    private Optional<FeatureCollection> buildGeoJsonEndPoint(LocationJson locationJson) {
        if (Objects.isNull(locationJson)) {
            return Optional.empty();
        }
        return Optional.of(FeatureCollection.builder()
                .features(List.of(
                        Feature.builder()
                                .id(1)
                                .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(
                                        locationJson.getLongitude(),
                                        locationJson.getLatitude()))))
                                .properties(PointNodeGraphProperties.builder()
                                        .name("endpoint")
                                        .build())
                                .build()
                ))
                .build());
    }

    public String getEndpoint() {
        return String.format("http://%s:%s", getHost(), getPort());
    }

    @Override
    protected int getPort() {
        return accessibilityMapApiConfiguration.getPort();
    }

    @Override
    protected String getHost() {
        return accessibilityMapApiConfiguration.getHost();
    }

    @Override
    public void prepareState() {
        validateApiIsStarted();

        keycloakDriver.createAndActivateClient(
                ADMIN_CLIENT_ID,
                Set.of("admin"));
    }

    @Override
    public void clearState() {
        validateApiIsStarted();

        //KeycloakDriver will handle clean-up of the admin client.
        super.clearState();
    }

    private void validateApiIsStarted() {
        if (!apiIsStarted) {
            awaitService.waitFor(
                    URI.create("http://%s:%s/api/rest/static-road-data/accessibility-map/actuator/health".formatted(getHost(), getPort())),
                    "AccessibilityApi",
                    accessibilityMapApiConfiguration.getAwaitDuration(),
                    AwaitResponseStatusOkPredicate.getInstance());

            apiIsStarted = true;
        }
    }

    public Response<Void, String> getMunicipalities(String apiVersion) {
        Request<Void> request = Request.<Void>builder()
                .id("getMunicipalities")
                .method(HttpMethod.GET)
                .path("api/rest/static-road-data/accessibility-map/%s/municipalities".formatted(apiVersion))
                .build();

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    public Response<Void, String> getLastResponseForGetMunicipalities() {
        return responseWebCache().findResponsesByFilter(
                        response ->
                                "getMunicipalities".equals(response.request().id())
                                && response.request().method().equals(HttpMethod.GET),
                        Void.class, String.class)
                .getLast();
    }

    public Response<Void, String> getRoadOperators(String apiVersion) {
        Request<Void> request = Request.<Void>builder()
                .id("getRoadOperators")
                .method(HttpMethod.GET)
                .path("api/rest/static-road-data/accessibility-map/%s/road-operators".formatted(apiVersion))
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .build();

        return request(
                request, new ParameterizedTypeReference<>() {
                });
    }

    public Response<Void, String> getLastResponseForGetRoadOperators() {
        return responseWebCache().findResponsesByFilter(
                        response ->
                                "getRoadOperators".equals(response.request().id())
                                && response.request().method().equals(HttpMethod.GET),
                        Void.class, String.class)
                .getLast();
    }

    private void debugLogDestination(LocationJson locationJson, Request<?> request) {
        buildGeoJsonEndPoint(locationJson)
                .ifPresent(endpoint -> {
                    try {
                        fileService.writeDataToFile(
                                driverGeneralConfiguration.getDebugFolder()
                                        .resolve("request-%s-endpoint.geojson".formatted(request.id()))
                                        .toFile(),
                                JsonMapper.builder().build().writeValueAsString(endpoint));
                    } catch (JsonProcessingException exception) {
                        fail(exception);
                    }
                });
    }
}
