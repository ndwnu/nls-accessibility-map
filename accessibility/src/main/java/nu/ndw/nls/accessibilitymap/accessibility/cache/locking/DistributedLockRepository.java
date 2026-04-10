package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.time.Instant;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DistributedLockRepository {

    public static final String COLUMN_LOCK_NAME = "lock_name";

    public static final String COLUMN_OWNER_ID = "owner_id";

    public static final String COLUMN_LOCK_EXPIRY = "lock_expiry";

    private final DSLContext dsl;

    private final LockInfo lockInfo;

    private final Table<?> locksTable;

    private final Field<String> lockNameField;

    private final Field<String> ownerIdField;

    private final Field<Instant> lockExpiryField;

    public DistributedLockRepository(DSLContext dsl, LockInfo lockInfo) {
        this.dsl = dsl;
        this.lockInfo = lockInfo;

        var schema = DSL.schema("accessibility_map");
        this.locksTable = DSL.table(DSL.name(schema.getName(), "distributed_locks"));

        this.lockNameField = DSL.field(DSL.name(COLUMN_LOCK_NAME), String.class);
        this.ownerIdField = DSL.field(DSL.name(COLUMN_OWNER_ID), String.class);
        this.lockExpiryField = DSL.field(DSL.name(COLUMN_LOCK_EXPIRY), Instant.class);
    }

    /**
     * Try to acquire the lock: - Insert if not exists - Or "steal" if expired
     */
    @Transactional
    public boolean tryAcquireLock(String lockName, Instant now, Instant newExpiry) {
        String ownerId = lockInfo.getLockOwnerId();

        int rows = dsl.insertInto(locksTable)
                .columns(lockNameField, ownerIdField, lockExpiryField)
                .values(lockName, ownerId, newExpiry)
                .onConflict(lockNameField)
                .doUpdate()
                .set(ownerIdField, ownerId)
                .set(lockExpiryField, newExpiry)
                .where(DSL.field(DSL.name("distributed_locks", "lock_expiry")).lt(now)) // <- explicit table// ONLY steal if expired
                .execute();

        return rows > 0;
    }

    /**
     * Release lock if owned by this instance
     */
    @Transactional
    public int releaseLock(String lockName) {
        return dsl.deleteFrom(locksTable)
                .where(lockNameField.eq(lockName))
                .and(ownerIdField.eq(lockInfo.getLockOwnerId()))
                .execute();
    }

    /**
     * Extend lock expiry (auto-renew)
     */
    @Transactional
    public int extendLock(String lockName, Instant newExpiry) {
        return dsl.update(locksTable)
                .set(lockExpiryField, newExpiry)
                .where(lockNameField.eq(lockName))
                .and(ownerIdField.eq(lockInfo.getLockOwnerId()))
                .execute();
    }

    /**
     * Optional: debugging only
     */
    @Transactional
    public Record findLock(String lockName) {
        return dsl.select()
                .from(locksTable)
                .where(lockNameField.eq(lockName))
                .fetchOne();
    }
}
