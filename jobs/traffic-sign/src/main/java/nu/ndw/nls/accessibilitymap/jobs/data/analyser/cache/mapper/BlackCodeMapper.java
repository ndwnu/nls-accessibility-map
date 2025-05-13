package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BlackCodeMapper {

    public Double map(TrafficSignGeoJsonDto trafficSignGeoJsonDto, TrafficSignType type) {

        String blackCode = trafficSignGeoJsonDto.getProperties().getBlackCode();
        try {
            return Double.parseDouble(blackCode.replace(",", "."));
        } catch (RuntimeException exception) {
            if (!Strings.isEmpty(blackCode)) {
                log.warn("Unprocessable value {} for traffic sign with id {} and RVV code {} on road section {}",
                        blackCode,
                        trafficSignGeoJsonDto.getId(),
                        trafficSignGeoJsonDto.getProperties().getRvvCode(),
                        trafficSignGeoJsonDto.getProperties().getRoadSectionId(),
                        exception);
            }

            if (List.of(C17, C18, C19, C20, C21).contains(type)) {
                throw new IllegalStateException(
                        "Traffic sign with id '%s' is not containing a black code but that is required for type '%s'"
                                .formatted(trafficSignGeoJsonDto.getId(), type),
                        exception);
            }
            return null;
        }
    }
}
