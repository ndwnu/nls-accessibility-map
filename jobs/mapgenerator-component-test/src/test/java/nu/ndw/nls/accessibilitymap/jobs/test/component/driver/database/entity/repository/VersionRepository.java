package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.repository;

import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.Version;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;

@Order
public interface VersionRepository extends CrudRepository<Version, Integer> {

}
