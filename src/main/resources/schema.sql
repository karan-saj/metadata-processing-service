-- Table to store metadata
CREATE TABLE metadata_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type TEXT NOT NULL,
    metadata JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    version INTEGER DEFAULT 1
);

-- Indexes for fast lookups
CREATE INDEX index_metadata_jsonb ON metadata_store USING GIN (metadata);
CREATE INDEX index_event_type ON metadata_store (event_type);

-- Table to track metadata change history
CREATE TABLE metadata_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metadata_id UUID REFERENCES metadata_store(id) ON DELETE CASCADE,
    before_state JSONB,
    after_state JSONB NOT NULL,
    operation TEXT NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    timestamp TIMESTAMP DEFAULT NOW(),
    user TEXT DEFAULT 'system'
);

-- Indexes for CDC queries
CREATE INDEX index_metadata_history_metadata_id ON metadata_history (metadata_id);
CREATE INDEX index_metadata_history_operation ON metadata_history (operation);

-- New tables for metadata rules
CREATE TABLE metadata_rules (
    id VARCHAR(255) PRIMARY KEY,
    source_id VARCHAR(255),
    source_type VARCHAR(255),
    tenant_id VARCHAR(255) NOT NULL,
    allowed_input_formats TEXT,
    allowed_output_formats TEXT,
    required_fields TEXT,
    pii_fields TEXT,
    priority VARCHAR(50),
    batching_allowed BOOLEAN,
    max_batch_size INTEGER,
    use_global_defaults BOOLEAN,
    configuration JSONB
);

-- Indexes for better query performance
CREATE INDEX idx_metadata_rules_tenant ON metadata_rules(tenant_id);
CREATE INDEX idx_metadata_rules_source ON metadata_rules(source_id, source_type);
