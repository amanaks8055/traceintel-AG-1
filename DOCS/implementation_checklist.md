# TraceIntel AG-1 — Implementation Checklist

This checklist tracks the dependency-ordered implementation work. An item is marked complete only after its changes have been implemented and verified.

## Mandatory completion gate for every work package

Before marking any package or checklist item complete:

- [ ] Re-read all directly relevant planning and design documents
- [ ] Compare the implementation against the documented files, behavior, data shapes, and access rules
- [ ] Audit changed files for schema, naming, error handling, security, and UX consistency
- [ ] Run the smallest relevant build, test, migration, or startup verification
- [ ] Record blockers explicitly; never tick a blocked or unverified item
- [ ] Inform the user of the completed work, evidence, mismatches, and remaining gaps

## Project organization standards

- Keep one domain responsibility per production file; split large feature modules as behavior grows.
- Keep API access in `frontend/src/api/` and keep pages focused on presentation and user interaction.
- Do not retain framework starter assets or demo behavior once the real project surface exists.
- Use domain names and explicit error states rather than generic placeholders or silent fallbacks.
- Keep migrations, entities, DTOs, services, controllers, and tests aligned by feature.
- Treat seed/demo data as clearly labeled and never use it to mask unavailable integrations.
- Keep database guidance in `database/` while retaining executable Flyway migrations under the backend classpath.

## Phase 1 — Backend

- [x] **Backend foundation**
  - [x] Create schema-aligned JPA entities
  - [x] Create shared entity base classes
  - [x] Create Spring Data repositories
  - [x] Verify backend compilation
  - [x] Add request and response DTOs
  - [x] Add input validation annotations and validation responses
  - [x] Add custom exceptions and global exception handler
- [x] **Seed/demo data**
  - [x] Add Flyway seed migration
  - [x] Add roles and demo users
  - [x] Add master data: parts, BOM, suppliers, customers, and machines
  - [x] Add production data: lots, batches, consumption, serials, and scans
  - [x] Add shipments, VIN links, and quality-hold scenarios
  - [x] Verify migration data and constraints against PostgreSQL
  - [x] Verify documented seed quantities and demo scenarios
- [x] **JWT and RBAC security**
  - [x] Add password hashing
  - [x] Add login and registration flow
  - [x] Add JWT token creation and validation
  - [x] Add stateless Spring Security filter chain
  - [x] Add role-based endpoint authorization
  - [x] Add CORS configuration
  - [x] Verify database-backed authentication against seeded user
- [x] **Core traceability services**
  - [x] Implement scan validation
  - [x] Implement material-lot linking
  - [x] Implement forward trace
  - [x] Implement backward trace
  - [x] Implement vehicle and loss estimates
  - [x] Implement quality-hold two-person approval
  - [x] Implement audit logging
- [x] **REST API layer**
  - [x] Add authentication endpoints
  - [x] Add dashboard endpoints
  - [x] Add core scan endpoints (validation and material-lot linking)
  - [x] Add core trace endpoints (forward and backward)
  - [x] Add core quality-hold endpoints (request and approval)
  - [x] Add scan event listing and detail endpoints
  - [x] Add trace history endpoint
  - [x] Add quality-hold listing and detail endpoints
  - [x] Add dashboard stats, activity, and recent-scan endpoints
  - [x] Add shipment read endpoints
  - [x] Add VIN import endpoints
  - [x] Add admin CRUD endpoints
  - [x] Add admin parts read endpoint
  - [x] Add QR-code endpoint
  - [x] Add audit-log endpoint
  - [x] Add consistent validation and error responses
- [ ] **Backend test coverage**
  - [x] Add scan service unit tests
  - [x] Add trace service unit tests
  - [x] Add quality-hold unit tests
  - [x] Add estimate tests
  - [ ] Add API integration tests (security and validation are covered; full CRUD tests are not in the repo yet)
  - [x] Add security and RBAC tests
  - [x] Add migration and database constraint tests

## Phase 2 — Frontend

- [x] **Frontend foundation**
  - [x] Initialize React/Vite application
  - [x] Add routing and protected routes
  - [x] Add API client
  - [x] Add authentication state management
  - [x] Add shared layout and navigation
  - [x] Add design tokens and reusable UI components
- [x] **Feature pages**
  - [x] Add login page
  - [x] Add dashboard
  - [x] Add scan station
  - [x] Add forward and backward trace pages
  - [x] Add hold management
  - [x] Add shipments
  - [x] Add admin parts and BOM management
  - [x] Add suppliers, machines, customers, and users management
  - [x] Add VIN import
  - [x] Add audit log

## Phase 3 — Integration and Delivery

- [x] **Integration and demo validation**
  - [x] Verify PostgreSQL setup and Flyway migrations
  - [x] Verify backend startup
  - [x] Verify database-backed login and protected API access
  - [x] Verify frontend startup
  - [x] Run the wrong-part blocked-scan flow
  - [x] Run the quality-hold approval flow
  - [x] Run the forward trace flow
  - [x] Run the backward VIN trace flow
  - [x] Verify role restrictions
  - [x] Fix issues found during end-to-end validation
  - [x] Document final run commands
