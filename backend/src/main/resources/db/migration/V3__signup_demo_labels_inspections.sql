-- Email is used as the login name for self-registered accounts.
ALTER TABLE users ALTER COLUMN username TYPE VARCHAR(100);

INSERT INTO roles (name, description)
VALUES ('USER', 'Self-registered account. Dashboard only until an admin assigns a role.');

-- Everything loaded by V2 is demonstration data, so label it.
ALTER TABLE material_lots ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE production_batches ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE serial_numbers ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE scan_events ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE shipments ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE vehicle_links ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE quality_holds ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE trace_runs ADD COLUMN demo BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE material_lots SET demo = TRUE;
UPDATE production_batches SET demo = TRUE;
UPDATE serial_numbers SET demo = TRUE;
UPDATE scan_events SET demo = TRUE;
UPDATE shipments SET demo = TRUE;
UPDATE vehicle_links SET demo = TRUE;
UPDATE quality_holds SET demo = TRUE;
UPDATE trace_runs SET demo = TRUE;

-- Generated V2 serials were given a part that did not match their batch.
UPDATE serial_numbers s
SET part_id = b.part_id,
    serial = p.part_number || '-1' || RIGHT(s.serial, 4)
FROM production_batches b
JOIN parts p ON p.id = b.part_id
WHERE s.batch_id = b.id AND s.id > 3;

-- B-2026-007 has not shipped, so it cannot be fitted to a vehicle yet.
DELETE FROM vehicle_links WHERE batch_id = 7;
UPDATE vehicle_links SET serial_number_id = 1 WHERE vin = 'MA3FJEB1S00123456';

SELECT setval('serial_numbers_id_seq', (SELECT MAX(id) FROM serial_numbers));
SELECT setval('shipment_items_id_seq', (SELECT MAX(id) FROM shipment_items));

CREATE TABLE quality_inspections (
    id           BIGSERIAL PRIMARY KEY,
    lot_id       BIGINT NOT NULL REFERENCES material_lots(id),
    inspector_id BIGINT NOT NULL REFERENCES users(id),
    result       VARCHAR(10) NOT NULL CHECK (result IN ('PASS', 'FAIL')),
    notes        TEXT,
    inspected_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_quality_inspections_lot ON quality_inspections(lot_id);

-- The machines shown in the 3D plant views are real records.
INSERT INTO machines (code, name, station, location) VALUES
    ('RA-001', 'Robotic Arm', 'Station-F', 'Assembly'),
    ('CNC-002', 'CNC Machine', 'Station-B', 'Machining'),
    ('CS-003', 'Conveyor System', 'Station-G', 'Material flow'),
    ('WR-004', 'Welding Robot', 'Station-H', 'Welding'),
    ('AU-005', 'Assembly Unit', 'Station-D', 'Assembly');
