# Stage 0 — AWS Infrastructure Planning: SME Interview Notes

**Artifact:** `openspec/changes/wa-doh-sql-backend/tasks.md` — Stage 0 tasks (0.1–0.6)
**SME:** Keith Boone, architect of IZ Gateway and `izgw-transform`
**Purpose:** Ground-truth the AWS infrastructure requirements before any code is written
**Date:** 2026-06-18

---

## Cross-Cutting Notes

**AWS environment reference document created:** `C:\Users\boonek\eclipse-workspace\izg-aws-environment.md` — derived from live CLI inspection 2026-06-18. Reusable across all IZ Gateway projects.

**Key infrastructure facts from CLI inspection:**
- Account: 357442695278, region: us-east-1, primary VPC: vpc-03bd3ca016c59ca45
- Transform cluster: `xform-service-alb-dev`; hub cluster: `izgateway-dev-izgateway-services`
- Both services share task/execution role `izgateway-dev-izgateway-service`
- Both services share EFS `fs-0c76fe796cfc1d1e8` (`izgateway-DevandTest-efs`) via separate access points
- Transform EFS access point: `fsap-00296663339c03fd0` → `/dev/xform-service-alb`, mounted at `/configuration`
- ECR repo for transform: `357442695278.dkr.ecr.us-east-1.amazonaws.com/transformation-service`
- Each service gets its own ALB — no shared ALB with listener rules
- DNS pattern: `<env>.<service>.izgateway.org`; transform dev = `dev.xform.izgateway.org`
- No port 1433 in any existing security group; no RDS permissions on shared IAM role

---

## Task 0.1 — ECS Cluster and Service Definition

**Artifact claim:** Identify target ECS cluster and service definition for the SQL-enabled `izgw-transform` image; confirm it is distinct from the standard APHL image service.

### Open questions

- **Q0.0** *(framing)* Briefly describe the overall AWS infrastructure for IZ Gateway — how many clusters exist, which services run where, and how `izgw-transform` fits in. This primes all subsequent questions.
- **Q0.1.1** What ECS cluster should the SQL-enabled `izgw-transform` image be deployed to?
- **Q0.1.2** Is the SQL-enabled service a new ECS service definition, or a variant of the existing `izgw-transform` service definition?
- **Q0.1.3** How is it kept distinct from the APHL-deployed image — separate service, separate cluster, separate task definition, or some combination?

### Notes

**Q0.1.1:** Same cluster as the existing transform service — `xform-service-alb-dev` — deployed as a second service in that cluster.

**Q0.1.2:** Almost certainly a new service definition (new task definition family). Will likely start from the existing `xform-service-alb-dev` task definition as a base but will depart with new environment variables supporting SQL configuration.

**Q0.1.3:** The SQL-enabled image is a fatter uberjar built with the `sql-support` Maven profile (contains additional JARs not in the standard build). It will load from ECR with a different image tag or repository than the standard image. The standard `transformation-service` ECR image continues to be built without SQL profile — its Maven build does not change. The SQL-enabled service will have its own new task definition. The EFS (`fs-0c76fe796cfc1d1e8`) is the SAME EFS currently used by `izgw-transform` — SQL mapping/configuration files will be placed there. A new EFS access point will be needed for the SQL-enabled service's configuration path.

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.1 says "confirm distinct from APHL image service" — APHL deploys to their own environments; our dev cluster is self-managed. No APHL separation needed in dev. | task 0.1 |
| New | SQL-enabled image is a fatter uberjar; standard image Maven build must remain unchanged — default profile produces the APHL image | design Decision 10, tasks Stage 1 |
| New | New EFS access point needed on existing EFS for SQL service configuration path | task 0.1 addition |

### Open follow-up questions

- What should the EFS access point path be for the SQL-enabled service? (follow pattern: `/dev/xform-service-sql` or `/dev/xform-service-alb-sql`?)

---

## Task 0.2 — ALB Listener Rules and DNS Hostname

**Artifact claim:** Determine ALB listener rules and DNS hostname for the SQL-enabled deployment endpoint.

### Open questions

- **Q0.2.1** Does `izgw-transform` sit behind an existing ALB? If so, would the SQL-enabled service share that ALB with a new listener rule, or get its own?
- **Q0.2.2** What is the DNS naming convention for IZ Gateway services — is there a pattern we should follow for the SQL-enabled endpoint hostname?
- **Q0.2.3** Are there any existing listener rule patterns (path-based vs. host-based routing) we should follow?

### Notes

**Q0.2.1:** Share the existing `xform-service-alb-dev` ALB. Use host-header-based listener rules to route traffic by DNS name. The existing listener forwards to `xform-service-alb-dev-alb-tg` as its default action; the SQL-enabled service gets a new target group and a host-header rule with higher priority.

**Q0.2.2:** DNS hostname: `dev.sql-xform.izgateway.org`. Follows existing pattern `<env>.<service>.izgateway.org`.

**Q0.2.3:** Same WAF (`xform-service-alb-dev-waf` already associated with this ALB), same mTLS (`verify` mode, trust store `izgateway-dev-truststore`), same FIPS SSL policy (`ELBSecurityPolicy-TLS13-1-2-Res-FIPS-2023-04`). New ACM certificate needed for `dev.sql-xform.izgateway.org` — request via AWS Certificate Manager (Amazon-issued, same as existing cert). Add as an additional certificate on the existing listener (SNI); ALB supports multiple certs per listener.

**From CLI inspection:**
- Current cert covers `dev.xform.izgateway.org` only (no SANs) — cannot cover the new hostname
- mTLS mode: `verify` with trust store `arn:aws:elasticloadbalancing:us-east-1:357442695278:truststore/izgateway-dev-truststore/5656cac6e1c79940`
- WAF: `xform-service-alb-dev-waf` already associated with the ALB — no change needed

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.2 implies a new ALB — actually reusing existing `xform-service-alb-dev` with host-header rule | task 0.2 |
| New | New ACM certificate required for `dev.sql-xform.izgateway.org` | task 0.2 addition |
| New | New ALB target group required pointing to the SQL-enabled ECS service | task 0.2 addition |

### Open follow-up questions

- Route 53: who creates the DNS record for `dev.sql-xform.izgateway.org` → ALB? (same team; flag for task 0.2)

---

## Task 0.3 — IAM Role for RDS Access

**Artifact claim:** Define IAM role additions required for ECS tasks to access RDS (policy for `rds-db:connect` on the test instance ARN).

### Open questions

- **Q0.3.1** Would RDS permissions be added to the existing shared role, or a new task role for the SQL-enabled service?
- **Q0.3.2** IAM database auth or username/password via Secrets Manager?

### Notes

**Q0.3.1:** Add RDS permissions to the existing shared role `izgateway-dev-izgateway-service`. No new task role.

**Q0.3.2:** Prefer IAM database authentication with the task role. Username/password as fallback only if absolutely necessary.

**Constraint identified:** IAM database authentication (`rds-db:connect`) is NOT supported for SQL Server on AWS RDS. AWS supports IAM auth only for MySQL and PostgreSQL. SQL Server on RDS uses SQL Server Authentication (username/password) or Windows/AD auth only.

**Resolution:**
- For dev/test RDS SQL Server: store credentials in Secrets Manager; Spring Boot retrieves at startup via AWS Secrets Manager property source. No additional IAM policy needed — `SecretsManagerReadWrite` is already on the shared role.
- Add `rds-db:connect` to the role anyway for future-proofing (any non-SQL-Server backend added later will benefit).
- For WA DOH's Azure SQL Server: Azure AD authentication is theoretically possible but depends on WA DOH's configuration; username/password from their secrets store is the practical approach for v1.

**IAM additions to `izgateway-dev-izgateway-service`:**
- Custom policy granting `rds-db:connect` on the dev RDS instance ARN (future-proofing)
- No Secrets Manager addition needed — `SecretsManagerReadWrite` already attached

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.3 specifies `rds-db:connect` as the key IAM permission — this does not apply to SQL Server on RDS | task 0.3 |
| Correction | Credential access for SQL Server is via Secrets Manager (already available on shared role) | task 0.3 |
| New | Add `rds-db:connect` anyway for future non-SQL-Server backend support | task 0.3 addition |

### Open follow-up questions

- Confirm whether WA DOH wants Azure AD auth or username/password for their Azure SQL Server in production (flag for WA DOH deployment doc)

---

## Task 0.4 — Security Group Rules

**Artifact claim:** Define security group rules: allow SQL Server port 1433 from the ECS task security group to the RDS security group.

### Open questions

- **Q0.4.1** Default outbound or explicit outbound rule for port 1433 from ECS SG?
- **Q0.4.2** Should SQL-enabled ECS service reuse existing SG or get its own for precise RDS inbound scoping?
- **Q0.4.3** Cross-cloud connectivity to WA DOH's Azure SQL Server?

### Notes

**Q0.4.1:** Fargate default outbound (allow all) is sufficient. No explicit outbound rule needed on the ECS security group. However, the need for outbound port 1433 access must be documented in the AWS configuration guide for customer deployments — customers may have more restrictive egress policies.

**Q0.4.2:** The RDS instance gets its own dedicated security group (not shared). The RDS security group has a single inbound rule: allow TCP 1433 from the ECS task security group (`sg-0673ad74bd304fe8f`). This gives precise network-level isolation — only the transform ECS tasks can reach this RDS instance.

**Q0.4.3:** WA DOH is a completely separate deployment in their own environment. There is no connectivity requirement from our dev AWS environment to WA DOH's Azure SQL Server. Their deployment is self-contained. No VPN or cross-cloud connectivity to configure on our side.

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.4 framing implied cross-cloud connectivity concern — WA DOH is their own deployment, no cross-cloud needed | task 0.4 |
| New | New dedicated RDS security group needed (not shared); single inbound rule: TCP 1433 from `sg-0673ad74bd304fe8f` | task 0.4 |
| New | Customer deployment docs must note that outbound port 1433 from ECS tasks must not be blocked | task 0.6 / docs |

### Open follow-up questions

---

## Task 0.5 — Secrets Manager / Parameter Store Paths

**Artifact claim:** Identify AWS Secrets Manager / Parameter Store paths for DB credentials (`spring.datasource.url`, username, password); confirm ECS task role can read them.

### Open questions

- **Q0.5.1** Secret structure and naming?
- **Q0.5.2** Existing naming convention — investigate names (not values)
- **Q0.5.3** Spring Boot injection mechanism?

### Notes

**Q0.5.1:** `spring.datasource.url` is NOT a secret — it goes in the SQL endpoint configuration file on EFS. Username and password go into a single Secrets Manager secret as key/value pairs with keys `"username"` and `"password"`. The secret NAME is a parameter in the EFS-mounted SQL config file. Proposed secret name: `TRANSFORM_SQL_RDS_CREDENTIALS` (pending naming convention confirmation from Q0.5.2).

**Q0.5.2:** Naming conventions confirmed from `aws secretsmanager list-secrets`:
- Database credentials follow `izgateway-<env>-db-<role>` (e.g., `izgateway-dev-db-mastercredentials`, `izgateway-dev-db-applicationuser`)
- All-caps used for single-value GitHub Actions secrets only (`COMMON_PASS`, `NVDAPIKEY`, etc.)
- Path-style used for scoped multi-value secrets (`dev/okta/Postman`, `aphl/ecr-credentials`)
- **Chosen name: `xform-dev-sql-credentials`** — follows database credential convention, scoped to the transform service and dev environment

**Q0.5.3:** No static `spring.datasource.username/password` in environment or `application.yml`. Instead: `SqlBackendProperties` has a `credentialsSecret` field; `SqlBackendAutoConfiguration` at startup retrieves the named secret from Secrets Manager via AWS SDK, extracts `"username"` and `"password"` key/value pairs, and programmatically constructs the `DataSource`. Credentials never appear in the task definition or environment variables. No Spring Cloud AWS dependency needed — AWS SDK available via existing task role.

**Role check:** `SecretsManagerReadWrite` already attached to `izgateway-dev-izgateway-service`. No additional IAM policy needed.

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.5 lists `spring.datasource.url` as a secret — it is not; it belongs in the EFS config file | task 0.5 |
| New | Single Secrets Manager secret holds both username and password as key/value pairs | task 0.5 |
| New | Secret name in EFS config file, not in task definition — credentials never in env vars | design, task 0.5 |
| New | `SqlBackendAutoConfiguration` retrieves and injects credentials programmatically at startup | implementation |

### Open follow-up questions

- ⚠️ Re-run `aws secretsmanager list-secrets` after session refresh to confirm naming convention before finalising secret name

---

## Task 0.6 — Documentation for WA State DOH

**Artifact claim:** Document all of the above in `docs/aws-sql-deployment.md`; primary reference for WA State DOH when deploying the SQL-enabled image to their environments.

### Open questions

- **Q0.6.1** Documentation format?
- **Q0.6.2** AWS-only or Azure too?
- **Q0.6.3** Anything missing for WA DOH?

### Notes

**Q0.6.1:** GFM Markdown files delivered in chunks — same style as agent-skillz documentation. Can be converted to PDF or Word for formal delivery later if needed. Developers write MD; that is the source of truth. This convention should be noted in the developer conventions section of the new `izgw-transform-sql` repo.

**Q0.6.2:** Document AWS deployment first. Azure SQL Server details will come at a later stage — likely a new CR. The `docs/aws-sql-deployment.md` covers IZ Gateway's AWS dev/test RDS deployment, not WA DOH's Azure environment.

**Q0.6.3:** WA DOH-specific deployment details are out of scope now and will be refined in a future CR. Not our issue to resolve in this change.

### Corrections / new items identified

| Type | Item | Applies to |
|------|------|-----------|
| Correction | Task 0.6 names the doc audience as WA DOH — doc is AWS-first; Azure details deferred to new CR | task 0.6 |
| New | Developer convention for `izgw-transform-sql` repo: documentation delivered as GFM MD chunks, convertible to PDF/Word | new repo setup (Stage 1 task 1.1) |

### Open follow-up questions

- Azure SQL Server deployment documentation → new CR after pilot

---

## Summary Corrections Table

*(populated after interview)*

## Summary New Items Table

*(populated after interview)*

## Methodology Note

Six-task interview, one section per Stage 0 task. Q0.0 (framing) asked at the
start of Task 0.1 to establish overall AWS infrastructure context before drilling
into specifics.
