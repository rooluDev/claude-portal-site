# ⚠️ Error Spec

## 김승현 포트폴리오 포털 사이트

---

## 목차

1. [에러 처리 원칙](#1-에러-처리-원칙)
2. [에러 코드 정의](#2-에러-코드-정의)
3. [백엔드 에러 처리 구조](#3-백엔드-에러-처리-구조)
4. [프론트엔드 에러 처리 구조](#4-프론트엔드-에러-처리-구조)
5. [프론트엔드 에러 표시 방식](#5-프론트엔드-에러-표시-방식)
6. [도메인별 에러 케이스 전체 목록](#6-도메인별-에러-케이스-전체-목록)
7. [유효성 검사 에러 메시지](#7-유효성-검사-에러-메시지)

---

## 1. 에러 처리 원칙

### 1.1 전체 원칙

| 원칙 | 설명 |
|------|------|
| **일관된 응답 형식** | 모든 에러는 `{ success: false, message: "...", data: null }` 형식으로 반환 |
| **사용자 친화적 메시지** | 기술적 에러 문구 대신 사용자가 이해할 수 있는 한국어 메시지 사용 |
| **노출 최소화** | 서버 내부 정보(스택 트레이스, SQL 등)는 응답에 절대 포함하지 않음 |
| **표시 방식 구분** | 에러 유형에 따라 인라인 메시지 / 토스트 / 페이지 이동으로 구분 |
| **이중 검증** | 프론트엔드 유효성 검사 후 백엔드에서도 동일하게 재검증 |

### 1.2 에러 표시 방식 결정 기준

| 표시 방식 | 사용 상황 |
|----------|----------|
| **인라인 메시지** | 폼 필드 유효성 오류 (회원가입, 글 작성 등) |
| **폼 상단 메시지** | 서버 유효성 오류 (중복 아이디 등) |
| **토스트 메시지** | 작성/수정/삭제 결과, 권한 오류 등 일시적 알림 |
| **페이지 이동** | 접근 불가 리소스(404), 권한 없는 페이지(403) 접근 시 |
| **모달** | 비회원이 회원 전용 기능 접근 (로그인 유도 모달) |

---

## 2. 에러 코드 정의

### 2.1 HTTP 상태코드 및 에러 코드

```java
// ErrorCode.java (enum)
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT           (400, "입력값이 올바르지 않습니다."),
    INVALID_CATEGORY        (400, "올바르지 않은 카테고리 값입니다."),
    INVALID_DATE_RANGE      (400, "날짜 범위가 올바르지 않습니다."),
    FILE_COUNT_EXCEEDED     (400, "첨부파일은 최대 %d개까지 업로드할 수 있습니다."),
    FILE_SIZE_EXCEEDED      (400, "파일 크기는 %dMB를 초과할 수 없습니다."),
    FILE_EXTENSION_INVALID  (400, "이미지 파일만 업로드할 수 있습니다."),
    COMMENT_TOO_LONG        (400, "댓글은 1000자를 초과할 수 없습니다."),

    // 401 Unauthorized
    UNAUTHORIZED            (401, "로그인이 필요합니다."),
    LOGIN_FAILED            (401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED           (401, "로그인 세션이 만료되었습니다. 다시 로그인해주세요."),

    // 403 Forbidden
    FORBIDDEN               (403, "접근 권한이 없습니다."),
    SECRET_POST_FORBIDDEN   (403, "비밀글은 작성자와 관리자만 열람할 수 있습니다."),
    ANSWERED_POST_FORBIDDEN (403, "답변이 완료된 문의는 수정할 수 없습니다."),

    // 404 Not Found
    NOT_FOUND               (404, "요청한 리소스를 찾을 수 없습니다."),
    POST_NOT_FOUND          (404, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND       (404, "댓글을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND    (404, "첨부파일을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND        (404, "답변을 찾을 수 없습니다."),
    FILE_NOT_FOUND          (404, "요청한 파일을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_USERNAME      (409, "이미 사용 중인 아이디입니다."),
    ANSWER_ALREADY_EXISTS   (409, "이미 답변이 등록된 문의입니다."),

    // 500 Internal Server Error
    SERVER_ERROR            (500, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    FILE_UPLOAD_FAILED      (500, "파일 업로드 중 오류가 발생했습니다."),
    FILE_DELETE_FAILED      (500, "파일 삭제 중 오류가 발생했습니다.");
}
```

---

### 2.2 에러 응답 형식

**모든 에러**는 아래 형식으로 통일 반환한다.

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

---

## 3. 백엔드 에러 처리 구조

### 3.1 GlobalExceptionHandler

`@RestControllerAdvice`로 전역 예외를 처리한다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외: ErrorCode에 정의된 에러 반환
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(e.getMessage()));
    }

    // Spring Validation 예외: 필드 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    // multipart 파일 크기 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleFileSizeException(
            MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("파일 크기는 제한 용량을 초과할 수 없습니다."));
    }

    // 그 외 모든 예외: 500 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        // 로그에는 스택 트레이스 기록, 응답에는 일반 메시지만
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
```

---

### 3.2 CustomException

```java
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

**사용 예시**

```java
// Service 레이어에서 사용
if (post == null) {
    throw new CustomException(ErrorCode.POST_NOT_FOUND);
}
if (!post.getUserId().equals(currentUserId) && !isAdmin) {
    throw new CustomException(ErrorCode.FORBIDDEN);
}
if (inquiry.getAnswerStatus() == AnswerStatus.ANSWERED) {
    throw new CustomException(ErrorCode.ANSWERED_POST_FORBIDDEN);
}
```

---

### 3.3 JwtAuthFilter 에러 처리

```
JWT 처리 흐름:
┌──────────────────────────────────────────────────────┐
│ Authorization 헤더 없음                               │
│   → SecurityContext 설정 없이 통과                    │
│   → 보호된 API 접근 시 Spring Security가 401 반환     │
├──────────────────────────────────────────────────────┤
│ JWT 형식 오류 (MalformedJwtException)                │
│   → SecurityContext 설정 없이 통과 → 401             │
├──────────────────────────────────────────────────────┤
│ JWT 만료 (ExpiredJwtException)                       │
│   → SecurityContext 설정 없이 통과 → 401             │
│   → message: "로그인 세션이 만료되었습니다."          │
├──────────────────────────────────────────────────────┤
│ JWT 서명 오류 (SignatureException)                   │
│   → SecurityContext 설정 없이 통과 → 401             │
└──────────────────────────────────────────────────────┘
```

Spring Security의 `AuthenticationEntryPoint`를 구현하여 401 응답도 동일한 JSON 형식으로 반환:

```java
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"success\":false,\"message\":\"로그인이 필요합니다.\",\"data\":null}"
        );
    }
}
```

---

## 4. 프론트엔드 에러 처리 구조

### 4.1 Axios 인터셉터 에러 처리

```javascript
// api/axios.js

// Response 인터셉터
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message;

    if (status === 401) {
      // JWT 만료 또는 미인증
      authStore.logout();                        // sessionStorage 토큰 삭제, Pinia 상태 초기화
      const currentPath = router.currentRoute.value.fullPath;
      const isProtectedPage = router.currentRoute.value.meta.requiresAuth
                           || router.currentRoute.value.meta.requiresAdmin;

      if (isProtectedPage) {
        // 보호된 페이지 → 로그인 페이지로 이동
        router.push(`/login?ret=${encodeURIComponent(currentPath)}`);
      } else {
        // 공개 페이지 → 로그인 모달 팝업
        modalStore.openLoginModal();
      }
    }
    // 401 외 에러는 각 호출부(컴포넌트/composable)에서 처리
    return Promise.reject(error);
  }
);
```

---

### 4.2 컴포넌트 레벨 에러 처리 패턴

각 API 호출은 `try-catch`로 감싸고, 에러 유형에 따라 처리한다.

```javascript
// 예시: 게시글 삭제
async function deletePost() {
  try {
    await freeApi.deletePost(postId);
    toast.show('success', '게시글이 삭제되었습니다.');
    router.push('/boards/free');
  } catch (error) {
    const status = error.response?.status;
    const message = error.response?.data?.message;

    if (status === 403) {
      toast.show('error', message || '접근 권한이 없습니다.');
    } else if (status === 404) {
      toast.show('error', '게시글을 찾을 수 없습니다.');
      router.push('/boards/free');
    } else if (status !== 401) {
      // 401은 인터셉터에서 처리하므로 여기서는 제외
      toast.show('error', message || '오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
  }
}
```

---

### 4.3 Vue Router 가드 에러 처리

```javascript
// router/index.js

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();

  // 이미 로그인된 사용자가 로그인/회원가입 페이지 접근
  if (to.meta.guestOnly && auth.isLoggedIn) {
    return next('/');
  }

  // 로그인 필요 페이지에 비로그인 접근
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    // 페이지 이동 자체를 막고 현재 페이지에서 로그인 모달 표시
    modalStore.openLoginModal();
    return next(false);
  }

  // 관리자 전용 페이지에 비관리자 접근
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return next('/');
  }

  next();
});
```

---

## 5. 프론트엔드 에러 표시 방식

### 5.1 인라인 에러 메시지

폼 필드 하단에 빨간 텍스트로 표시. 해당 필드가 수정되면 에러 메시지 초기화.

**사용 위치**: 회원가입, 공지사항 작성/수정, 자유게시판 작성/수정, 갤러리 작성/수정, 문의 작성/수정

```
아이디 *
[user]
⚠ 아이디는 영문과 숫자 조합 5~20자여야 합니다.

비밀번호 *
[pass]
⚠ 비밀번호는 영문과 숫자 조합 8자 이상이어야 합니다.
```

**트리거 시점**:
- 필드 `blur` 이벤트 (포커스 벗어날 때) → 해당 필드 유효성 검사
- 폼 제출 클릭 → 전체 필드 유효성 검사

---

### 5.2 폼 상단 에러 메시지

서버에서 반환한 에러 (중복 아이디 등) 폼 상단에 표시.

```
┌──────────────────────────────────┐
│ ⚠ 이미 사용 중인 아이디입니다.   │  ← 빨간 배경 박스
└──────────────────────────────────┘
  아이디 *
  [user01]
```

**사용 위치**: 회원가입 (409 응답), 로그인 실패 (401 응답)

---

### 5.3 토스트 메시지

화면 우하단 고정, 3초 후 자동 소멸.

| 타입 | 색상 | 사용 상황 |
|------|------|----------|
| `success` | 초록 | 게시글/댓글/답변 등록·수정·삭제 성공 |
| `error` | 빨강 | 403 권한 오류, 파일 오류, 500 서버 오류 |
| `info` | 파랑 | 비밀글 접근 차단 안내 |

**사용 위치 및 메시지**

| 상황 | 타입 | 메시지 |
|------|------|--------|
| 게시글 등록 성공 | success | 게시글이 등록되었습니다. |
| 게시글 수정 성공 | success | 게시글이 수정되었습니다. |
| 게시글 삭제 성공 | success | 게시글이 삭제되었습니다. |
| 댓글 등록 성공 | success | 댓글이 등록되었습니다. |
| 댓글 삭제 성공 | success | 댓글이 삭제되었습니다. |
| 답변 등록 성공 | success | 답변이 등록되었습니다. |
| 답변 수정 성공 | success | 답변이 수정되었습니다. |
| 답변 삭제 성공 | success | 답변이 삭제되었습니다. |
| 비밀글 타인 접근 | info | 접근 권한이 없습니다. |
| 403 에러 | error | 접근 권한이 없습니다. |
| 500 에러 | error | 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요. |
| 파일 업로드 실패 | error | 파일 업로드 중 오류가 발생했습니다. |

---

### 5.4 페이지 이동 (리다이렉트)

| 상황 | 이동 대상 | 이유 |
|------|----------|------|
| 보호 페이지 + 비로그인 접근 | `/login?ret={currentPath}` | requiresAuth 가드 |
| 보호 페이지 + JWT 만료 | `/login?ret={currentPath}` | 401 인터셉터 |
| 관리자 전용 페이지 + 비관리자 접근 | `/` | requiresAdmin 가드 |
| 이미 로그인 상태에서 `/login`, `/join` 접근 | `/` | guestOnly 가드 |
| 답변 완료 문의글 수정 페이지 직접 접근 | `/boards/inquiry` | 403 응답 수신 시 |
| 존재하지 않는 게시글 상세 | `/boards/{type}` (목록) | 404 응답 수신 시 |

---

### 5.5 로그인 모달 팝업

| 상황 | 트리거 |
|------|--------|
| 공개 페이지에서 회원 전용 기능 클릭 | 글 등록 버튼, 댓글 등록 버튼, 나의 문의내역 버튼 |
| 비밀글을 비회원이 클릭 | 문의게시판 목록/위젯에서 비밀글 클릭 |
| 공개 페이지에서 JWT 만료 후 보호 API 호출 | 401 인터셉터 (공개 페이지 판별 후) |

---

## 6. 도메인별 에러 케이스 전체 목록

---

### 6.1 인증

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `LOGIN_FAILED` | 401 | 아이디 또는 비밀번호 불일치 | AuthService에서 throw | 폼 상단 에러 메시지 |
| `DUPLICATE_USERNAME` | 409 | 중복 아이디로 회원가입 시도 | UserRepository 중복 확인 | 폼 상단 에러 메시지 |
| `INVALID_INPUT` | 400 | 회원가입 필드 유효성 실패 | @Valid 어노테이션 | 인라인 에러 메시지 |
| `UNAUTHORIZED` | 401 | JWT 없이 보호 API 접근 | JwtAuthEntryPoint | 로그인 모달 또는 /login 이동 |
| `TOKEN_EXPIRED` | 401 | 만료된 JWT로 보호 API 접근 | JwtAuthFilter | 로그인 모달 또는 /login 이동 |

---

### 6.2 공지사항

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 상태로 작성/수정/삭제 | JwtAuthEntryPoint | /login 이동 |
| `FORBIDDEN` | 403 | ADMIN 아닌 사용자가 작성/수정/삭제 | Spring Security hasRole | / 리다이렉트 (라우터 가드) |
| `INVALID_INPUT` | 400 | 필수값(category, title, content) 누락 | @Valid | 인라인 에러 메시지 |
| `INVALID_CATEGORY` | 400 | NOTICE/EVENT 외 category 값 전달 | Service 검증 | 인라인 에러 메시지 |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 공지사항 ID 접근 | NoticeService에서 throw | 토스트 후 목록 이동 |

---

### 6.3 자유게시판

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 작성/수정/삭제/댓글 시도 | JwtAuthEntryPoint | 로그인 모달 |
| `FORBIDDEN` | 403 | 타인 글 수정/삭제 시도 | FreeService 작성자 확인 | 토스트 에러 |
| `INVALID_INPUT` | 400 | 필수값(category, title, content) 누락 | @Valid | 인라인 에러 메시지 |
| `INVALID_CATEGORY` | 400 | HUMOR/HOBBY 외 category 값 | Service 검증 | 인라인 에러 메시지 |
| `FILE_COUNT_EXCEEDED` | 400 | 첨부파일 5개 초과 (신규+기존 합산 포함) | AttachmentService 검증 | 인라인 에러 메시지 |
| `FILE_SIZE_EXCEEDED` | 400 | 파일당 20MB 초과 | AttachmentService 검증 | 인라인 에러 메시지 |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 게시글 ID | FreeService에서 throw | 토스트 후 목록 이동 |
| `ATTACHMENT_NOT_FOUND` | 404 | 존재하지 않는 첨부파일 ID (수정 시) | AttachmentService에서 throw | 토스트 에러 |
| `FILE_UPLOAD_FAILED` | 500 | 파일 저장 중 IO 오류 | AttachmentService catch | 토스트 에러 |

---

### 6.4 갤러리

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 작성/수정/삭제/댓글 시도 | JwtAuthEntryPoint | 로그인 모달 |
| `FORBIDDEN` | 403 | 타인 글 수정/삭제 시도 | GalleryService 작성자 확인 | 토스트 에러 |
| `INVALID_INPUT` | 400 | 필수값(category, title, content) 누락 | @Valid | 인라인 에러 메시지 |
| `INVALID_CATEGORY` | 400 | FOOD/CELEBRITY 외 category 값 | Service 검증 | 인라인 에러 메시지 |
| `FILE_COUNT_EXCEEDED` | 400 | 이미지 10장 초과 (신규+기존 합산 포함) | AttachmentService 검증 | 인라인 에러 메시지 |
| `FILE_SIZE_EXCEEDED` | 400 | 이미지당 10MB 초과 | AttachmentService 검증 | 인라인 에러 메시지 |
| `FILE_EXTENSION_INVALID` | 400 | 이미지 외 파일 확장자 업로드 | AttachmentService 검증 | 인라인 에러 메시지 |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 게시글 ID | GalleryService에서 throw | 토스트 후 목록 이동 |
| `ATTACHMENT_NOT_FOUND` | 404 | 존재하지 않는 이미지 ID (수정 시) | AttachmentService에서 throw | 토스트 에러 |
| `FILE_UPLOAD_FAILED` | 500 | 이미지 저장 중 IO 오류 | AttachmentService catch | 토스트 에러 |

---

### 6.5 문의게시판

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 작성/수정/삭제 시도 | JwtAuthEntryPoint | 로그인 모달 |
| `UNAUTHORIZED` | 401 | 비로그인으로 비밀글 상세 접근 | InquiryService 검증 | 로그인 모달 |
| `SECRET_POST_FORBIDDEN` | 403 | 타인 비밀글 상세 접근 | InquiryService 검증 | 토스트 info 후 목록 유지 |
| `FORBIDDEN` | 403 | 타인 문의글 수정/삭제 시도 | InquiryService 작성자 확인 | 토스트 에러 |
| `ANSWERED_POST_FORBIDDEN` | 403 | 답변 완료 문의글 수정 시도 | InquiryService 상태 확인 | 토스트 후 목록 이동 |
| `INVALID_INPUT` | 400 | 필수값(title, content) 누락 | @Valid | 인라인 에러 메시지 |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 문의글 ID | InquiryService에서 throw | 토스트 후 목록 이동 |
| `ANSWER_NOT_FOUND` | 404 | 답변 없는 상태에서 수정/삭제 시도 | InquiryService에서 throw | 토스트 에러 |
| `UNAUTHORIZED` | 401 | 미로그인으로 `my=true` 목록 요청 | InquiryService 검증 | 로그인 모달 |

---

### 6.6 관리자 답변

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 답변 등록/수정/삭제 | JwtAuthEntryPoint | /login 이동 |
| `FORBIDDEN` | 403 | ADMIN 아닌 사용자 답변 시도 | Spring Security hasRole | 토스트 에러 |
| `INVALID_INPUT` | 400 | 답변 내용 누락 | @Valid | 인라인 에러 메시지 (답변 입력란 하단) |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 문의글 ID | InquiryService에서 throw | 토스트 에러 |
| `ANSWER_ALREADY_EXISTS` | 409 | 이미 답변 있는 문의글에 답변 등록 | InquiryService 검증 | 토스트 에러 |
| `ANSWER_NOT_FOUND` | 404 | 답변 없는 문의글 답변 수정/삭제 | InquiryService에서 throw | 토스트 에러 |

---

### 6.7 댓글

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `UNAUTHORIZED` | 401 | 미로그인 댓글 등록/삭제 시도 | JwtAuthEntryPoint | 로그인 모달 |
| `FORBIDDEN` | 403 | 타인 댓글 삭제 시도 | CommentService 작성자 확인 | 토스트 에러 |
| `INVALID_INPUT` | 400 | 댓글 내용 누락 | @Valid | 인라인 에러 메시지 |
| `COMMENT_TOO_LONG` | 400 | 댓글 1000자 초과 | @Valid `@Size` | 인라인 에러 메시지 |
| `POST_NOT_FOUND` | 404 | 존재하지 않는 게시글에 댓글 등록 | CommentService에서 throw | 토스트 에러 |
| `COMMENT_NOT_FOUND` | 404 | 존재하지 않는 댓글 삭제 | CommentService에서 throw | 토스트 에러 |

---

### 6.8 파일 서빙

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `FILE_NOT_FOUND` | 404 | 존재하지 않는 파일명 요청 | AttachmentController | 이미지 대체 이미지 표시 또는 빈 영역 |

> 갤러리 이미지 로딩 실패 시: `<img>`의 `@error` 이벤트로 대체 이미지 표시

---

### 6.9 공통 서버 에러

| 에러 코드 | HTTP | 발생 상황 | 백엔드 처리 | 프론트 표시 |
|----------|------|----------|------------|------------|
| `SERVER_ERROR` | 500 | 예상치 못한 서버 예외 | GlobalExceptionHandler 최종 catch | 토스트 에러 |
| `FILE_UPLOAD_FAILED` | 500 | 파일 저장 IO 오류 | try-catch 후 throw | 토스트 에러 |
| `FILE_DELETE_FAILED` | 500 | 파일 삭제 IO 오류 | try-catch 후 로그 기록 (비치명적: 게시글 삭제는 계속 진행) | 없음 (로그만 기록) |

> `FILE_DELETE_FAILED`는 게시글 삭제 트랜잭션과 별개로 처리. 로컬 파일 삭제 실패 시에도 DB 레코드는 삭제되어야 하므로 예외를 삼키고 로그만 남긴다.

---

## 7. 유효성 검사 에러 메시지

### 7.1 회원가입 폼

| 필드 | 조건 | 프론트 에러 메시지 | 서버 에러 메시지 |
|------|------|--------------------|-----------------|
| 아이디 | 공백 | 아이디를 입력해주세요. | 입력값이 올바르지 않습니다. |
| 아이디 | 영문+숫자 5~20자 위반 | 아이디는 영문과 숫자 조합 5~20자여야 합니다. | 입력값이 올바르지 않습니다. |
| 아이디 | 중복 (서버) | - | 이미 사용 중인 아이디입니다. |
| 비밀번호 | 공백 | 비밀번호를 입력해주세요. | 입력값이 올바르지 않습니다. |
| 비밀번호 | 영문+숫자 8자 이상 위반 | 비밀번호는 영문과 숫자 조합 8자 이상이어야 합니다. | 입력값이 올바르지 않습니다. |
| 비밀번호 확인 | 비밀번호 불일치 | 비밀번호가 일치하지 않습니다. | - |
| 이름 | 공백 | 이름을 입력해주세요. | 입력값이 올바르지 않습니다. |
| 이름 | 1~20자 위반 | 이름은 1~20자여야 합니다. | 입력값이 올바르지 않습니다. |

### 7.2 게시글 작성/수정 폼 (공통)

| 필드 | 조건 | 에러 메시지 |
|------|------|------------|
| 분류 | 미선택 | 분류를 선택해주세요. |
| 제목 | 공백 | 제목을 입력해주세요. |
| 제목 | 255자 초과 | 제목은 255자를 초과할 수 없습니다. |
| 내용 | 공백 | 내용을 입력해주세요. |

### 7.3 자유게시판 첨부파일

| 조건 | 에러 메시지 |
|------|------------|
| 5개 초과 | 첨부파일은 최대 5개까지 업로드할 수 있습니다. |
| 파일당 20MB 초과 | 파일 크기는 20MB를 초과할 수 없습니다. |

### 7.4 갤러리 이미지 첨부

| 조건 | 에러 메시지 |
|------|------------|
| 10장 초과 | 이미지는 최대 10장까지 업로드할 수 있습니다. |
| 장당 10MB 초과 | 파일 크기는 10MB를 초과할 수 없습니다. |
| 허용 외 확장자 | 이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp) |

### 7.5 댓글

| 조건 | 에러 메시지 |
|------|------------|
| 공백 | 댓글 내용을 입력해주세요. |
| 1000자 초과 | 댓글은 1000자를 초과할 수 없습니다. |

### 7.6 문의 작성/수정 폼

| 필드 | 조건 | 에러 메시지 |
|------|------|------------|
| 제목 | 공백 | 제목을 입력해주세요. |
| 내용 | 공백 | 내용을 입력해주세요. |

### 7.7 관리자 답변

| 조건 | 에러 메시지 |
|------|------------|
| 내용 공백 | 답변 내용을 입력해주세요. |

---

## 에러 처리 흐름 요약

```
사용자 액션 발생
    │
    ├─ [프론트 유효성 검사]
    │     실패 → 인라인 에러 메시지 표시 (API 호출 없음)
    │     성공 → API 호출
    │
    ├─ [API 호출 중 에러]
    │     400 → 인라인 에러 또는 토스트 에러
    │     401 → 로그인 모달 또는 /login 이동 (인터셉터 처리)
    │     403 → 토스트 에러 또는 / 이동
    │     404 → 토스트 에러 후 목록 페이지 이동
    │     409 → 폼 상단 에러 메시지
    │     500 → 토스트 에러
    │
    └─ [API 호출 성공]
          → 토스트 성공 메시지 표시 후 적절한 페이지 이동
```
