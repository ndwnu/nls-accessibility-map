package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import io.cucumber.java.DataTableType;
import jakarta.validation.Valid;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto.Environment;

public class DataTypeRegister {

    @DataTableType
    public @Valid Environment environment(final Map<String, String> entry) {

        return Environment.builder()
                .key(entry.get("key"))
                .value(entry.get("value"))
                .build();
    }

}
