CREATE TABLE availabilities
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    date       date         NOT NULL,
    time       time         NOT NULL,
    duration   INT          NOT NULL COMMENT 'Duration in minutes',
    details    VARCHAR(255) NOT NULL,
    type       VARCHAR(255) NOT NULL,
    status     VARCHAR(255) NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_availabilities PRIMARY KEY (id)
);

CREATE TABLE board_games
(
    item_id          BIGINT                 NOT NULL,
    number_of_pieces INT                    NOT NULL,
    recommended_age  INT                    NOT NULL,
    game_rules       VARCHAR(65535)         NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    gtin             VARCHAR(16) DEFAULT '' NOT NULL,
    CONSTRAINT pk_board_games PRIMARY KEY (item_id)
);

CREATE TABLE books
(
    item_id          BIGINT       NOT NULL,
    isbn             VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    publication_date date         NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    CONSTRAINT pk_books PRIMARY KEY (item_id)
);

CREATE TABLE categories
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE communications
(
    id           BIGINT       NOT NULL,
    member_id    BIGINT       NOT NULL,
    message_type VARCHAR(255) NOT NULL,
    content      LONGTEXT     NOT NULL,
    send_date    date         NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    CONSTRAINT pk_communications PRIMARY KEY (id)
);

CREATE TABLE copies
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    item_id          BIGINT       NOT NULL,
    status           VARCHAR(255) NOT NULL,
    acquisition_date date         NOT NULL,
    price DOUBLE NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    CONSTRAINT pk_copies PRIMARY KEY (id)
);

CREATE TABLE items
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    type         VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    category     BIGINT       NOT NULL,
    publisher_id BIGINT       NOT NULL,
    supplier_id  BIGINT       NOT NULL,
    value DOUBLE NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    link         VARCHAR(2000) NULL,
    CONSTRAINT pk_items PRIMARY KEY (id)
);

CREATE TABLE loan_settings
(
    id                     BIGINT NOT NULL,
    loan_duration_days     INT    NOT NULL,
    max_loans_adult        INT    NOT NULL,
    max_loans_child        INT    NOT NULL,
    max_reservations_adult INT    NOT NULL,
    max_reservations_child INT    NOT NULL,
    created_at             datetime NULL,
    updated_at             datetime NULL,
    CONSTRAINT pk_loan_settings PRIMARY KEY (id)
);

CREATE TABLE loans
(
    id              BIGINT       NOT NULL,
    copy_id         BIGINT       NOT NULL,
    member_id       BIGINT       NOT NULL,
    loan_date       date         NOT NULL,
    return_due_date date         NOT NULL,
    status          VARCHAR(255) NOT NULL,
    created_at      datetime NULL,
    updated_at      datetime NULL,
    CONSTRAINT pk_loans PRIMARY KEY (id)
);

CREATE TABLE magazines
(
    item_id          BIGINT       NOT NULL,
    isni             VARCHAR(255) NOT NULL,
    month            VARCHAR(255) NOT NULL,
    publication_date date         NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    year             VARCHAR(4)   NOT NULL,
    CONSTRAINT pk_magazines PRIMARY KEY (item_id)
);

CREATE TABLE permissions
(
    id            BIGINT       NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (id)
);

CREATE TABLE publishers
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    name         VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255) NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    CONSTRAINT pk_publishers PRIMARY KEY (id)
);

CREATE TABLE reservations
(
    id               BIGINT       NOT NULL,
    copy_id          BIGINT       NOT NULL,
    member_id        BIGINT       NOT NULL,
    reservation_date date         NOT NULL,
    status           VARCHAR(255) NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    CONSTRAINT pk_reservations PRIMARY KEY (id)
);

CREATE TABLE role_permissions
(
    created_at    datetime NULL,
    updated_at    datetime NULL,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE roles
(
    id         BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE sessions
(
    id            VARCHAR(255) NOT NULL,
    user_id       BIGINT NULL,
    ip_address    VARCHAR(45) NULL,
    user_agent    LONGTEXT NULL,
    payload       LONGTEXT     NOT NULL,
    last_activity INT          NOT NULL,
    CONSTRAINT pk_sessions PRIMARY KEY (id)
);

CREATE TABLE special_limits
(
    id               BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    max_loans        INT          NOT NULL,
    max_reservations INT          NOT NULL,
    status           VARCHAR(255) NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    CONSTRAINT pk_special_limits PRIMARY KEY (id)
);

CREATE TABLE suppliers
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    name         VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255) NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    CONSTRAINT pk_suppliers PRIMARY KEY (id)
);

CREATE TABLE user_relationships
(
    relationship_type VARCHAR(255) NOT NULL,
    created_at        datetime NULL,
    updated_at        datetime NULL,
    parent_id         BIGINT       NOT NULL,
    child_id          BIGINT       NOT NULL,
    CONSTRAINT pk_user_relationships PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE users
(
    id                BIGINT       NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    username          VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NULL,
    email_verified_at datetime NULL,
    status            VARCHAR(255) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    phone_number      VARCHAR(255) NULL,
    cell_number       VARCHAR(255) NULL,
    is_child          BIT(1)       NOT NULL,
    role_id           BIGINT       NOT NULL,
    date_of_birth     datetime     NOT NULL,
    remember_token    VARCHAR(100) NULL,
    created_at        datetime NULL,
    updated_at        datetime NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE availabilities
    ADD CONSTRAINT FK_AVAILABILITIES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE board_games
    ADD CONSTRAINT FK_BOARD_GAMES_ON_ITEM FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;

ALTER TABLE books
    ADD CONSTRAINT FK_BOOKS_ON_ITEM FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;

ALTER TABLE communications
    ADD CONSTRAINT FK_COMMUNICATIONS_ON_MEMBER FOREIGN KEY (member_id) REFERENCES users (id);

ALTER TABLE copies
    ADD CONSTRAINT FK_COPIES_ON_ITEM FOREIGN KEY (item_id) REFERENCES items (id);

ALTER TABLE items
    ADD CONSTRAINT FK_ITEMS_ON_CATEGORY FOREIGN KEY (category) REFERENCES categories (id);

ALTER TABLE items
    ADD CONSTRAINT FK_ITEMS_ON_PUBLISHER FOREIGN KEY (publisher_id) REFERENCES publishers (id);

ALTER TABLE items
    ADD CONSTRAINT FK_ITEMS_ON_SUPPLIER FOREIGN KEY (supplier_id) REFERENCES suppliers (id);

ALTER TABLE loans
    ADD CONSTRAINT FK_LOANS_ON_COPY FOREIGN KEY (copy_id) REFERENCES copies (id);

ALTER TABLE loans
    ADD CONSTRAINT FK_LOANS_ON_MEMBER FOREIGN KEY (member_id) REFERENCES users (id);

ALTER TABLE magazines
    ADD CONSTRAINT FK_MAGAZINES_ON_ITEM FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;

ALTER TABLE reservations
    ADD CONSTRAINT FK_RESERVATIONS_ON_COPY FOREIGN KEY (copy_id) REFERENCES copies (id);

ALTER TABLE reservations
    ADD CONSTRAINT FK_RESERVATIONS_ON_MEMBER FOREIGN KEY (member_id) REFERENCES users (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT FK_ROLE_PERMISSIONS_ON_PERMISSION FOREIGN KEY (permission_id) REFERENCES permissions (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT FK_ROLE_PERMISSIONS_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE special_limits
    ADD CONSTRAINT FK_SPECIAL_LIMITS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_relationships
    ADD CONSTRAINT FK_USER_RELATIONSHIPS_ON_CHILD FOREIGN KEY (child_id) REFERENCES users (id);

ALTER TABLE user_relationships
    ADD CONSTRAINT FK_USER_RELATIONSHIPS_ON_PARENT FOREIGN KEY (parent_id) REFERENCES users (id);