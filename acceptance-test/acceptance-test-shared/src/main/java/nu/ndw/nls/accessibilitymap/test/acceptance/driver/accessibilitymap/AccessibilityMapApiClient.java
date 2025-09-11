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
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
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

    private boolean apiIsStarted;

    private final JtsPointJsonMapper jtsPointJsonMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @SneakyThrows
    public Response<Void, Void> reloadGraphHopper() {

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

        var endpoint = buildGeoJsonEndPoint(accessibilityRequest.endLatitude(), accessibilityRequest.endLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-endpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(endpoint));

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    public Response<Void, AccessibilityMapResponseJson> getLastResponseForGetAccessibilityForMunicipality() {

        return responseWebCache().findResponsesByFilter(response ->
                                "getAccessibilityForMunicipality".equals(response.request().id())
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, AccessibilityMapResponseJson.class)
                .getLast();
    }

    @SneakyThrows
    public Response<Void, RoadSectionFeatureCollectionJson> getAccessibilityGeoJsonForMunicipality(
            AccessibilityRequest accessibilityRequest
    ) {

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

        var endpoint = buildGeoJsonEndPoint(accessibilityRequest.endLatitude(), accessibilityRequest.endLongitude());

        fileService.writeDataToFile(
                driverGeneralConfiguration.getDebugFolder().resolve("request-%s-endpoint.geojson".formatted(request.id())).toFile(),
                JsonMapper.builder().build().writeValueAsString(endpoint));

        return request(request, new ParameterizedTypeReference<>() {
        });
    }

    public Response<Void, RoadSectionFeatureCollectionJson> getLastResponseForGetAccessibilityGeoJsonForMunicipality() {

        return responseWebCache().findResponsesByFilter(response ->
                                "getAccessibilityGeoJsonForMunicipality".equals(response.request().id())
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, RoadSectionFeatureCollectionJson.class)
                .getLast();
    }

    @SneakyThrows
    public Response<Void, String> genericRequest(String path, String method) {

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
                                "genericRequest".equals(response.request().id())
                                        && response.request().method().equals(HttpMethod.GET),
                        Void.class, String.class)
                .getLast();
    }

    public static MultiValueMap<String, String> buildQueryParameters(AccessibilityRequest accessibilityRequest) {

        Map<String, List<String>> queryParameters = new HashMap<>();
        queryParameters.put("latitude", List.of(accessibilityRequest.endLatitude() + ""));
        queryParameters.put("longitude", List.of(accessibilityRequest.endLongitude() + ""));

        if (Objects.nonNull(accessibilityRequest.vehicleType())) {
            queryParameters.put("vehicleType", List.of(accessibilityRequest.vehicleType().getValue()));
        }
        if (Objects.nonNull(accessibilityRequest.fuelTypes())) {
            queryParameters.put("fuelTypes", accessibilityRequest.fuelTypes().stream().map(FuelTypeJson::getValue).toList());
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
        if (Objects.nonNull(accessibilityRequest.excludeRestrictionsWithEmissionZoneIds())) {
            queryParameters.put("excludeEmissionZoneIds", accessibilityRequest.excludeRestrictionsWithEmissionZoneIds());
        }
        if (Objects.nonNull(accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes())) {
            queryParameters.put("excludeEmissionZoneTypes", accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes().stream().
                    map(EmissionZoneTypeJson::getValue).toList());
        }

        return MultiValueMap.fromMultiValue(queryParameters);
    }

    @NotNull
    private FeatureCollection buildGeoJsonEndPoint(double endLatitude, double endLongitude) {

        return FeatureCollection.builder()
                .features(List.of(
                        Feature.builder()
                                .id(1)
                                .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(endLongitude, endLatitude))))
                                .properties(PointNodeGraphProperties.builder()
                                        .name("endpoint")
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
}
