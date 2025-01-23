-- Coins table
CREATE TABLE coins (
    coinId UUID NOT NULL,
    coinName VARCHAR(255) NOT NULL,
    PRIMARY KEY (coinId)
);

-- Wallet_Coins table
CREATE TABLE wallet_coins (
    id SERIAL PRIMARY KEY NOT NULL,
    walletId UUID NOT NULL,
    coinId UUID NOT NULL,
    amount DECIMAL(18, 8) NOT NULL CHECK (amount >= 0),
    FOREIGN KEY (walletId) REFERENCES wallets(id),
    FOREIGN KEY (coinId) REFERENCES coins(coinId)
);


-- Define the trigger function
CREATE OR REPLACE FUNCTION check_coin_sum()
RETURNS TRIGGER AS $$
DECLARE
    total_amount DECIMAL(18, 8);
BEGIN
    -- Calculate the current total for the coin_id excluding the row being inserted or updated
    SELECT COALESCE(SUM(amount), 0)
    INTO total_amount
    FROM wallet_coins
    WHERE coinId = NEW.coinId AND walletId != NEW.walletId;

    -- Add the new or updated amount to the total
    total_amount := total_amount + NEW.amount;

    -- Check if the total exceeds 1
    IF total_amount >= 1 THEN
        RAISE EXCEPTION 'The total amount for this coin_id exceeds the allowed limit of 1';
    END IF;

    -- Allow the operation to proceed
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER check_coin_sum_before_insert_update
BEFORE INSERT OR UPDATE ON wallet_coins
FOR EACH ROW
EXECUTE FUNCTION check_coin_sum();
