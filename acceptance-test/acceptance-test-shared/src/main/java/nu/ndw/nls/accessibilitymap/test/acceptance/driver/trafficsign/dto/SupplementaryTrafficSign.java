package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto;

import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json.SignCodeEnum;

public record SupplementaryTrafficSign(String name, SignCodeEnum signType, String text) {
}
