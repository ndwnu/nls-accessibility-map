package nu.ndw.nls.accessibilitymap.accessibility.municipality.mappers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;


@Component
public class MunicipalityIdMapper {
    private static final Pattern PATTERN = Pattern.compile(".{2}0*(\\d+)$");
    public int map(String municipalityId) {
        Matcher m = PATTERN.matcher(municipalityId);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalStateException("Incorrect municipalityId " + municipalityId);
        }
    }
}
