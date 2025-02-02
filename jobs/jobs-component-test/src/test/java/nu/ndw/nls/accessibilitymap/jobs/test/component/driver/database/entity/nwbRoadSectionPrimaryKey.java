package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class nwbRoadSectionPrimaryKey implements Serializable {

    private int versionId;

    private long roadSectionId;
}
