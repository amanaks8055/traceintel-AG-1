-- The app starts empty so lots, batches, scans and shipments are entered live.
-- Roles, accounts and master data stay. VINs with no batch stay for live linking.
DELETE FROM scan_events WHERE demo;
DELETE FROM trace_runs WHERE demo;
DELETE FROM quality_holds WHERE demo;

DELETE FROM shipment_items si
USING shipments s
WHERE si.shipment_id = s.id AND s.demo;
DELETE FROM shipments WHERE demo;

DELETE FROM vehicle_links WHERE demo AND batch_id IS NOT NULL;
DELETE FROM serial_numbers WHERE demo;

DELETE FROM batch_material_consumption bmc
USING production_batches b
WHERE bmc.batch_id = b.id AND b.demo;
DELETE FROM production_batches WHERE demo;
DELETE FROM material_lots WHERE demo;

DELETE FROM audit_logs WHERE id <= 5;
