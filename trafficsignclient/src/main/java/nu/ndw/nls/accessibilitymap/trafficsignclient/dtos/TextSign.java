package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import lombok.Builder;

@Builder
public record TextSign(
        TextSignType type,
        String text) {

    public boolean hasWindowTime() {
        return type() == TextSignType.TIME_PERIOD;
    }
}
