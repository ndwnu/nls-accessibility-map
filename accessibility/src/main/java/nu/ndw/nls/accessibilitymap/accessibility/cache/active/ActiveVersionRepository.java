package nu.ndw.nls.accessibilitymap.accessibility.cache.active;

import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ActiveVersionRepository {

    private static final String SCHEMA_ACCESSIBILITY_MAP = "accessibility_map";

    private static final String TABLE_ACTIVE_VERSION = "active_version";

    private static final String COLUMN_NAME = "name";

    private static final String COLUMN_VERSION = "version";

    private static final String COLUMN_CACHE_VERSION = "cache_version";

    private final DSLContext dsl;

    private final Table<?> activeVersionTable;

    private final Field<String> nameField;

    private final Field<String> versionField;

    private final Field<Integer> cacheVersionField;

    public ActiveVersionRepository(DSLContext dsl) {
        this.dsl = dsl;
        var schema = DSL.schema(SCHEMA_ACCESSIBILITY_MAP);
        this.activeVersionTable = DSL.table(DSL.name(schema.getName(), TABLE_ACTIVE_VERSION));
        this.nameField = DSL.field(DSL.name(COLUMN_NAME), String.class);
        this.versionField = DSL.field(DSL.name(COLUMN_VERSION), String.class);
        this.cacheVersionField = DSL.field(DSL.name(COLUMN_CACHE_VERSION), Integer.class);
    }

    @Transactional
    public Optional<String> findActiveVersion(String cacheName, int cacheVersion) {

        return Optional.ofNullable(dsl.select(versionField).from(activeVersionTable)
                .where(nameField.eq(cacheName).and(cacheVersionField.eq(cacheVersion)))
                .fetchOne(versionField));
    }

    @Transactional
    public void switchActiveVersion(String cacheName, String activeVersion, int cacheVersion) {
        dsl.insertInto(activeVersionTable)
                .columns(nameField, versionField, cacheVersionField)
                .values(cacheName, activeVersion, cacheVersion)
                .onConflict(nameField, cacheVersionField)
                .doUpdate()
                .set(versionField, activeVersion)
                .execute();
    }
}
