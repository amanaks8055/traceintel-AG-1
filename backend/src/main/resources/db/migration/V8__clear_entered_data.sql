-- Clears the records entered while testing so the app starts empty again.
-- Roles, accounts and master data (parts, BOM, suppliers, customers, machines, VINs) stay.
DELETE FROM scan_events;
DELETE FROM trace_runs;
DELETE FROM quality_holds;
DELETE FROM quality_inspections;

DELETE FROM shipment_items;
DELETE FROM shipments;

UPDATE vehicle_links SET batch_id = NULL, serial_number_id = NULL
WHERE batch_id IS NOT NULL OR serial_number_id IS NOT NULL;

DELETE FROM serial_numbers;
DELETE FROM batch_material_consumption;
DELETE FROM production_batches;
DELETE FROM material_lots;

DELETE FROM audit_logs;
