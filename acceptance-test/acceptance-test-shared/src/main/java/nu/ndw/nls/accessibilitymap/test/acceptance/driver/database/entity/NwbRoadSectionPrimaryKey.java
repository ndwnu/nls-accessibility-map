package nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity;

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class NwbRoadSectionPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int versionId;

    private long roadSectionId;
}
