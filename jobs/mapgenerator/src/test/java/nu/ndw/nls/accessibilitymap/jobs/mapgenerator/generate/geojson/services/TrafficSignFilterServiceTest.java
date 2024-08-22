package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignFilterServiceTest {

    @Mock
    private TextSignFilterService textSignFilterService;

    @InjectMocks
    private TrafficSignFilterService trafficSignFilterService;

    @Mock
    private TrafficSignGeoJsonDto trafficSignA;

    @Mock
    private TrafficSignGeoJsonDto trafficSignB;

    @Mock
    private TrafficSignGeoJsonDto trafficSignC;

    @Mock
    private TrafficSignPropertiesDto propertiesA;

    @Mock
    private TrafficSignPropertiesDto propertiesB;

    @Mock
    private TrafficSignPropertiesDto propertiesC;

    @Mock
    private List<TextSignDto> textSignsA;

    @Mock
    private List<TextSignDto> textSignsB;

    @Mock
    private List<TextSignDto> textSignsC;

    @Test
    void findWindowTimeTrafficSignsOrderInDrivingDirection_ok_complexScenario() {
        when(trafficSignA.getProperties()).thenReturn(propertiesA);
        when(trafficSignB.getProperties()).thenReturn(propertiesB);
        when(trafficSignC.getProperties()).thenReturn(propertiesC);
        when(propertiesA.getTextSigns()).thenReturn(textSignsA);
        when(propertiesB.getTextSigns()).thenReturn(textSignsB);
        when(propertiesC.getTextSigns()).thenReturn(textSignsC);

        // a: no window time -> no match
        // b: window time, but has excluding or a pre-announcement -> No match
        // c: window time and no excluding or a pre-announcement -> OK
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.FORTH);
        when(propertiesB.getDrivingDirection()).thenReturn(DirectionType.FORTH);
        when(propertiesC.getDrivingDirection()).thenReturn(DirectionType.FORTH);

        when(textSignFilterService.hasWindowTime(textSignsA)).thenReturn(false);
        when(textSignFilterService.hasWindowTime(textSignsB)).thenReturn(true);
        when(textSignFilterService.hasWindowTime(textSignsC)).thenReturn(true);

        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsB)).thenReturn(false);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsC)).thenReturn(true);

        List<TrafficSignGeoJsonDto> result =
                trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(List.of(trafficSignA,
                                trafficSignB, trafficSignC), true);

        assertThat(result).isEqualTo(List.of(trafficSignC));
    }

    @Test
    void findWindowTimeTrafficSignsOrderInDrivingDirection_ok_wrongDrivingDirection() {
        when(trafficSignA.getProperties()).thenReturn(propertiesA);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.BACK);

        List<TrafficSignGeoJsonDto> result =
                trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(List.of(trafficSignA), true);

        assertThat(result).isEqualTo(List.of());
    }

    @Test
    void findWindowTimeTrafficSignsOrderInDrivingDirection_ok_allMatchShouldBeInCorrectForwardOrder() {
        when(trafficSignA.getProperties()).thenReturn(propertiesA);
        when(trafficSignB.getProperties()).thenReturn(propertiesB);
        when(trafficSignC.getProperties()).thenReturn(propertiesC);
        when(propertiesA.getTextSigns()).thenReturn(textSignsA);
        when(propertiesB.getTextSigns()).thenReturn(textSignsB);
        when(propertiesC.getTextSigns()).thenReturn(textSignsC);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.FORTH);
        when(propertiesB.getDrivingDirection()).thenReturn(DirectionType.FORTH);
        when(propertiesC.getDrivingDirection()).thenReturn(DirectionType.FORTH);
        when(textSignFilterService.hasWindowTime(textSignsA)).thenReturn(true);
        when(textSignFilterService.hasWindowTime(textSignsB)).thenReturn(true);
        when(textSignFilterService.hasWindowTime(textSignsC)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsA)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsB)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsC)).thenReturn(true);

        when(propertiesA.getFraction()).thenReturn(0.3);
        when(propertiesB.getFraction()).thenReturn(0.6);
        when(propertiesC.getFraction()).thenReturn(0.9);

        List<TrafficSignGeoJsonDto> result =
                trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(List.of(trafficSignA,
                        trafficSignB, trafficSignC), true);

        assertThat(result).isEqualTo(List.of(trafficSignA, trafficSignB, trafficSignC));
    }

    @Test
    void findWindowTimeTrafficSignsOrderInDrivingDirection_ok_allMatchShouldBeInCorrectBackwardsOrder() {
        when(trafficSignA.getProperties()).thenReturn(propertiesA);
        when(trafficSignB.getProperties()).thenReturn(propertiesB);
        when(trafficSignC.getProperties()).thenReturn(propertiesC);
        when(propertiesA.getTextSigns()).thenReturn(textSignsA);
        when(propertiesB.getTextSigns()).thenReturn(textSignsB);
        when(propertiesC.getTextSigns()).thenReturn(textSignsC);
        when(propertiesA.getDrivingDirection()).thenReturn(DirectionType.BACK);
        when(propertiesB.getDrivingDirection()).thenReturn(DirectionType.BACK);
        when(propertiesC.getDrivingDirection()).thenReturn(DirectionType.BACK);
        when(textSignFilterService.hasWindowTime(textSignsA)).thenReturn(true);
        when(textSignFilterService.hasWindowTime(textSignsB)).thenReturn(true);
        when(textSignFilterService.hasWindowTime(textSignsC)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsA)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsB)).thenReturn(true);
        when(textSignFilterService.hasNoExcludingOrPreAnnouncement(textSignsC)).thenReturn(true);

        when(propertiesA.getFraction()).thenReturn(0.3);
        when(propertiesB.getFraction()).thenReturn(0.6);
        when(propertiesC.getFraction()).thenReturn(0.9);

        List<TrafficSignGeoJsonDto> result =
                trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(List.of(trafficSignA,
                        trafficSignB, trafficSignC), false);

        assertThat(result).isEqualTo(List.of(trafficSignC, trafficSignB, trafficSignA));
    }

}