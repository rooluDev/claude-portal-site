# 커뮤니티형 포털 사이트

> Vue.js 3 + Spring Boot 3 기반 풀스택 커뮤니티 포털

---

## 프로젝트 소개

공지사항, 자유게시판, 갤러리, 문의게시판 4개 게시판을 제공하는 커뮤니티형 포털 사이트입니다.

**이 프로젝트의 핵심은 단순히 "구현"이 아닙니다.**  
Claude Code(AI 코딩 도구)를 활용해 PRD·아키텍처·API 스펙·DB 스키마·UI 스펙·에러 스펙 등 **기획 문서 작성부터 설계, 구현, 통합까지 전 과정을 주도적으로 진행**했습니다. 코드를 작성하기 전에 문서를 먼저 완성하고, 문서를 기준으로 구현을 검증하는 방식으로 개발했습니다.

---

## 화면

**메인**

https://github.com/user-attachments/assets/21138aae-62e4-47bb-a64a-17a7ef3b2346

**공지사항**

https://github.com/user-attachments/assets/aac56700-66e7-4ff4-a015-6d5b09d1e926

**자유 게시판**

https://github.com/user-attachments/assets/15260cab-ce75-4bb1-9f8a-2a8f4e0e47ea

**갤러리 게시판**

https://github.com/user-attachments/assets/f6ddcc31-3f44-4ac2-a990-8edb34e566f7

**문의 게시판**

https://github.com/user-attachments/assets/8e0a2684-ed01-44c3-968b-c17fbc1bff2d

---

## AI 코딩 도구 활용 방식

AI 도구를 단순히 코드 자동완성에 쓰는 것이 아니라, **소프트웨어 개발 전 과정의 협업 파트너**로 활용했습니다.

| 단계 | 활용 내용 |
|------|-----------|
| 기획 | 요구사항 정리 → PRD 작성 (기능 명세, 권한 매트릭스, URL 구조) |
| 설계 | 시스템 아키텍처, DB 스키마, API 스펙, UI 스펙, 에러 스펙 문서화 |
| 구현 | 문서 기반 백엔드(Spring Boot) → 프론트엔드(Vue.js) 순차 구현 |
| 검증 | 비즈니스 규칙 누락 여부 검토, 코드-문서 정합성 확인 |

기획 단계에서 작성한 문서들이 구현의 기준이 되었고, 개발 중 발생하는 의사결정(에러 처리 방식, 권한 분기, 파일 업로드 정책 등)도 모두 문서에 근거해 진행했습니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue.js 3 (Composition API), Vite, Pinia, Vue Router 4, Axios |
| Backend | Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| 인증 | JWT (sessionStorage, 만료 2시간) |
| 파일 스토리지 | 로컬 파일 시스템 |

---

## 주요 기능

### 게시판 4종
- **공지사항**: 관리자 전용 작성·수정·삭제 / 고정글(isPinned) 최상단 고정
- **자유게시판**: 회원 작성 / 파일 첨부 (최대 5개, 20MB) / 댓글
- **갤러리**: 이미지 업로드 (최대 10장, 10MB) / 썸네일·슬라이더 / 댓글
- **문의게시판**: 비밀글 / 관리자 답변 / 답변 완료 시 수정 불가

### 인증·인가
- JWT 기반 인증 (sessionStorage 저장, 탭 닫으면 자동 소멸)
- 3단계 권한: 비회원 → USER → ADMIN
- Spring Security 레이어 + Service 레이어 이중 권한 검증
- Axios 인터셉터: 401 응답 시 JWT 삭제 + 로그인 모달/리다이렉트 자동 처리

### 공통 기능
- 게시판 공통 검색·필터 (날짜 범위, 카테고리, 검색어, 정렬, 페이지 크기)
- 조회수 중복 방지 (서버 세션 기반, ConcurrentHashMap)
- 공통 응답 형식 `ApiResponse { success, message, data }` 전 API 적용
- GlobalExceptionHandler + ErrorCode enum 기반 일관된 에러 처리

---

## 구현 시 고민한 기술적 포인트

### 1. 권한 검증 이중 설계
Spring Security의 `hasRole()` 선언만으로는 "본인 글만 수정 가능" 같은 세밀한 권한 제어가 불가능합니다. Service 레이어에서 `currentUserId == post.getUserId()` 검증을 별도로 수행하는 이중 구조를 채택했습니다.

### 2. Spring Security + CORS 충돌 해결
`WebMvcConfigurer` CORS 설정만으로는 Spring Security 필터 체인이 preflight `OPTIONS` 요청을 먼저 차단합니다. `SecurityConfig`에 `CorsConfigurationSource` 빈을 직접 등록해 문제를 해결했습니다.

### 3. 비밀글 2단계 접근 제어
목록 클릭 시 프론트엔드에서 1차 제어(비로그인 → 로그인 모달, 타인 → 토스트), 상세 API에서 백엔드 2차 제어(401/403 반환). 클라이언트 우회 시도를 서버에서 확실히 막는 구조입니다.

### 4. 파일 업로드 + 수정 흐름
수정 요청에 `deleteAttachmentIds` 목록을 함께 전송해 "기존 파일 삭제 + 새 파일 추가"를 단일 트랜잭션에서 처리합니다. 파일 IO 오류는 예외를 삼키고 로그만 기록해 게시글 트랜잭션에 영향을 주지 않습니다.

### 5. Multipart + JSON 동시 전송
파일과 JSON 데이터를 함께 보낼 때 `@RequestPart("data")`로 JSON을 받고, 프론트에서는 `new Blob([JSON.stringify(dto)], { type: "application/json" })`으로 감싸 Content-Type을 명시하는 패턴을 적용했습니다.

---

## 프로젝트 구조

```
portfolio/
├── backend/                  # Spring Boot 백엔드
│   └── src/main/java/com/portfolio/
│       ├── config/           # SecurityConfig, CorsConfig, WebMvcConfig
│       ├── auth/             # 로그인·회원가입 (controller, service, dto, jwt)
│       ├── user/             # User entity, Role enum
│       ├── board/
│       │   ├── common/       # BoardListRequestDto, PageResponseDto, ViewCountService
│       │   ├── notice/       # 공지사항
│       │   ├── free/         # 자유게시판
│       │   ├── gallery/      # 갤러리
│       │   └── inquiry/      # 문의게시판 (InquiryAnswer 포함)
│       ├── comment/          # 댓글
│       ├── attachment/       # 첨부파일
│       └── common/           # ApiResponse, ErrorCode, CustomException, GlobalExceptionHandler
│
├── frontend/                 # Vue.js 3 프론트엔드
│   └── src/
│       ├── api/              # axios.js + 도메인별 API 모듈
│       ├── stores/           # auth.js, modal.js, toast.js (Pinia)
│       ├── router/           # index.js (라우트 + 네비게이션 가드)
│       ├── views/            # 페이지 컴포넌트
│       └── components/       # 재사용 컴포넌트 (layout, common, 도메인별)
│
├── docs/                     # 기획·설계 문서
│   ├── prd.md                # 제품 요구사항 정의서
│   ├── architecture.md       # 시스템 아키텍처
│   ├── api-spec.md           # REST API 명세
│   ├── db-schema.md          # DB 테이블 설계
│   ├── ui-spec.md            # UI 레이아웃·컴포넌트 명세
│   ├── error-spec.md         # 에러 코드 전체 목록
│   ├── env-spec.md           # 환경 변수·설정 파일
│   └── user-flow.md          # 기능별 흐름 정의
├── db-schema.sql
└── db-seed.sql               # 관리자 계정 초기 데이터
```

---

## REST API 엔드포인트 요약

| 도메인 | Method | URI | 권한 |
|--------|--------|-----|------|
| 인증 | POST | `/api/auth/join` | 전체 |
| 인증 | POST | `/api/auth/login` | 전체 |
| 공지사항 | GET | `/api/notice` | 전체 |
| 공지사항 | POST/PUT/DELETE | `/api/notice/{id}` | ADMIN |
| 자유게시판 | GET | `/api/free` | 전체 |
| 자유게시판 | POST/PUT/DELETE | `/api/free/{id}` | USER(본인)/ADMIN |
| 갤러리 | GET | `/api/gallery` | 전체 |
| 갤러리 | POST/PUT/DELETE | `/api/gallery/{id}` | USER(본인)/ADMIN |
| 문의게시판 | GET | `/api/inquiry` | 전체 (비밀글 제한) |
| 문의게시판 | POST/PUT/DELETE | `/api/inquiry/{id}` | USER(본인, 미답변)/ADMIN |
| 문의 답변 | POST/PUT/DELETE | `/api/inquiry/{id}/answer` | ADMIN |
| 댓글 | GET/POST/DELETE | `/api/comments` | 전체/USER/USER(본인) |

---

## 로컬 실행 방법

### 사전 요구사항

- Java 17+
- Node.js 18+
- MySQL 8.x

### 설정

```bash
# 1. DB 스키마 생성
mysql -u root -p portfolio < db-schema.sql

# 2. 관리자 계정 초기 데이터 삽입
mysql -u root -p portfolio < db-seed.sql

# 3. 백엔드 로컬 설정 파일 생성
# backend/src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio?serverTimezone=Asia/Seoul
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
jwt:
  secret: {JWT_SECRET_KEY_32자_이상}
```

### 실행

```bash
# 터미널 1 — 백엔드
cd backend
./gradlew bootRun

# 터미널 2 — 프론트엔드
cd frontend
npm install
npm run dev
```

| 서비스 | URL |
|--------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |

---

## 문서 목록

개발에 앞서 작성한 설계 문서들입니다. 구현 전 문서를 먼저 완성하고, 이를 기준으로 개발·검증했습니다.

| 문서 | 내용 |
|------|------|
| [PRD](docs/prd.md) | 제품 요구사항, 권한 매트릭스, URL 구조 |
| [Architecture](docs/architecture.md) | 시스템 구조, 인증 흐름, 데이터 흐름 |
| [API Spec](docs/api-spec.md) | 전체 REST API 엔드포인트·요청·응답 필드 |
| [DB Schema](docs/db-schema.md) | 테이블 설계, ERD, DDL |
| [UI Spec](docs/ui-spec.md) | 페이지 레이아웃, 컴포넌트, 버튼 표시 조건 |
| [Error Spec](docs/error-spec.md) | 에러 코드 전체 목록, 프론트 처리 방식 |
| [User Flow](docs/user-flow.md) | 기능별 상세 흐름, 분기 조건 |
