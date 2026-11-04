-- ═══════════════════════════════════════════════════════════════
-- TraceIntel AG-1 — Database Schema
-- Flyway Migration V1: Full normalized schema
-- ═══════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════
-- CORE TABLES
-- ═══════════════════════════════════════

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(30) NOT NULL UNIQUE,
    description     VARCHAR(255),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE,
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    contact_email   VARCHAR(100),
    phone           VARCHAR(30),
    address         TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    contact_email   VARCHAR(100),
    phone           VARCHAR(30),
    address         TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE machines (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    station         VARCHAR(50),
    location        VARCHAR(100),
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE parts (
    id              BIGSERIAL PRIMARY KEY,
    part_number     VARCHAR(50) NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    cost_per_part   NUMERIC(12,2) DEFAULT 0,
    parts_per_vehicle INTEGER DEFAULT 1,
    scrap_cost      NUMERIC(12,2) DEFAULT 0,
    rework_cost     NUMERIC(12,2) DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════
-- BOM (Bill of Materials) — supports multi-level
-- ═══════════════════════════════════════

CREATE TABLE bom_items (
    id              BIGSERIAL PRIMARY KEY,
    parent_part_id  BIGINT NOT NULL REFERENCES parts(id),
    child_part_id   BIGINT NOT NULL REFERENCES parts(id),
    quantity        NUMERIC(10,3) NOT NULL DEFAULT 1,
    level           INTEGER NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(parent_part_id, child_part_id)
);

-- ═══════════════════════════════════════
-- PRODUCTION TABLES
-- ═══════════════════════════════════════

CREATE TABLE material_lots (
    id              BIGSERIAL PRIMARY KEY,
    lot_number      VARCHAR(50) NOT NULL UNIQUE,
    supplier_id     BIGINT NOT NULL REFERENCES suppliers(id),
    part_id         BIGINT REFERENCES parts(id),
    received_date   DATE NOT NULL,
    expiry_date     DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'OK',
    quantity        NUMERIC(12,2),
    unit            VARCHAR(20) DEFAULT 'pcs',
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE production_batches (
    id              BIGSERIAL PRIMARY KEY,
    batch_number    VARCHAR(50) NOT NULL UNIQUE,
    part_id         BIGINT NOT NULL REFERENCES parts(id),
    machine_id      BIGINT NOT NULL REFERENCES machines(id),
    operator_id     BIGINT NOT NULL REFERENCES users(id),
    shift           VARCHAR(20) NOT NULL DEFAULT 'DAY',
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    quantity        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE batch_material_consumption (
    id              BIGSERIAL PRIMARY KEY,
    batch_id        BIGINT NOT NULL REFERENCES production_batches(id),
    lot_id          BIGINT NOT NULL REFERENCES material_lots(id),
    quantity_used   NUMERIC(12,2),
    recorded_at     TIMESTAMP DEFAULT NOW(),
    UNIQUE(batch_id, lot_id)
);

CREATE TABLE serial_numbers (
    id              BIGSERIAL PRIMARY KEY,
    serial          VARCHAR(100) NOT NULL UNIQUE,
    part_id         BIGINT NOT NULL REFERENCES parts(id),
    batch_id        BIGINT NOT NULL REFERENCES production_batches(id),
    produced_at     TIMESTAMP DEFAULT NOW(),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE scan_events (
    id              BIGSERIAL PRIMARY KEY,
    serial_number_id BIGINT REFERENCES serial_numbers(id),
    serial_input    VARCHAR(100) NOT NULL,
    batch_id        BIGINT REFERENCES production_batches(id),
    operator_id     BIGINT NOT NULL REFERENCES users(id),
    station         VARCHAR(50),
    result          VARCHAR(20) NOT NULL,
    reason          TEXT,
    manual_entry    BOOLEAN DEFAULT FALSE,
    approved_by     BIGINT REFERENCES users(id),
    scanned_at      TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════
-- SHIPMENT TABLES
-- ═══════════════════════════════════════

CREATE TABLE shipments (
    id              BIGSERIAL PRIMARY KEY,
    shipment_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    shipped_date    DATE NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shipment_items (
    id              BIGSERIAL PRIMARY KEY,
    shipment_id     BIGINT NOT NULL REFERENCES shipments(id),
    batch_id        BIGINT NOT NULL REFERENCES production_batches(id),
    quantity        INTEGER NOT NULL DEFAULT 0,
    UNIQUE(shipment_id, batch_id)
);

-- ═══════════════════════════════════════
-- VIN LINK TABLE
-- ═══════════════════════════════════════

CREATE TABLE vehicle_links (
    id              BIGSERIAL PRIMARY KEY,
    vin             VARCHAR(17) NOT NULL,
    batch_id        BIGINT REFERENCES production_batches(id),
    serial_number_id BIGINT REFERENCES serial_numbers(id),
    customer_id     BIGINT REFERENCES customers(id),
    imported_at     TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════
-- QUALITY HOLD (Two-Person Approval)
-- ═══════════════════════════════════════

CREATE TABLE quality_holds (
    id              BIGSERIAL PRIMARY KEY,
    lot_id          BIGINT NOT NULL REFERENCES material_lots(id),
    requested_by    BIGINT NOT NULL REFERENCES users(id),
    request_reason  TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    approved_by     BIGINT REFERENCES users(id),
    approval_notes  TEXT,
    requested_at    TIMESTAMP DEFAULT NOW(),
    resolved_at     TIMESTAMP
);

-- ═══════════════════════════════════════
-- TRACE RUN LOG
-- ═══════════════════════════════════════

CREATE TABLE trace_runs (
    id              BIGSERIAL PRIMARY KEY,
    lot_id          BIGINT REFERENCES material_lots(id),
    direction       VARCHAR(10) NOT NULL DEFAULT 'FORWARD',
    input_value     VARCHAR(100),
    result_json     TEXT,
    duration_ms     BIGINT NOT NULL,
    executed_by     BIGINT NOT NULL REFERENCES users(id),
    executed_at     TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════
-- AUDIT LOG
-- ═══════════════════════════════════════

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    details         TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════
-- INDEXES (for trace performance)
-- ═══════════════════════════════════════

CREATE INDEX idx_bmc_lot ON batch_material_consumption(lot_id);
CREATE INDEX idx_bmc_batch ON batch_material_consumption(batch_id);
CREATE INDEX idx_shipment_items_batch ON shipment_items(batch_id);
CREATE INDEX idx_shipment_items_shipment ON shipment_items(shipment_id);
CREATE INDEX idx_serial_numbers_serial ON serial_numbers(serial);
CREATE INDEX idx_serial_numbers_batch ON serial_numbers(batch_id);
CREATE INDEX idx_scan_events_batch ON scan_events(batch_id);
CREATE INDEX idx_material_lots_status ON material_lots(status);
CREATE INDEX idx_production_batches_part ON production_batches(part_id);
CREATE INDEX idx_quality_holds_lot ON quality_holds(lot_id);
CREATE INDEX idx_vehicle_links_vin ON vehicle_links(vin);
CREATE INDEX idx_vehicle_links_batch ON vehicle_links(batch_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
