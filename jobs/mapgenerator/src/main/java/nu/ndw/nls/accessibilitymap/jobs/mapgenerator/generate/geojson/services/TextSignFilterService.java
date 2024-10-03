package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TextSignFilterService {

    public Optional<TextSign> findFirstWindowTimeTextSign(List<TextSign> textSignDtos) {
        return textSignDtos
                .stream()
                .filter(this::isTimePeriodTextSign)
                .findFirst();
    }

    public boolean hasWindowTime(List<TextSign> textSignDtos) {
        return textSignDtos
                .stream()
                .anyMatch(this::isTimePeriodTextSign);
    }

    public boolean hasNoExcludingOrPreAnnouncement(List<TextSign> textSignDtos) {
        return textSignDtos
                .stream()
                .noneMatch(this::isExcludingOrPreAnnouncementTextSign);
    }

    private boolean isTimePeriodTextSign(TextSign textSignDto) {
        return  textSignDto.getType() == TextSignType.TIME_PERIOD;
    }

    private boolean isExcludingOrPreAnnouncementTextSign(TextSign textSignDto) {
        return  textSignDto.getType() == TextSignType.EXCLUDING ||
                textSignDto.getType() == TextSignType.PRE_ANNOUNCEMENT;
    }

}
