package gov.cdc.izgateway.xform.migration;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents the result of a migration operation.
 */
@Getter
@Builder
public class MigrationResult {
    private final boolean success;
    private final String entityType;
    private final int totalCount;
    private final int migratedCount;
    private final int skippedCount;
    private final String message;
    private final Exception error;
    
    /**
     * Creates a successful migration result.
     */
    public static MigrationResult success(String entityType, int totalCount, int migratedCount, int skippedCount) {
        return MigrationResult.builder()
                .success(true)
                .entityType(entityType)
                .totalCount(totalCount)
                .migratedCount(migratedCount)
                .skippedCount(skippedCount)
                .message(String.format("Successfully migrated %d/%d %s entities (%d skipped)", 
                        migratedCount, totalCount, entityType, skippedCount))
                .build();
    }
    
    /**
     * Creates a failed migration result.
     */
    public static MigrationResult failure(String entityType, Exception error) {
        return MigrationResult.builder()
                .success(false)
                .entityType(entityType)
                .totalCount(0)
                .migratedCount(0)
                .skippedCount(0)
                .message(String.format("Failed to migrate %s entities: %s", entityType, error.getMessage()))
                .error(error)
                .build();
    }
}