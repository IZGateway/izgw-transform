# izgw-transform — Project Instructions

IZ Gateway Transformation Service backend. Provides transformation pipelines, solutions,
and mappings consumed by `izg-transformation-ui` (Xform Console).

**Skills:** `java-maven-style`, `xml-xslt-patterns`

**Public repo** — follow IZ Gateway Public Repo Policy (in global CLAUDE.md).

---

## Build

```cmd
mvn clean package
mvn test
mvn dependency-check:check
mvn clean site
```

---

## Notes

- Backend API consumed by `izg-transformation-ui` via its Next.js API proxy layer
- Uses mTLS for connections from the console (`XFORM_SERVICE_ENDPOINT_CRT_PATH` etc.)
- Deployed on AWS ECS (Fargate)
- Test class suffix: `Tests`
