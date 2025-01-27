-- Coins table
CREATE TABLE coins (
    coinId UUID NOT NULL,
    coinName VARCHAR(255) NOT NULL,
    PRIMARY KEY (coinId)
);

-- accountCoins table
CREATE TABLE accountCoins (
    accountCoinId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accountId UUID NOT NULL,
    coinId UUID NOT NULL,
    satoshis BIGINT NOT NULL CHECK (satoshis >= 0 AND satoshis <= 100000000),
    FOREIGN KEY (accountId) REFERENCES accounts(accountId),
    FOREIGN KEY (coinId) REFERENCES coins(coinId)
);


-- Define the trigger function
CREATE OR REPLACE FUNCTION calculate_total_satoshis()
RETURNS TRIGGER AS $$
DECLARE
    total_satoshis BIGINT;
BEGIN
    -- Calculate the current total for the coin_id excluding the row being inserted or updated
    SELECT COALESCE(SUM(satoshis), 0)
    INTO total_satoshis
    FROM accountCoins
    WHERE coinId = NEW.coinId;

    -- Optional: Add logic to use total_satoshis
    -- Example: Raise an exception if the total exceeds a threshold
    IF total_satoshis + NEW.satoshis >= 100000000 THEN
        RAISE EXCEPTION 'Total satoshis for coinId % exceed 100,000,000.', NEW.coinId;
    END IF;

    -- Return NEW to allow the operation to proceed
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER calculate_total_satoshis_before_insert_update
BEFORE INSERT OR UPDATE ON accountCoins
FOR EACH ROW
EXECUTE FUNCTION calculate_total_satoshis();
