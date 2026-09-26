ALTER TABLE signup_requests
    ADD COLUMN contact_status VARCHAR(50) NULL,
    ADD COLUMN subscription_status VARCHAR(50) NULL,
    ADD COLUMN message TEXT NULL;

CREATE TABLE signup_notifications
(
    id CHAR(36) NOT NULL,
    signup_id CHAR(36) NOT NULL,
    recipient_email VARCHAR(150) NULL,
    notification_mode VARCHAR(50) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    allowed_user_count INT NOT NULL,
    admin_email VARCHAR(150) NOT NULL,
    message TEXT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_signup_notifications_signup_id (signup_id),
    CONSTRAINT fk_signup_notifications_signup
        FOREIGN KEY (signup_id) REFERENCES signup_requests(id)
        ON DELETE RESTRICT
);
