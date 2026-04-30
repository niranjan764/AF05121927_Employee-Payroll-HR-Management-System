-- ============================================================
--  EMPLOYEE PAYROLL & HR MANAGEMENT - DATABASE SETUP
-- ============================================================

CREATE DATABASE IF NOT EXISTS payroll_db;
USE payroll_db;

-- DEPARTMENTS TABLE
CREATE TABLE IF NOT EXISTS departments (
    dept_id     INT AUTO_INCREMENT PRIMARY KEY,
    dept_name   VARCHAR(100) NOT NULL UNIQUE,
    manager_id  INT,
    location    VARCHAR(100)
);

-- EMPLOYEES TABLE
CREATE TABLE IF NOT EXISTS employees (
    emp_id          INT AUTO_INCREMENT PRIMARY KEY,
    emp_code        VARCHAR(20) NOT NULL UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    phone           VARCHAR(15),
    dept_id         INT,
    designation     VARCHAR(100),
    join_date       DATE NOT NULL,
    basic_salary    DECIMAL(12,2) NOT NULL,
    hra_percent     DECIMAL(5,2) DEFAULT 40.00,   -- % of basic
    da_percent      DECIMAL(5,2) DEFAULT 20.00,   -- % of basic
    pf_percent      DECIMAL(5,2) DEFAULT 12.00,   -- % of basic (employee share)
    tax_bracket     DECIMAL(5,2) DEFAULT 10.00,   -- income tax %
    status          ENUM('ACTIVE','INACTIVE','RESIGNED','TERMINATED') DEFAULT 'ACTIVE',
    bank_account    VARCHAR(20),
    pan_number      VARCHAR(15),
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
);

-- LEAVE TYPES
CREATE TABLE IF NOT EXISTS leave_types (
    leave_type_id   INT AUTO_INCREMENT PRIMARY KEY,
    leave_name      VARCHAR(50) NOT NULL,
    max_per_year    INT DEFAULT 12
);

-- LEAVE REQUESTS
CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id        INT AUTO_INCREMENT PRIMARY KEY,
    emp_id          INT NOT NULL,
    leave_type_id   INT NOT NULL,
    from_date       DATE NOT NULL,
    to_date         DATE NOT NULL,
    total_days      INT NOT NULL,
    reason          TEXT,
    status          ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    applied_on      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by     INT,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id)
);

-- ATTENDANCE TABLE
CREATE TABLE IF NOT EXISTS attendance (
    att_id          INT AUTO_INCREMENT PRIMARY KEY,
    emp_id          INT NOT NULL,
    att_date        DATE NOT NULL,
    status          ENUM('PRESENT','ABSENT','HALF_DAY','HOLIDAY','LEAVE') DEFAULT 'PRESENT',
    check_in        TIME,
    check_out       TIME,
    UNIQUE KEY unique_att (emp_id, att_date),
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
);

-- PAYROLL TABLE (monthly payroll records)
CREATE TABLE IF NOT EXISTS payroll (
    payroll_id      INT AUTO_INCREMENT PRIMARY KEY,
    emp_id          INT NOT NULL,
    pay_month       INT NOT NULL,       -- 1–12
    pay_year        INT NOT NULL,
    basic_salary    DECIMAL(12,2),
    hra             DECIMAL(12,2),
    da              DECIMAL(12,2),
    other_allowance DECIMAL(12,2) DEFAULT 0.00,
    gross_salary    DECIMAL(12,2),
    pf_deduction    DECIMAL(12,2),
    tax_deduction   DECIMAL(12,2),
    other_deduction DECIMAL(12,2) DEFAULT 0.00,
    absent_days     INT DEFAULT 0,
    absent_deduction DECIMAL(12,2) DEFAULT 0.00,
    net_salary      DECIMAL(12,2),
    status          ENUM('GENERATED','PAID') DEFAULT 'GENERATED',
    generated_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_pay (emp_id, pay_month, pay_year),
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
);

-- PERFORMANCE REVIEWS
CREATE TABLE IF NOT EXISTS performance_reviews (
    review_id       INT AUTO_INCREMENT PRIMARY KEY,
    emp_id          INT NOT NULL,
    review_period   VARCHAR(20),        -- e.g. "Q1-2025"
    rating          DECIMAL(3,1),       -- 1.0 to 5.0
    remarks         TEXT,
    bonus_percent   DECIMAL(5,2) DEFAULT 0.00,
    reviewed_by     INT,
    review_date     DATE,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
);

-- SAMPLE DATA
INSERT INTO departments (dept_name, location) VALUES
('Engineering','Bangalore'),
('HR','Bangalore'),
('Finance','Mumbai'),
('Sales','Delhi'),
('Operations','Chennai');

INSERT INTO leave_types (leave_name, max_per_year) VALUES
('Casual Leave', 12),
('Sick Leave', 15),
('Earned Leave', 21),
('Maternity Leave', 180),
('Paternity Leave', 15);

INSERT INTO employees (emp_code, full_name, email, phone, dept_id, designation, join_date,
    basic_salary, hra_percent, da_percent, pf_percent, tax_bracket, bank_account, pan_number) VALUES
('EMP001','Ravi Kumar','ravi@company.com','9876543210',1,'Software Engineer','2023-01-15',
    50000,40,20,12,10,'1234567890','ABCDE1234F'),
('EMP002','Priya Sharma','priya@company.com','9876543211',2,'HR Manager','2022-06-01',
    70000,40,20,12,20,'0987654321','FGHIJ5678K'),
('EMP003','Amit Patel','amit@company.com','9876543212',3,'Finance Analyst','2021-03-10',
    60000,40,20,12,15,'1122334455','LMNOP9012Q');

SELECT 'Payroll DB setup complete!' AS status;
