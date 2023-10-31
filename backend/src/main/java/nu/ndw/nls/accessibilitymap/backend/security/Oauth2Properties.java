package nu.ndw.nls.accessibilitymap.backend.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Getter
@Setter
@Validated
public class Oauth2Properties {

    @NotBlank
    private String jwtEmailProperty = "email";

    @NotBlank
    public String jwtResourceAccessProperty = "resource_access";

    @Value("${nls.oauth2.jwtResourceProperty}")
    public String jwtResourceProperty;

    @NotBlank
    public String jwtRolesProperty = "roles";
}
