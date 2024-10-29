package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data;

import io.cucumber.java.DataTableType;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;

public class DataTypeRegister {

    @DataTableType
    public @Valid TrafficSign mapTrafficSign(final Map<String, String> entry) {

        return TrafficSign.builder()
                .startNodeId(Integer.parseInt(entry.get("startNodeId")))
                .endNodeId(Integer.parseInt(entry.get("endNodeId")))
                .fraction(Double.parseDouble(entry.get("fraction")))
                .rvvCode(entry.get("rvvCode").toUpperCase(Locale.US))
                .directionType(DirectionType.valueOf(entry.get("directionType").toUpperCase(Locale.US)))
                .windowTime(entry.get("windowTime"))
                .build();
    }

}
