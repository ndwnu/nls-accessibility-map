package nu.ndw.nls.accessibilitymap.test.performance.simulation.roadsections;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.assertj.core.api.Assertions.assertThat;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperTestDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignTestDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.springboot.gatling.test.simulation.AbstractSimulation;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import nu.ndw.nls.springboot.test.component.state.StateManager;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.service.dto.GenerateSpecification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoadSectionsSimulation extends AbstractSimulation {

    private final GraphHopperTestDataService graphHopperTestDataService;

    private final TrafficSignDriver trafficSignDriver;

    private final TrafficSignTestDataService trafficSignTestDataService;

    private final StateManager stateManager;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final JobDriver jobDriver;

    private Graph graph;

    public RoadSectionsSimulation(
            StateManager stateManager,
            AccessibilityMapApiClient accessibilityMapApiClient,
            GraphHopperTestDataService graphHopperTestDataService,
            TrafficSignDriver trafficSignDriver,
            TrafficSignTestDataService trafficSignTestDataService,
            JobDriver jobDriver
    ) {

        super(RoadSectionsSimulationConfiguration.class);

        this.stateManager = stateManager;
        this.graphHopperTestDataService = graphHopperTestDataService;
        this.trafficSignDriver = trafficSignDriver;
        this.trafficSignTestDataService = trafficSignTestDataService;
        this.accessibilityMapApiClient = accessibilityMapApiClient;
        this.jobDriver = jobDriver;
    }

    @Override
    public void before() {

        super.before();
        stateManager.beforeScenario();

        setupGraphHopperNetwork();
        setupTrafficSigns();
    }

    @Override
    public void after() {

        super.after();
        stateManager.afterScenario();
    }

    private void setupGraphHopperNetwork() {
        RoadSectionsSimulationConfiguration roadSectionsSimulationConfiguration = this.getSimulationSpecificConfiguration();
        graph = graphHopperTestDataService.generate(GenerateSpecification.builder()
                .numberOfNodes(roadSectionsSimulationConfiguration.numberOfGraphNodes())
                .build());

        graphhopperDataIsReloaded();
    }

    private void setupTrafficSigns() {
        Random randomGenerator = new Random(Long.MAX_VALUE);

        RoadSectionsSimulationConfiguration roadSectionsSimulationConfiguration = this.getSimulationSpecificConfiguration();
        List<TrafficSignGeoJsonDto> trafficSigns = Stream.generate(
                        () -> {
                            Edge edge = graph.getEdges().get(randomGenerator.nextInt(0, graph.getEdges().size()));
                            return trafficSignTestDataService.createTrafficSignGeoJsonDto(TrafficSign.builder()
                                    .id(UUID.randomUUID().toString())
                                    .startNodeId(edge.getFromNode().getId().intValue())
                                    .endNodeId(edge.getToNode().getId().intValue())
                                    .fraction(0.5)
                                    .rvvCode(TrafficSignType.C12.getRvvCode())
                                    .directionType(DirectionType.FORTH)
                                    .build());
                        })
                .limit(roadSectionsSimulationConfiguration.numberOfTrafficSigns())
                .toList();

        trafficSignDriver.stubTrafficSignRequest(trafficSigns);
        jobDriver.run("trafficSignUpdateCache");
        trafficSignsDataIsReloaded();
    }

    public List<PopulationBuilder> getSimulations() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .municipalityId("GM0001")
                .endLatitude(3D)
                .endLongitude(7D)
                .vehicleType(VehicleTypeJson.TRUCK)
                .fuelTypes(List.of(FuelTypeJson.DIESEL))
                .emissionClass(EmissionClassJson.EURO_3)
                .vehicleWidthInMeters(2D)
                .build();

        return List.of(
                scenario("InaccessibleRoadSections-Json")
                        .group(getSimulationName()).on(
                                InaccessibleRoadSectionsJson(accessibilityRequest)
                        )
                        .injectOpen(getSimulationBehaviour())
                        .protocols(List.of(getHttpProtocol())),

                scenario("InaccessibleRoadSections-GeoJson")
                        .group(getSimulationName()).on(
                                InaccessibleRoadSectionsGeoJson(accessibilityRequest)
                        )
                        .injectOpen(getSimulationBehaviour())
                        .protocols(List.of(getHttpProtocol()))
        );
    }

    private HttpProtocolBuilder getHttpProtocol() {
        return http
                .baseUrl(accessibilityMapApiClient.getEndpoint())
                .acceptHeader(MediaType.APPLICATION_JSON_VALUE)
                .contentTypeHeader(MediaType.APPLICATION_JSON_VALUE);
    }

    private ChainBuilder InaccessibleRoadSectionsJson(AccessibilityRequest accessibilityRequest) {
        Map<String, String> queryParams = AccessibilityMapApiClient.buildQueryParameters(accessibilityRequest).asSingleValueMap();

        return exec(http("InaccessibleRoadSections-Json")
                .get("/api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections"
                        .formatted(accessibilityRequest.municipalityId()))
                .queryParamMap(queryParams.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (Object) entry.getValue())))
                .check(status().is(HttpStatus.OK.value()))
        );
    }

    private ChainBuilder InaccessibleRoadSectionsGeoJson(AccessibilityRequest accessibilityRequest) {
        Map<String, String> queryParams = AccessibilityMapApiClient.buildQueryParameters(accessibilityRequest).asSingleValueMap();

        return exec(http("InaccessibleRoadSections-GeoJson")
                .get("/api/rest/static-road-data/accessibility-map/v1/municipalities/%s/road-sections.geojson"
                        .formatted(accessibilityRequest.municipalityId()))
                .queryParamMap(queryParams.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (Object) entry.getValue())))
                .check(status().is(HttpStatus.OK.value()))
        );
    }

    private void graphhopperDataIsReloaded() {
        Response<Void, Void> response = accessibilityMapApiClient.reloadCache();
        assertThat(response.containsError())
                .withFailMessage("Reloading graphhopper failed. %s", response.error())
                .isFalse();
    }

    private void trafficSignsDataIsReloaded() {
        Response<Void, Void> response = accessibilityMapApiClient.reloadCache();
        assertThat(response.containsError())
                .withFailMessage("Reloading traffic signs failed. %s", response.error())
                .isFalse();
    }
}
