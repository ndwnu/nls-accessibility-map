package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import static org.assertj.core.api.Fail.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import jakarta.validation.Valid;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BaseNetworkAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BlockedRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.springboot.test.graph.dto.Node;
import org.apache.logging.log4j.util.Strings;

@RequiredArgsConstructor
public class DataTypeRegister {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GraphHopperDriver graphHopperDriver;

    @DataTableType
    public @Valid BlockedRoadSection mapBlockedRoadSection(Map<String, String> entry) {

        return BlockedRoadSection.builder()
                .roadSectionId(Integer.parseInt(entry.get("roadSectionId")))
                .backwardAccessible(Boolean.parseBoolean(entry.get("backwardAccessible")))
                .forwardAccessible(Boolean.parseBoolean(entry.get("forwardAccessible")))
                .build();
    }

    @DataTableType
    public @Valid TrafficSign mapTrafficSign(Map<String, String> entry) {

        return TrafficSign.builder()
                .id(entry.get("id"))
                .startNodeId(Integer.parseInt(entry.get("startNodeId")))
                .endNodeId(Integer.parseInt(entry.get("endNodeId")))
                .fraction(Double.parseDouble(entry.get("fraction")))
                .rvvCode(entry.get("rvvCode"))
                .blackCode(Objects.nonNull(entry.get("blackCode")) ? entry.get("blackCode").toUpperCase(Locale.US) : null)
                .directionType(DirectionType.valueOf(entry.get("directionType").toUpperCase(Locale.US)))
                .windowTime(Objects.nonNull(entry.get("windowTime")) ? entry.get("windowTime") : null)
                .regulationOrderId(entry.get("regulationOrderId"))
                .build();
    }

    @DataTableType
    public AccessibilityRequest mapAccessibilityRequest(Map<String, String> entry) {

        return AccessibilityRequest.builder()
                .municipalityId(entry.getOrDefault("municipalityId", null))
                .endLatitude(mapDoubleValue("endLatitude", entry))
                .endLongitude(mapDoubleValue("endLongitude", entry))
                .vehicleLengthInMeters(mapDoubleValue("vehicleLength", entry))
                .vehicleHeightInMeters(mapDoubleValue("vehicleHeight", entry))
                .vehicleWidthInMeters(mapDoubleValue("vehicleWidth", entry))
                .vehicleWeightInTonnes(mapDoubleValue("vehicleWeight", entry))
                .vehicleAxleLoadInTonnes(mapDoubleValue("vehicleAxleLoad", entry))
                .vehicleType(entry.containsKey("vehicleType") ? VehicleTypeJson.fromValue(entry.get("vehicleType")) : null)
                .emissionClass(entry.containsKey("emissionClass") ? EmissionClassJson.fromValue(entry.get("emissionClass")) : null)
                .fuelTypes(entry.containsKey("fuelTypes") && Objects.nonNull(entry.get("fuelTypes"))
                        ? Arrays.stream(entry.get("fuelTypes").split(",")).map(FuelTypeJson::fromValue).toList()
                        : null)
                .excludeRestrictionsWithEmissionZoneIds(entry.containsKey("excludeRestrictionsWithEmissionZoneIds") && Objects.nonNull(
                        entry.get("excludeRestrictionsWithEmissionZoneIds"))
                        ? Arrays.stream(entry.get("excludeRestrictionsWithEmissionZoneIds").split(",")).toList()
                        : null)
                .excludeRestrictionsWithEmissionZoneTypes(entry.containsKey("excludeRestrictionsWithEmissionZoneTypes") && Objects.nonNull(
                        entry.get("excludeRestrictionsWithEmissionZoneTypes"))
                        ? Arrays.stream(entry.get("excludeRestrictionsWithEmissionZoneTypes").split(","))
                        .map(EmissionZoneTypeJson::fromValue).toList()
                        : null)
                .build();
    }

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {

        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    private static Double mapDoubleValue(String key, Map<String, String> entry) {

        return entry.containsKey(key) ? Double.parseDouble(entry.get(key)) : null;
    }

    @DataTableType
    public @Valid TrafficSignAnalyserJobConfiguration mapTrafficSignAnalyserJobConfiguration(Map<String, String> entry) {

        Optional<Node> node = graphHopperDriver.getLastBuiltGraph().findNodeById(Long.parseLong(entry.get("startNodeId")));
        if (node.isEmpty()) {
            fail("Node with id " + entry.get("startNodeId") + " does not exist.");
        }

        return TrafficSignAnalyserJobConfiguration.builder()
                .startNode(node.get())
                .trafficSignGroups(
                        Arrays.stream(entry.get("trafficSignGroups").split(":"))
                                .map(trafficSigns -> Arrays.stream(trafficSigns.split(","))
                                        .map(String::trim)
                                        .collect(Collectors.toSet()))
                                .toList())
                .reportIssues(
                        Strings.isNotEmpty(entry.get("reportIssues")) && Boolean.parseBoolean(entry.get("reportIssues")))
                .build();
    }

    @DataTableType
    public @Valid BaseNetworkAnalyserJobConfiguration mapBaseNetworkAnalyserJobConfiguration(Map<String, String> entry) {

        Optional<Node> node = graphHopperDriver.getLastBuiltGraph().findNodeById(Long.parseLong(entry.get("startNodeId")));
        if (node.isEmpty()) {
            fail("Node with id " + entry.get("startNodeId") + " does not exist.");
        }

        return BaseNetworkAnalyserJobConfiguration.builder()
                .startNode(node.get())
                .reportIssues(
                        Strings.isNotEmpty(entry.get("reportIssues")) && Boolean.parseBoolean(entry.get("reportIssues")))
                .build();
    }

    @DataTableType
    public @Valid MapGeneratorJobConfiguration mapMapGeneratorJobConfiguration(Map<String, String> entry) {

        Optional<Node> node = graphHopperDriver.getLastBuiltGraph().findNodeById(Long.parseLong(entry.get("startNodeId")));
        if (node.isEmpty()) {
            fail("Node with id " + entry.get("startNodeId") + " does not exist.");
        }

        return MapGeneratorJobConfiguration.builder()
                .exportName(entry.get("exportName"))
                .startNode(node.get())
                .exportTypes(Arrays.stream(entry.get("exportTypes").split(",")).collect(Collectors.toSet()))
                .trafficSignTypes(Arrays.stream(entry.get("trafficSignTypes").split(",")).collect(Collectors.toSet()))
                .includeOnlyWindowSigns(
                        Strings.isNotEmpty(entry.get("exportName"))
                                && Boolean.parseBoolean(entry.get("includeOnlyWindowSigns")))
                .publishEvents(Strings.isNotEmpty(entry.get("publishEvents")) && Boolean.parseBoolean(entry.get("publishEvents")))
                .polygonMaxDistanceBetweenPoints(
                        Strings.isNotEmpty(entry.get("polygonMaxDistanceBetweenPoints"))
                                ? Double.parseDouble(entry.get("polygonMaxDistanceBetweenPoints"))
                                : null)
                .build();
    }
}
