package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.JobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import org.apache.logging.log4j.util.Strings;

@RequiredArgsConstructor
public class DataTypeRegister {

    private final NetworkDataService networkDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DataTableType
    public @Valid TrafficSign mapTrafficSign(Map<String, String> entry) {

        return TrafficSign.builder()
                .id(entry.get("id"))
                .startNodeId(Integer.parseInt(entry.get("startNodeId")))
                .endNodeId(Integer.parseInt(entry.get("endNodeId")))
                .fraction(Double.parseDouble(entry.get("fraction")))
                .rvvCode(entry.get("rvvCode").toUpperCase(Locale.US))
                .directionType(DirectionType.valueOf(entry.get("directionType").toUpperCase(Locale.US)))
                .windowTime(entry.get("windowTime"))
                .build();
    }

    @DataTableType
    public @Valid JobConfiguration mapJobConfiguration(Map<String, String> entry) {

        return JobConfiguration.builder()
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
