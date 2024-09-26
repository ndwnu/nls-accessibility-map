package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.springframework.stereotype.Service;

@Service("TextSignFilterServiceV2")
@RequiredArgsConstructor
public class TextSignFilterService {

    public Optional<TextSignDto> findFirstWindowTimeTextSign(List<TextSignDto> textSignDtos) {
        return textSignDtos
                .stream()
                .filter(this::isTimePeriodTextSign)
                .findFirst();
    }

    public boolean hasWindowTime(List<TextSignDto> textSignDtos) {
        return textSignDtos
                .stream()
                .anyMatch(this::isTimePeriodTextSign);
    }

    public boolean hasNoExcludingOrPreAnnouncement(List<TextSignDto> textSignDtos) {
        return textSignDtos
                .stream()
                .noneMatch(this::isExcludingOrPreAnnouncementTextSign);
    }

    private boolean isTimePeriodTextSign(TextSignDto textSignDto) {
        return  textSignDto.getType() == TextSignType.TIME_PERIOD;
    }

    private boolean isExcludingOrPreAnnouncementTextSign(TextSignDto textSignDto) {
        return  textSignDto.getType() == TextSignType.EXCLUDING ||
                textSignDto.getType() == TextSignType.PRE_ANNOUNCEMENT;
    }

}
