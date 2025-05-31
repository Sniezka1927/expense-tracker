-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create categories table
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    color_code VARCHAR(7) DEFAULT '#007bff',
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create expenses table
CREATE TABLE expenses (
    id SERIAL PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    category_id INTEGER NOT NULL REFERENCES categories(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create budgets table
CREATE TABLE budgets (
    id SERIAL PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    category_id INTEGER NOT NULL REFERENCES categories(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert default categories
INSERT INTO categories (name, description, is_default) VALUES
('Food', 'Groceries and dining out', TRUE),
('Transportation', 'Public transport, fuel, car maintenance', TRUE),
('Housing', 'Rent, mortgage, utilities', TRUE),
('Entertainment', 'Movies, games, hobbies', TRUE),
('Healthcare', 'Medical expenses, insurance', TRUE),
('Shopping', 'Clothing, electronics, personal items', TRUE),
('Education', 'Courses, books, tuition', TRUE),
('Travel', 'Vacations, trips', TRUE),
('Other', 'Miscellaneous expenses', TRUE);