package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers;

import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import org.springframework.stereotype.Component;

@Component
public class TextSignDtoV5JsonMapper {

    public TextSignDtoV5Json map(SupplementaryTrafficSign supplementaryTrafficSign) {
        if (supplementaryTrafficSign == null) {
            return null;
        }

        return TextSignDtoV5Json.builder()
                .text(supplementaryTrafficSign.text())
                .signCode(supplementaryTrafficSign.signType())
                .build();
    }
}
