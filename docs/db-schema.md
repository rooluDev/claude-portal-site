# 🗄️ DB Schema

## 김승현 포트폴리오 포털 사이트

---

## 목차

1. [테이블 목록 및 관계 요약](#1-테이블-목록-및-관계-요약)
2. [ERD](#2-erd)
3. [테이블 정의](#3-테이블-정의)
4. [DDL (CREATE TABLE)](#4-ddl-create-table)
5. [인덱스 설계](#5-인덱스-설계)
6. [설계 결정 사항](#6-설계-결정-사항)

---

## 1. 테이블 목록 및 관계 요약

| 테이블명          | 설명                                  | 비고                          |
| ----------------- | ------------------------------------- | ----------------------------- |
| `users`           | 회원 / 관리자 계정                    | role 컬럼으로 권한 구분       |
| `notice`          | 공지사항 게시글                       | 관리자 전용 작성              |
| `free_post`       | 자유게시판 게시글                     | 댓글 + 첨부파일 지원          |
| `gallery_post`    | 갤러리 게시글                         | 댓글 + 이미지 첨부 지원       |
| `inquiry`         | 문의게시판 게시글                     | 비밀글 + 답변 상태 관리       |
| `inquiry_answer`  | 문의 답변 (관리자 작성)               | inquiry와 1:1 관계            |
| `comment`         | 댓글 (자유게시판 / 갤러리 공통)       | board_type으로 게시판 구분    |
| `attachment`      | 첨부파일 (자유게시판 / 갤러리 공통)   | board_type으로 게시판 구분    |

### 관계 요약

```
users ──< notice           (1:N)  관리자가 공지사항 작성
users ──< free_post        (1:N)  회원이 자유게시판 작성
users ──< gallery_post     (1:N)  회원이 갤러리 작성
users ──< inquiry          (1:N)  회원이 문의 작성
users ──< inquiry_answer   (1:N)  관리자가 답변 작성
users ──< comment          (1:N)  회원이 댓글 작성
inquiry ──── inquiry_answer (1:1) 문의 1건당 답변 1건
free_post ──< comment      (1:N, board_type='FREE')
gallery_post ──< comment   (1:N, board_type='GALLERY')
free_post ──< attachment   (1:N, board_type='FREE')
gallery_post ──< attachment(1:N, board_type='GALLERY')
```

---

## 2. ERD

```
┌─────────────────────────┐
│          users          │
├─────────────────────────┤
│ PK id          BIGINT   │
│    username    VARCHAR  │◄──────────────────────────────────┐
│    password    VARCHAR  │                                   │
│    name        VARCHAR  │                                   │
│    role        ENUM     │                                   │
│    created_at  DATETIME │                                   │
└──────────┬──────────────┘                                   │
           │ 1                                                 │
     ┌─────┼──────────────────────────────────────┐           │
     │     │                                      │           │
     │ N   │ N                            N       │ N         │
┌────▼─────┴──┐  ┌─────────────┐  ┌──────┴────┐  │  ┌───────▼──────┐
│   notice    │  │  free_post  │  │gallery_post│  │  │   inquiry    │
├─────────────┤  ├─────────────┤  ├────────────┤  │  ├──────────────┤
│PK id        │  │PK id        │  │PK id       │  │  │PK id         │
│FK user_id   │  │FK user_id   │  │FK user_id  │  │  │FK user_id    │
│   category  │  │   category  │  │   category │  │  │   title      │
│   is_pinned │  │   title     │  │   title    │  │  │   content    │
│   title     │  │   content   │  │   content  │  │  │   is_secret  │
│   content   │  │   view_count│  │   view_count│ │  │   answer_status│
│   view_count│  │   created_at│  │   created_at│ │  │   view_count │
│   created_at│  │   updated_at│  │   updated_at│ │  │   created_at │
│   updated_at│  └──────┬──────┘  └─────┬──────┘ │  │   updated_at │
└─────────────┘         │ 1             │ 1       │  └──────┬───────┘
                        │               │         │         │ 1
              ┌─────────┴───────────────┘         │         │
              │ board_type='FREE'/'GALLERY'        │   ┌─────▼──────────────┐
              │                                   │   │  inquiry_answer    │
         ┌────▼────────────┐                      │   ├────────────────────┤
         │    comment      │                      │   │PK id               │
         ├─────────────────┤                      │   │FK inquiry_id(UNIQ) │
         │PK id            │                      │   │FK admin_id → users │
         │FK user_id ──────┼──────────────────────┘   │   content          │
         │   board_type    │                           │   created_at       │
         │   post_id       │                           │   updated_at       │
         │   content       │                           └────────────────────┘
         │   created_at    │
         └─────────────────┘

         ┌─────────────────┐
         │   attachment    │   (board_type='FREE' / 'GALLERY')
         ├─────────────────┤
         │PK id            │
         │   board_type    │
         │   post_id       │
         │   original_name │
         │   stored_name   │
         │   file_path     │
         │   file_size     │
         │   sort_order    │
         │   created_at    │
         └─────────────────┘
```

---

## 3. 테이블 정의

---

### 3.1 `users` — 사용자

| 컬럼명       | 타입              | 제약조건                        | 설명                      |
| ------------ | ----------------- | ------------------------------- | ------------------------- |
| `id`         | BIGINT            | PK, AUTO_INCREMENT              | 사용자 고유 ID            |
| `username`   | VARCHAR(20)       | NOT NULL, UNIQUE                | 로그인 아이디 (5~20자)    |
| `password`   | VARCHAR(255)      | NOT NULL                        | BCrypt 해시된 비밀번호    |
| `name`       | VARCHAR(20)       | NOT NULL                        | 표시 이름 (GNB 노출)      |
| `role`       | ENUM('USER','ADMIN') | NOT NULL, DEFAULT 'USER'     | 권한 (관리자는 DB 직접 삽입) |
| `created_at` | DATETIME          | NOT NULL, DEFAULT NOW()         | 가입일시                  |

---

### 3.2 `notice` — 공지사항

| 컬럼명       | 타입                      | 제약조건                    | 설명                                 |
| ------------ | ------------------------- | --------------------------- | ------------------------------------ |
| `id`         | BIGINT                    | PK, AUTO_INCREMENT          | 게시글 고유 ID                       |
| `user_id`    | BIGINT                    | NOT NULL, FK → users(id)    | 작성자 (관리자)                      |
| `category`   | ENUM('NOTICE','EVENT')    | NOT NULL                    | 분류 (공지 / 이벤트)                 |
| `is_pinned`  | TINYINT(1)                | NOT NULL, DEFAULT 0         | 고정 공지 여부 (1=고정)              |
| `title`      | VARCHAR(255)              | NOT NULL                    | 제목                                 |
| `content`    | TEXT                      | NOT NULL                    | 본문                                 |
| `view_count` | INT                       | NOT NULL, DEFAULT 0         | 조회수                               |
| `created_at` | DATETIME                  | NOT NULL, DEFAULT NOW()     | 작성일시                             |
| `updated_at` | DATETIME                  | NOT NULL, DEFAULT NOW() ON UPDATE NOW() | 수정일시             |

> 첨부파일 미지원 (PRD 4.4.2 참조)

---

### 3.3 `free_post` — 자유게시판

| 컬럼명       | 타입                      | 제약조건                    | 설명                        |
| ------------ | ------------------------- | --------------------------- | --------------------------- |
| `id`         | BIGINT                    | PK, AUTO_INCREMENT          | 게시글 고유 ID              |
| `user_id`    | BIGINT                    | NOT NULL, FK → users(id)    | 작성자                      |
| `category`   | ENUM('HUMOR','HOBBY')     | NOT NULL                    | 분류 (유머 / 취미)          |
| `title`      | VARCHAR(255)              | NOT NULL                    | 제목                        |
| `content`    | TEXT                      | NOT NULL                    | 본문                        |
| `view_count` | INT                       | NOT NULL, DEFAULT 0         | 조회수                      |
| `created_at` | DATETIME                  | NOT NULL, DEFAULT NOW()     | 작성일시                    |
| `updated_at` | DATETIME                  | NOT NULL, DEFAULT NOW() ON UPDATE NOW() | 수정일시    |

> 댓글: `comment` 테이블 (board_type='FREE')
> 첨부파일: `attachment` 테이블 (board_type='FREE')

---

### 3.4 `gallery_post` — 갤러리

| 컬럼명       | 타입                         | 제약조건                    | 설명                        |
| ------------ | ---------------------------- | --------------------------- | --------------------------- |
| `id`         | BIGINT                       | PK, AUTO_INCREMENT          | 게시글 고유 ID              |
| `user_id`    | BIGINT                       | NOT NULL, FK → users(id)    | 작성자                      |
| `category`   | ENUM('FOOD','CELEBRITY')     | NOT NULL                    | 분류 (음식 / 연예인)        |
| `title`      | VARCHAR(255)                 | NOT NULL                    | 제목                        |
| `content`    | TEXT                         | NOT NULL                    | 본문                        |
| `view_count` | INT                          | NOT NULL, DEFAULT 0         | 조회수                      |
| `created_at` | DATETIME                     | NOT NULL, DEFAULT NOW()     | 작성일시                    |
| `updated_at` | DATETIME                     | NOT NULL, DEFAULT NOW() ON UPDATE NOW() | 수정일시    |

> 댓글: `comment` 테이블 (board_type='GALLERY')
> 첨부(이미지): `attachment` 테이블 (board_type='GALLERY', sort_order 활용)

---

### 3.5 `inquiry` — 문의게시판

| 컬럼명          | 타입                           | 제약조건                    | 설명                                     |
| --------------- | ------------------------------ | --------------------------- | ---------------------------------------- |
| `id`            | BIGINT                         | PK, AUTO_INCREMENT          | 게시글 고유 ID                           |
| `user_id`       | BIGINT                         | NOT NULL, FK → users(id)    | 작성자 (회원)                            |
| `title`         | VARCHAR(255)                   | NOT NULL                    | 제목                                     |
| `content`       | TEXT                           | NOT NULL                    | 본문                                     |
| `is_secret`     | TINYINT(1)                     | NOT NULL, DEFAULT 0         | 비밀글 여부 (1=비밀글)                   |
| `answer_status` | ENUM('PENDING','ANSWERED')     | NOT NULL, DEFAULT 'PENDING' | 답변 상태 (미답변 / 답변완료)            |
| `view_count`    | INT                            | NOT NULL, DEFAULT 0         | 조회수                                   |
| `created_at`    | DATETIME                       | NOT NULL, DEFAULT NOW()     | 작성일시                                 |
| `updated_at`    | DATETIME                       | NOT NULL, DEFAULT NOW() ON UPDATE NOW() | 수정일시                     |

> 답변: `inquiry_answer` 테이블 (1:1)
> 첨부파일 미지원 (PRD 2.3 참조)
> 댓글 미지원 (PRD 4.7.3 참조)

---

### 3.6 `inquiry_answer` — 문의 답변

| 컬럼명       | 타입         | 제약조건                              | 설명                                  |
| ------------ | ------------ | ------------------------------------- | ------------------------------------- |
| `id`         | BIGINT       | PK, AUTO_INCREMENT                    | 답변 고유 ID                          |
| `inquiry_id` | BIGINT       | NOT NULL, FK → inquiry(id), UNIQUE    | 연결된 문의글 (1:1, 삭제 시 CASCADE)  |
| `admin_id`   | BIGINT       | NOT NULL, FK → users(id)              | 답변 작성 관리자                      |
| `content`    | TEXT         | NOT NULL                              | 답변 내용                             |
| `created_at` | DATETIME     | NOT NULL, DEFAULT NOW()               | 답변 등록일시                         |
| `updated_at` | DATETIME     | NOT NULL, DEFAULT NOW() ON UPDATE NOW() | 답변 수정일시                       |

> `inquiry_id`에 UNIQUE 제약으로 문의 1건당 답변 1건만 보장
> 답변 삭제 시 inquiry.answer_status → 'PENDING' 복귀 (Service 처리)

---

### 3.7 `comment` — 댓글

| 컬럼명       | 타입                      | 제약조건                    | 설명                                        |
| ------------ | ------------------------- | --------------------------- | ------------------------------------------- |
| `id`         | BIGINT                    | PK, AUTO_INCREMENT          | 댓글 고유 ID                                |
| `user_id`    | BIGINT                    | NOT NULL, FK → users(id)    | 작성자                                      |
| `board_type` | ENUM('FREE','GALLERY')    | NOT NULL                    | 게시판 구분 (자유게시판 / 갤러리)           |
| `post_id`    | BIGINT                    | NOT NULL                    | 게시글 ID (board_type에 따라 참조 테이블 다름) |
| `content`    | VARCHAR(1000)             | NOT NULL                    | 댓글 내용 (최대 1000자)                     |
| `created_at` | DATETIME                  | NOT NULL, DEFAULT NOW()     | 작성일시                                    |

> `post_id`는 FK 제약 없음 (Polymorphic Association)
> 게시글 삭제 시 관련 댓글 삭제는 Service 레이어에서 처리
> 댓글 수정 기능 미지원 (PRD 상 삭제만 가능)

---

### 3.8 `attachment` — 첨부파일

| 컬럼명          | 타입                     | 제약조건                | 설명                                          |
| --------------- | ------------------------ | ----------------------- | --------------------------------------------- |
| `id`            | BIGINT                   | PK, AUTO_INCREMENT      | 첨부파일 고유 ID                              |
| `board_type`    | ENUM('FREE','GALLERY')   | NOT NULL                | 게시판 구분                                   |
| `post_id`       | BIGINT                   | NOT NULL                | 게시글 ID (board_type에 따라 참조 테이블 다름) |
| `original_name` | VARCHAR(255)             | NOT NULL                | 원본 파일명 (사용자가 올린 이름)              |
| `stored_name`   | VARCHAR(255)             | NOT NULL, UNIQUE        | 서버 저장 파일명 ({UUID}-original.ext)        |
| `file_path`     | VARCHAR(500)             | NOT NULL                | 서버 내 저장 경로 (/uploads/gallery/UUID.jpg) |
| `file_size`     | BIGINT                   | NOT NULL                | 파일 크기 (bytes)                             |
| `sort_order`    | INT                      | NOT NULL, DEFAULT 0     | 갤러리 이미지 순서 (캐러셀 순서 결정)         |
| `created_at`    | DATETIME                 | NOT NULL, DEFAULT NOW() | 업로드일시                                    |

> `sort_order`: 갤러리 이미지 캐러셀 순서 관리용. 자유게시판은 0 고정.
> `post_id`는 FK 제약 없음 (Polymorphic Association)
> 게시글 삭제 시 관련 파일 삭제는 Service 레이어에서 처리

---

## 4. DDL (CREATE TABLE)

```sql
-- ============================================================
-- 데이터베이스 생성
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
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         TEXT        NOT NULL,
    is_secret       TINYINT(1)  NOT NULL DEFAULT 0,
    answer_status   ENUM('PENDING', 'ANSWERED') NOT NULL DEFAULT 'PENDING',
    view_count      INT         NOT NULL DEFAULT 0,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

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
    -- post_id FK 미설정: board_type별 다른 테이블 참조 (Polymorphic)
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
    -- post_id FK 미설정: board_type별 다른 테이블 참조 (Polymorphic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 5. 인덱스 설계

```sql
-- ============================================================
-- notice 인덱스
-- ============================================================
-- 목록 조회: 고정글 상단 정렬 + 날짜 필터 + 정렬
CREATE INDEX idx_notice_is_pinned_created_at ON notice (is_pinned DESC, created_at DESC);

-- 카테고리 필터
CREATE INDEX idx_notice_category ON notice (category);

-- 작성자 조회 (관리자용)
CREATE INDEX idx_notice_user_id ON notice (user_id);


-- ============================================================
-- free_post 인덱스
-- ============================================================
-- 목록 조회: 날짜 필터 + 정렬
CREATE INDEX idx_free_post_created_at ON free_post (created_at DESC);

-- 카테고리 필터
CREATE INDEX idx_free_post_category ON free_post (category);

-- 본인 글 조회
CREATE INDEX idx_free_post_user_id ON free_post (user_id);


-- ============================================================
-- gallery_post 인덱스
-- ============================================================
-- 목록 조회: 날짜 필터 + 정렬
CREATE INDEX idx_gallery_post_created_at ON gallery_post (created_at DESC);

-- 카테고리 필터
CREATE INDEX idx_gallery_post_category ON gallery_post (category);

-- 본인 글 조회
CREATE INDEX idx_gallery_post_user_id ON gallery_post (user_id);


-- ============================================================
-- inquiry 인덱스
-- ============================================================
-- 목록 조회: 날짜 필터 + 정렬
CREATE INDEX idx_inquiry_created_at ON inquiry (created_at DESC);

-- 나의 문의내역 필터 (?my=true)
CREATE INDEX idx_inquiry_user_id ON inquiry (user_id);

-- 답변 상태 필터
CREATE INDEX idx_inquiry_answer_status ON inquiry (answer_status);


-- ============================================================
-- comment 인덱스
-- ============================================================
-- 댓글 목록 조회: 게시판 + 게시글 ID 복합 인덱스 (가장 빈번한 쿼리)
CREATE INDEX idx_comment_board_type_post_id ON comment (board_type, post_id);

-- 본인 댓글 삭제 시 user_id 조회
CREATE INDEX idx_comment_user_id ON comment (user_id);


-- ============================================================
-- attachment 인덱스
-- ============================================================
-- 첨부파일 목록 조회: 게시판 + 게시글 ID 복합 인덱스
-- sort_order 포함으로 갤러리 캐러셀 정렬 쿼리 최적화
CREATE INDEX idx_attachment_board_type_post_id_sort ON attachment (board_type, post_id, sort_order);
```

---

## 6. 설계 결정 사항

### 6.1 게시판별 분리 테이블 vs 단일 통합 테이블

**선택: 게시판별 분리 테이블** (`notice`, `free_post`, `gallery_post`, `inquiry`)

| 항목 | 분리 테이블 | 통합 테이블 |
|------|-------------|-------------|
| 게시판별 고유 컬럼 | 명확히 표현 가능 (is_pinned, is_secret 등) | NULL 컬럼 다수 발생 |
| JPQL 쿼리 복잡도 | 단순 | board_type 조건 추가 필요 |
| 확장성 | 게시판별 독립 확장 용이 | 한 테이블에 혼재 |
| 이 프로젝트 규모 | 적합 | 과도한 추상화 |

> `notice`의 `is_pinned`, `inquiry`의 `is_secret` / `answer_status` 같은 게시판 고유 컬럼이 존재하므로 분리 방식이 적합하다.

---

### 6.2 댓글 / 첨부파일의 Polymorphic Association

`comment`와 `attachment`는 자유게시판과 갤러리 두 게시판에서 공통으로 사용한다.

**선택: `board_type` + `post_id` 컬럼으로 구분 (DB FK 없음)**

```sql
-- 자유게시판 게시글 1번의 댓글 조회
SELECT * FROM comment WHERE board_type = 'FREE' AND post_id = 1;

-- 갤러리 게시글 3번의 첨부파일 조회 (이미지 순서 포함)
SELECT * FROM attachment WHERE board_type = 'GALLERY' AND post_id = 3 ORDER BY sort_order;
```

- DB 레벨 FK 대신 **Service 레이어에서 무결성 보장**
- 게시글 삭제 시 → Service에서 `comment`, `attachment` 먼저 삭제 후 게시글 삭제

---

### 6.3 inquiry_answer ON DELETE CASCADE

`inquiry_answer.inquiry_id`에 `ON DELETE CASCADE` 적용.

- 문의글 삭제 시 답변도 자동 삭제
- Service에서 문의글 삭제 전 별도 답변 삭제 코드 불필요
- 단, `inquiry.answer_status`를 'PENDING'으로 되돌리는 처리는 **답변 삭제 API**에서만 필요 (문의글 삭제 시에는 불필요)

---

### 6.4 notice 테이블에 첨부파일 미포함

PRD 검토 결과:
- 공지사항 작성 폼 (4.4.2): 첨부파일 필드 없음
- 공지사항 수정 페이지 (4.4.4): 수정 가능 항목에 첨부파일 없음

→ `attachment` 테이블의 `board_type` ENUM에 `'NOTICE'` 미포함

---

### 6.5 카테고리 값 매핑표

| 게시판 | DB ENUM 값 | 화면 표시 (한국어) |
|--------|------------|-------------------|
| 공지사항 | `NOTICE` | 공지 |
| 공지사항 | `EVENT` | 이벤트 |
| 자유게시판 | `HUMOR` | 유머 |
| 자유게시판 | `HOBBY` | 취미 |
| 갤러리 | `FOOD` | 음식 |
| 갤러리 | `CELEBRITY` | 연예인 |
| 문의게시판 | (카테고리 없음) | - |

---

### 6.6 삭제 전략: Hard Delete

회원 탈퇴 기능 미지원, 게시글 복구 기능 미지원 → 모두 **Hard Delete** 적용.

삭제 순서 (Service 레이어 책임):

```
자유게시판 글 삭제:
  1. attachment 삭제 (로컬 파일 + DB)
  2. comment 삭제 (DB)
  3. free_post 삭제

갤러리 글 삭제:
  1. attachment 삭제 (로컬 파일 + DB)
  2. comment 삭제 (DB)
  3. gallery_post 삭제

문의게시판 글 삭제:
  1. inquiry 삭제
     → inquiry_answer: ON DELETE CASCADE 자동 삭제
```

---

### 6.7 초기 데이터 (관리자 계정)

관리자 계정은 DB에 직접 삽입 (PRD 2.1 참조).

```sql
-- 비밀번호는 BCrypt 해시값으로 삽입
-- 예: 'admin1234' → BCrypt 해시
INSERT INTO users (username, password, name, role)
VALUES (
    'admin',
    '$2a$10$...BCrypt해시값...',
    '관리자',
    'ADMIN'
);
```
