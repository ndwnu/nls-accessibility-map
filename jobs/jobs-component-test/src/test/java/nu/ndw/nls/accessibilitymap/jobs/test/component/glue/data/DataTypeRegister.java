package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.MapGeneratorJobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSignAnalyserJobConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import org.apache.logging.log4j.util.Strings;

@RequiredArgsConstructor
public class DataTypeRegister {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NetworkDataService networkDataService;

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
                .emissionZoneId(entry.get("emissionZoneId"))
                .build();
    }

    @DataTableType
    public AccessibilityRequest mapAccessibilityRequest(Map<String, String> entry) {

        return AccessibilityRequest.builder()
                .municipalityId(entry.getOrDefault("municipalityId", null))
                .startLatitude(mapDoubleValue("startLatitude", entry))
                .startLongitude(mapDoubleValue("startLongitude", entry))
                .vehicleLengthInMeters(mapDoubleValue("vehicleLength", entry))
                .vehicleHeightInMeters(mapDoubleValue("vehicleHeight", entry))
                .vehicleWidthInMeters(mapDoubleValue("vehicleWidth", entry))
                .vehicleWeightInKg(mapDoubleValue("vehicleWeight", entry))
                .vehicleAxleLoadInKg(mapDoubleValue("vehicleAxleLoad", entry))
                .vehicleType(entry.containsKey("vehicleType") ? VehicleTypeJson.fromValue(entry.get("vehicleType")) : null)
                .emissionClass(entry.containsKey("emissionClass") ? EmissionClassJson.fromValue(entry.get("emissionClass")) : null)
                .fuelType(entry.containsKey("fuelType") ? FuelTypeJson.fromValue(entry.get("fuelType")) : null)
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
