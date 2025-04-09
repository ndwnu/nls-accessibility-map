package nu.ndw.nls.accessibilitymap.accessibility.time;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class ClockService {

    private final Clock clock;

    public OffsetDateTime now() {

        return OffsetDateTime.now(clock).atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }
}
