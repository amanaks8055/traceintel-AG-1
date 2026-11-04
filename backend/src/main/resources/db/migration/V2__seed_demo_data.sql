-- TraceIntel AG-1 demo data.
-- All demo users use password: pass123.

INSERT INTO roles (id, name, description) VALUES
    (1, 'ADMIN', 'System administrator'),
    (2, 'OPERATOR', 'Production line operator'),
    (3, 'SUPERVISOR', 'Production supervisor'),
    (4, 'QUALITY_ENGINEER', 'Quality engineer'),
    (5, 'MANAGER', 'Plant manager');

INSERT INTO users (id, username, password_hash, full_name, email, role_id) VALUES
    (1, 'admin', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Amit Verma', 'amit.verma@traceintel.local', 1),
    (2, 'operator1', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Rajesh Kumar', 'rajesh.kumar@traceintel.local', 2),
    (3, 'operator2', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Suresh Yadav', 'suresh.yadav@traceintel.local', 2),
    (4, 'supervisor1', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Deepak Chauhan', 'deepak.chauhan@traceintel.local', 3),
    (5, 'priya', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Priya Sharma', 'priya.sharma@traceintel.local', 4),
    (6, 'vikram', '$2a$10$BaHxLJwOegiVmv6qdcS6PekOW5FYU0jZQKXV/6Upd.MPGoPdAvL6y', 'Vikram Singh', 'vikram.singh@traceintel.local', 5);

INSERT INTO suppliers (id, code, name, contact_email, phone, address) VALUES
    (1, 'SUP-TSL', 'Tata Steel Ltd', 'procurement@tatasteel.local', '+91-22-5550-1001', 'Mumbai, Maharashtra'),
    (2, 'SUP-SAB', 'SABIC Polymers India', 'orders@sabic.local', '+91-80-5550-1002', 'Bengaluru, Karnataka'),
    (3, 'SUP-BEL', 'Bharat Electronics Ltd', 'supply@bel.local', '+91-80-5550-1003', 'Bengaluru, Karnataka'),
    (4, 'SUP-SUN', 'Sundram Fasteners Ltd', 'orders@sundram.local', '+91-44-5550-1004', 'Chennai, Tamil Nadu'),
    (5, 'SUP-HAV', 'Havells India Ltd', 'supply@havells.local', '+91-120-5550-1005', 'Noida, Uttar Pradesh');

INSERT INTO customers (id, code, name, contact_email, phone, address) VALUES
    (1, 'MARUTI', 'Maruti Suzuki India Ltd', 'quality@maruti.local', '+91-124-5550-2001', 'Gurugram, Haryana'),
    (2, 'TATA', 'Tata Motors Ltd', 'quality@tatamotors.local', '+91-20-5550-2002', 'Pune, Maharashtra'),
    (3, 'HYUNDAI', 'Hyundai Motor India Ltd', 'quality@hyundai.local', '+91-44-5550-2003', 'Sriperumbudur, Tamil Nadu'),
    (4, 'MAHINDRA', 'Mahindra & Mahindra Ltd', 'quality@mahindra.local', '+91-22-5550-2004', 'Mumbai, Maharashtra'),
    (5, 'TOYOTA', 'Toyota Kirloskar Motor', 'quality@toyota.local', '+91-80-5550-2005', 'Bidadi, Karnataka'),
    (6, 'KIA', 'Kia India Pvt Ltd', 'quality@kia.local', '+91-855-5550-2006', 'Anantapur, Andhra Pradesh');

INSERT INTO machines (id, code, name, station, location) VALUES
    (1, 'IM-001', 'Injection Molding Machine 1', 'Station-A', 'Molding'),
    (2, 'IM-002', 'Injection Molding Machine 2', 'Station-A', 'Molding'),
    (3, 'CNC-001', 'CNC Lathe Machine', 'Station-B', 'Machining'),
    (4, 'HP-001', 'Hydraulic Press 200T', 'Station-C', 'Pressing'),
    (5, 'ASM-001', 'Assembly Station 1', 'Station-D', 'Assembly'),
    (6, 'QI-001', 'Quality Inspection Station', 'Station-E', 'Inspection');

INSERT INTO parts (id, part_number, name, description, cost_per_part, parts_per_vehicle, scrap_cost, rework_cost) VALUES
    (1, 'MTH-DH-001', 'Door Handle Assembly', 'Exterior door handle assembly', 300, 4, 80, 45),
    (2, 'MTH-WH-002', 'Wiring Harness Module', 'Automotive wiring harness module', 850, 1, 150, 100),
    (3, 'MTH-BG-003', 'Bumper Guard Front', 'Front bumper guard', 450, 1, 120, 75),
    (4, 'MTH-RM-004', 'Rearview Mirror Housing', 'Rearview mirror housing', 620, 2, 140, 90),
    (5, 'MTH-IP-005', 'Instrument Panel Trim', 'Instrument panel trim', 1200, 1, 250, 140),
    (6, 'MTH-SA-006', 'Seat Adjustment Mechanism', 'Seat adjustment mechanism', 780, 4, 180, 110),
    (7, 'MTH-HL-007', 'Headlamp Assembly', 'Headlamp assembly', 950, 2, 220, 130),
    (8, 'MTH-FW-008', 'Fender Wheel Arch', 'Fender wheel arch', 380, 4, 90, 55),
    (9, 'RAW-STL-001', 'Steel Sheet', 'Stamped steel sheet', 120, 1, 30, 20),
    (10, 'RAW-PLY-001', 'ABS Polymer Granules', 'ABS polymer granules', 90, 1, 25, 15),
    (11, 'RAW-SPR-001', 'Spring Mechanism', 'Handle spring mechanism', 35, 1, 10, 8),
    (12, 'RAW-SCR-001', 'Mounting Screws', 'Mounting screw set', 12, 1, 4, 3),
    (13, 'RAW-COP-001', 'Copper Wire Bundle', 'Copper wire bundle', 400, 1, 80, 50),
    (14, 'RAW-CON-001', 'Electrical Connectors', 'Electrical connector set', 180, 1, 40, 25),
    (15, 'RAW-INS-001', 'PVC Insulation Sleeve', 'PVC insulation sleeve', 75, 1, 20, 12),
    (16, 'RAW-RUB-001', 'Rubber Compound', 'Automotive rubber compound', 60, 1, 15, 10);

INSERT INTO bom_items (id, parent_part_id, child_part_id, quantity, level) VALUES
    (1, 1, 9, 2, 1), (2, 1, 10, .5, 1), (3, 1, 11, 1, 1), (4, 1, 12, 6, 1),
    (5, 2, 13, 3, 1), (6, 2, 14, 12, 1), (7, 2, 15, 3, 1),
    (8, 3, 16, 1, 1), (9, 4, 10, .4, 1), (10, 5, 10, 1, 1),
    (11, 6, 16, 1, 1), (12, 8, 16, 1, 1);

INSERT INTO material_lots (id, lot_number, supplier_id, part_id, received_date, expiry_date, status, quantity, unit) VALUES
    (1, 'LOT-STL-2026-001', 1, 9, '2026-10-01', '2027-10-01', 'OK', 1000, 'pcs'),
    (2, 'LOT-STL-2026-005', 1, 9, '2026-10-05', '2027-10-05', 'ON_HOLD', 800, 'pcs'),
    (3, 'LOT-PLY-2026-002', 2, 10, '2026-10-02', '2027-10-02', 'OK', 500, 'kg'),
    (4, 'LOT-COP-2026-001', 5, 13, '2026-10-03', '2027-10-03', 'OK', 600, 'm'),
    (5, 'LOT-CON-2026-001', 3, 14, '2026-10-04', '2027-10-04', 'OK', 1200, 'pcs'),
    (6, 'LOT-SPR-2026-001', 4, 11, '2026-10-05', '2027-10-05', 'OK', 700, 'pcs'),
    (7, 'LOT-SCR-2026-001', 4, 12, '2026-10-05', '2027-10-05', 'OK', 5000, 'pcs'),
    (8, 'LOT-INS-2026-001', 5, 15, '2026-10-06', '2027-10-06', 'OK', 900, 'm'),
    (9, 'LOT-RUB-2026-001', 2, 16, '2026-10-06', '2027-10-06', 'OK', 800, 'kg'),
    (10, 'LOT-STL-2026-006', 1, 9, '2026-10-07', '2027-10-07', 'OK', 700, 'pcs');

INSERT INTO production_batches (id, batch_number, part_id, machine_id, operator_id, shift, start_time, quantity) VALUES
    (1, 'B-2026-001', 1, 5, 2, 'DAY', '2026-10-21 08:00:00', 250),
    (2, 'B-2026-002', 2, 3, 3, 'NIGHT', '2026-10-22 20:00:00', 200),
    (3, 'B-2026-003', 1, 5, 2, 'DAY', '2026-10-23 08:00:00', 500),
    (4, 'B-2026-004', 3, 4, 3, 'DAY', '2026-10-24 08:00:00', 300),
    (5, 'B-2026-005', 4, 2, 2, 'GENERAL', '2026-10-25 09:00:00', 200),
    (6, 'B-2026-006', 5, 5, 3, 'DAY', '2026-10-26 08:00:00', 150),
    (7, 'B-2026-007', 1, 5, 2, 'DAY', '2026-10-27 08:00:00', 300),
    (8, 'B-2026-008', 2, 3, 3, 'NIGHT', '2026-10-27 20:00:00', 180);

INSERT INTO batch_material_consumption (id, batch_id, lot_id, quantity_used) VALUES
    (1, 1, 1, 500), (2, 2, 4, 600), (3, 3, 2, 500), (4, 4, 9, 300),
    (5, 5, 3, 200), (6, 6, 10, 150), (7, 7, 2, 300), (8, 8, 4, 540),
    (9, 1, 6, 250), (10, 3, 7, 300);

INSERT INTO serial_numbers (id, serial, part_id, batch_id, produced_at) VALUES
    (1, 'MTH-DH-001-00042', 1, 3, '2026-10-23 10:00:00'),
    (2, 'MTH-DH-001-00043', 1, 1, '2026-10-21 10:00:00'),
    (3, 'MTH-WH-002-00015', 2, 2, '2026-10-22 22:00:00');

INSERT INTO serial_numbers (id, serial, part_id, batch_id, produced_at)
SELECT 4 + series.i,
       part.part_number || '-' || LPAD((series.i + 1)::text, 5, '0'),
       part.id,
       batch.id,
       batch.start_time + ((series.i % 8) * INTERVAL '15 minutes')
FROM generate_series(0, 46) AS series(i)
JOIN parts part ON part.id = 1 + (series.i % 8)
JOIN production_batches batch ON batch.id = 1 + (series.i % 8);

INSERT INTO shipments (id, shipment_number, customer_id, shipped_date, notes) VALUES
    (1, 'SH-2026-001', 1, '2026-10-26', 'Door handle batch dispatch'),
    (2, 'SH-2026-002', 2, '2026-10-27', 'Wiring harness dispatch'),
    (3, 'SH-2026-003', 3, '2026-10-27', 'Mirror housing dispatch');

INSERT INTO shipment_items (id, shipment_id, batch_id, quantity) VALUES
    (1, 1, 3, 500), (2, 2, 2, 200), (3, 3, 5, 200),
    (4, 1, 1, 250), (5, 2, 8, 180);

INSERT INTO vehicle_links (id, vin, batch_id, customer_id) VALUES
    (1, 'MA3FJEB1S00123456', 3, 1),
    (2, 'MA3FJEB1S00123457', 3, 1),
    (3, 'MA3FJEB1S00123458', 3, 1),
    (4, 'MATNAT51SJT100201', 2, 2),
    (5, 'MATNAT51SJT100202', 2, 2),
    (6, 'MALBB51BLHM200301', 5, 3),
    (7, 'MALBB51BLHM200302', 5, 3),
    (8, 'MA1HA2HS7PH300101', 1, 4),
    (9, 'MA1HA2HS7PH300102', 1, 4),
    (10, 'MA3FJEB1S00123459', 7, 1);

INSERT INTO scan_events (id, serial_number_id, serial_input, batch_id, operator_id, station, result, reason, scanned_at) VALUES
    (1, 2, 'MTH-DH-001-00043', 1, 2, 'Assembly-A1', 'ACCEPTED', NULL, '2026-10-21 10:01:00'),
    (2, 3, 'MTH-WH-002-00015', 1, 2, 'Assembly-A1', 'BLOCKED', 'WRONG_PART', '2026-10-23 10:02:00'),
    (3, 1, 'MTH-DH-001-00042', 3, 2, 'Assembly-A1', 'BLOCKED', 'LOT_ON_HOLD', '2026-10-23 10:03:00');

INSERT INTO scan_events (id, serial_number_id, serial_input, batch_id, operator_id, station, result, reason, scanned_at)
SELECT 4 + i,
       NULL,
       'MTH-DH-001-DEMO-' || LPAD((i + 1)::text, 5, '0'),
       CASE WHEN i % 2 = 0 THEN 1 ELSE 3 END,
       CASE WHEN i % 2 = 0 THEN 2 ELSE 3 END,
       'Assembly-A1',
       CASE WHEN i % 20 = 0 THEN 'BLOCKED' ELSE 'ACCEPTED' END,
       CASE WHEN i % 20 = 0 THEN 'DUPLICATE_SERIAL' ELSE NULL END,
       TIMESTAMP '2026-10-21 08:00:00' + (i / 185) * INTERVAL '1 day' + (i % 185) * INTERVAL '3 minutes'
FROM generate_series(0, 1289) AS series(i);

INSERT INTO quality_holds (id, lot_id, requested_by, request_reason, status, approved_by, approval_notes, requested_at, resolved_at) VALUES
    (1, 2, 5, 'Material dimension variance reported by inspection', 'APPROVED', 6, 'Approved after QE review', '2026-10-23 11:00:00', '2026-10-23 14:00:00');

INSERT INTO trace_runs (id, lot_id, direction, input_value, result_json, duration_ms, executed_by, executed_at) VALUES
    (1, 2, 'FORWARD', 'LOT-STL-2026-005', '{"batches":["B-2026-003","B-2026-007"]}', 47, 6, '2026-10-27 10:00:00');

INSERT INTO audit_logs (id, user_id, action, entity_type, entity_id, details, ip_address) VALUES
    (1, 2, 'SCAN_ACCEPTED', 'SERIAL_NUMBER', 2, 'Valid serial accepted at Assembly-A1', '127.0.0.1'),
    (2, 2, 'SCAN_BLOCKED', 'SERIAL_NUMBER', 3, 'Wrong part blocked', '127.0.0.1'),
    (3, 5, 'HOLD_REQUESTED', 'MATERIAL_LOT', 2, 'Dimension variance reported', '127.0.0.1'),
    (4, 6, 'HOLD_APPROVED', 'QUALITY_HOLD', 1, 'Two-person approval completed', '127.0.0.1'),
    (5, 6, 'TRACE_RUN', 'MATERIAL_LOT', 2, 'Forward trace completed in 47ms', '127.0.0.1');

SELECT setval('roles_id_seq', 5);
SELECT setval('users_id_seq', 6);
SELECT setval('suppliers_id_seq', 5);
SELECT setval('customers_id_seq', 6);
SELECT setval('machines_id_seq', 6);
SELECT setval('parts_id_seq', 16);
SELECT setval('bom_items_id_seq', 12);
SELECT setval('material_lots_id_seq', 10);
SELECT setval('production_batches_id_seq', 8);
SELECT setval('batch_material_consumption_id_seq', 10);
SELECT setval('serial_numbers_id_seq', 3);
SELECT setval('shipments_id_seq', 3);
SELECT setval('shipment_items_id_seq', 3);
SELECT setval('vehicle_links_id_seq', 10);
SELECT setval('scan_events_id_seq', 1293);
SELECT setval('quality_holds_id_seq', 1);
SELECT setval('trace_runs_id_seq', 1);
SELECT setval('audit_logs_id_seq', 5);
