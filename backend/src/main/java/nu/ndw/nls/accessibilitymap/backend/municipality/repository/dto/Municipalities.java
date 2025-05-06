package nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto;

import java.io.Serial;
import java.util.ArrayList;
import org.springframework.validation.annotation.Validated;

@Validated
public class Municipalities extends ArrayList<Municipality> {

    @Serial
    private static final long serialVersionUID = 1L;
}
