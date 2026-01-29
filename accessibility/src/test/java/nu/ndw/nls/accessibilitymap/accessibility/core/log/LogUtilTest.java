package nu.ndw.nls.accessibilitymap.accessibility.core.log;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import net.logstash.logback.argument.StructuredArgument;
import org.junit.jupiter.api.Test;

class LogUtilTest {

    @Test
    void keyValueJson() {

        OffsetDateTime timestamp = OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        StructuredArgument keyValue = LogUtil.keyValueJson("key", new CustomObject("name", timestamp));

        assertThat(keyValue).hasToString("key={\"name\":\"name\",\"timestamp\":\"%s\"}"
                .formatted(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timestamp)));
    }

    private record CustomObject(
            String name,
            OffsetDateTime timestamp
    ) {

    }
}
