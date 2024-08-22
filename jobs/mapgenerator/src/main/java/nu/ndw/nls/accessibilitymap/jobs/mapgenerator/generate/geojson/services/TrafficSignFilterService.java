package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiDrivingDirection;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignFilterService {

    private final TextSignFilterService textSignFilterService;

    public List<TrafficSignGeoJsonDto> findWindowTimeTrafficSignsOrderInDrivingDirection(
            List<TrafficSignGeoJsonDto> trafficSignGeoJsonDtos, boolean drivingDirectionForward) {

        return trafficSignGeoJsonDtos
                .stream()
                .filter(trafficSignGeoJsonDto -> isInSameDrivingDirection(  trafficSignGeoJsonDto,
                                                                            drivingDirectionForward))
                .filter(this::hasWindowTimeTextSign)
                .filter(this::hasNoExcludingOrPreAnnouncementTextSign)
                .sorted(sortByFraction(drivingDirectionForward))
                .toList();
    }

    private boolean hasWindowTimeTextSign(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        return textSignFilterService.hasWindowTime(trafficSignGeoJsonDto.getProperties().getTextSigns());
    }

    private boolean hasNoExcludingOrPreAnnouncementTextSign(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        return textSignFilterService.hasNoExcludingOrPreAnnouncement(trafficSignGeoJsonDto.getProperties()
                .getTextSigns());
    }

    private boolean isInSameDrivingDirection(TrafficSignGeoJsonDto trafficSignGeoJsonDto, boolean forward) {
        return TrafficSignApiDrivingDirection.from(trafficSignGeoJsonDto.getProperties().getDrivingDirection())
                .isForward() == forward;
    }

    private Comparator<TrafficSignGeoJsonDto> sortByFraction(boolean forward) {
        Comparator<TrafficSignGeoJsonDto> sortByFractionAsc = Comparator.comparing(
                (TrafficSignGeoJsonDto trafficSignGeoJsonDto) -> trafficSignGeoJsonDto.getProperties().getFraction());

        if (!forward) {
            return sortByFractionAsc.reversed();
        }

        return sortByFractionAsc;
    }

}
