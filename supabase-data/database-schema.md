# ASEAN Tariff Simulator - Supabase Database Schema

## Database Structure

This document outlines the database schema for the ASEAN Tariff Simulator application using Supabase (PostgreSQL).

### Tables

#### 1. Products Table
Stores product information including name, brand, cost, and unit of measurement.

```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for better performance
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_brand ON products(brand);
```

#### 2. Tariff Rates Table
Stores tariff rate information between countries, including AHS (ASEAN Harmonized System) and MFN (Most Favored Nation) rates.

```sql
CREATE TABLE tariff_rates (
    id SERIAL PRIMARY KEY,
    country VARCHAR(100) NOT NULL,
    partner VARCHAR(100) NOT NULL,
    ahs_weighted DECIMAL(8,2) NOT NULL,
    mfn_weighted DECIMAL(8,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Ensure unique country-partner combinations
    UNIQUE(country, partner)
);

-- Indexes for better performance
CREATE INDEX idx_tariff_rates_country ON tariff_rates(country);
CREATE INDEX idx_tariff_rates_partner ON tariff_rates(partner);
CREATE INDEX idx_tariff_rates_country_partner ON tariff_rates(country, partner);
```

#### 3. Users Table (Optional - for authentication)
If you plan to implement user authentication:

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for email lookups
CREATE INDEX idx_users_email ON users(email);
```

#### 4. Calculation History Table (Optional - for tracking calculations)
If you want to track calculation history:

```sql
CREATE TABLE calculation_history (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    product_id INTEGER REFERENCES products(id),
    exporting_from VARCHAR(100) NOT NULL,
    importing_to VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    custom_cost DECIMAL(10,2),
    tariff_rate DECIMAL(8,2) NOT NULL,
    tariff_type VARCHAR(10) NOT NULL, -- 'AHS' or 'MFN'
    product_cost DECIMAL(10,2) NOT NULL,
    tariff_amount DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for better performance
CREATE INDEX idx_calculation_history_user_id ON calculation_history(user_id);
CREATE INDEX idx_calculation_history_product_id ON calculation_history(product_id);
CREATE INDEX idx_calculation_history_calculated_at ON calculation_history(calculated_at);
```

### Functions and Triggers

#### Update Timestamp Trigger
Automatically update the `updated_at` field when records are modified:

```sql
-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to products table
CREATE TRIGGER update_products_updated_at 
    BEFORE UPDATE ON products 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to tariff_rates table
CREATE TRIGGER update_tariff_rates_updated_at 
    BEFORE UPDATE ON tariff_rates 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to users table (if using)
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
```

### Helper Functions

#### Check FTA Status Function
Function to determine if countries have a Free Trade Agreement:

```sql
CREATE OR REPLACE FUNCTION has_fta(
    p_country VARCHAR(100),
    p_partner VARCHAR(100)
)
RETURNS BOOLEAN AS $$
DECLARE
    tariff_record RECORD;
BEGIN
    SELECT ahs_weighted, mfn_weighted 
    INTO tariff_record
    FROM tariff_rates 
    WHERE country = p_country AND partner = p_partner;
    
    IF tariff_record IS NULL THEN
        RETURN FALSE;
    END IF;
    
    RETURN tariff_record.ahs_weighted < tariff_record.mfn_weighted;
END;
$$ LANGUAGE plpgsql;
```

#### Get Effective Tariff Rate Function
Function to get the effective tariff rate based on FTA status:

```sql
CREATE OR REPLACE FUNCTION get_effective_tariff_rate(
    p_country VARCHAR(100),
    p_partner VARCHAR(100)
)
RETURNS DECIMAL(8,2) AS $$
DECLARE
    tariff_record RECORD;
BEGIN
    SELECT ahs_weighted, mfn_weighted 
    INTO tariff_record
    FROM tariff_rates 
    WHERE country = p_country AND partner = p_partner;
    
    IF tariff_record IS NULL THEN
        RETURN NULL;
    END IF;
    
    IF tariff_record.ahs_weighted < tariff_record.mfn_weighted THEN
        RETURN tariff_record.ahs_weighted;
    ELSE
        RETURN tariff_record.mfn_weighted;
    END IF;
END;
$$ LANGUAGE plpgsql;
```

### Row Level Security (RLS) Policies

If you want to implement security policies:

```sql
-- Enable RLS on tables
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE tariff_rates ENABLE ROW LEVEL SECURITY;

-- Allow public read access to products and tariff_rates
CREATE POLICY "Allow public read access to products" ON products
    FOR SELECT USING (true);

CREATE POLICY "Allow public read access to tariff_rates" ON tariff_rates
    FOR SELECT USING (true);

-- If using users table, add appropriate policies
-- ALTER TABLE users ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY "Users can view own profile" ON users
--     FOR SELECT USING (auth.uid() = id);
```

### Data Import Instructions

1. **Import Products Data:**
   ```sql
   COPY products (id, name, brand, cost, unit, created_at, updated_at)
   FROM '/path/to/products.csv'
   WITH (FORMAT csv, HEADER true);
   ```

2. **Import Tariff Rates Data:**
   ```sql
   COPY tariff_rates (id, country, partner, ahs_weighted, mfn_weighted, created_at, updated_at)
   FROM '/path/to/tariff_rates.csv'
   WITH (FORMAT csv, HEADER true);
   ```

### API Integration Notes

When implementing your Java backend, ensure that:

1. **Tariff Calculation Logic:**
   - Use the same formula as frontend: `tariffAmount = (productCost * tariffRate) / 100`
   - Check FTA status by comparing `ahs_weighted < mfn_weighted`
   - Use AHS rate if FTA exists, otherwise use MFN rate

2. **Database Connection:**
   - Use Supabase connection string for PostgreSQL
   - Consider using connection pooling for better performance
   - Implement proper error handling for database operations

3. **Data Validation:**
   - Validate country pairs exist in tariff_rates table
   - Validate product exists in products table
   - Handle edge cases where tariff data might be missing

### Sample Queries

#### Get all countries:
```sql
SELECT DISTINCT country FROM tariff_rates
UNION
SELECT DISTINCT partner FROM tariff_rates
ORDER BY country;
```

#### Get products by name:
```sql
SELECT * FROM products WHERE name = 'Rice (Paddy & Milled)';
```

#### Calculate tariff for specific trade:
```sql
SELECT 
    p.name,
    p.brand,
    p.cost,
    tr.ahs_weighted,
    tr.mfn_weighted,
    CASE 
        WHEN tr.ahs_weighted < tr.mfn_weighted THEN tr.ahs_weighted
        ELSE tr.mfn_weighted
    END as effective_rate,
    CASE 
        WHEN tr.ahs_weighted < tr.mfn_weighted THEN 'AHS (with FTA)'
        ELSE 'MFN (no FTA)'
    END as tariff_type
FROM products p
CROSS JOIN tariff_rates tr
WHERE tr.country = 'Australia' 
  AND tr.partner = 'China'
  AND p.name = 'Rice (Paddy & Milled)'
LIMIT 1;
```
