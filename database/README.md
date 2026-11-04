# TraceIntel Database

This folder is the human-facing database boundary for TraceIntel AG-1.

## Source of truth

Flyway migrations are kept with the backend because Spring Boot loads them from the classpath:

- `V1__init_schema.sql`: tables, constraints and indexes
- `V2__seed_demo_data.sql`: demo data
- `V3__signup_demo_labels_inspections.sql`: `USER` role, `demo` labels, inspections, seed fixes and machine records
- `V4__kaggle_master_data.sql`: suppliers, parts, customers and VINs from Kaggle
- `V5__remove_demo_transactions.sql`: removes demo lots, batches, scans, shipments and holds, so the app starts empty
- `V6__password_reset.sql`: adds the password reset request and one-time code columns to `users`
- `V7__lot_quantity_required.sql`: makes lot quantity and the quantity used by a batch required

They are in `backend/src/main/resources/db/migration/`. Do not copy them here, and do not edit an applied migration. Add a new one instead.

## Configuration

The connection comes from `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. Never commit a real password.

## Tables

There are 19 tables: `roles`, `users`, `parts`, `bom_items`, `suppliers`, `customers`, `machines`, `material_lots`, `production_batches`, `batch_material_consumption`, `serial_numbers`, `scan_events`, `shipments`, `shipment_items`, `vehicle_links`, `quality_holds`, `quality_inspections`, `trace_runs` and `audit_logs`.

Check applied migrations with:

```sql
SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;
```
