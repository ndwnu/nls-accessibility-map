package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database;

import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseDriver implements StateManagement {

    private final DataSource dataSource;

    private final List<CrudRepository<?, ?>> repositories;

    @Override
    public void prepareBeforeEachScenario() {
        StateManagement.super.prepareBeforeEachScenario();
    }

    @Override
    public void clearStateAfterEachScenario() {

        repositories.forEach(CrudRepository::deleteAll);
    }
}
