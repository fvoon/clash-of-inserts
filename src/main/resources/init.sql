
-- ============================
-- TABLE DEFINITIONS
-- ============================

-- 1. billing_address table
CREATE TABLE billing_address (
                                 id             bigserial PRIMARY KEY,
                                 created_at     timestamp    NOT NULL,
                                 updated_at     timestamp    NOT NULL,
                                 addr_line1     varchar(255) NOT NULL,
                                 addr_line2     varchar(255),
                                 address_id     varchar(255),
                                 address_source varchar(255) NOT NULL,
                                 city           varchar(255) NOT NULL,
                                 deleted        boolean      NOT NULL,
                                 hash           varchar(255) NOT NULL,
                                 postal_code    varchar(255) NOT NULL,
                                 state          varchar(255) NOT NULL,
                                 user_id        varchar(255) NOT NULL
);
CREATE INDEX idx_billing_address_user_id ON billing_address (user_id);
CREATE INDEX idx_billing_address_hash ON billing_address (hash);
CREATE INDEX idx_billing_address_deleted ON billing_address (deleted);
CREATE INDEX idx_billing_address_updated_at ON billing_address (updated_at);

-- 2. fund_option table
CREATE TABLE fund_option (
                             id              bigserial PRIMARY KEY,
                             created_at      timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                             details         jsonb,
                             identifier      varchar(255) NOT NULL,
                             is_deleted      boolean   DEFAULT false NOT NULL,
                             is_disabled     boolean   DEFAULT false NOT NULL,
                             name            varchar(255),
                             type            varchar(255) NOT NULL,
                             updated_at      timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                             user_id         varchar(255) NOT NULL,
                             is_verified     boolean   DEFAULT false,
                             is_available    boolean   DEFAULT true,
                             billing_address bigint,
                             status          varchar(255),
                             is_moneylion    boolean   DEFAULT false
);
CREATE INDEX idx_fund_user_id ON fund_option (user_id);
CREATE INDEX idx_fund_identifier ON fund_option (identifier);
CREATE INDEX fund_option_is_deleted_idx ON fund_option (is_deleted);
CREATE INDEX fund_option_type_idx ON fund_option (type);
CREATE INDEX fund_option_is_verified_idx ON fund_option (is_verified);
CREATE INDEX fund_option_name_idx ON fund_option (name);
CREATE INDEX fund_option_created_at_idx ON fund_option (created_at);
CREATE INDEX fund_option_billing_address_idx ON fund_option (billing_address);
CREATE INDEX idx_fund_details_expiry_date ON fund_option ((details ->> 'expiryDate'));
CREATE INDEX idx_fund_details_card_hash ON fund_option ((details ->> 'cardHash'));
CREATE INDEX idx_fund_type ON fund_option (type);
CREATE INDEX idx_fund_option_identifier_type ON fund_option (identifier, type);
CREATE INDEX idx_fund_option_details_account_routing_number ON fund_option ((details ->> 'bankAccountNumber'), (details ->> 'bankRoutingNumber'));
CREATE INDEX idx_fund_option_updated_at ON fund_option (updated_at);
CREATE INDEX idx_fund_option_account_number_trim_leading_zeros ON fund_option (ltrim(details ->> 'bankAccountNumber', '0'));

-- 3. fund_ach_information table
CREATE TABLE fund_ach_information (
                                      id                       bigserial PRIMARY KEY,
                                      created_at               timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                                      updated_at               timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                                      fund_id                  bigint NOT NULL,
                                      account_number           varchar(255),
                                      routing_number           varchar(255),
                                      tokenized_account_number varchar(255),
                                      tokenized_routing_number varchar(255)
);
CREATE INDEX idx_fund_ach_information_fund_id ON fund_ach_information (fund_id);
CREATE INDEX idx_fund_ach_information_account_number ON fund_ach_information (account_number);
CREATE INDEX idx_fund_ach_information_routing_number ON fund_ach_information (routing_number);
CREATE INDEX idx_fund_ach_information_tokenized_account_number ON fund_ach_information (tokenized_account_number);
CREATE INDEX idx_fund_ach_information_tokenized_routing_number ON fund_ach_information (tokenized_routing_number);
CREATE INDEX idx_fund_ach_information_account_number_trim_leading_zeros ON fund_ach_information (ltrim(account_number::text, '0'));
CREATE INDEX idx_fund_ach_information_tokenized_number_trim_leading_zeros ON fund_ach_information (ltrim(tokenized_account_number::text, '0'));

-- 4. fund_option_details_history table
CREATE TABLE fund_option_details_history (
                                             id         bigserial PRIMARY KEY,
                                             created_at timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                                             updated_at timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                                             fund_id    bigint NOT NULL,
                                             user_id    varchar(255) NOT NULL,
                                             details    jsonb,
                                             source     varchar(255)
);
CREATE INDEX idx_fund_details_history_user_id ON fund_option_details_history (user_id);
CREATE INDEX idx_fund_details_history_fund_id ON fund_option_details_history (fund_id);
CREATE INDEX idx_fund_details_history_source ON fund_option_details_history (source);
CREATE INDEX idx_fund_option_details_history_updated_at ON fund_option_details_history (updated_at);

-- 5. fund_option_history table
CREATE TABLE fund_option_history (
                                     id           bigserial PRIMARY KEY,
                                     created_at   timestamp DEFAULT timezone('UTC+0', now()) NOT NULL,
                                     identifier   varchar(255) NOT NULL,
                                     is_deleted   boolean   DEFAULT false NOT NULL,
                                     is_disabled  boolean   DEFAULT false NOT NULL,
                                     name         varchar(255),
                                     type         varchar(255) NOT NULL,
                                     user_id      varchar(255) NOT NULL,
                                     is_available boolean   DEFAULT true,
                                     fund_id      bigint
);
CREATE INDEX fund_option_history_created_at_idx ON fund_option_history (created_at);
CREATE INDEX fund_option_history_user_id_idx ON fund_option_history (user_id);

-- ============================
-- FOREIGN KEYS (after tables)
-- ============================

ALTER TABLE fund_option
    ADD CONSTRAINT fk_fund_option_billing_address
        FOREIGN KEY (billing_address) REFERENCES billing_address(id);

ALTER TABLE fund_ach_information
    ADD CONSTRAINT fk_fund_ach_information_fund_id
        FOREIGN KEY (fund_id) REFERENCES fund_option(id);

-- ============================
-- TRIGGERS AND FUNCTIONS
-- ============================

-- Updated_at triggers
CREATE FUNCTION function_update_billing_address() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = timezone('UTC+0', now());
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER billing_address_updated_at_trigger
    BEFORE UPDATE ON billing_address
    FOR EACH ROW EXECUTE FUNCTION function_update_billing_address();

CREATE FUNCTION function_update_fund_option() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = timezone('UTC+0', now());
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_updated_at_trigger
    BEFORE UPDATE ON fund_option
    FOR EACH ROW EXECUTE FUNCTION function_update_fund_option();

CREATE FUNCTION function_update_fund_ach_information() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = timezone('UTC+0', now());
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_ach_information_updated_at_trigger
    BEFORE UPDATE ON fund_ach_information
    FOR EACH ROW EXECUTE FUNCTION function_update_fund_ach_information();

CREATE FUNCTION function_update_fund_option_details_history() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = timezone('UTC+0', now());
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_details_history_updated_at_trigger
    BEFORE UPDATE ON fund_option_details_history
    FOR EACH ROW EXECUTE FUNCTION function_update_fund_option_details_history();

-- Additional logic triggers and functions were in the original file and can be appended here...


-- ============================
-- LOGGING FUNCTION AND TRIGGER
-- ============================

CREATE FUNCTION fund_option_logging_function() RETURNS trigger AS $$
BEGIN
    IF (NEW IS DISTINCT FROM OLD) THEN
        INSERT INTO fund_option_history (
            user_id, identifier, name, type,
            is_deleted, is_disabled, is_available, fund_id
        )
        VALUES (
            OLD.user_id, OLD.identifier, OLD.name, OLD.type,
            OLD.is_deleted, OLD.is_disabled, OLD.is_available, OLD.id
        );
        NEW.updated_at = CURRENT_TIMESTAMP AT TIME ZONE 'UTC+0';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_logging_trigger
    BEFORE UPDATE ON fund_option
    FOR EACH ROW EXECUTE FUNCTION fund_option_logging_function();

-- ============================
-- AUDIT DETAILS HISTORY TRIGGER
-- ============================

CREATE FUNCTION fund_option_details_audit_function() RETURNS trigger AS $$
BEGIN
INSERT INTO fund_option_details_history (fund_id, user_id, details)
VALUES (OLD.id, OLD.user_id, OLD.details);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_details_audit_trigger
    BEFORE UPDATE ON fund_option
    FOR EACH ROW
    WHEN (OLD.details IS DISTINCT FROM NEW.details)
EXECUTE FUNCTION fund_option_details_audit_function();

-- ============================
-- ACH DUPLICATE CONSTRAINT
-- ============================

CREATE FUNCTION fund_option_constraint_check_function() RETURNS trigger AS $$
BEGIN
    PERFORM 1
    FROM fund_option
    WHERE user_id = NEW.user_id
      AND type = 'ACH'
      AND details ->> 'bankRoutingNumber' = NEW.details ->> 'bankRoutingNumber'
      AND details ->> 'bankAccountNumber' = NEW.details ->> 'bankAccountNumber'
      AND is_deleted = false
      AND is_disabled = false;

    IF FOUND THEN
        RAISE EXCEPTION 'ACH fund for this selected bank routing number and account number is already existed for this user';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_constraint_check_trigger
    BEFORE INSERT ON fund_option
    FOR EACH ROW EXECUTE FUNCTION fund_option_constraint_check_function();

-- ============================
-- ML_ACTIVE_INVESTMENT DUPLICATE CONSTRAINT
-- ============================

CREATE FUNCTION fund_option_duplicate_active_investment_check_function() RETURNS trigger AS $$
BEGIN
    PERFORM 1
    FROM fund_option
    WHERE user_id = NEW.user_id
      AND type = 'ML_ACTIVE_INVESTMENT'
      AND identifier = NEW.identifier
      AND is_deleted = false
      AND is_disabled = false;

    IF FOUND THEN
        RAISE EXCEPTION 'Active Investment fund already exists for this user';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_option_duplicate_active_investment_check
    BEFORE INSERT ON fund_option
    FOR EACH ROW EXECUTE FUNCTION fund_option_duplicate_active_investment_check_function();

-- ============================
-- INSERT fund_ach_information ON INSERT INTO fund_option
-- ============================

CREATE FUNCTION fund_ach_information_insert_function() RETURNS trigger AS $$
DECLARE
new_account_number VARCHAR(255);
    new_routing_number VARCHAR(255);
BEGIN
    IF (NEW.details ->> 'bankAccountNumber' LIKE ('%' || NEW.identifier)) THEN
SELECT tokenized_account_number, tokenized_routing_number
INTO new_account_number, new_routing_number
FROM fund_ach_information
WHERE account_number = NEW.details ->> 'bankAccountNumber'
  AND routing_number = NEW.details ->> 'bankRoutingNumber'
    LIMIT 1;

INSERT INTO fund_ach_information (
    fund_id, account_number, routing_number,
    tokenized_account_number, tokenized_routing_number
) VALUES (
             NEW.id, NEW.details ->> 'bankAccountNumber', NEW.details ->> 'bankRoutingNumber',
             new_account_number, new_routing_number
         );
ELSE
SELECT account_number, routing_number
INTO new_account_number, new_routing_number
FROM fund_ach_information
WHERE tokenized_account_number = NEW.details ->> 'bankAccountNumber'
  AND tokenized_routing_number = NEW.details ->> 'bankRoutingNumber'
    LIMIT 1;

INSERT INTO fund_ach_information (
    fund_id, account_number, routing_number,
    tokenized_account_number, tokenized_routing_number
) VALUES (
             NEW.id, new_account_number, new_routing_number,
             NEW.details ->> 'bankAccountNumber', NEW.details ->> 'bankRoutingNumber'
         );
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_ach_information_insert_trigger
    AFTER INSERT ON fund_option
    FOR EACH ROW
    WHEN (NEW.type = 'ACH')
    EXECUTE FUNCTION fund_ach_information_insert_function();

-- ============================
-- UPDATE fund_ach_information ON ACCOUNT NUMBER CHANGE
-- ============================

CREATE FUNCTION fund_ach_information_update_function() RETURNS trigger AS $$
DECLARE
date_now TIMESTAMP;
    new_account_number VARCHAR(255);
    new_routing_number VARCHAR(255);
BEGIN
    date_now := timezone('UTC+0', now());

    IF (NEW.details ->> 'bankAccountNumber' LIKE ('%' || NEW.identifier)) THEN
        IF EXISTS (SELECT 1 FROM fund_ach_information WHERE fund_id = OLD.id) THEN
UPDATE fund_ach_information
SET account_number = NEW.details ->> 'bankAccountNumber',
    routing_number = NEW.details ->> 'bankRoutingNumber',
    updated_at     = date_now
WHERE fund_id = OLD.id;
ELSE
            INSERT INTO fund_ach_information (
                fund_id, account_number, routing_number,
                tokenized_account_number, tokenized_routing_number
            ) VALUES (
                OLD.id, NEW.details ->> 'bankAccountNumber', NEW.details ->> 'bankRoutingNumber',
                OLD.details ->> 'bankAccountNumber', OLD.details ->> 'bankRoutingNumber'
            );
END IF;

SELECT tokenized_account_number, tokenized_routing_number
INTO new_account_number, new_routing_number
FROM fund_ach_information
WHERE fund_id = OLD.id;

UPDATE fund_ach_information
SET tokenized_account_number = new_account_number,
    tokenized_routing_number = new_routing_number
WHERE tokenized_account_number IS NULL
  AND tokenized_routing_number IS NULL
  AND account_number = NEW.details ->> 'bankAccountNumber'
  AND routing_number = NEW.details ->> 'bankRoutingNumber';

ELSE
        IF EXISTS (SELECT 1 FROM fund_ach_information WHERE fund_id = OLD.id) THEN
UPDATE fund_ach_information
SET tokenized_account_number = NEW.details ->> 'bankAccountNumber',
    tokenized_routing_number = NEW.details ->> 'bankRoutingNumber',
    updated_at               = date_now
WHERE fund_id = OLD.id;

UPDATE fund_ach_information
SET tokenized_account_number = NEW.details ->> 'bankAccountNumber',
    tokenized_routing_number = NEW.details ->> 'bankRoutingNumber',
    updated_at               = date_now
WHERE tokenized_account_number IS NULL
  AND tokenized_routing_number IS NULL
  AND account_number = OLD.details ->> 'bankAccountNumber'
  AND routing_number = OLD.details ->> 'bankRoutingNumber';
ELSE
            INSERT INTO fund_ach_information (
                fund_id, account_number, routing_number,
                tokenized_account_number, tokenized_routing_number
            ) VALUES (
                OLD.id, OLD.details ->> 'bankAccountNumber', OLD.details ->> 'bankRoutingNumber',
                NEW.details ->> 'bankAccountNumber', NEW.details ->> 'bankRoutingNumber'
            );
END IF;

SELECT account_number, routing_number
INTO new_account_number, new_routing_number
FROM fund_ach_information
WHERE fund_id = OLD.id;

UPDATE fund_ach_information
SET account_number = new_account_number,
    routing_number = new_routing_number
WHERE account_number IS NULL
  AND routing_number IS NULL
  AND tokenized_account_number = NEW.details ->> 'bankAccountNumber'
  AND tokenized_routing_number = NEW.details ->> 'bankRoutingNumber';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fund_ach_information_update_trigger
    BEFORE UPDATE ON fund_option
    FOR EACH ROW
    WHEN (
    NEW.type = 'ACH'
    AND (OLD.details ->> 'bankAccountNumber') IS DISTINCT FROM (NEW.details ->> 'bankAccountNumber')
)
EXECUTE FUNCTION fund_ach_information_update_function();