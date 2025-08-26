package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.PointNodeProperties;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.await.services.AwaitService;
import nu.ndw.nls.springboot.test.await.services.predicates.AwaitResponseStatusOkPredicate;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.driver.web.AbstractWebClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Request;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

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
    public Response<Void, Void> reloadGraphHopper() {

        validateApiIsStarted();

        Request<Void> request = Request.<Void>builder()
                .id("reloadGraphHopper")
                .method(HttpMethod.PUT)
                .path("/api/rest/static-road-data/accessibility-map/management/graph-hopper/reload")
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .build();

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    @SneakyThrows
    public Response<Void, Void> reloadTrafficSigns() {

        validateApiIsStarted();

        Request<Void> request = Request.<Void>builder()
                .id("reloadTrafficSigns")
                .method(HttpMethod.PUT)
                .path("/api/rest/static-road-data/accessibility-map/management/traffic-sign/reload")
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .build();

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    @SneakyThrows
    public Response<Void, AccessibilityMapResponseJson> getAccessibilityForMunicipality(AccessibilityRequest accessibilityRequest) {

        validateApiIsStarted();

        Request<Void> request = Request.<Void>builder()
                .id("getAccessibilityForMunicipality")
                .method(HttpMethod.GET)
                .path("api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections".formatted(
                        accessibilityRequest.municipalityId()))
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .queryParameters(buildQueryParameters(accessibilityRequest))
                .build();

        var startPoint = buildGeoJsonStartPoint(accessibilityRequest.startLatitude(), accessibilityRequest.startLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-startpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(startPoint));

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    public Response<Void, AccessibilityMapResponseJson> getLastResponseForGetAccessibilityForMunicipality() {

        return responseWebCache().findResponsesByFilter(response ->
                                response.request().id().equals("getAccessibilityForMunicipality")
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, AccessibilityMapResponseJson.class)
                .getLast();
    }

    @SneakyThrows
    public Response<Void, RoadSectionFeatureCollectionJson> getAccessibilityGeoJsonForMunicipality(
            AccessibilityRequest accessibilityRequest) {

        validateApiIsStarted();

        Request<Void> request = Request.<Void>builder()
                .id("getAccessibilityGeoJsonForMunicipality")
                .method(HttpMethod.GET)
                .path("api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections.geojson".formatted(
                        accessibilityRequest.municipalityId()))
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .queryParameters(buildQueryParameters(accessibilityRequest))
                .build();

        var startPoint = buildGeoJsonStartPoint(accessibilityRequest.startLatitude(), accessibilityRequest.startLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-startpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(startPoint));

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    public Response<Void, RoadSectionFeatureCollectionJson> getLastResponseForGetAccessibilityGeoJsonForMunicipality() {

        return responseWebCache().findResponsesByFilter(response ->
                                response.request().id().equals("getAccessibilityGeoJsonForMunicipality")
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, RoadSectionFeatureCollectionJson.class)
                .getLast();
    }

    @SneakyThrows
    public Response<Void, String> genericRequest(String path, String method) {

        validateApiIsStarted();

        Request<Void> request = Request.<Void>builder()
                .id("genericRequest")
                .method(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)))
                .path(path)
                .headers(Map.of(
                        HttpHeaders.AUTHORIZATION, keycloakDriver.getActiveClient().obtainBearerToken()
                ))
                .build();

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    public Response<Void, String> getLastResponseForGenericRequest() {

        return responseWebCache().findResponsesByFilter(response ->
                                response.request().id().equals("genericRequest")
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, String.class)
                .getLast();
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
