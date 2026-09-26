CREATE TABLE signup_requests
(
    id CHAR(36) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    allowed_user_count INT NOT NULL,
    admin_email VARCHAR(150) NOT NULL,
    admin_password_hash VARCHAR(255) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    onboarding_status VARCHAR(50) NOT NULL,
    payment_session_id CHAR(36) NOT NULL,
    onboarded_company_id CHAR(36) NULL,
    onboarded_admin_user_id CHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_signup_requests_admin_email (admin_email),
    UNIQUE KEY uk_signup_requests_payment_session_id (payment_session_id),
    CONSTRAINT chk_signup_requests_allowed_user_count CHECK (allowed_user_count >= 1)
);
