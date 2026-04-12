# 📋 plan.md

## 김승현 포트폴리오 포털 사이트

> Claude Code 구현 순서 및 체크리스트.
> 각 단계는 의존 관계 순서로 정렬되어 있다. 위에서 아래로 순서대로 구현한다.

---

## 전체 단계 요약

| Phase | 내용 | 산출물 |
|-------|------|--------|
| Phase 1 | 프로젝트 초기화 | 백엔드·프론트엔드 프로젝트 골격 |
| Phase 2 | DB 설정 | 스키마 + 초기 데이터 |
| Phase 3 | 백엔드 공통 기반 | 설정·예외·응답·JWT·Security |
| Phase 4 | 백엔드 인증 | 회원가입·로그인 API |
| Phase 5 | 백엔드 게시판 | 공지사항·자유·갤러리·문의 API |
| Phase 6 | 백엔드 공통 기능 | 댓글·첨부파일·메인·조회수 API |
| Phase 7 | 프론트엔드 공통 기반 | Axios·Router·Pinia·공통 컴포넌트 |
| Phase 8 | 프론트엔드 인증 | 로그인·회원가입 페이지 |
| Phase 9 | 프론트엔드 게시판 | 4개 게시판 전체 페이지 |
| Phase 10 | 프론트엔드 메인 | 메인 페이지 위젯 |
| Phase 11 | 통합 확인 | 전체 플로우 동작 점검 |

---

## Phase 1. 프로젝트 초기화

### 1-1. 백엔드 프로젝트 생성

- [x] Spring Initializr로 프로젝트 생성
  - Group: `com.portfolio`
  - Artifact: `backend`
  - Java: 17
  - 의존성: `Spring Web`, `Spring Data JPA`, `Spring Security`, `Validation`, `MySQL Driver`, `Lombok`
- [x] `build.gradle`에 JWT 의존성 추가
  ```gradle
  implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
  runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
  runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
  ```
- [x] `src/main/resources/application.yml` 생성 (env-spec.md 2.2 참조)
- [x] `src/main/resources/application-local.yml` 생성 (env-spec.md 2.3 참조)
- [x] `backend/.gitignore`에 `application-local.yml`, `uploads/` 추가

### 1-2. 프론트엔드 프로젝트 생성

- [x] Vite + Vue 3 프로젝트 생성
  ```bash
  npm create vite@latest frontend -- --template vue
  cd frontend
  npm install
  ```
- [x] 추가 패키지 설치
  ```bash
  npm install vue-router@4 pinia axios
  ```
- [x] `frontend/.env` 생성
  ```
  VITE_API_BASE_URL=http://localhost:8080/api
  ```
- [x] `frontend/vite.config.js` 설정 (env-spec.md 3.5 참조, `@` 경로 별칭 포함)
- [x] `frontend/.gitignore`에 `.env.local`, `.env.*.local` 추가

---

## Phase 2. DB 설정

### 2-1. 스키마 생성

- [x] MySQL 접속 후 `portfolio` 데이터베이스 생성
  ```sql
  CREATE DATABASE IF NOT EXISTS portfolio
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
  ```
- [x] `db-schema.sql` 파일 작성 및 실행 (db-schema.md 4절 DDL 전체)
  - [x] `users` 테이블
  - [x] `notice` 테이블
  - [x] `free_post` 테이블
  - [x] `gallery_post` 테이블
  - [x] `inquiry` 테이블
  - [x] `inquiry_answer` 테이블 (`ON DELETE CASCADE` 포함)
  - [x] `comment` 테이블
  - [x] `attachment` 테이블

### 2-2. 인덱스 생성

- [x] `db-schema.sql`에 인덱스 DDL 추가 후 실행 (db-schema.md 5절 전체)
  - [x] `notice`: `idx_notice_is_pinned_created_at`, `idx_notice_category`, `idx_notice_user_id`
  - [x] `free_post`: `idx_free_post_created_at`, `idx_free_post_category`, `idx_free_post_user_id`
  - [x] `gallery_post`: `idx_gallery_post_created_at`, `idx_gallery_post_category`, `idx_gallery_post_user_id`
  - [x] `inquiry`: `idx_inquiry_created_at`, `idx_inquiry_user_id`, `idx_inquiry_answer_status`
  - [x] `comment`: `idx_comment_board_type_post_id`, `idx_comment_user_id`
  - [x] `attachment`: `idx_attachment_board_type_post_id_sort`

### 2-3. 초기 데이터

- [x] `db-seed.sql` 작성 및 실행
  - [x] 관리자 계정 INSERT (username: `admin`, role: `ADMIN`, 비밀번호: BCrypt 해시)
  - [x] BCrypt 해시 생성: 임시 `main()` 메서드 또는 온라인 생성기 활용

---

## Phase 3. 백엔드 공통 기반

### 3-1. 패키지 구조 생성

- [x] 아래 패키지 디렉토리 생성 (architecture.md 3.2 참조)
  ```
  com.portfolio
  ├── config/
  ├── auth/
  │   ├── controller/ service/ dto/ jwt/
  ├── user/
  │   ├── entity/ repository/ enums/
  ├── board/
  │   ├── common/ notice/ free/ gallery/ inquiry/
  ├── comment/
  │   ├── controller/ service/ repository/ entity/ dto/
  ├── attachment/
  │   ├── controller/ service/ repository/ entity/
  └── common/
      ├── exception/ response/
  ```

### 3-2. 공통 응답 / 예외

- [x] `ApiResponse.java` 작성
  - 필드: `boolean success`, `String message`, `T data`
  - 정적 팩토리: `success(data)`, `success(message, data)`, `error(message)`
- [x] `ErrorCode.java` (enum) 작성 (error-spec.md 2.1 전체 22개 코드)
  - 필드: `int status`, `String message`
- [x] `CustomException.java` 작성 (error-spec.md 3.2 참조)
- [x] `GlobalExceptionHandler.java` 작성 (error-spec.md 3.1 참조)
  - `CustomException` 핸들러
  - `MethodArgumentNotValidException` 핸들러
  - `MaxUploadSizeExceededException` 핸들러
  - `Exception` (최종 catch) 핸들러

### 3-3. @ConfigurationProperties 바인딩 클래스

- [x] `FileProperties.java` (`prefix = "file"`) — uploadDir, galleryDir, freeDir
- [x] `FilePolicyProperties.java` (`prefix = "file-policy"`) — gallery/free 정책
- [x] `JwtProperties.java` (`prefix = "jwt"`) — secret, expiration
- [x] `CorsProperties.java` (`prefix = "cors"`) — allowedOrigins
- [x] `PortfolioApplication.java`에 `@EnableConfigurationProperties` 추가

### 3-4. JWT

- [x] `JwtProvider.java` 작성
  - `generateToken(User user)` — sub: userId, role, exp: now + expiration
  - `validateToken(String token)` → boolean
  - `getUserId(String token)` → Long
  - `getRole(String token)` → String
- [x] `JwtAuthFilter.java` 작성 (`OncePerRequestFilter`)
  - Authorization 헤더에서 Bearer 토큰 추출
  - `validateToken()` 통과 시 SecurityContext에 인증 객체 설정
  - 실패 시 SecurityContext 설정 없이 통과 (필터 체인 계속)
- [x] `JwtAuthEntryPoint.java` 작성 (`AuthenticationEntryPoint`)
  - 401 응답을 JSON 형식으로 반환 (error-spec.md 3.3 참조)

### 3-5. Spring Security 설정

- [x] `SecurityConfig.java` 작성
  - `JwtAuthFilter` 등록 (`UsernamePasswordAuthenticationFilter` 앞)
  - `JwtAuthEntryPoint` 등록
  - CSRF 비활성화, 세션 STATELESS
  - `permitAll()` 경로: `POST /api/auth/**`, `GET /api/**` (기본 조회), `GET /api/files/**`
  - `hasAnyRole(USER, ADMIN)`: 게시글/댓글 CUD
  - `hasRole(ADMIN)`: 공지사항 CUD, 문의 답변 CUD
- [x] `CorsConfig.java` 작성 (env-spec.md 2.6 참조)
- [x] `WebMvcConfig.java` 작성 (env-spec.md 2.7 참조)
  - `/api/files/**` → 로컬 uploadDir 정적 리소스 매핑

### 3-6. 유틸리티

- [x] `Role.java` (enum) — `USER`, `ADMIN`
- [x] `FileDirectoryInitializer.java` — 앱 기동 시 uploads/gallery/free 디렉토리 자동 생성 (env-spec.md 2.8 참조)

---

## Phase 4. 백엔드 인증

### 4-1. User 도메인

- [x] `User.java` (Entity)
  - 필드: `id`, `username`, `password`, `name`, `role`, `createdAt`
  - `@Enumerated(EnumType.STRING)` for role
- [x] `UserRepository.java`
  - `Optional<User> findByUsername(String username)`
  - `boolean existsByUsername(String username)`

### 4-2. Auth API

- [x] `JoinRequestDto.java`
  - `@NotBlank`, `@Pattern`, `@Size` 유효성 어노테이션 (error-spec.md 7.1 참조)
- [x] `LoginRequestDto.java`
- [x] `LoginResponseDto.java` — token, user(id, username, name, role)
- [x] `AuthService.java`
  - `join(JoinRequestDto)` — 중복 확인 → BCrypt 해시 → 저장
  - `login(LoginRequestDto)` → JWT 발급
- [x] `AuthController.java`
  - `POST /api/auth/join` → 200 OK
  - `POST /api/auth/login` → 200 OK with token

---

## Phase 5. 백엔드 게시판

> 공통 패턴: Entity → Repository → DTO → Service → Controller 순서로 구현

### 5-1. 공통 DTO

- [x] `BoardListRequestDto.java` — startDate, endDate, category, searchText, pageSize, orderValue, orderDirection, pageNum
- [x] `PageResponseDto.java` — content, totalCount, totalPages, currentPage, pageSize
- [x] `ViewCountService.java` — `ConcurrentHashMap` 기반 세션별 중복 방지 (architecture.md 6절 참조)

### 5-2. 공지사항

- [x] `Notice.java` (Entity) — id, userId, category(ENUM), isPinned, title, content, viewCount, createdAt, updatedAt
- [x] `NoticeRepository.java`
  - `findByCondition(BoardListRequestDto)` — JPQL 동적 쿼리 (isPinned DESC 우선 정렬)
  - `countByCondition(BoardListRequestDto)`
  - `findTop6ByOrderByIsPinnedDescCreatedAtDesc()` — 메인 위젯용
- [x] `NoticeListResponseDto.java` — id, category, categoryLabel, isPinned, title, viewCount, createdAt, authorName
- [x] `NoticeDetailResponseDto.java` — 상세 필드 + isEditable
- [x] `NoticeWriteRequestDto.java` — category, isPinned, title, content
- [x] `NoticeService.java`
  - `getList(dto)` → `PageResponseDto`
  - `getDetail(id, currentUser)` → `NoticeDetailResponseDto` + 조회수 처리
  - `create(dto, currentUser)` → 저장 후 id 반환
  - `update(id, dto, currentUser)` → 수정
  - `delete(id, currentUser)` → 삭제
- [x] `NoticeController.java` — 5개 엔드포인트 (api-spec.md 4절)

### 5-3. 자유게시판

- [x] `FreePost.java` (Entity) — id, userId, category(ENUM), title, content, viewCount, createdAt, updatedAt
- [x] `FreeRepository.java`
  - `findByCondition(dto)` — JPQL 동적 쿼리 (제목/내용/등록자 검색 포함)
  - `countByCondition(dto)`
  - `findTop6ByOrderByCreatedAtDesc()` — 메인 위젯용
- [x] `FreeListResponseDto.java` — id, category, categoryLabel, title, commentCount, hasAttachment, viewCount, createdAt, authorName
- [x] `FreeDetailResponseDto.java` — 상세 필드 + authorId + isEditable + attachments[]
- [x] `FreeWriteRequestDto.java` — category, title, content, deleteAttachmentIds(수정 시)
- [x] `FreeService.java`
  - `getList(dto)` → `PageResponseDto`
  - `getDetail(id, currentUser)` → 조회수 처리 포함
  - `create(dto, files, currentUser)` → 게시글 저장 → 파일 저장
  - `update(id, dto, files, currentUser)` → 권한 확인 → 기존 파일 삭제 → 새 파일 저장 → 내용 수정
  - `delete(id, currentUser)` → 권한 확인 → 파일 삭제 → 댓글 삭제 → 게시글 삭제
- [x] `FreeController.java` — 5개 엔드포인트 (`multipart/form-data`, api-spec.md 5절)

### 5-4. 갤러리

- [x] `GalleryPost.java` (Entity) — id, userId, category(ENUM), title, content, viewCount, createdAt, updatedAt
- [x] `GalleryRepository.java`
  - `findByCondition(dto)` — JPQL 동적 쿼리
  - `countByCondition(dto)`
  - `findTop4ByOrderByCreatedAtDesc()` — 메인 위젯용 (4건)
- [x] `GalleryListResponseDto.java` — id, category, categoryLabel, title, contentPreview(100자), thumbnailUrl, additionalImageCount, viewCount, createdAt, authorName
- [x] `GalleryDetailResponseDto.java` — 상세 필드 + authorId + isEditable + images[] (sortOrder 포함)
- [x] `GalleryWriteRequestDto.java` — category, title, content, deleteAttachmentIds(수정 시)
- [x] `GalleryService.java`
  - `getList(dto)` → 썸네일 URL, 추가 이미지 수 계산 포함
  - `getDetail(id, currentUser)` → 이미지 목록 sortOrder 정렬
  - `create(dto, files, currentUser)` → sort_order 0, 1, 2... 순서 부여
  - `update(id, dto, files, currentUser)` → 이미지 개별 삭제 + 새 이미지 추가 (합산 10장 초과 검증)
  - `delete(id, currentUser)` → 이미지 삭제 → 댓글 삭제 → 게시글 삭제
- [x] `GalleryController.java` — 5개 엔드포인트 (`multipart/form-data`, api-spec.md 6절)

### 5-5. 문의게시판

- [x] `Inquiry.java` (Entity) — id, userId, title, content, isSecret, answerStatus(ENUM), viewCount, createdAt, updatedAt
- [x] `InquiryAnswer.java` (Entity) — id, inquiryId, adminId, content, createdAt, updatedAt
- [x] `InquiryRepository.java`
  - `findByCondition(dto, currentUserId, isAdmin)` — `my=true` 필터, 비밀글 처리 포함
  - `countByCondition(dto, currentUserId, isAdmin)`
  - `findTop6ByOrderByCreatedAtDesc()` — 메인 위젯용
- [x] `InquiryAnswerRepository.java`
  - `findByInquiryId(inquiryId)` → `Optional<InquiryAnswer>`
- [x] `InquiryListResponseDto.java` — id, title, isSecret, answerStatus, answerStatusLabel, viewCount, createdAt, authorName
- [x] `InquiryDetailResponseDto.java` — 상세 필드 + isEditable + answer(id, content, adminName, createdAt, updatedAt)
- [x] `InquiryWriteRequestDto.java` — title, content, isSecret
- [x] `InquiryAnswerRequestDto.java` — content
- [x] `InquiryService.java`
  - `getList(dto, currentUser)` — `my=true` 시 비로그인이면 401
  - `getDetail(id, currentUser)` — 비밀글 접근 제어 (error-spec.md 6.5 참조)
  - `create(dto, currentUser)` → 저장
  - `update(id, dto, currentUser)` → 본인 확인 + ANSWERED 상태면 403
  - `delete(id, currentUser)` → 본인/ADMIN 확인 → 삭제 (CASCADE로 답변 자동 삭제)
  - `createAnswer(inquiryId, dto, admin)` → 중복 답변 확인 → 저장 → answerStatus ANSWERED 업데이트
  - `updateAnswer(inquiryId, dto, admin)` → 수정
  - `deleteAnswer(inquiryId, admin)` → 삭제 → answerStatus PENDING 복귀
- [x] `InquiryController.java` — 8개 엔드포인트 (api-spec.md 7절)

---

## Phase 6. 백엔드 공통 기능

### 6-1. 댓글

- [x] `Comment.java` (Entity) — id, userId, boardType(ENUM), postId, content(1000자), createdAt
- [x] `CommentRepository.java`
  - `findByBoardTypeAndPostIdOrderByCreatedAtAsc(boardType, postId)`
  - `deleteByBoardTypeAndPostId(boardType, postId)` — 게시글 삭제 시 일괄 삭제용
- [x] `CommentListResponseDto.java` — id, content, authorId, authorName, createdAt, isDeletable
- [x] `CommentWriteRequestDto.java` — boardType, postId, content
- [x] `CommentService.java`
  - `getComments(boardType, postId, currentUser)` → isDeletable 계산 포함
  - `create(dto, currentUser)` → 게시글 존재 확인 → 저장 → 등록된 댓글 바로 반환
  - `delete(id, currentUser)` → 본인/ADMIN 확인 → 삭제
- [x] `CommentController.java` — 3개 엔드포인트 (api-spec.md 8절)

### 6-2. 첨부파일

- [x] `Attachment.java` (Entity) — id, boardType, postId, originalName, storedName, filePath, fileSize, sortOrder, createdAt
- [x] `AttachmentRepository.java`
  - `findByBoardTypeAndPostIdOrderBySortOrderAsc(boardType, postId)`
  - `findByIdAndBoardTypeAndPostId(id, boardType, postId)`
  - `deleteByBoardTypeAndPostId(boardType, postId)` — 게시글 삭제 시 일괄 삭제용
  - `countByBoardTypeAndPostId(boardType, postId)` — 파일 개수 확인용
- [x] `AttachmentService.java`
  - `saveFiles(files, boardType, postId, startSortOrder)` → 확장자·용량·개수 검증 → UUID 파일명 생성 → 로컬 저장 → DB 저장
  - `deleteFile(attachment)` → 로컬 파일 삭제 (실패 시 로그만, 예외 삼킴) → DB 삭제
  - `deleteAllByPost(boardType, postId)` → 일괄 삭제
  - `validateGalleryFile(file)` — 확장자 검증 (jpg, jpeg, png, gif, webp)
  - `validateFileSize(file, maxMb)` — 용량 검증

### 6-3. 메인 페이지 API

- [x] `MainResponseDto.java` — notice[], free[], gallery[], inquiry[]
- [x] `MainService.java`
  - `getMainData()` — 각 Repository에서 위젯용 데이터 조회 (notice 6건, free 6건, gallery 4건, inquiry 6건)
- [x] `MainController.java` — `GET /api/main`

---

## Phase 7. 프론트엔드 공통 기반

### 7-1. 전역 상태 (Pinia)

- [x] `src/stores/auth.js` 작성
  - state: `token`, `user`(id, username, name, role)
  - getters: `isLoggedIn`, `isAdmin`, `isUser`
  - actions: `login(token, user)`, `logout()`, `loadFromStorage()`
  - `loadFromStorage()`: `main.js`의 앱 마운트 전에 호출
- [x] `src/stores/modal.js` 작성
  - state: `isLoginModalOpen`
  - actions: `openLoginModal()`, `closeLoginModal()`
- [x] `src/stores/toast.js` 작성
  - state: `toasts[]` (id, type, message)
  - actions: `show(type, message)`, `remove(id)`

### 7-2. API 모듈

- [x] `src/api/axios.js` — Axios 인스턴스 생성 (env-spec.md 3.4 참조)
  - baseURL: `import.meta.env.VITE_API_BASE_URL`
  - timeout: 10000ms
  - Request 인터셉터: JWT 헤더 자동 주입
  - Response 인터셉터: 401 감지 → logout + 모달/리다이렉트 (error-spec.md 4.1 참조)
- [x] `src/api/auth.js` — `join(data)`, `login(data)`
- [x] `src/api/notice.js` — `getList(params)`, `getDetail(id)`, `create(data)`, `update(id, data)`, `remove(id)`
- [x] `src/api/free.js` — `getList(params)`, `getDetail(id)`, `create(formData)`, `update(id, formData)`, `remove(id)`
- [x] `src/api/gallery.js` — `getList(params)`, `getDetail(id)`, `create(formData)`, `update(id, formData)`, `remove(id)`
- [x] `src/api/inquiry.js` — `getList(params)`, `getDetail(id)`, `create(data)`, `update(id, data)`, `remove(id)`, `createAnswer(id, data)`, `updateAnswer(id, data)`, `removeAnswer(id)`
- [x] `src/api/comment.js` — `getComments(boardType, postId)`, `create(data)`, `remove(id)`
- [x] `src/api/main.js` — `getMainData()`

### 7-3. 라우터

- [x] `src/router/index.js` 작성 (architecture.md 2.3 참조)
  - 전체 20개 라우트 정의 (prd.md 2.4 URL 구조)
  - `meta.guestOnly`: 로그인 상태면 `/` 리다이렉트
  - `meta.requiresAuth`: 비로그인이면 로그인 모달 + `next(false)`
  - `meta.requiresAdmin`: ADMIN 아닌 경우 `/` 리다이렉트
  - 전역 `beforeEach` 가드 등록 (error-spec.md 4.3 참조)

### 7-4. App.vue

- [x] `App.vue` 작성
  - `GnbHeader` 컴포넌트 포함
  - `LoginModal` 컴포넌트 포함 (modalStore 상태로 표시 제어)
  - `ToastMessage` 컴포넌트 포함
  - `RouterView` 렌더링
  - `onMounted`: `authStore.loadFromStorage()` 호출

### 7-5. 공통 컴포넌트

- [x] `src/components/layout/GnbHeader.vue`
  - 로고, 메뉴 4개, 우측 인증 영역
  - 현재 경로 기반 활성 메뉴 강조 (`useRoute` 활용)
  - 비로그인: 로그인 버튼 → `/login`, 회원가입 링크 → `/join`
  - 로그인: `{name}님 안녕하세요!` (클릭 불가) + 로그아웃 버튼

- [x] `src/components/common/LoginModal.vue`
  - modalStore `isLoginModalOpen`으로 표시 제어
  - 아이디/비밀번호 입력, 로그인 버튼, 에러 메시지, 회원가입 링크
  - 로그인 성공: JWT 저장, GNB 갱신, 모달 닫기, 현재 페이지 유지
  - 배경 클릭 / ESC 키: 닫기
  - `POST /api/auth/login` 연동

- [x] `src/components/common/BoardFilter.vue`
  - props: `showCategory(Boolean)`, `categoryOptions(Array)`, `searchPlaceholder(String)`, `showOrderCategory(Boolean)`
  - 날짜 범위(시작/종료), 카테고리 드롭다운, 검색어, 검색 버튼
  - 개씩 보기, 정렬 기준, 정렬 방향 드롭다운
  - emit: `search(queryParams)` — 필터 조건 부모로 전달
  - 검색어 Enter 키 지원

- [x] `src/components/common/Pagination.vue`
  - props: `totalPages`, `currentPage`
  - 10개 페이지 그룹, 이전/다음 그룹 버튼 (`◀` / `▶`)
  - 현재 페이지 강조 표시
  - emit: `page-change(pageNum)`

- [x] `src/components/common/ToastMessage.vue`
  - toastStore의 `toasts` 배열 렌더링
  - 타입별 색상: success(초록), error(빨강), info(파랑)
  - 3초 후 자동 소멸 (`setTimeout` + `toastStore.remove(id)`)
  - 화면 우하단 고정

- [x] `src/components/common/ConfirmDialog.vue`
  - props: `message`
  - emit: `confirm`, `cancel`
  - 취소/확인 버튼

- [x] `src/components/file/FileAttachment.vue`
  - props: `existingFiles(Array)`, `maxCount(Number, 기본 5)`, `maxSizeMb(Number, 기본 20)`
  - 파일 추가 버튼, 추가된 파일 목록, 개별 삭제(×) 버튼
  - 수정 모드: 기존 파일 삭제 예약 (빨간 취소선 표시)
  - emit: `update:newFiles`, `update:deleteIds`
  - 개수/용량 초과 시 인라인 에러 메시지

- [x] `src/components/file/ImageUploader.vue`
  - props: `existingImages(Array)`, `maxCount(Number, 기본 10)`, `maxSizeMb(Number, 기본 10)`
  - 이미지 추가 버튼, 썸네일 미리보기 + 개별 삭제(×)
  - 허용 확장자: jpg, jpeg, png, gif, webp
  - emit: `update:newFiles`, `update:deleteIds`
  - 개수/용량/확장자 위반 시 인라인 에러 메시지

- [x] `src/components/gallery/ImageCarousel.vue`
  - props: `images(Array)` — fileUrl, sortOrder 포함
  - 이미지 1장: 화살표 미표시
  - 복수: `[<]` `[>]` 화살표 + 하단 인디케이터 점
  - 인디케이터 점 클릭 시 해당 이미지로 이동

- [x] `src/components/free/CommentList.vue` + `CommentInput.vue`
  - CommentList: 댓글 목록, `isDeletable=true`인 댓글에만 삭제 버튼 표시
  - CommentInput: 입력란 항상 노출, 비로그인 등록 클릭 시 로그인 모달
  - 댓글 등록 성공: 입력란 초기화 + 목록에 새 댓글 즉시 추가 (재조회 없음)
  - 댓글 삭제: ConfirmDialog → `DELETE /api/comments/{id}`

- [x] `src/components/inquiry/InquiryAnswer.vue`
  - props: `answer`, `answerStatus`, `isAdmin`
  - PENDING + 비회원/회원: "아직 답변이 등록되지 않았습니다." 표시
  - ANSWERED + 비회원/회원: 관리자명, 답변일, 답변 내용 표시 (읽기 전용)
  - PENDING + ADMIN: 답변 입력란 + `답변 등록` 버튼
  - ANSWERED + ADMIN: 답변 내용 + `답변 수정`(인라인 textarea 전환) / `답변 삭제` 버튼

---

## Phase 8. 프론트엔드 인증

### 8-1. 로그인 페이지

- [x] `src/views/auth/LoginView.vue`
  - 아이디/비밀번호 입력, 로그인 버튼
  - 실패: 폼 하단 에러 메시지 표시
  - 성공: JWT 저장 → `ret` 파라미터 있으면 해당 경로, 없으면 `/`
  - 이미 로그인 상태: `/` 리다이렉트 (guestOnly 가드)
  - `POST /api/auth/login` 연동

### 8-2. 회원가입 페이지

- [x] `src/views/auth/JoinView.vue`
  - 아이디 / 비밀번호 / 비밀번호 확인 / 이름 입력
  - 모든 필드 입력 전 버튼 비활성화 (회색)
  - 각 필드 blur 이벤트 시 인라인 유효성 검사
  - 서버 409 응답: 폼 상단 에러 메시지
  - 성공: `/login` 이동
  - `POST /api/auth/join` 연동

---

## Phase 9. 프론트엔드 게시판

> 각 게시판은 **목록 → 상세 → 작성 → 수정** 순서로 구현

### 9-1. 공지사항

- [x] `src/views/notice/NoticeListView.vue`
  - `BoardFilter` 컴포넌트 사용 (category: 공지/이벤트, 정렬 기준 포함)
  - 테이블 컬럼: 번호, 분류, 제목, 조회, 등록일시, 등록자
  - `isPinned=true` 행: 회색 배경, 번호 `-` 표시, 목록 최상단 고정
  - ADMIN만 글 등록 버튼 표시
  - `Pagination` 컴포넌트 사용
  - `GET /api/notice` 연동

- [x] `src/views/notice/NoticeDetailView.vue`
  - 분류 뱃지, 제목, 작성일/작성자/조회수, 본문
  - `isEditable=true`(ADMIN)일 때만 수정/삭제 버튼 표시
  - 삭제: ConfirmDialog → `DELETE /api/notice/{id}` → 목록 이동
  - `GET /api/notice/{id}` 연동

- [x] `src/views/notice/NoticeWriteView.vue`
  - 카테고리(공지/이벤트), 고정 공지 체크박스, 제목, 내용
  - 필드 유효성 검사 (인라인 에러)
  - 등록: `POST /api/notice` → 상세 페이지 이동
  - requiresAdmin 가드 적용

- [x] `src/views/notice/NoticeModifyView.vue`
  - 진입 시 `GET /api/notice/{id}`로 기존 데이터 채움
  - 저장: `PUT /api/notice/{id}` → 상세 페이지 이동
  - requiresAdmin 가드 적용

### 9-2. 자유게시판

- [x] `src/views/free/FreeListView.vue`
  - `BoardFilter` 컴포넌트 사용 (category: 유머/취미, 제목+내용+등록자 검색)
  - 테이블 컬럼: 번호, 분류, 제목(댓글수 `(N)`, 첨부 📎), 조회, 등록일시, 등록자
  - 글 등록 버튼: 비로그인 → 로그인 모달, 로그인 → `/boards/free/write`
  - `GET /api/free` 연동

- [x] `src/views/free/FreeDetailView.vue`
  - 분류 뱃지, 제목, 작성일/작성자/조회수, 본문
  - 첨부파일 목록 (파일명 + 크기, 클릭 시 다운로드)
  - `isEditable=true`일 때만 수정/삭제 버튼 표시
  - `CommentList` + `CommentInput` 컴포넌트 사용
  - `GET /api/free/{id}` + `GET /api/comments?boardType=FREE&postId={id}` 연동

- [x] `src/views/free/FreeWriteView.vue`
  - 카테고리(유머/취미), 제목, 내용, `FileAttachment` 컴포넌트
  - 등록: `POST /api/free` (multipart) → 상세 페이지 이동
  - requiresAuth 가드 적용

- [x] `src/views/free/FreeModifyView.vue`
  - 진입 시 기존 데이터 채움 (기존 첨부파일 목록 표시)
  - `FileAttachment` 수정 모드 (기존 파일 삭제 예약 가능)
  - 저장: `PUT /api/free/{id}` (multipart) → 상세 페이지 이동

### 9-3. 갤러리

- [x] `src/views/gallery/GalleryListView.vue`
  - `BoardFilter` 컴포넌트 사용 (category: 음식/연예인)
  - **카드형 레이아웃**: 이미지 있으면 썸네일 카드, 없으면 텍스트 카드
  - 카드에 `+N` 추가 이미지 수 표시 (`additionalImageCount > 0`인 경우)
  - 글 등록 버튼: 비로그인 → 로그인 모달, 로그인 → `/boards/gallery/write`
  - `GET /api/gallery` 연동

- [x] `src/views/gallery/GalleryDetailView.vue`
  - `ImageCarousel` 컴포넌트 사용 (이미지 없으면 미표시)
  - 제목, 작성자, 작성일, 조회수, 본문, 첨부파일 목록
  - `CommentList` + `CommentInput` 컴포넌트 사용
  - `GET /api/gallery/{id}` + `GET /api/comments?boardType=GALLERY&postId={id}` 연동

- [x] `src/views/gallery/GalleryWriteView.vue`
  - 카테고리(음식/연예인), 제목, 내용, `ImageUploader` 컴포넌트
  - 등록: `POST /api/gallery` (multipart) → 상세 페이지 이동

- [x] `src/views/gallery/GalleryModifyView.vue`
  - 진입 시 기존 데이터 채움 (기존 이미지 썸네일 표시)
  - `ImageUploader` 수정 모드 (기존 이미지 개별 삭제 예약)
  - 저장: `PUT /api/gallery/{id}` (multipart) → 상세 페이지 이동

### 9-4. 문의게시판

- [x] `src/views/inquiry/InquiryListView.vue`
  - `BoardFilter` 컴포넌트 사용 (카테고리 없음, 정렬 기준 `등록 일시`만)
  - 테이블 컬럼: 번호, 제목(🔒 아이콘 + 답변상태), 조회, 등록일시, 등록자
  - `나의 문의내역` 체크박스: 비로그인 → 로그인 모달, 로그인 → `?my=true`
  - 비밀글 클릭 분기: 비로그인 → 로그인 모달 / 타인 → 토스트 "접근 권한 없음" / 본인·ADMIN → 상세 이동
  - `GET /api/inquiry` 연동

- [x] `src/views/inquiry/InquiryDetailView.vue`
  - 제목(🔒 아이콘), 작성일/작성자/조회수, 본문
  - `InquiryAnswer` 컴포넌트 사용
  - 버튼 표시 조건: 본인+PENDING → 수정/삭제, 본인+ANSWERED → 삭제만, ADMIN → 목록만
  - `GET /api/inquiry/{id}` 연동

- [x] `src/views/inquiry/InquiryWriteView.vue`
  - 제목, 내용, 비밀글 체크박스
  - 등록: `POST /api/inquiry` → 상세 페이지 이동
  - requiresAuth 가드 적용

- [x] `src/views/inquiry/InquiryModifyView.vue`
  - 진입 시 기존 데이터 채움
  - ANSWERED 상태 글 접근 시: 토스트 + `/boards/inquiry` 이동
  - 저장: `PUT /api/inquiry/{id}` → 상세 페이지 이동

---

## Phase 10. 프론트엔드 메인 페이지

- [x] `src/views/HomeView.vue`
  - 2×2 그리드 레이아웃, 4개 위젯 배치
  - `GET /api/main` 연동 (페이지 진입 시 1회 호출)

- [x] `src/components/notice/NoticeWidget.vue`
  - 번호/분류/제목 6건, `isPinned=true` 행 회색 배경
  - 더보기 + 버튼 → `/boards/notice`

- [x] `src/components/free/FreeWidget.vue`
  - 번호/분류/제목(댓글수, 📎) 6건
  - 더보기 + 버튼 → `/boards/free`

- [x] `src/components/gallery/GalleryWidget.vue`
  - 번호/분류/썸네일/+N 4건
  - 더보기 + 버튼 → `/boards/gallery`

- [x] `src/components/inquiry/InquiryWidget.vue`
  - 번호/제목(🔒, 답변상태) 6건
  - 비밀글 클릭 분기 처리 (목록과 동일)
  - 나의 문의 내역 버튼
  - 더보기 + 버튼 → `/boards/inquiry`

---

## Phase 11. 통합 확인

### 11-1. 인증 플로우

- [x] 회원가입 → 로그인 → GNB 상태 변경 확인
- [x] 로그아웃 → sessionStorage JWT 삭제 → GNB 비로그인 상태 확인
- [x] 비로그인으로 `/boards/free/write` 직접 접근 → 로그인 모달 표시 확인
- [x] 비로그인 댓글 등록 클릭 → 로그인 모달 → 로그인 후 댓글 입력 내용 유지 확인
- [x] JWT 만료 시뮬레이션 → 401 응답 → 로그아웃 + 모달/리다이렉트 확인

### 11-2. 게시판 공통 기능

- [x] 목록 검색/필터 (기간, 카테고리, 키워드, 개씩 보기, 정렬) URL 파라미터 반영 확인
- [x] 페이지네이션 10개 그룹, `◀▶` 이동 확인
- [x] 조회수: 동일 세션 재방문 중복 미증가 확인 (상세 페이지 새로고침 후 카운트 변화 확인)

### 11-3. 공지사항 플로우 (ADMIN)

- [x] 글 작성 (고정 공지 체크박스 포함) → 목록 상단 고정 확인
- [x] 글 수정 → 내용 변경 확인
- [x] 글 삭제 → 목록 이동 확인
- [x] 비ADMIN 계정으로 `/boards/notice/write` 직접 접근 → `/` 리다이렉트 확인

### 11-4. 자유게시판 플로우

- [x] 글 작성 (파일 첨부 포함) → 상세 페이지 첨부파일 표시 확인
- [x] 글 수정 → 기존 파일 개별 삭제 + 새 파일 추가 확인
- [x] 타인 글 수정/삭제 버튼 미표시 확인
- [x] 댓글 등록/삭제 확인

### 11-5. 갤러리 플로우

- [x] 이미지 첨부 작성 → 카드 썸네일 + `+N` 표시 확인
- [x] 상세 이미지 캐러셀 탐색 확인 (1장일 때 화살표 미표시 확인)
- [x] 이미지 수정 → 기존 이미지 개별 삭제 + 새 이미지 추가 확인

### 11-6. 문의게시판 플로우

- [x] 비밀글 작성 → 비로그인/타인 접근 차단 확인
- [x] `나의 문의내역` 필터 확인
- [x] 관리자 답변 등록 → answerStatus ANSWERED 전환, 작성자 수정 버튼 미표시 확인
- [x] 관리자 답변 삭제 → answerStatus PENDING 복귀 확인
- [x] 답변 완료 문의글 수정 시도 → 403 처리 + 목록 이동 확인

### 11-7. 에러 처리 확인

- [x] 회원가입 중복 아이디 → 폼 상단 에러 메시지 확인
- [x] 갤러리 10장 초과 업로드 → 인라인 에러 메시지 확인
- [x] 자유게시판 파일 20MB 초과 → 인라인 에러 메시지 확인
- [x] 존재하지 않는 게시글 ID 직접 접근 → 토스트 + 목록 이동 확인
- [x] 500 서버 오류 시뮬레이션 → 토스트 에러 메시지 확인

---

## 구현 시 참조 문서 빠른 가이드

| 구현 항목 | 참조 문서 |
|----------|----------|
| 화면 레이아웃/컴포넌트 구조 | `ui-spec.md` |
| API 요청/응답 형식 | `api-spec.md` |
| DB 테이블/컬럼 | `db-schema.md` |
| 에러 처리 패턴 | `error-spec.md` |
| 환경 변수/설정 파일 | `env-spec.md` |
| 사용자별 접근 권한 | `prd.md 5절` |
| 비즈니스 흐름 | `user-flow.md` |
| 패키지/파일 구조 | `architecture.md` |
