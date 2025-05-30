package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.accessibilitymap;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.PointNodeProperties;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.await.services.AwaitService;
import nu.ndw.nls.springboot.test.await.services.predicates.AwaitResponseStatusOkPredicate;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.driver.web.AbstractWebClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Request;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class AccessibilityMapApiClient extends AbstractWebClient {

    private static final String ADMIN_CLIENT_ID = "admin-client";

    private final AccessibilityMapApiConfiguration accessibilityMapApiConfiguration;

    private final FileService fileService;

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final KeycloakDriver keycloakDriver;

    private final AwaitService awaitService;

    private boolean apiIsStarted = false;

    @SneakyThrows
    public Response reloadGraphHopper() {

        validateApiIsStarted();
        Request request = Request.builder()
                .id("reloadGraphHopper")
                .path("/api/rest/static-road-data/accessibility-map/management/graph-hopper/reload")
                .method(HttpMethod.PUT)
                .build();

        getCache().getRequests().add(request);

        try {
            ResponseEntity<String> response = WebClient.create(getEndpoint() + request.path())
                    .method(request.method())
                    .header(HttpHeaders.AUTHORIZATION, keycloakDriver.getClientById(ADMIN_CLIENT_ID).obtainBearerToken())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return cacheResponse(response, request);
        } catch (WebClientResponseException exception) {
            return cacheErrorResponse(exception, request);
        }
    }

    @SneakyThrows
    public Response reloadTrafficSigns() {

        validateApiIsStarted();
        Request request = Request.builder()
                .id("reloadTrafficSigns")
                .path("/api/rest/static-road-data/accessibility-map/management/traffic-sign/reload")
                .method(HttpMethod.PUT)
                .build();

        getCache().getRequests().add(request);

        try {
            ResponseEntity<String> response = WebClient.create(getEndpoint() + request.path())
                    .method(request.method())
                    .header(HttpHeaders.AUTHORIZATION, keycloakDriver.getClientById(ADMIN_CLIENT_ID).obtainBearerToken())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return cacheResponse(response, request);
        } catch (WebClientResponseException exception) {
            return cacheErrorResponse(exception, request);
        }
    }

    @SneakyThrows
    public Response getAccessibilityForMunicipality(AccessibilityRequest accessibilityRequest) {

        validateApiIsStarted();
        Request request = Request.builder()
                .id("getAccessibilityForMunicipality")
                .path("api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections".formatted(
                        accessibilityRequest.municipalityId()))
                .method(HttpMethod.GET)
                .queryParameters(buildQueryParameters(accessibilityRequest))
                .build();

        var startPoint = buildGeoJsonStartPoint(accessibilityRequest.startLatitude(), accessibilityRequest.startLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-startpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(startPoint));

        getCache().getRequests().add(request);
        try {
            ResponseEntity<String> response = WebClient.create()
                    .get().uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(getHost())
                            .port(getPort())
                            .path(request.path())
                            .queryParams(request.queryParameters())
                            .build()
                    )
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return cacheResponse(response, request);
        } catch (WebClientResponseException exception) {
            return cacheErrorResponse(exception, request);
        }
    }

    @SneakyThrows
    public Response getAccessibilityGeoJsonForMunicipality(AccessibilityRequest accessibilityRequest) {

        validateApiIsStarted();
        Request request = Request.builder()
                .id("getAccessibilityForMunicipality")
                .path("api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections.geojson".formatted(
                        accessibilityRequest.municipalityId()))
                .method(HttpMethod.GET)
                .queryParameters(buildQueryParameters(accessibilityRequest))
                .build();

        var startPoint = buildGeoJsonStartPoint(accessibilityRequest.startLatitude(), accessibilityRequest.startLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-startpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(startPoint));

        getCache().getRequests().add(request);
        try {
            ResponseEntity<String> response = WebClient.create()
                    .get().uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(getHost())
                            .port(getPort())
                            .path(request.path())
                            .queryParams(request.queryParameters())
                            .build()
                    )
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return cacheResponse(response, request);
        } catch (WebClientResponseException exception) {
            return cacheErrorResponse(exception, request);
        }
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

    private MultiValueMap<String, String> buildQueryParameters(AccessibilityRequest accessibilityRequest) {

        Map<String, List<String>> queryParameters = new HashMap<>();
        queryParameters.put("latitude", List.of(accessibilityRequest.startLatitude() + ""));
        queryParameters.put("longitude", List.of(accessibilityRequest.startLongitude() + ""));

        if (Objects.nonNull(accessibilityRequest.vehicleType())) {
            queryParameters.put("vehicleType", List.of(accessibilityRequest.vehicleType().getValue()));
        }
        if (Objects.nonNull(accessibilityRequest.fuelType())) {
            queryParameters.put("fuelType", List.of(accessibilityRequest.fuelType().getValue()));
        }
        if (Objects.nonNull(accessibilityRequest.emissionClass())) {
            queryParameters.put("emissionClass", List.of(accessibilityRequest.emissionClass().getValue()));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleLengthInMeters())) {
            queryParameters.put("vehicleLength", List.of(accessibilityRequest.vehicleLengthInMeters() + ""));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleWidthInMeters())) {
            queryParameters.put("vehicleWidth", List.of(accessibilityRequest.vehicleWidthInMeters() + ""));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleHeightInMeters())) {
            queryParameters.put("vehicleHeight", List.of(accessibilityRequest.vehicleHeightInMeters() + ""));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleWeightInKg())) {
            queryParameters.put("vehicleWeight", List.of(accessibilityRequest.vehicleWeightInKg() + ""));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleAxleLoadInKg())) {
            queryParameters.put("vehicleAxleLoad", List.of(accessibilityRequest.vehicleAxleLoadInKg() + ""));
        }
        if (Objects.nonNull(accessibilityRequest.vehicleAxleLoadInKg())) {
            queryParameters.put("vehicleHasTrailer", List.of(accessibilityRequest.vehicleHasTrailer() + ""));
        }

        return MultiValueMap.fromMultiValue(queryParameters);
    }

    @NotNull
    private static FeatureCollection buildGeoJsonStartPoint(double startLatitude, double startLongitude) {

        return FeatureCollection.builder()
                .features(List.of(
                        Feature.builder()
                                .id(1)
                                .geometry(PointGeometry.builder()
                                        .coordinates(List.of(startLongitude, startLatitude))
                                        .build())
                                .properties(PointNodeProperties.builder()
                                        .name("startPoint")
                                        .build())
                                .build()
                ))
                .build();
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

        keycloakDriver.createAndActivateClient(
                ADMIN_CLIENT_ID,
                Set.of("admin"));
    }

    @Override
    public void clearState() {
        //KeycloakDriver will handle cleanup of the admin client.
        super.clearState();
    }
}
