package gov.cdc.izgateway.xform.repository.dynamodb.migration;

/**
 * Just here to track migration counts.
 */
public record MigrationCounts(int total, int migrated, int skipped) {
}
