package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@AllArgsConstructor
public class ClockService {

	private Clock clock;

	public OffsetDateTime now() {

		return OffsetDateTime.now(clock).atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
	}
}
