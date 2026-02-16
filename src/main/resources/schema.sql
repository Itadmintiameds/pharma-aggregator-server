-- Create a sequence for seller ID generation
CREATE SEQUENCE IF NOT EXISTS seller_id_seq START WITH 1 INCREMENT BY 1;;;

-- Create a function to generate seller_id
CREATE OR REPLACE FUNCTION generate_seller_id(
    p_seller_name VARCHAR,
    p_seller_type_id BIGINT
)
    RETURNS VARCHAR AS $$
DECLARE
    company_prefix VARCHAR(2);
    seller_type_abbr VARCHAR;
    sequence_num VARCHAR;
    current_seq BIGINT;
    padding_length INT;
    new_seller_id VARCHAR;
BEGIN
    company_prefix := UPPER(SUBSTRING(REGEXP_REPLACE(p_seller_name, '[^a-zA-Z]', '', 'g'), 1, 2));

    SELECT UPPER(seller_type_abbreviation)
    INTO seller_type_abbr
    FROM tbl_seller_type_master
    WHERE seller_type_id = p_seller_type_id;

    current_seq := nextval('seller_id_seq');
    padding_length := GREATEST(4, LENGTH(current_seq::TEXT));
    sequence_num := LPAD(current_seq::TEXT, padding_length, '0');
    new_seller_id := company_prefix || seller_type_abbr || sequence_num;

    RETURN new_seller_id;
END;
$$ LANGUAGE plpgsql;;;

-- Create trigger function
CREATE OR REPLACE FUNCTION trg_generate_seller_id()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.seller_id IS NULL THEN
        NEW.seller_id := generate_seller_id(NEW.seller_name, NEW.seller_type_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;;;

-- Create trigger
DROP TRIGGER IF EXISTS trg_generate_seller_id ON tbl_seller;;;

CREATE TRIGGER trg_generate_seller_id
    BEFORE INSERT ON tbl_seller
    FOR EACH ROW
EXECUTE FUNCTION trg_generate_seller_id();;;