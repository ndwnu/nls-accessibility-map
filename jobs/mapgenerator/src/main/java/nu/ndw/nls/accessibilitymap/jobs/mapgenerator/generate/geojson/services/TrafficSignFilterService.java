package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiDrivingDirection;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignFilterService {

    private final TextSignFilterService textSignFilterService;

    public List<TrafficSignGeoJsonDto> findWindowTimeTrafficSignsOrderInDrivingDirection(
            List<TrafficSignGeoJsonDto> trafficSignGeoJsonDtos, Direction direction) {

        return trafficSignGeoJsonDtos
                .stream()
                .filter(trafficSignGeoJsonDto -> isInSameDrivingDirection(  trafficSignGeoJsonDto, direction))
                .filter(this::hasWindowTimeTextSign)
                .filter(this::hasNoExcludingOrPreAnnouncementTextSign)
                .sorted(sortByFraction(direction))
                .toList();
    }

    private boolean hasWindowTimeTextSign(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        return textSignFilterService.hasWindowTime(trafficSignGeoJsonDto.getProperties().getTextSigns());
    }

    private boolean hasNoExcludingOrPreAnnouncementTextSign(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        return textSignFilterService.hasNoExcludingOrPreAnnouncement(trafficSignGeoJsonDto.getProperties()
                .getTextSigns());
    }

    private boolean isInSameDrivingDirection(TrafficSignGeoJsonDto trafficSignGeoJsonDto, Direction direction) {
        return TrafficSignApiDrivingDirection.from(trafficSignGeoJsonDto.getProperties().getDrivingDirection())
                .isForward() == direction.isForward();
    }

    private Comparator<TrafficSignGeoJsonDto> sortByFraction(Direction direction) {
        Comparator<TrafficSignGeoJsonDto> sortByFractionAsc = Comparator.comparing(
                (TrafficSignGeoJsonDto trafficSignGeoJsonDto) -> trafficSignGeoJsonDto.getProperties().getFraction());

        if (direction == Direction.BACKWARD) {
            return sortByFractionAsc.reversed();
        }

        return sortByFractionAsc;
    }

}
