package gov.cdc.izgateway.xform.repository.dynamodb.migration;

/**
 * Tracks migration counts.
 *
 * @param total Total number of entities evaluated.
 * @param migrated Number of entities migrated.
 * @param skipped Number of entities skipped.
 */
public record MigrationCounts(int total, int migrated, int skipped) {
}
