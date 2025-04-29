package nu.ndw.nls.accessibilitymap.backend.yaml.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Data {

    @Length(min = 5)
    private String value;
}
