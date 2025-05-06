package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto;

import java.io.Serial;
import java.util.ArrayList;
import org.springframework.validation.annotation.Validated;

@Validated
public class RoadOperators extends ArrayList<RoadOperator> {

    @Serial
    private static final long serialVersionUID = 1L;

}
