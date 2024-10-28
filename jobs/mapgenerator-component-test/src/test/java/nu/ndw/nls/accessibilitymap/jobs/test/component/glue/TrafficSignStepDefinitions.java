package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Fail.fail;

import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.geojson.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignStepDefinitions {

    private final NetworkData networkData;

    private final TrafficSignDriver trafficSignDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Given("with traffic signs")
    public void trafficSigns() {

        trafficSignDriver.stubTrafficSignRequest(
                Set.of("C12"),
                List.of(
                        createTrafficSigns(5, 11, 0.5, "C12"),
                        createTrafficSigns(2, 8, 0.5, "C12")
                )
        );
    }

    private TrafficSignGeoJsonDto createTrafficSigns(long nodeId1, long nodeId2, double fraction, String rvvCode) {

        AccessibilityLink link = networkData.findLinkBetweenNodes(nodeId1, nodeId2);

        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString(
                link.getGeometry(),
                fraction);

        if (fractionLineString.getCoordinates().length != 2) {
            fail("There should a start and end coordinate. But there was only %s"
                    .formatted(fractionLineString.getCoordinates().length));
        }
        Coordinate endCoordinate = fractionLineString.getCoordinates()[1];

        return TrafficSignGeoJsonDto.builder()
                .id(UUID.randomUUID())
                .geometry(new Point(endCoordinate.getX(), endCoordinate.getY()))
                .properties(TrafficSignPropertiesDto.builder()
                        .fraction(fraction)
                        .rvvCode(rvvCode)
                        .drivingDirection(DirectionType.FORTH)
                        .roadSectionId(link.getId())
                        .textSigns(List.of(
                                TextSign.builder()
                                        .type(TextSignType.TIME_PERIOD)
                                        .text("window")
                                        .build()))
                        .build())
                .build();
    }
}
