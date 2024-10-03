package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TextSign {

    private TextSignType type;
    private String text;
    private String openingHours;

    public boolean hasWindowTime() {
        return  getType() == TextSignType.TIME_PERIOD;
    }

    public boolean hasNoExcludingOrPreAnnouncement() {
        return  getType() == TextSignType.EXCLUDING ||
                getType() == TextSignType.PRE_ANNOUNCEMENT;
    }
}
