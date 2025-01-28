package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import io.cucumber.java.DataTableType;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import org.apache.logging.log4j.util.Strings;

@RequiredArgsConstructor
public class DataTypeRegister {

    private final NetworkDataService networkDataService;

    @DataTableType
    public @Valid TrafficSign mapTrafficSign(Map<String, String> entry) {

        return TrafficSign.builder()
                .id(entry.get("id"))
                .startNodeId(Integer.parseInt(entry.get("startNodeId")))
                .endNodeId(Integer.parseInt(entry.get("endNodeId")))
                .fraction(Double.parseDouble(entry.get("fraction")))
                .rvvCode(entry.get("rvvCode").toUpperCase(Locale.US))
                .blackCode(Objects.isNull(entry.get("blackCode")) ? null : entry.get("blackCode").toUpperCase(Locale.US))
                .directionType(DirectionType.valueOf(entry.get("directionType").toUpperCase(Locale.US)))
                .windowTime(entry.get("windowTime"))
                .build();
    }

    @DataTableType
    public @Valid TrafficSignAnalyserJobConfiguration mapTrafficSignAnalyserJobConfiguration(Map<String, String> entry) {

        return TrafficSignAnalyserJobConfiguration.builder()
                .startNode(networkDataService.findNodeById(Long.parseLong(entry.get("startNodeId"))))
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
    public @Valid MapGeneratorJobConfiguration mapMapGeneratorJobConfiguration(Map<String, String> entry) {

        return MapGeneratorJobConfiguration.builder()
                .exportName(entry.get("exportName"))
                .startNode(networkDataService.findNodeById(Long.parseLong(entry.get("startNodeId"))))
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
