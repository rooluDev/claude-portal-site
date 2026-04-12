# CLAUDE.md

## 김승현 포트폴리오 포털 사이트

> 이 파일은 Claude Code가 매 세션 시작 시 가장 먼저 읽는 프로젝트 마스터 가이드다.
> 코드를 작성하기 전에 이 파일을 반드시 끝까지 읽고 숙지한 후 작업을 시작한다.

---

## 1. 프로젝트 개요

커뮤니티형 포털 사이트. 공지사항·자유게시판·갤러리·문의게시판 4개 게시판을 제공한다.

| 항목          | 내용                                                         |
| ------------- | ------------------------------------------------------------ |
| Frontend      | Vue.js 3 (Composition API), Vite, Pinia, Vue Router 4, Axios |
| Backend       | Spring Boot 3, Spring Security, JPA, MySQL 8                 |
| 인증          | JWT (sessionStorage 저장, 만료 2시간)                        |
| 파일 스토리지 | 로컬 파일 시스템 (`./uploads/`)                              |
| 관리자 계정   | DB 직접 삽입 (별도 가입 없음)                                |

---

## 2. 프로젝트 구조

```
portfolio/
├── backend/                  # Spring Boot 백엔드
│   ├── src/main/java/com/portfolio/
│   │   ├── config/           # SecurityConfig, CorsConfig, WebMvcConfig
│   │   ├── auth/             # 인증 (controller, service, dto, jwt)
│   │   ├── user/             # User entity, Role enum
│   │   ├── board/
│   │   │   ├── common/       # BoardListRequestDto, PageResponseDto, ViewCountService
│   │   │   ├── notice/       # 공지사항
│   │   │   ├── free/         # 자유게시판
│   │   │   ├── gallery/      # 갤러리
│   │   │   └── inquiry/      # 문의게시판 (InquiryAnswer 포함)
│   │   ├── comment/          # 댓글
│   │   ├── attachment/       # 첨부파일
│   │   └── common/           # ApiResponse, ErrorCode, CustomException, GlobalExceptionHandler
│   └── src/main/resources/
│       ├── application.yml           # 공통 설정 (Git O)
│       └── application-local.yml     # DB·JWT Secret (Git X)
│
├── frontend/                 # Vue.js 3 프론트엔드
│   └── src/
│       ├── api/              # axios.js + 도메인별 API 모듈
│       ├── stores/           # auth.js, modal.js, toast.js (Pinia)
│       ├── router/           # index.js (라우트 + 가드)
│       ├── views/            # 페이지 컴포넌트 (HomeView, auth/, notice/, free/, gallery/, inquiry/)
│       └── components/       # 재사용 컴포넌트 (layout/, common/, 도메인별/)
│
├── db-schema.sql             # 스키마 DDL + 인덱스
├── db-seed.sql               # 관리자 계정 초기 데이터
└── docs/                     # 스펙 문서 (PRD, Architecture, DB Schema 등)
```

---

## 3. 개발 환경 실행

### 사전 요구사항

| 도구    | 버전    |
| ------- | ------- |
| Java    | 17 이상 |
| Node.js | 18 이상 |
| MySQL   | 8.x     |

### 실행 명령어

```bash
# 터미널 1 — 백엔드
cd backend
./gradlew bootRun

# 터미널 2 — 프론트엔드
cd frontend
npm run dev
```

| 서비스      | URL                       |
| ----------- | ------------------------- |
| Frontend    | http://localhost:5173     |
| Backend API | http://localhost:8080/api |
| MySQL       | localhost:3306/portfolio  |

### 최초 환경 구성 (한 번만)

```bash
# 1. DB 스키마 생성
mysql -u root -p portfolio < db-schema.sql

# 2. 관리자 계정 삽입
mysql -u root -p portfolio < db-seed.sql

# 3. 백엔드 환경 파일 생성 (직접 편집)
# backend/src/main/resources/application-local.yml
# → DB 비밀번호, JWT Secret Key 입력 (env-spec.md 2.3 참조)

# 4. 프론트엔드 의존성 설치
cd frontend && npm install
```

---

## 4. 핵심 설계 규칙

### 4.1 공통 응답 형식 (절대 변경 금지)

모든 API 응답은 아래 구조를 따른다. 예외 없음.

```json
{ "success": true,  "message": "...", "data": { ... } }
{ "success": false, "message": "...", "data": null }
```

페이지네이션 응답의 `data` 구조:

```json
{
  "content": [...],
  "totalCount": 87,
  "totalPages": 9,
  "currentPage": 1,
  "pageSize": 10
}
```

### 4.2 에러 처리 원칙

- **백엔드**: `CustomException(ErrorCode)` throw → `GlobalExceptionHandler`가 일관된 JSON 반환
- **프론트엔드 401**: Axios 인터셉터가 처리. 보호 페이지 → `/login?ret=...` 이동 / 공개 페이지 → 로그인 모달
- **프론트엔드 403**: 토스트 에러 표시
- **프론트엔드 404**: 토스트 후 목록 페이지 이동
- **프론트엔드 500**: 토스트 에러 표시
- 서버 내부 정보(스택 트레이스, SQL)는 절대 응답에 포함하지 않는다

### 4.3 인증 / 권한

- JWT는 `sessionStorage`에 저장 (탭 닫으면 소멸, `localStorage` 사용 금지)
- JWT 만료 시간: 2시간. 자동 갱신 없음
- 권한 3단계: `비회원` → `USER` → `ADMIN`
- Spring Security `permitAll` / `hasAnyRole(USER,ADMIN)` / `hasRole(ADMIN)` 3계층
- Service 레이어에서 추가 검증: 본인 확인, 비밀글 접근, 답변 완료 후 수정 불가

### 4.3.1 CORS 설정 (필수 — 누락 시 모든 API 호출 차단됨)

Spring Security 필터가 `WebMvcConfigurer`보다 먼저 실행되므로, **`CorsConfig`(WebMvcConfigurer)만으로는 부족하다.**
`SecurityConfig`에 반드시 `.cors()` 설정을 추가해야 preflight `OPTIONS` 요청이 통과된다.

```java
// SecurityConfig.java — filterChain() 최상단에 반드시 추가
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(AbstractHttpConfigurer::disable)
    ...

// SecurityConfig.java — 빈으로 등록
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(corsProperties.getAllowedOrigins()));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

`CorsProperties`를 `SecurityConfig`에도 주입(`@RequiredArgsConstructor`)해야 한다.

### 4.4 파일 업로드

| 게시판     | 최대 개수 | 파일당 최대 | 허용 확장자               |
| ---------- | --------- | ----------- | ------------------------- |
| 갤러리     | 10장      | 10MB        | jpg, jpeg, png, gif, webp |
| 자유게시판 | 5개       | 20MB        | 제한 없음                 |
| 문의게시판 | 미지원    | -           | -                         |

- 저장 경로: `./uploads/gallery/`, `./uploads/free/`
- 저장 파일명: `{UUID}-{originalName}` (중복 방지)
- 파일 서빙: Spring 정적 리소스 핸들러 (`/api/files/**` → 로컬 디렉토리)
- 파일 삭제 IO 오류는 예외를 삼키고 로그만 기록 (게시글 삭제 트랜잭션에 영향 없음)

#### 파일 서빙 — Vite 프록시 필수 (누락 시 이미지 깨짐)

`<img src="/api/files/...">` 태그는 Axios를 거치지 않고 브라우저가 직접 요청한다.
Vite 개발 서버(5173)에는 해당 경로가 없으므로 **반드시 프록시를 설정해야 한다.**

```javascript
// frontend/vite.config.js
server: {
  port: 5173,
  proxy: {
    '/api/files': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
},
```

이 설정이 없으면 갤러리·자유게시판 첨부 이미지가 모두 깨진다.

### 4.5 조회수 중복 방지

- `ViewCountService`: `ConcurrentHashMap<String, Set<String>>` 인메모리 관리
- key: `"{boardType}_{postId}"`, value: `Set<sessionId>`
- 서버 재시작 시 초기화됨 (허용 범위)

### 4.6 DB 설계 주요 사항

- `comment`, `attachment`는 Polymorphic Association: `board_type` + `post_id` 컬럼으로 게시판 구분
    - FK 제약 없음 → Service 레이어에서 무결성 보장
- `inquiry_answer.inquiry_id`는 UNIQUE 제약 + `ON DELETE CASCADE`
    - 문의글 삭제 시 답변 자동 삭제 (별도 처리 불필요)
- 게시글 삭제 순서: `attachment`(파일+DB) → `comment`(DB) → 게시글(DB)
- 관리자 답변 삭제 시: DB 삭제 + `inquiry.answer_status` → `PENDING` 복귀

---

## 5. 백엔드 코딩 컨벤션

### 5.1 계층별 책임

```
Controller  → 요청 수신, 응답 반환. 비즈니스 로직 없음
Service     → 비즈니스 로직, 권한 검사, 트랜잭션
Repository  → DB 쿼리 (JPA / JPQL)
Entity      → DB 매핑 전용. 비즈니스 로직 없음
DTO         → 계층 간 데이터 전달. Entity 직접 노출 금지
```

### 5.2 네이밍 규칙

| 대상          | 규칙                                                   | 예시                                |
| ------------- | ------------------------------------------------------ | ----------------------------------- |
| Entity 클래스 | 도메인명 단수                                          | `Notice`, `FreePost`, `GalleryPost` |
| Repository    | `{Entity}Repository`                                   | `NoticeRepository`                  |
| Service       | `{Domain}Service`                                      | `NoticeService`                     |
| Controller    | `{Domain}Controller`                                   | `NoticeController`                  |
| DTO (요청)    | `{Domain}WriteRequestDto`, `{Domain}ListRequestDto`    | `NoticeWriteRequestDto`             |
| DTO (응답)    | `{Domain}DetailResponseDto`, `{Domain}ListResponseDto` | `NoticeDetailResponseDto`           |
| API 매핑      | `/api/{domain}` (소문자, 복수형 사용 안 함)            | `/api/notice`, `/api/free`          |

### 5.3 Controller 작성 패턴

```java
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<NoticeListResponseDto>>> getList(
            @ModelAttribute BoardListRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getList(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getDetail(id, userDetails)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @RequestBody @Valid NoticeWriteRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long id = noticeService.create(dto, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 등록되었습니다.", Map.of("id", id)));
    }
}
```

### 5.4 Service 권한 검사 패턴

```java
// 본인 또는 관리자 확인
if (!post.getUserId().equals(currentUserId) && !isAdmin(currentUser)) {
    throw new CustomException(ErrorCode.FORBIDDEN);
}

// 존재 확인
Notice notice = noticeRepository.findById(id)
    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

// 상태 확인 (문의 답변 완료 후 수정 불가)
if (inquiry.getAnswerStatus() == AnswerStatus.ANSWERED) {
    throw new CustomException(ErrorCode.ANSWERED_POST_FORBIDDEN);
}
```

### 5.5 multipart 요청 처리 패턴 (자유게시판, 갤러리)

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> create(
        @RequestPart("data") @Valid FreeWriteRequestDto dto,
        @RequestPart(value = "files", required = false) List<MultipartFile> files,
        @AuthenticationPrincipal UserDetails userDetails) { ... }
```

### 5.6 JPQL 동적 쿼리 패턴 (목록 조회)

```java
// Repository에서 JPQL + 조건부 WHERE 절 사용
@Query("SELECT f FROM FreePost f WHERE " +
       "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
       "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
       "(:category IS NULL OR f.category = :category) AND " +
       "(:searchText IS NULL OR f.title LIKE %:searchText% OR f.content LIKE %:searchText%) " +
       "ORDER BY f.createdAt DESC")
Page<FreePost> findByCondition(...);
```

---

## 6. 프론트엔드 코딩 컨벤션

### 6.1 Composition API 사용 원칙

모든 컴포넌트는 `<script setup>` 형태로 작성한다.

```vue
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const posts = ref([])
const isLoading = ref(false)

onMounted(async () => {
  await fetchPosts()
})

async function fetchPosts() { ... }
</script>
```

### 6.2 API 호출 패턴 (에러 처리 포함)

```javascript
// 모든 API 호출은 try-catch로 감싼다
async function deletePost() {
    try {
        await freeApi.remove(postId);
        toastStore.show("success", "게시글이 삭제되었습니다.");
        router.push("/boards/free");
    } catch (error) {
        const status = error.response?.status;
        const message = error.response?.data?.message;
        // 401은 Axios 인터셉터가 처리하므로 여기서 제외
        if (status !== 401) {
            toastStore.show("error", message || "오류가 발생했습니다.");
        }
    }
}
```

### 6.3 multipart 전송 패턴 (파일 업로드)

```javascript
// FormData 구성 패턴
async function submitPost() {
    const formData = new FormData();
    // JSON 데이터는 Blob으로 감싸서 Content-Type 명시
    formData.append(
        "data",
        new Blob([JSON.stringify({ category, title, content, deleteAttachmentIds })], { type: "application/json" }),
    );
    // 파일 배열 추가
    newFiles.value.forEach((file) => formData.append("files", file));

    await freeApi.create(formData);
}
```

### 6.4 네이밍 규칙

| 대상            | 규칙                       | 예시                             |
| --------------- | -------------------------- | -------------------------------- |
| 페이지 컴포넌트 | `{Domain}{Action}View.vue` | `NoticeListView.vue`             |
| 재사용 컴포넌트 | `{Domain}{Feature}.vue`    | `ImageCarousel.vue`              |
| Pinia 스토어    | `{domain}.js`              | `auth.js`                        |
| API 모듈        | `{domain}.js`              | `notice.js`                      |
| ref 변수        | camelCase                  | `postList`, `isLoading`          |
| 함수            | camelCase                  | `fetchPosts()`, `handleSubmit()` |

### 6.5 라우터 가드 메타 3종

```javascript
meta: {
    guestOnly: true;
} // 이미 로그인이면 / 로 리다이렉트
meta: {
    requiresAuth: true;
} // 비로그인이면 로그인 모달 + next(false)
meta: {
    requiresAdmin: true;
} // ADMIN 아니면 / 로 리다이렉트
```

### 6.6 Pinia 스토어 사용 패턴

```javascript
// 컴포넌트에서
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { useModalStore } from '@/stores/modal'

const authStore = useAuthStore()
const toastStore = useToastStore()
const modalStore = useModalStore()

// 권한 확인
if (!authStore.isLoggedIn) { ... }
if (authStore.isAdmin) { ... }

// 토스트
toastStore.show('success', '게시글이 등록되었습니다.')
toastStore.show('error', '오류가 발생했습니다.')
toastStore.show('info', '접근 권한이 없습니다.')

// 로그인 모달
modalStore.openLoginModal()
```

---

## 7. 주요 비즈니스 규칙 (구현 시 반드시 준수)

### 7.1 로그인 모달 vs 로그인 페이지

| 진입 경로                    | 처리 방식                           |
| ---------------------------- | ----------------------------------- |
| GNB `로그인` 버튼 클릭       | `/login` 페이지 이동                |
| 비회원이 회원 전용 기능 클릭 | 로그인 모달 팝업 (현재 페이지 유지) |

둘은 완전히 다른 컴포넌트다. 혼용 금지.

### 7.2 비밀글 접근 제어 (문의게시판)

목록에서 비밀글 클릭 시 프론트에서 1차 제어, 상세 API에서 2차 제어.

| 사용자       | 프론트 처리             | 백엔드 처리 |
| ------------ | ----------------------- | ----------- |
| 비회원       | 로그인 모달             | 401 반환    |
| 타인 회원    | 토스트 "접근 권한 없음" | 403 반환    |
| 본인 / ADMIN | 상세 이동               | 정상 반환   |

### 7.3 답변 완료 문의글 수정 불가

- 상세 페이지: `answerStatus === 'ANSWERED'`이면 수정 버튼 미표시
- 수정 페이지(`/boards/inquiry/modify/:id`) 진입 시: API 호출 후 403 응답이면 토스트 + `/boards/inquiry` 이동
- 백엔드: `InquiryService.update()`에서 ANSWERED 상태 확인 후 `throw new CustomException(ErrorCode.ANSWERED_POST_FORBIDDEN)`

### 7.4 공지사항 고정글 (isPinned)

- 목록에서 `isPinned=true` 행은 **정렬 조건과 무관하게** 항상 최상단 고정
- 번호 컬럼에 `-` 표시, 행 배경색 회색
- 쿼리: `ORDER BY is_pinned DESC, {orderColumn} {direction}`

### 7.5 갤러리 이미지 sort_order

- 작성 시: 업로드 순서대로 0, 1, 2... 부여
- 수정 시: 기존 이미지 삭제 후 → 남은 이미지 재정렬 → 새 이미지 이어붙임
- 목록 API 응답의 `thumbnailUrl`: `sort_order = 0` 이미지의 URL
- `additionalImageCount`: 전체 이미지 수 - 1 (0이하면 미표시)

### 7.6 자유게시판/갤러리 수정 시 파일 개수 검증

- 수정 요청 시: `(기존 파일 수 - 삭제할 파일 수) + 새 파일 수 ≤ 최대 개수` 검증
- 이 검증은 **백엔드 Service에서** 수행

### 7.7 조회수 처리

- 상세 API (`GET /api/{board}/{id}`) 호출 시 `ViewCountService.processView()` 실행
- 동일 `sessionId` + 동일 게시글이면 카운트 증가 없음
- `sessionId`는 Spring의 `HttpSession`에서 추출

### 7.8 댓글 등록 성공 시 UI 처리

- `POST /api/comments` 성공 응답에 새 댓글 전체 정보가 포함됨
- 댓글 목록 재조회 없이 응답 데이터를 목록 배열 끝에 직접 추가

### 7.9 게시글 삭제 순서 (Service 책임)

```
자유게시판 / 갤러리 삭제:
  1. AttachmentService.deleteAllByPost() → 로컬 파일 삭제 + DB 삭제
  2. commentRepository.deleteByBoardTypeAndPostId()
  3. 게시글 Repository.delete()

문의게시판 삭제:
  1. 게시글 Repository.delete() (inquiry_answer는 ON DELETE CASCADE 자동 삭제)
```

### 7.10 나의 문의내역 (`?my=true`)

- 비로그인으로 요청 시 백엔드에서 401 반환
- 프론트에서도 체크박스 클릭 시 미로그인이면 로그인 모달 우선 표시

---

## 8. 카테고리 값 매핑표 (DB ENUM ↔ 화면 표시)

| 게시판     | DB ENUM     | 화면 표시 |
| ---------- | ----------- | --------- |
| 공지사항   | `NOTICE`    | 공지      |
| 공지사항   | `EVENT`     | 이벤트    |
| 자유게시판 | `HUMOR`     | 유머      |
| 자유게시판 | `HOBBY`     | 취미      |
| 갤러리     | `FOOD`      | 음식      |
| 갤러리     | `CELEBRITY` | 연예인    |

프론트엔드에서 ENUM 값 → 한국어 변환은 각 컴포넌트에서 computed 또는 상수 맵으로 처리한다.

```javascript
// 예시
const CATEGORY_LABELS = {
    NOTICE: "공지",
    EVENT: "이벤트",
    HUMOR: "유머",
    HOBBY: "취미",
    FOOD: "음식",
    CELEBRITY: "연예인",
};
```

---

## 9. URL 파라미터 명세 (게시판 목록)

| 파라미터         | 기본값      | 설명                                             |
| ---------------- | ----------- | ------------------------------------------------ |
| `startDate`      | 오늘 - 1년  | `YYYY-MM-DD`                                     |
| `endDate`        | 오늘        | `YYYY-MM-DD`                                     |
| `category`       | 생략(전체)  | ENUM 값 (미입력 시 전체 조회)                    |
| `searchText`     | 없음        | 최소 2자 미만이면 전체 조회                      |
| `pageSize`       | `10`        | `10` / `20` / `30`                               |
| `orderValue`     | `createdAt` | `createdAt` / `title` / `viewCount` / `category` |
| `orderDirection` | `desc`      | `desc` / `asc`                                   |
| `pageNum`        | `1`         | 페이지 번호                                      |
| `my` (문의만)    | `false`     | `true`이면 본인 글만                             |

---

## 10. 환경 변수 / 설정 파일 위치

| 파일                                               | Git 포함 | 용도                                          |
| -------------------------------------------------- | -------- | --------------------------------------------- |
| `backend/src/main/resources/application.yml`       | ✅       | 포트, 파일 정책, JWT 만료시간, CORS, 로깅     |
| `backend/src/main/resources/application-local.yml` | ❌       | DB 접속정보, JWT Secret Key                   |
| `frontend/.env`                                    | ✅       | `VITE_API_BASE_URL=http://localhost:8080/api` |
| `frontend/.env.local`                              | ❌       | 로컬 오버라이드 (필요 시)                     |

`application-local.yml`이 없으면 백엔드가 기동되지 않는다. 없을 경우 env-spec.md 2.3을 참고해서 생성한다.

---

## 11. 주요 에러 코드 (빠른 참조)

| ErrorCode                 | HTTP | 메시지                                               | 사용 상황                |
| ------------------------- | ---- | ---------------------------------------------------- | ------------------------ |
| `INVALID_INPUT`           | 400  | 입력값이 올바르지 않습니다.                          | @Valid 실패, 필수값 누락 |
| `FILE_COUNT_EXCEEDED`     | 400  | 첨부파일은 최대 N개까지 업로드할 수 있습니다.        | 파일 개수 초과           |
| `FILE_SIZE_EXCEEDED`      | 400  | 파일 크기는 NMB를 초과할 수 없습니다.                | 파일 용량 초과           |
| `FILE_EXTENSION_INVALID`  | 400  | 이미지 파일만 업로드할 수 있습니다.                  | 갤러리 비이미지 파일     |
| `UNAUTHORIZED`            | 401  | 로그인이 필요합니다.                                 | JWT 없음 / 만료          |
| `LOGIN_FAILED`            | 401  | 아이디 또는 비밀번호가 올바르지 않습니다.            | 로그인 실패              |
| `FORBIDDEN`               | 403  | 접근 권한이 없습니다.                                | 타인 글 수정·삭제        |
| `SECRET_POST_FORBIDDEN`   | 403  | 비밀글은 작성자와 관리자만 열람할 수 있습니다.       | 타인 비밀글 접근         |
| `ANSWERED_POST_FORBIDDEN` | 403  | 답변이 완료된 문의는 수정할 수 없습니다.             | 답변 완료 문의 수정      |
| `POST_NOT_FOUND`          | 404  | 게시글을 찾을 수 없습니다.                           | 존재하지 않는 게시글     |
| `DUPLICATE_USERNAME`      | 409  | 이미 사용 중인 아이디입니다.                         | 아이디 중복              |
| `ANSWER_ALREADY_EXISTS`   | 409  | 이미 답변이 등록된 문의입니다.                       | 중복 답변 등록           |
| `SERVER_ERROR`            | 500  | 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 예상치 못한 예외         |

전체 에러 코드는 `error-spec.md` 2절을 참조한다.

---

## 12. 현재 구현 상태 추적

> 작업 시작 전 `plan.md`를 열어 현재 완료된 Phase를 확인한다.
> 각 Phase가 완료되면 `plan.md`의 해당 체크박스를 체크한다.

```
plan.md 위치: docs/plan.md (또는 프로젝트 루트)

Phase 1  [ ] 프로젝트 초기화
Phase 2  [ ] DB 설정
Phase 3  [ ] 백엔드 공통 기반
Phase 4  [ ] 백엔드 인증
Phase 5  [ ] 백엔드 게시판
Phase 6  [ ] 백엔드 공통 기능
Phase 7  [ ] 프론트엔드 공통 기반
Phase 8  [ ] 프론트엔드 인증
Phase 9  [ ] 프론트엔드 게시판
Phase 10 [ ] 프론트엔드 메인
Phase 11 [ ] 통합 확인
```

---

## 13. 스펙 문서 빠른 참조

| 궁금한 내용                              | 참조 문서              |
| ---------------------------------------- | ---------------------- |
| 페이지/컴포넌트 레이아웃, 버튼 표시 조건 | `docs/ui-spec.md`      |
| API 엔드포인트, 요청/응답 필드           | `docs/api-spec.md`     |
| DB 테이블 컬럼, DDL                      | `docs/db-schema.md`    |
| 에러 코드 전체 목록, 표시 방식           | `docs/error-spec.md`   |
| 환경 변수, 설정 파일 전체 내용           | `docs/env-spec.md`     |
| 사용자별 기능 권한 매트릭스              | `docs/prd.md` 5절      |
| 각 기능의 상세 흐름 (분기 조건)          | `docs/user-flow.md`    |
| 패키지 구조, 컴포넌트 구조               | `docs/architecture.md` |
| 구현 순서, 체크리스트                    | `plan.md`              |

---

## 14. 작업 전 체크리스트

새 작업을 시작하기 전에 아래를 확인한다.

- [ ] `plan.md`에서 현재 작업할 Phase와 항목을 확인했는가?
- [ ] 해당 기능의 API 스펙을 `api-spec.md`에서 확인했는가?
- [ ] 해당 페이지의 UI 레이아웃과 버튼 조건을 `ui-spec.md`에서 확인했는가?
- [ ] 에러 케이스를 `error-spec.md`에서 확인했는가?
- [ ] 백엔드라면: Controller → Service → Repository → Entity → DTO 순서로 작성하는가?
- [ ] 프론트엔드라면: API 모듈 → Store → 컴포넌트 → 뷰 순서로 작성하는가?
- [ ] 공통 응답 형식 `ApiResponse`를 사용하는가?
- [ ] 에러 발생 시 `CustomException(ErrorCode)`를 throw하는가?
- [ ] 파일 삭제 실패는 예외를 삼키고 로그만 기록하는가?
- [ ] `application-local.yml`이 `.gitignore`에 포함되어 있는가?
