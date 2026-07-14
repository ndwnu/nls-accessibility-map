package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import static org.assertj.core.api.Fail.fail;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.SupplementaryTrafficSignDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignConditionDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.json.JsonMapper;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import jakarta.validation.Valid;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BaseNetworkAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.BlockedRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.NwbVersion;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit.dto.SpeedLimit;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.dto.Node;
import org.apache.logging.log4j.util.Strings;
import org.locationtech.jts.geom.LineString;

@RequiredArgsConstructor
public class DataTypeRegister {

    private final JsonMapper jsonMapper = new JsonMapper();

    private final GraphHopperDriver graphHopperDriver;

    private final TrafficSignConditionDriver trafficSignConditionDriver;

    private final SupplementaryTrafficSignDriver supplementaryTrafficSignDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

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

        String restrictionsConditionName = entry.get("restrictions");
        TrafficSignCondition trafficSignRestrictions;
        if (StringUtils.isBlank(restrictionsConditionName)) {
            trafficSignRestrictions = null;
        } else {
            trafficSignRestrictions = trafficSignConditionDriver.getTrafficSignCondition(restrictionsConditionName)
                    .orElseThrow(() -> new IllegalArgumentException("Failed to resolve restriction with name %s".formatted(restrictionsConditionName)));
        }

        String supplementaryTrafficSignNames = entry.get("supplementaryTrafficSigns");
        List<SupplementaryTrafficSign> supplementaryTrafficSigns;
        if (StringUtils.isBlank(supplementaryTrafficSignNames)) {
            supplementaryTrafficSigns = null;
        } else {
            supplementaryTrafficSigns = Arrays.stream(supplementaryTrafficSignNames.split(","))
                    .map(String::trim)
                    .map(supplementaryTrafficSignName -> supplementaryTrafficSignDriver.getSupplementaryTrafficSign(supplementaryTrafficSignName)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Failed to resolve supplementary traffic sign with name: %s".formatted(
                                            supplementaryTrafficSignName))))
                    .toList();
        }

        String exemptionConditionNames = entry.get("exemptions");
        List<TrafficSignCondition> trafficSignExemptions;
        if (StringUtils.isBlank(exemptionConditionNames)) {
            trafficSignExemptions = Collections.emptyList();
        } else {
            trafficSignExemptions = Arrays.stream(exemptionConditionNames.split(","))
                    .map(String::trim)
                    .map(trafficSignExemptionName -> trafficSignConditionDriver.getTrafficSignCondition(trafficSignExemptionName)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Failed to resolve traffic sign exemption condition with name: %s".formatted(
                                            trafficSignExemptionName))))
                    .toList();
        }

        Edge edge = getEdgeBetweenStartAndEndNode(Integer.parseInt(entry.get("startNodeId")), Integer.parseInt(entry.get("endNodeId")));

        double fraction = Double.parseDouble(entry.get("fraction"));
        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString(
                edge.getWgs84LineString(),
                fraction);

        return TrafficSign.builder()
                .id(entry.get("id"))
                .roadSectionId(edge.getId())
                .fraction(fraction)
                .location(fractionLineString.getCoordinates()[1])
                .rvvCode(entry.get("rvvCode"))
                .restrictions(trafficSignRestrictions)
                .exemptions(trafficSignExemptions)
                .supplementaryTrafficSigns(supplementaryTrafficSigns)
                .blackCode(Objects.nonNull(entry.get("blackCode")) ? entry.get("blackCode").toUpperCase(Locale.US) : null)
                .directionType(DrivingDirectionEnum.valueOf(entry.get("directionType").toUpperCase(Locale.US)))
                .windowTime(Objects.nonNull(entry.get("windowTime")) ? entry.get("windowTime") : null)
                .regulationOrderId(entry.get("regulationOrderId"))
                .build();
    }

    @DataTableType
    public @Valid SpeedLimit mapSpeedLimit(Map<String, String> entry) {

        Edge edge = getEdgeBetweenStartAndEndNode(Integer.parseInt(entry.get("startNodeId")), Integer.parseInt(entry.get("endNodeId")));

        return SpeedLimit.builder()
                .roadSectionId(edge.getId())
                .forwardAverageSpeedLimit(
                        Objects.nonNull(entry.get("forwardAverageSpeedLimit"))
                                ? Integer.parseInt(entry.get("forwardAverageSpeedLimit"))
                                : null)
                .backwardAverageSpeedLimit(
                        Objects.nonNull(entry.get("backwardAverageSpeedLimit"))
                                ? Integer.parseInt(entry.get("backwardAverageSpeedLimit"))
                                : null)
                .build();
    }

    private Edge getEdgeBetweenStartAndEndNode(int startNodeId, int endNodeId) {
        Graph graph = graphHopperDriver.getLastBuiltGraph();
        List<Edge> edges = graph.findEdgesBetweenNodes(startNodeId, endNodeId);

        if (edges.size() != 1) {
            fail("There should be exactly one link between the start and end node. But there were %s"
                    .formatted(edges.size()));
        }
        return edges.getFirst();
    }

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {

        return jsonMapper.convertValue(fromValue, jsonMapper.constructType(toValueType));
    }

    @DataTableType
    public @Valid NwbVersion mapToNwbVersion(Map<String, String> entry) {
        return NwbVersion.builder()
                .versionId(Integer.parseInt(entry.get("versionId")))
                .versionDate(LocalDate.parse(entry.get("versionDate")))
                .isCurrent(Boolean.parseBoolean(entry.get("isCurrent")))
                .build();
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
