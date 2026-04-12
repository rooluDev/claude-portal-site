# 🏗️ Architecture

## 김승현 포트폴리오 포털 사이트

---

## 목차

1. [전체 시스템 구조](#1-전체-시스템-구조)
2. [Frontend Architecture](#2-frontend-architecture)
3. [Backend Architecture](#3-backend-architecture)
4. [인증 / 인가 설계](#4-인증--인가-설계)
5. [파일 업로드 설계](#5-파일-업로드-설계)
6. [조회수 중복 방지 설계](#6-조회수-중복-방지-설계)
7. [데이터 흐름 요약](#7-데이터-흐름-요약)

---

## 1. 전체 시스템 구조

```
┌─────────────────────────────────────────────────────┐
│                      Browser                        │
│                                                     │
│   Vue.js 3 SPA (sessionStorage: JWT)                │
│   - Axios (API 요청 / JWT 헤더 자동 주입)            │
│   - Vue Router (페이지 라우팅 / 가드)                │
│   - Pinia (전역 상태: 인증 정보)                     │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/REST (JSON)
                       │ Authorization: Bearer {JWT}
┌──────────────────────▼──────────────────────────────┐
│               Spring Boot 3 (Backend)               │
│                                                     │
│   ┌─────────────┐  ┌──────────────┐  ┌──────────┐  │
│   │   Security  │  │  Controller  │  │  Filter  │  │
│   │   (JWT 검증) │  │  (REST API)  │  │  (CORS)  │  │
│   └──────┬──────┘  └──────┬───────┘  └──────────┘  │
│          │                │                         │
│   ┌──────▼────────────────▼──────────────────────┐  │
│   │              Service Layer                   │  │
│   │  (비즈니스 로직 / 권한 검사 / 조회수 처리)     │  │
│   └───────────────────────┬──────────────────────┘  │
│                           │                         │
│   ┌───────────────────────▼──────────────────────┐  │
│   │            Repository Layer (JPA)            │  │
│   └───────────────────────┬──────────────────────┘  │
│                           │                         │
│   ┌───────────────────────▼──────────────────────┐  │
│   │          Local File System (업로드)           │  │
│   └──────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────┘
                       │ JDBC / JPA
┌──────────────────────▼──────────────────────────────┐
│                  MySQL 8 Database                   │
│                                                     │
│   users / posts / comments / attachments /          │
│   inquiry_answers / view_logs                       │
└─────────────────────────────────────────────────────┘
```

### 포트 구성

| 구분      | 포트   | 비고                        |
| --------- | ------ | --------------------------- |
| Frontend  | 5173   | Vite dev server (개발 환경) |
| Backend   | 8080   | Spring Boot embedded Tomcat |
| MySQL     | 3306   | 기본 포트                   |

---

## 2. Frontend Architecture

### 2.1 기술 스택

| 라이브러리    | 용도                           |
| ------------- | ------------------------------ |
| Vue.js 3      | UI 프레임워크 (Composition API)|
| Vue Router 4  | SPA 라우팅 / 네비게이션 가드   |
| Pinia         | 전역 상태 관리 (인증 정보)     |
| Axios         | HTTP 클라이언트 / 인터셉터     |
| Vite          | 빌드 도구                      |

---

### 2.2 디렉토리 구조

```
frontend/
├── public/
├── src/
│   ├── main.js                  # 앱 진입점
│   ├── App.vue                  # 루트 컴포넌트 (GNB 포함)
│   │
│   ├── router/
│   │   └── index.js             # 라우트 정의 + 네비게이션 가드
│   │
│   ├── stores/
│   │   └── auth.js              # Pinia 인증 스토어 (JWT, user 정보)
│   │
│   ├── api/
│   │   ├── axios.js             # Axios 인스턴스 + 인터셉터 설정
│   │   ├── auth.js              # 로그인 / 회원가입 API
│   │   ├── notice.js            # 공지사항 API
│   │   ├── free.js              # 자유게시판 API
│   │   ├── gallery.js           # 갤러리 API
│   │   ├── inquiry.js           # 문의게시판 API
│   │   └── comment.js           # 댓글 API
│   │
│   ├── views/
│   │   ├── HomeView.vue                      # 메인 페이지 (/)
│   │   ├── auth/
│   │   │   ├── LoginView.vue                 # 로그인 페이지 (/login)
│   │   │   └── JoinView.vue                  # 회원가입 페이지 (/join)
│   │   ├── notice/
│   │   │   ├── NoticeListView.vue            # 공지사항 목록
│   │   │   ├── NoticeDetailView.vue          # 공지사항 상세
│   │   │   ├── NoticeWriteView.vue           # 공지사항 작성 (관리자)
│   │   │   └── NoticeModifyView.vue          # 공지사항 수정 (관리자)
│   │   ├── free/
│   │   │   ├── FreeListView.vue              # 자유게시판 목록
│   │   │   ├── FreeDetailView.vue            # 자유게시판 상세
│   │   │   ├── FreeWriteView.vue             # 자유게시판 작성
│   │   │   └── FreeModifyView.vue            # 자유게시판 수정
│   │   ├── gallery/
│   │   │   ├── GalleryListView.vue           # 갤러리 목록
│   │   │   ├── GalleryDetailView.vue         # 갤러리 상세
│   │   │   ├── GalleryWriteView.vue          # 갤러리 작성
│   │   │   └── GalleryModifyView.vue         # 갤러리 수정
│   │   └── inquiry/
│   │       ├── InquiryListView.vue           # 문의게시판 목록
│   │       ├── InquiryDetailView.vue         # 문의게시판 상세
│   │       ├── InquiryWriteView.vue          # 문의게시판 작성
│   │       └── InquiryModifyView.vue         # 문의게시판 수정
│   │
│   └── components/
│       ├── layout/
│       │   └── GnbHeader.vue                # GNB (전 페이지 공유)
│       ├── common/
│       │   ├── BoardFilter.vue              # 게시판 공통 검색/필터 패널
│       │   ├── Pagination.vue               # 페이지네이션
│       │   ├── LoginModal.vue               # 로그인 유도 모달
│       │   ├── ConfirmDialog.vue            # 삭제 confirm 다이얼로그
│       │   └── ToastMessage.vue             # 토스트 알림
│       ├── notice/
│       │   └── NoticeWidget.vue             # 메인 공지사항 위젯
│       ├── free/
│       │   ├── FreeWidget.vue               # 메인 자유게시판 위젯
│       │   ├── CommentList.vue              # 댓글 목록
│       │   └── CommentInput.vue             # 댓글 입력
│       ├── gallery/
│       │   ├── GalleryWidget.vue            # 메인 갤러리 위젯
│       │   ├── ImageCarousel.vue            # 이미지 슬라이더
│       │   └── ImageUploader.vue            # 이미지 업로드 컴포넌트
│       ├── inquiry/
│       │   ├── InquiryWidget.vue            # 메인 문의게시판 위젯
│       │   └── InquiryAnswer.vue            # 관리자 답변 영역
│       └── file/
│           └── FileAttachment.vue           # 자유게시판 파일 첨부 컴포넌트
│
├── index.html
├── vite.config.js
└── package.json
```

---

### 2.3 라우팅 구조 및 네비게이션 가드

```js
// router/index.js 구조 개요

const routes = [
  { path: '/',                           component: HomeView },

  // 인증 (로그인 상태면 / 로 리다이렉트)
  { path: '/login',                      component: LoginView,          meta: { guestOnly: true } },
  { path: '/join',                       component: JoinView,           meta: { guestOnly: true } },

  // 공지사항 (전체 열람 가능 / 작성·수정은 관리자만)
  { path: '/boards/notice',              component: NoticeListView },
  { path: '/boards/notice/:id',          component: NoticeDetailView },
  { path: '/boards/notice/write',        component: NoticeWriteView,    meta: { requiresAdmin: true } },
  { path: '/boards/notice/modify/:id',   component: NoticeModifyView,   meta: { requiresAdmin: true } },

  // 자유게시판 (전체 열람 / 작성·수정은 회원 이상)
  { path: '/boards/free',                component: FreeListView },
  { path: '/boards/free/:id',            component: FreeDetailView },
  { path: '/boards/free/write',          component: FreeWriteView,      meta: { requiresAuth: true } },
  { path: '/boards/free/modify/:id',     component: FreeModifyView,     meta: { requiresAuth: true } },

  // 갤러리 (전체 열람 / 작성·수정은 회원 이상)
  { path: '/boards/gallery',             component: GalleryListView },
  { path: '/boards/gallery/:id',         component: GalleryDetailView },
  { path: '/boards/gallery/write',       component: GalleryWriteView,   meta: { requiresAuth: true } },
  { path: '/boards/gallery/modify/:id',  component: GalleryModifyView,  meta: { requiresAuth: true } },

  // 문의게시판 (전체 열람 / 작성·수정은 회원 이상)
  { path: '/boards/inquiry',             component: InquiryListView },
  { path: '/boards/inquiry/:id',         component: InquiryDetailView },
  { path: '/boards/inquiry/write',       component: InquiryWriteView,   meta: { requiresAuth: true } },
  { path: '/boards/inquiry/modify/:id',  component: InquiryModifyView,  meta: { requiresAuth: true } },
]

// 네비게이션 가드 처리 규칙
// meta.guestOnly  → 로그인 상태면 /로 리다이렉트
// meta.requiresAuth  → 비로그인이면 로그인 모달 표시 (페이지 이동 차단)
// meta.requiresAdmin → 관리자가 아니면 / 로 리다이렉트
```

---

### 2.4 Pinia 인증 스토어

```js
// stores/auth.js 구조 개요

state: {
  token: null,       // sessionStorage에서 로드한 JWT
  user: {
    id: null,        // 사용자 DB id
    username: null,  // 아이디
    name: null,      // 이름
    role: null,      // 'USER' | 'ADMIN'
  }
}

getters: {
  isLoggedIn,        // token !== null
  isAdmin,           // role === 'ADMIN'
  isUser,            // role === 'USER'
}

actions: {
  login(token, user),   // JWT 저장 + 상태 설정
  logout(),             // JWT 삭제 + 상태 초기화
  loadFromStorage(),    // 앱 초기화 시 sessionStorage에서 JWT 복원
}
```

---

### 2.5 Axios 인터셉터

```
[Request Interceptor]
모든 요청 전 → sessionStorage에서 JWT 읽어 Authorization: Bearer {token} 헤더 자동 주입

[Response Interceptor]
401 Unauthorized 응답 수신 시
├── sessionStorage JWT 삭제
├── Pinia auth 상태 초기화 (GNB 비회원 전환)
└── 현재 페이지가 공개 페이지면 로그인 모달 팝업
    현재 페이지가 보호 페이지면 /login?ret={현재경로} 리다이렉트
```

---

## 3. Backend Architecture

### 3.1 레이어 구조

```
┌─────────────────────────────────────────────┐
│         Presentation Layer                  │
│  Controller (REST API 엔드포인트 정의)       │
│  @RestController, @RequestMapping           │
└───────────────────────┬─────────────────────┘
                        │ DTO
┌───────────────────────▼─────────────────────┐
│           Business Layer                    │
│  Service (비즈니스 로직, 권한 검사)           │
│  @Service, @Transactional                   │
└───────────────────────┬─────────────────────┘
                        │ Entity / DTO
┌───────────────────────▼─────────────────────┐
│         Persistence Layer                   │
│  Repository (JPA / JPQL 쿼리)               │
│  JpaRepository, @Query                      │
└───────────────────────┬─────────────────────┘
                        │ SQL
┌───────────────────────▼─────────────────────┐
│              MySQL 8                        │
└─────────────────────────────────────────────┘
```

---

### 3.2 패키지 구조

```
backend/
└── src/main/java/com/portfolio/
    │
    ├── PortfolioApplication.java          # 앱 진입점
    │
    ├── config/
    │   ├── SecurityConfig.java            # Spring Security 설정 (JWT 필터 등록)
    │   ├── CorsConfig.java                # CORS 설정 (Vue 개발서버 허용)
    │   └── WebMvcConfig.java              # 정적 파일(업로드) 경로 매핑
    │
    ├── auth/
    │   ├── controller/
    │   │   └── AuthController.java        # POST /api/auth/login, /api/auth/join
    │   ├── service/
    │   │   └── AuthService.java
    │   ├── dto/
    │   │   ├── LoginRequestDto.java
    │   │   ├── LoginResponseDto.java      # JWT 포함
    │   │   └── JoinRequestDto.java
    │   └── jwt/
    │       ├── JwtProvider.java           # JWT 생성 / 검증 / 클레임 추출
    │       └── JwtAuthFilter.java         # OncePerRequestFilter: 요청마다 JWT 검증
    │
    ├── user/
    │   ├── entity/
    │   │   └── User.java                  # id, username, password, name, role
    │   ├── repository/
    │   │   └── UserRepository.java
    │   └── enums/
    │       └── Role.java                  # USER, ADMIN
    │
    ├── board/
    │   ├── common/
    │   │   ├── dto/
    │   │   │   ├── BoardListRequestDto.java   # 공통 검색 파라미터 (startDate~pageNum)
    │   │   │   └── PageResponseDto.java       # 페이지네이션 응답 래퍼
    │   │   └── service/
    │   │       └── ViewCountService.java      # 조회수 중복 방지 처리
    │   │
    │   ├── notice/
    │   │   ├── controller/NoticeController.java
    │   │   ├── service/NoticeService.java
    │   │   ├── repository/NoticeRepository.java
    │   │   ├── entity/Notice.java             # id, category, isPinned, title, content, viewCount, createdAt, user
    │   │   └── dto/
    │   │       ├── NoticeListResponseDto.java
    │   │       ├── NoticeDetailResponseDto.java
    │   │       └── NoticeWriteRequestDto.java
    │   │
    │   ├── free/
    │   │   ├── controller/FreeController.java
    │   │   ├── service/FreeService.java
    │   │   ├── repository/FreeRepository.java
    │   │   ├── entity/Free.java               # id, category, title, content, viewCount, createdAt, user
    │   │   └── dto/ ...
    │   │
    │   ├── gallery/
    │   │   ├── controller/GalleryController.java
    │   │   ├── service/GalleryService.java
    │   │   ├── repository/GalleryRepository.java
    │   │   ├── entity/Gallery.java             # id, category, title, content, viewCount, createdAt, user
    │   │   └── dto/ ...
    │   │
    │   └── inquiry/
    │       ├── controller/InquiryController.java
    │       ├── service/InquiryService.java
    │       ├── repository/InquiryRepository.java
    │       ├── entity/
    │       │   ├── Inquiry.java                # id, title, content, isSecret, answerStatus, viewCount, createdAt, user
    │       │   └── InquiryAnswer.java          # id, content, createdAt, updatedAt, inquiry, admin
    │       └── dto/ ...
    │
    ├── comment/
    │   ├── controller/CommentController.java
    │   ├── service/CommentService.java
    │   ├── repository/CommentRepository.java
    │   ├── entity/Comment.java                 # id, content, boardType, postId, createdAt, user
    │   └── dto/ ...
    │
    ├── attachment/
    │   ├── controller/AttachmentController.java  # 파일 서빙 (GET /api/files/{filename})
    │   ├── service/AttachmentService.java         # 파일 저장 / 삭제 처리
    │   ├── repository/AttachmentRepository.java
    │   └── entity/Attachment.java                # id, originalName, storedName, filePath, boardType, postId
    │
    └── common/
        ├── exception/
        │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice
        │   ├── CustomException.java
        │   └── ErrorCode.java                 # 에러 코드 enum
        └── response/
            └── ApiResponse.java               # 공통 응답 래퍼 { success, message, data }
```

---

### 3.3 REST API 엔드포인트 목록

#### 인증

| Method | URI                  | 권한   | 설명          |
| ------ | -------------------- | ------ | ------------- |
| POST   | `/api/auth/join`     | 전체   | 회원가입      |
| POST   | `/api/auth/login`    | 전체   | 로그인 (JWT 발급) |

#### 공지사항

| Method | URI                          | 권한      | 설명          |
| ------ | ---------------------------- | --------- | ------------- |
| GET    | `/api/notice`                | 전체      | 목록 조회     |
| GET    | `/api/notice/{id}`           | 전체      | 상세 조회     |
| POST   | `/api/notice`                | ADMIN     | 작성          |
| PUT    | `/api/notice/{id}`           | ADMIN     | 수정          |
| DELETE | `/api/notice/{id}`           | ADMIN     | 삭제          |

#### 자유게시판

| Method | URI                          | 권한         | 설명          |
| ------ | ---------------------------- | ------------ | ------------- |
| GET    | `/api/free`                  | 전체         | 목록 조회     |
| GET    | `/api/free/{id}`             | 전체         | 상세 조회     |
| POST   | `/api/free`                  | USER / ADMIN | 작성          |
| PUT    | `/api/free/{id}`             | USER(본인) / ADMIN | 수정   |
| DELETE | `/api/free/{id}`             | USER(본인) / ADMIN | 삭제   |

#### 갤러리

| Method | URI                          | 권한         | 설명          |
| ------ | ---------------------------- | ------------ | ------------- |
| GET    | `/api/gallery`               | 전체         | 목록 조회     |
| GET    | `/api/gallery/{id}`          | 전체         | 상세 조회     |
| POST   | `/api/gallery`               | USER / ADMIN | 작성          |
| PUT    | `/api/gallery/{id}`          | USER(본인) / ADMIN | 수정   |
| DELETE | `/api/gallery/{id}`          | USER(본인) / ADMIN | 삭제   |

#### 문의게시판

| Method | URI                               | 권한              | 설명               |
| ------ | --------------------------------- | ----------------- | ------------------ |
| GET    | `/api/inquiry`                    | 전체              | 목록 조회          |
| GET    | `/api/inquiry/{id}`               | 전체 (비밀글 제한)| 상세 조회          |
| POST   | `/api/inquiry`                    | USER / ADMIN      | 작성               |
| PUT    | `/api/inquiry/{id}`               | USER(본인, 미답변) | 수정              |
| DELETE | `/api/inquiry/{id}`               | USER(본인) / ADMIN | 삭제              |
| POST   | `/api/inquiry/{id}/answer`        | ADMIN             | 답변 등록          |
| PUT    | `/api/inquiry/{id}/answer`        | ADMIN             | 답변 수정          |
| DELETE | `/api/inquiry/{id}/answer`        | ADMIN             | 답변 삭제          |

#### 댓글

| Method | URI                              | 권한              | 설명          |
| ------ | -------------------------------- | ----------------- | ------------- |
| GET    | `/api/comments?boardType=&postId=` | 전체            | 댓글 목록 조회|
| POST   | `/api/comments`                  | USER / ADMIN      | 댓글 등록     |
| DELETE | `/api/comments/{id}`             | USER(본인) / ADMIN | 댓글 삭제    |

#### 파일

| Method | URI                              | 권한   | 설명             |
| ------ | -------------------------------- | ------ | ---------------- |
| GET    | `/api/files/{filename}`          | 전체   | 파일 서빙 (다운로드 / 이미지) |

---

### 3.4 공통 응답 형식

**성공 응답**
```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": { ... }
}
```

**실패 응답**
```json
{
  "success": false,
  "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

**페이지네이션 응답 (목록 API)**
```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "content": [ ... ],
    "totalCount": 87,
    "totalPages": 9,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

---

## 4. 인증 / 인가 설계

### 4.1 로그인 흐름

```
[Frontend]                          [Backend]
    │                                   │
    │  POST /api/auth/login             │
    │  { username, password }           │
    ├──────────────────────────────────►│
    │                                   │ AuthService
    │                                   │  - username으로 User 조회
    │                                   │  - BCrypt 비밀번호 검증
    │                                   │  - JwtProvider.generateToken(user)
    │                                   │    payload: { sub: userId, role: "USER", exp: +2h }
    │◄──────────────────────────────────┤
    │  200 OK                           │
    │  { token, user: { name, role } }  │
    │                                   │
    │  sessionStorage.setItem('token')  │
    │  Pinia.auth.login(token, user)    │
    │  GNB → 회원 상태 렌더링            │
```

### 4.2 인증된 요청 흐름

```
[Frontend]                          [Backend - JwtAuthFilter]
    │                                   │
    │  GET /api/free/1                  │
    │  Authorization: Bearer {JWT}      │
    ├──────────────────────────────────►│
    │                                   │ JwtAuthFilter.doFilterInternal()
    │                                   │  1. Authorization 헤더 추출
    │                                   │  2. JwtProvider.validateToken(token)
    │                                   │     - 서명 검증
    │                                   │     - 만료 시간 검증
    │                                   │  3. 검증 성공 → SecurityContext에 인증 객체 설정
    │                                   │  4. Controller 진입
    │◄──────────────────────────────────┤
    │  200 OK { data: ... }             │
```

### 4.3 JWT 만료 처리 흐름

```
[Frontend]                          [Backend]
    │                                   │
    │  (만료된 JWT로 요청)               │
    ├──────────────────────────────────►│
    │                                   │ JwtAuthFilter
    │                                   │  validateToken() → ExpiredJwtException
    │                                   │  SecurityContext 설정 없이 통과
    │                                   │
    │                                   │ Controller / Security
    │                                   │  - 보호된 API → 401 Unauthorized
    │◄──────────────────────────────────┤
    │  401 Unauthorized                 │
    │                                   │
    │ [Axios Response Interceptor]      │
    │  - sessionStorage JWT 삭제        │
    │  - Pinia 상태 초기화              │
    │  - 공개 페이지: 로그인 모달        │
    │  - 보호 페이지: /login?ret=... 이동│
```

### 4.4 권한 계층 및 접근 제어

```
[Spring Security 설정]

permitAll()  →  GET /api/notice/**
                GET /api/free/**
                GET /api/gallery/**
                GET /api/inquiry/**    (비밀글은 Service 레이어에서 추가 검증)
                GET /api/files/**
                POST /api/auth/**

hasAnyRole(USER, ADMIN)  →  POST /api/free
                             PUT  /api/free/{id}
                             DELETE /api/free/{id}
                             POST /api/gallery, PUT, DELETE
                             POST /api/inquiry, PUT, DELETE
                             POST /api/comments, DELETE

hasRole(ADMIN)  →  POST /api/notice
                   PUT  /api/notice/{id}
                   DELETE /api/notice/{id}
                   POST /api/inquiry/{id}/answer
                   PUT  /api/inquiry/{id}/answer
                   DELETE /api/inquiry/{id}/answer

[Service 레이어 추가 검증]
- 자유게시판 / 갤러리 수정·삭제: 본인(USER) 또는 ADMIN만 허용
- 문의게시판 수정: 본인 + 미답변 상태만 허용
- 문의게시판 비밀글 조회: 본인 또는 ADMIN만 허용
```

---

## 5. 파일 업로드 설계

### 5.1 업로드 흐름

```
[Frontend]                          [Backend]
    │                                   │
    │  POST /api/gallery                │
    │  Content-Type: multipart/form-data│
    │  { category, title, content,      │
    │    files: [img1.jpg, img2.png] }  │
    ├──────────────────────────────────►│
    │                                   │ GalleryController
    │                                   │  - @RequestPart로 JSON DTO + 파일 분리 수신
    │                                   │
    │                                   │ AttachmentService.save(files, boardType, postId)
    │                                   │  1. 파일 확장자 / 용량 검증
    │                                   │  2. UUID 기반 저장 파일명 생성
    │                                   │     (예: a1b2c3d4-original.jpg)
    │                                   │  3. 로컬 디렉토리에 저장
    │                                   │     /uploads/{boardType}/{UUID}.jpg
    │                                   │  4. Attachment 엔티티 DB 저장
    │                                   │     (originalName, storedName, filePath)
    │◄──────────────────────────────────┤
    │  201 Created { postId, ... }      │
```

### 5.2 파일 저장 경로 구조

```
/uploads/
├── gallery/
│   ├── {UUID}-image1.jpg
│   └── {UUID}-image2.png
└── free/
    ├── {UUID}-document.pdf
    └── {UUID}-archive.zip
```

### 5.3 파일 서빙 흐름

```
[Frontend]
이미지 태그: <img src="/api/files/{storedFilename}">

[Backend - AttachmentController]
GET /api/files/{filename}
→ 로컬 파일 시스템에서 파일 읽기
→ Content-Type 자동 감지
→ 파일 바이트 응답 (Resource)
```

### 5.4 파일 수정 (삭제 + 추가) 흐름

```
PUT /api/gallery/{id}
Body: {
  category, title, content,
  deleteAttachmentIds: [3, 5],    // 삭제할 기존 파일 ID 목록
  newFiles: [newImage.jpg]        // 새로 추가할 파일
}

Service 처리:
1. deleteAttachmentIds → 로컬 파일 삭제 + DB 레코드 삭제
2. newFiles → 새 파일 저장 + DB 레코드 추가
3. 게시글 내용 업데이트
```

---

## 6. 조회수 중복 방지 설계

**요구사항**: 동일 세션 내 재방문 시 조회수를 중복 카운트하지 않는다.

### 처리 방식: 서버 세션 기반 Set 추적

```
[Backend - ViewCountService]

private Map<String, Set<String>> viewLog
  = ConcurrentHashMap<>()
  // key: "free_1", "gallery_3" (boardType_postId)
  // value: Set<sessionId>

processView(boardType, postId, request):
  1. request.getSession(true).getId() → sessionId 추출
  2. key = boardType + "_" + postId
  3. viewLog.get(key)에 sessionId 없으면:
     - DB viewCount +1
     - viewLog.get(key).add(sessionId)
  4. 이미 있으면: 조회수 증가 없이 반환

※ 서버 재시작 시 viewLog 초기화됨 (인메모리)
   → 재시작 후 첫 방문은 다시 카운트됨 (허용 범위로 간주)
```

---

## 7. 데이터 흐름 요약

### 7.1 게시글 목록 조회 (검색/필터 포함)

```
Frontend
  URL: /boards/free?category=1&searchText=안녕&pageNum=2&pageSize=10&orderValue=createdAt&orderDirection=desc
  → BoardFilter 컴포넌트가 쿼리 파라미터 구성
  → GET /api/free?category=1&searchText=안녕&pageNum=2&...

Backend
  FreeController.getList(@ModelAttribute BoardListRequestDto dto)
  → FreeService.getList(dto)
  → FreeRepository.findByCondition(dto)  // JPQL 동적 쿼리
  → PageResponseDto 생성 (content + totalCount + pagination 정보)
  → 200 OK

Frontend
  → 목록 렌더링
  → Pagination 컴포넌트에 totalPages, currentPage 전달
```

### 7.2 비밀글 접근 제어

```
Frontend (InquiryListView)
  게시글 클릭 시:
  ├─ isSecret=false → /boards/inquiry/{id} 라우터 이동
  ├─ isSecret=true + 비로그인 → 로그인 모달 팝업
  └─ isSecret=true + 로그인 → /boards/inquiry/{id} 라우터 이동

Backend (InquiryService.getDetail)
  1. Inquiry 조회
  2. isSecret=true 이면:
     ├─ 비로그인 → 403 Forbidden
     ├─ 로그인 사용자 id ≠ 작성자 id + role ≠ ADMIN → 403 Forbidden
     └─ 본인 또는 ADMIN → 정상 반환
```

### 7.3 댓글 등록 (비회원 시도)

```
Frontend (CommentInput.vue)
  등록 버튼 클릭
  └─ isLoggedIn=false → LoginModal 표시

  LoginModal 로그인 성공
  → Pinia 상태 갱신, 모달 닫기
  → 댓글 입력 내용 유지
  → 사용자가 다시 등록 버튼 클릭 → POST /api/comments

Backend
  JWT 검증 → USER 확인
  Comment 저장 → 201 Created
  
Frontend
  → 댓글 목록 재조회 렌더링
```
