package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Fail.fail;

import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Link;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.springboot.test.component.cucumber.StepArgumentParser;
import org.geojson.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignStepDefinitions {

    private final NetworkDataService networkDataService;

    private final TrafficSignDriver trafficSignDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Given("with traffic signs for requested traffic sign types {string}")
    public void trafficSigns(String rvvCodes, List<TrafficSign> trafficSigns) {
        Set<String> rvvCodesSet = StepArgumentParser.parseStringAsSet(rvvCodes);

        trafficSignDriver.stubTrafficSignRequest(
                rvvCodesSet,
                trafficSigns.stream()
                        .map(this::createTrafficSignGeoJsonDto)
                        .toList()
        );
    }

    private TrafficSignGeoJsonDto createTrafficSignGeoJsonDto(TrafficSign trafficSign) {

        Link link = networkDataService.findLinkBetweenNodes(trafficSign.startNodeId(), trafficSign.endNodeId());

        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString(
                link.getWgs84LineString(),
                trafficSign.fraction());

        if (fractionLineString.getCoordinates().length != 2) {
            fail("There should a start and end coordinate. But there was only %s"
                    .formatted(fractionLineString.getCoordinates().length));
        }
        Coordinate endCoordinate = fractionLineString.getCoordinates()[1];

        return TrafficSignGeoJsonDto.builder()
                .id(UUID.fromString(trafficSign.id()))
                .geometry(new Point(endCoordinate.getX(), endCoordinate.getY()))
                .properties(TrafficSignPropertiesDto.builder()
                        .fraction(trafficSign.fraction())
                        .blackCode(trafficSign.blackCode())
                        .rvvCode(trafficSign.rvvCode())
                        .drivingDirection(trafficSign.directionType())
                        .roadSectionId(link.getAccessibilityLink().getId())
                        .textSigns(List.of(
                                TextSign.builder()
                                        .type(TextSignType.TIME_PERIOD)
                                        .text(trafficSign.windowTime())
                                        .build()))
                        .build())
                .build();
    }
}
