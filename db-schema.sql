-- ============================================================
-- 포트폴리오 포털 사이트 DB 스키마
-- ============================================================

CREATE DATABASE IF NOT EXISTS portfolio
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE portfolio;


-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(20)     NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(20)     NOT NULL,
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2. notice
-- ============================================================
CREATE TABLE notice (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    category    ENUM('NOTICE', 'EVENT') NOT NULL,
    is_pinned   TINYINT(1)      NOT NULL DEFAULT 0,
    title       VARCHAR(255)    NOT NULL,
    content     TEXT            NOT NULL,
    view_count  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_notice PRIMARY KEY (id),
    CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 3. free_post
-- ============================================================
CREATE TABLE free_post (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    category    ENUM('HUMOR', 'HOBBY') NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    content     TEXT            NOT NULL,
    view_count  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_free_post PRIMARY KEY (id),
    CONSTRAINT fk_free_post_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 4. gallery_post
-- ============================================================
CREATE TABLE gallery_post (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    category    ENUM('FOOD', 'CELEBRITY') NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    content     TEXT            NOT NULL,
    view_count  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_gallery_post PRIMARY KEY (id),
    CONSTRAINT fk_gallery_post_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. inquiry
-- ============================================================
CREATE TABLE inquiry (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    content         TEXT            NOT NULL,
    is_secret       TINYINT(1)      NOT NULL DEFAULT 0,
    answer_status   ENUM('PENDING', 'ANSWERED') NOT NULL DEFAULT 'PENDING',
    view_count      INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_inquiry PRIMARY KEY (id),
    CONSTRAINT fk_inquiry_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 6. inquiry_answer
-- ============================================================
CREATE TABLE inquiry_answer (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    inquiry_id  BIGINT      NOT NULL,
    admin_id    BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_inquiry_answer PRIMARY KEY (id),
    CONSTRAINT uq_inquiry_answer_inquiry UNIQUE (inquiry_id),
    CONSTRAINT fk_inquiry_answer_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry (id) ON DELETE CASCADE,
    CONSTRAINT fk_inquiry_answer_admin   FOREIGN KEY (admin_id)   REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 7. comment
-- ============================================================
CREATE TABLE comment (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    board_type  ENUM('FREE', 'GALLERY') NOT NULL,
    post_id     BIGINT          NOT NULL,
    content     VARCHAR(1000)   NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
    -- post_id FK 미설정: board_type별 다른 테이블 참조 (Polymorphic Association)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 8. attachment
-- ============================================================
CREATE TABLE attachment (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    board_type      ENUM('FREE', 'GALLERY') NOT NULL,
    post_id         BIGINT          NOT NULL,
    original_name   VARCHAR(255)    NOT NULL,
    stored_name     VARCHAR(255)    NOT NULL,
    file_path       VARCHAR(500)    NOT NULL,
    file_size       BIGINT          NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_attachment PRIMARY KEY (id),
    CONSTRAINT uq_attachment_stored_name UNIQUE (stored_name)
    -- post_id FK 미설정: board_type별 다른 테이블 참조 (Polymorphic Association)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 인덱스
-- ============================================================

-- notice
CREATE INDEX idx_notice_is_pinned_created_at ON notice (is_pinned DESC, created_at DESC);
CREATE INDEX idx_notice_category              ON notice (category);
CREATE INDEX idx_notice_user_id               ON notice (user_id);

-- free_post
CREATE INDEX idx_free_post_created_at ON free_post (created_at DESC);
CREATE INDEX idx_free_post_category   ON free_post (category);
CREATE INDEX idx_free_post_user_id    ON free_post (user_id);

-- gallery_post
CREATE INDEX idx_gallery_post_created_at ON gallery_post (created_at DESC);
CREATE INDEX idx_gallery_post_category   ON gallery_post (category);
CREATE INDEX idx_gallery_post_user_id    ON gallery_post (user_id);

-- inquiry
CREATE INDEX idx_inquiry_created_at    ON inquiry (created_at DESC);
CREATE INDEX idx_inquiry_user_id       ON inquiry (user_id);
CREATE INDEX idx_inquiry_answer_status ON inquiry (answer_status);

-- comment
CREATE INDEX idx_comment_board_type_post_id ON comment (board_type, post_id);
CREATE INDEX idx_comment_user_id            ON comment (user_id);

-- attachment
CREATE INDEX idx_attachment_board_type_post_id_sort ON attachment (board_type, post_id, sort_order);
