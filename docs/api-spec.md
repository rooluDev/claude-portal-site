# 📡 API Spec

## 김승현 포트폴리오 포털 사이트

---

## 목차

1. [공통 규약](#1-공통-규약)
2. [인증 API](#2-인증-api)
3. [메인 페이지 API](#3-메인-페이지-api)
4. [공지사항 API](#4-공지사항-api)
5. [자유게시판 API](#5-자유게시판-api)
6. [갤러리 API](#6-갤러리-api)
7. [문의게시판 API](#7-문의게시판-api)
8. [댓글 API](#8-댓글-api)
9. [파일 API](#9-파일-api)

---

## 1. 공통 규약

### 1.1 Base URL

```
http://localhost:8080/api
```

---

### 1.2 인증 헤더

로그인이 필요한 API는 모든 요청에 아래 헤더를 포함한다.

```
Authorization: Bearer {JWT_TOKEN}
```

---

### 1.3 공통 응답 형식

모든 API 응답은 아래 구조를 따른다.

```json
{
  "success": true | false,
  "message": "처리 결과 메시지",
  "data": { ... } | null
}
```

---

### 1.4 목록 API 공통 응답 (페이지네이션)

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

### 1.5 목록 API 공통 쿼리 파라미터

| 파라미터         | 타입    | 필수 | 기본값                    | 설명                                          |
| ---------------- | ------- | ---- | ------------------------- | --------------------------------------------- |
| `startDate`      | String  | N    | 오늘 기준 1년 전          | 검색 시작일 (`YYYY-MM-DD`)                    |
| `endDate`        | String  | N    | 오늘                      | 검색 종료일 (`YYYY-MM-DD`)                    |
| `category`       | String  | N    | 없음 (전체)               | 카테고리 코드 (전체 조회 시 파라미터 생략)    |
| `searchText`     | String  | N    | 없음                      | 검색 키워드 (최소 2자, 미만 시 전체 조회)     |
| `pageSize`       | Integer | N    | `10`                      | 페이지당 건수 (`10` / `20` / `30`)            |
| `orderValue`     | String  | N    | `createdAt`               | 정렬 기준 (`createdAt` / `title` / `viewCount` / `category`) |
| `orderDirection` | String  | N    | `desc`                    | 정렬 방향 (`desc` / `asc`)                   |
| `pageNum`        | Integer | N    | `1`                       | 페이지 번호                                   |

---

### 1.6 날짜 형식

| 용도           | 형식                  | 예시                  |
| -------------- | --------------------- | --------------------- |
| 요청 날짜 필터 | `YYYY-MM-DD`          | `2025-04-11`          |
| 응답 날짜/시간 | `YYYY.MM.DD HH:mm`    | `2025.04.11 14:30`    |

---

### 1.7 공통 에러 코드

| HTTP 상태 | 코드                | 메시지                                    | 발생 상황                          |
| --------- | ------------------- | ----------------------------------------- | ---------------------------------- |
| 400       | `INVALID_INPUT`     | 입력값이 올바르지 않습니다.               | 필수값 누락, 형식 오류             |
| 401       | `UNAUTHORIZED`      | 로그인이 필요합니다.                      | JWT 없음 / 만료                    |
| 403       | `FORBIDDEN`         | 접근 권한이 없습니다.                     | 권한 부족 (타인 글, 비밀글 등)     |
| 404       | `NOT_FOUND`         | 요청한 리소스를 찾을 수 없습니다.         | 존재하지 않는 리소스               |
| 409       | `DUPLICATE`         | 이미 사용 중인 아이디입니다.              | 아이디 중복 가입                   |
| 500       | `SERVER_ERROR`      | 서버 오류가 발생했습니다.                 | 서버 내부 오류                     |

에러 응답 예시:
```json
{
  "success": false,
  "message": "접근 권한이 없습니다.",
  "data": null
}
```

---

## 2. 인증 API

---

### 2.1 회원가입

```
POST /api/auth/join
```

**권한**: 전체 (비회원 포함)

**Request Body** (`application/json`)

```json
{
  "username": "user01",
  "password": "abc12345",
  "name": "홍길동"
}
```

| 필드       | 타입   | 필수 | 유효성 규칙                  |
| ---------- | ------ | ---- | ---------------------------- |
| `username` | String | Y    | 영문+숫자 조합, 5~20자       |
| `password` | String | Y    | 영문+숫자 조합, 8자 이상     |
| `name`     | String | Y    | 1~20자                       |

**Response 200 OK**

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황             | HTTP | message                          |
| ---------------- | ---- | -------------------------------- |
| 아이디 중복      | 409  | 이미 사용 중인 아이디입니다.     |
| 유효성 검사 실패 | 400  | 입력값이 올바르지 않습니다.      |

---

### 2.2 로그인

```
POST /api/auth/login
```

**권한**: 전체 (비회원 포함)

**Request Body** (`application/json`)

```json
{
  "username": "user01",
  "password": "abc12345"
}
```

| 필드       | 타입   | 필수 |
| ---------- | ------ | ---- |
| `username` | String | Y    |
| `password` | String | Y    |

**Response 200 OK**

```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIiLCJleHAiOjE3NDQzNjk0MDB9.xxxxx",
    "user": {
      "id": 1,
      "username": "user01",
      "name": "홍길동",
      "role": "USER"
    }
  }
}
```

| 응답 필드       | 타입   | 설명                              |
| --------------- | ------ | --------------------------------- |
| `token`         | String | JWT (유효시간 2시간)              |
| `user.id`       | Long   | 사용자 DB id                      |
| `user.username` | String | 로그인 아이디                     |
| `user.name`     | String | 표시 이름 (GNB에 표시)            |
| `user.role`     | String | 권한 (`USER` / `ADMIN`)           |

**에러 케이스**

| 상황                    | HTTP | message                                      |
| ----------------------- | ---- | -------------------------------------------- |
| 아이디 또는 비밀번호 틀림| 401  | 아이디 또는 비밀번호가 올바르지 않습니다.    |

---

## 3. 메인 페이지 API

---

### 3.1 메인 위젯 데이터 조회

```
GET /api/main
```

**권한**: 전체 (비회원 포함)

**Query Parameters**: 없음

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "notice": [
      {
        "id": 10,
        "category": "NOTICE",
        "categoryLabel": "공지",
        "isPinned": true,
        "title": "2025년 운영 공지사항",
        "createdAt": "2025.04.11 10:00"
      }
    ],
    "free": [
      {
        "id": 55,
        "category": "HUMOR",
        "categoryLabel": "유머",
        "title": "재미있는 이야기",
        "commentCount": 3,
        "hasAttachment": true,
        "createdAt": "2025.04.11 09:30"
      }
    ],
    "gallery": [
      {
        "id": 21,
        "category": "FOOD",
        "categoryLabel": "음식",
        "title": "오늘 점심",
        "thumbnailUrl": "/api/files/uuid-thumbnail.jpg",
        "additionalImageCount": 2,
        "createdAt": "2025.04.10 18:00"
      }
    ],
    "inquiry": [
      {
        "id": 33,
        "title": "문의드립니다",
        "isSecret": false,
        "answerStatus": "PENDING",
        "answerStatusLabel": "미답변",
        "createdAt": "2025.04.09 11:00"
      }
    ]
  }
}
```

| 위젯      | 반환 건수 | 정렬      |
| --------- | --------- | --------- |
| notice    | 최신 6건  | createdAt DESC (isPinned DESC 우선) |
| free      | 최신 6건  | createdAt DESC |
| gallery   | 최신 4건  | createdAt DESC |
| inquiry   | 최신 6건  | createdAt DESC |

> `inquiry` 목록에서 `isSecret=true`인 글도 포함하여 반환 (제목은 표시, 클릭 접근 제어는 프론트/상세 API에서 처리)

---

## 4. 공지사항 API

---

### 4.1 공지사항 목록 조회

```
GET /api/notice
```

**권한**: 전체 (비회원 포함)

**Query Parameters**: [공통 파라미터 1.5 참조]

| 파라미터   | category 허용값           |
| ---------- | ------------------------- |
| `category` | `NOTICE` / `EVENT` / 생략(전체) |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "category": "NOTICE",
        "categoryLabel": "공지",
        "isPinned": true,
        "title": "2025년 운영 공지",
        "viewCount": 320,
        "createdAt": "2025.04.11 10:00",
        "authorName": "관리자"
      },
      {
        "id": 5,
        "category": "EVENT",
        "categoryLabel": "이벤트",
        "isPinned": false,
        "title": "봄맞이 이벤트",
        "viewCount": 150,
        "createdAt": "2025.04.09 14:00",
        "authorName": "관리자"
      }
    ],
    "totalCount": 25,
    "totalPages": 3,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

> `isPinned=true` 항목은 목록 상단에 항상 고정 (정렬 조건 무관)
> 고정 공지는 응답 `content` 배열의 앞부분에 위치하며, 번호 없이 표시됨 (프론트에서 `isPinned` 값으로 구분)

---

### 4.2 공지사항 상세 조회

```
GET /api/notice/{id}
```

**권한**: 전체 (비회원 포함)

**Path Parameters**

| 파라미터 | 타입 | 설명         |
| -------- | ---- | ------------ |
| `id`     | Long | 게시글 ID    |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "id": 1,
    "category": "NOTICE",
    "categoryLabel": "공지",
    "isPinned": true,
    "title": "2025년 운영 공지",
    "content": "안녕하세요. 운영 관련 공지사항입니다...",
    "viewCount": 321,
    "createdAt": "2025.04.11 10:00",
    "updatedAt": "2025.04.11 10:00",
    "authorName": "관리자",
    "isEditable": true
  }
}
```

| 응답 필드    | 타입    | 설명                                            |
| ------------ | ------- | ----------------------------------------------- |
| `isEditable` | Boolean | 수정/삭제 버튼 표시 여부 (ADMIN이면 `true`)     |

> 호출 시 조회수 +1 처리 (동일 세션 중복 방지)

**에러 케이스**

| 상황               | HTTP | message                          |
| ------------------ | ---- | -------------------------------- |
| 존재하지 않는 ID   | 404  | 요청한 리소스를 찾을 수 없습니다. |

---

### 4.3 공지사항 작성

```
POST /api/notice
```

**권한**: ADMIN

**Request Body** (`application/json`)

```json
{
  "category": "NOTICE",
  "isPinned": false,
  "title": "공지사항 제목",
  "content": "공지사항 본문 내용입니다."
}
```

| 필드       | 타입    | 필수 | 허용값                  |
| ---------- | ------- | ---- | ----------------------- |
| `category` | String  | Y    | `NOTICE` / `EVENT`      |
| `isPinned` | Boolean | Y    | `true` / `false`        |
| `title`    | String  | Y    | 최대 255자              |
| `content`  | String  | Y    | 제한 없음               |

**Response 201 Created**

```json
{
  "success": true,
  "message": "게시글이 등록되었습니다.",
  "data": {
    "id": 26
  }
}
```

**에러 케이스**

| 상황             | HTTP | message                        |
| ---------------- | ---- | ------------------------------ |
| 미인증           | 401  | 로그인이 필요합니다.           |
| ADMIN 아님       | 403  | 접근 권한이 없습니다.          |
| 필수값 누락      | 400  | 입력값이 올바르지 않습니다.    |

---

### 4.4 공지사항 수정

```
PUT /api/notice/{id}
```

**권한**: ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Request Body** (`application/json`)

```json
{
  "category": "EVENT",
  "isPinned": true,
  "title": "수정된 제목",
  "content": "수정된 본문 내용입니다."
}
```

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 수정되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                 | HTTP | message                          |
| -------------------- | ---- | -------------------------------- |
| 미인증               | 401  | 로그인이 필요합니다.             |
| ADMIN 아님           | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID     | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

### 4.5 공지사항 삭제

```
DELETE /api/notice/{id}
```

**권한**: ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 삭제되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                 | HTTP | message                          |
| -------------------- | ---- | -------------------------------- |
| 미인증               | 401  | 로그인이 필요합니다.             |
| ADMIN 아님           | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID     | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

## 5. 자유게시판 API

---

### 5.1 자유게시판 목록 조회

```
GET /api/free
```

**권한**: 전체 (비회원 포함)

**Query Parameters**: [공통 파라미터 1.5 참조]

| 파라미터   | category 허용값            |
| ---------- | -------------------------- |
| `category` | `HUMOR` / `HOBBY` / 생략(전체) |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 55,
        "category": "HUMOR",
        "categoryLabel": "유머",
        "title": "재미있는 이야기",
        "commentCount": 3,
        "hasAttachment": true,
        "viewCount": 88,
        "createdAt": "2025.04.11 09:30",
        "authorName": "홍길동"
      }
    ],
    "totalCount": 42,
    "totalPages": 5,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

| 응답 필드       | 타입    | 설명                                |
| --------------- | ------- | ----------------------------------- |
| `commentCount`  | Integer | 댓글 수 (목록에서 `(N)` 표시용)     |
| `hasAttachment` | Boolean | 첨부파일 여부 (📎 아이콘 표시용)   |

---

### 5.2 자유게시판 상세 조회

```
GET /api/free/{id}
```

**권한**: 전체 (비회원 포함)

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "id": 55,
    "category": "HUMOR",
    "categoryLabel": "유머",
    "title": "재미있는 이야기",
    "content": "오늘 있었던 재미있는 일인데요...",
    "viewCount": 89,
    "createdAt": "2025.04.11 09:30",
    "updatedAt": "2025.04.11 09:30",
    "authorId": 3,
    "authorName": "홍길동",
    "isEditable": true,
    "attachments": [
      {
        "id": 12,
        "originalName": "funny_image.jpg",
        "storedName": "uuid-funny_image.jpg",
        "fileSize": 204800,
        "fileUrl": "/api/files/uuid-funny_image.jpg"
      }
    ]
  }
}
```

| 응답 필드    | 타입    | 설명                                                    |
| ------------ | ------- | ------------------------------------------------------- |
| `authorId`   | Long    | 작성자 ID (프론트에서 현재 로그인 사용자와 비교하여 수정/삭제 버튼 표시 결정) |
| `isEditable` | Boolean | 수정/삭제 가능 여부 (본인 또는 ADMIN이면 `true`)        |

> 호출 시 조회수 +1 처리 (동일 세션 중복 방지)

**에러 케이스**

| 상황               | HTTP | message                          |
| ------------------ | ---- | -------------------------------- |
| 존재하지 않는 ID   | 404  | 요청한 리소스를 찾을 수 없습니다. |

---

### 5.3 자유게시판 글 작성

```
POST /api/free
```

**권한**: USER / ADMIN

**Request** (`multipart/form-data`)

| Part   | 타입                | 필수 | 설명                                              |
| ------ | ------------------- | ---- | ------------------------------------------------- |
| `data` | JSON (application/json) | Y | 게시글 정보                                   |
| `files`| 파일 배열           | N    | 첨부파일 (최대 5개, 파일당 최대 20MB)             |

`data` JSON 구조:

```json
{
  "category": "HUMOR",
  "title": "재미있는 이야기",
  "content": "오늘 있었던 재미있는 일인데요..."
}
```

| 필드       | 타입   | 필수 | 허용값              |
| ---------- | ------ | ---- | ------------------- |
| `category` | String | Y    | `HUMOR` / `HOBBY`   |
| `title`    | String | Y    | 최대 255자          |
| `content`  | String | Y    | 제한 없음           |

**Response 201 Created**

```json
{
  "success": true,
  "message": "게시글이 등록되었습니다.",
  "data": {
    "id": 56
  }
}
```

**에러 케이스**

| 상황                    | HTTP | message                                      |
| ----------------------- | ---- | -------------------------------------------- |
| 미인증                  | 401  | 로그인이 필요합니다.                         |
| 필수값 누락             | 400  | 입력값이 올바르지 않습니다.                  |
| 파일 개수 초과          | 400  | 첨부파일은 최대 5개까지 업로드할 수 있습니다.|
| 파일 용량 초과          | 400  | 파일 크기는 20MB를 초과할 수 없습니다.       |

---

### 5.4 자유게시판 글 수정

```
PUT /api/free/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Request** (`multipart/form-data`)

| Part   | 타입                | 필수 | 설명                      |
| ------ | ------------------- | ---- | ------------------------- |
| `data` | JSON (application/json) | Y | 수정 정보              |
| `files`| 파일 배열           | N    | 새로 추가할 파일          |

`data` JSON 구조:

```json
{
  "category": "HOBBY",
  "title": "수정된 제목",
  "content": "수정된 본문 내용입니다.",
  "deleteAttachmentIds": [12, 15]
}
```

| 필드                  | 타입      | 필수 | 설명                                |
| --------------------- | --------- | ---- | ----------------------------------- |
| `category`            | String    | Y    | `HUMOR` / `HOBBY`                   |
| `title`               | String    | Y    | 최대 255자                          |
| `content`             | String    | Y    | 제한 없음                           |
| `deleteAttachmentIds` | Long[]    | N    | 삭제할 기존 첨부파일 ID 목록        |

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 수정되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                        | HTTP | message                          |
| --------------------------- | ---- | -------------------------------- |
| 미인증                      | 401  | 로그인이 필요합니다.             |
| 본인 글 아님 (USER)         | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID            | 404  | 요청한 리소스를 찾을 수 없습니다.|
| 파일 최대 개수 초과 (기존+신규) | 400 | 첨부파일은 최대 5개까지 업로드할 수 있습니다. |

---

### 5.5 자유게시판 글 삭제

```
DELETE /api/free/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 삭제되었습니다.",
  "data": null
}
```

> 삭제 순서: 첨부파일(로컬+DB) → 댓글(DB) → 게시글(DB)

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| 본인 글 아님 (USER)   | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID      | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

## 6. 갤러리 API

---

### 6.1 갤러리 목록 조회

```
GET /api/gallery
```

**권한**: 전체 (비회원 포함)

**Query Parameters**: [공통 파라미터 1.5 참조]

| 파라미터   | category 허용값                   |
| ---------- | ---------------------------------- |
| `category` | `FOOD` / `CELEBRITY` / 생략(전체) |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 21,
        "category": "FOOD",
        "categoryLabel": "음식",
        "title": "오늘 점심",
        "contentPreview": "오늘은 파스타를 먹었는데...",
        "thumbnailUrl": "/api/files/uuid-thumbnail.jpg",
        "additionalImageCount": 2,
        "viewCount": 45,
        "createdAt": "2025.04.10 18:00",
        "authorName": "홍길동"
      }
    ],
    "totalCount": 18,
    "totalPages": 2,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

| 응답 필드              | 타입    | 설명                                                        |
| ---------------------- | ------- | ----------------------------------------------------------- |
| `contentPreview`       | String  | 본문 미리보기 (최대 100자, 이미지 없는 카드에 표시)         |
| `thumbnailUrl`         | String  | 첫 번째 이미지 URL (`null`이면 텍스트 카드로 렌더링)        |
| `additionalImageCount` | Integer | 첫 이미지 외 추가 이미지 수 (`+N` 표시용, 0이면 표시 안 함) |

---

### 6.2 갤러리 상세 조회

```
GET /api/gallery/{id}
```

**권한**: 전체 (비회원 포함)

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "id": 21,
    "category": "FOOD",
    "categoryLabel": "음식",
    "title": "오늘 점심",
    "content": "오늘은 파스타를 먹었는데 정말 맛있었어요...",
    "viewCount": 46,
    "createdAt": "2025.04.10 18:00",
    "updatedAt": "2025.04.10 18:00",
    "authorId": 3,
    "authorName": "홍길동",
    "isEditable": false,
    "images": [
      {
        "id": 7,
        "originalName": "lunch.jpg",
        "storedName": "uuid-lunch.jpg",
        "fileUrl": "/api/files/uuid-lunch.jpg",
        "sortOrder": 0
      },
      {
        "id": 8,
        "originalName": "dessert.png",
        "storedName": "uuid-dessert.png",
        "fileUrl": "/api/files/uuid-dessert.png",
        "sortOrder": 1
      }
    ]
  }
}
```

| 응답 필드       | 타입    | 설명                                            |
| --------------- | ------- | ----------------------------------------------- |
| `images`        | Array   | 첨부 이미지 목록 (`sortOrder` 오름차순 정렬)     |
| `images[].sortOrder` | Integer | 캐러셀 표시 순서 (0부터 시작)           |

> 호출 시 조회수 +1 처리 (동일 세션 중복 방지)

**에러 케이스**

| 상황               | HTTP | message                          |
| ------------------ | ---- | -------------------------------- |
| 존재하지 않는 ID   | 404  | 요청한 리소스를 찾을 수 없습니다. |

---

### 6.3 갤러리 글 작성

```
POST /api/gallery
```

**권한**: USER / ADMIN

**Request** (`multipart/form-data`)

| Part   | 타입                | 필수 | 설명                                                |
| ------ | ------------------- | ---- | --------------------------------------------------- |
| `data` | JSON (application/json) | Y | 게시글 정보                                     |
| `files`| 이미지 파일 배열    | N    | 이미지 첨부 (최대 10장, 장당 10MB, 이미지 확장자만) |

`data` JSON 구조:

```json
{
  "category": "FOOD",
  "title": "오늘 점심",
  "content": "오늘은 파스타를 먹었는데 정말 맛있었어요..."
}
```

| 필드       | 타입   | 필수 | 허용값                  |
| ---------- | ------ | ---- | ----------------------- |
| `category` | String | Y    | `FOOD` / `CELEBRITY`    |
| `title`    | String | Y    | 최대 255자              |
| `content`  | String | Y    | 제한 없음               |

> 이미지 허용 확장자: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
> 업로드 순서대로 `sort_order` 0, 1, 2... 부여

**Response 201 Created**

```json
{
  "success": true,
  "message": "게시글이 등록되었습니다.",
  "data": {
    "id": 22
  }
}
```

**에러 케이스**

| 상황              | HTTP | message                                       |
| ----------------- | ---- | --------------------------------------------- |
| 미인증            | 401  | 로그인이 필요합니다.                          |
| 이미지 개수 초과  | 400  | 이미지는 최대 10장까지 업로드할 수 있습니다.  |
| 이미지 용량 초과  | 400  | 파일 크기는 10MB를 초과할 수 없습니다.        |
| 허용되지 않는 확장자 | 400 | 이미지 파일만 업로드할 수 있습니다.         |

---

### 6.4 갤러리 글 수정

```
PUT /api/gallery/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Request** (`multipart/form-data`)

| Part   | 타입                | 필수 | 설명             |
| ------ | ------------------- | ---- | ---------------- |
| `data` | JSON (application/json) | Y | 수정 정보    |
| `files`| 이미지 파일 배열    | N    | 새로 추가할 이미지 |

`data` JSON 구조:

```json
{
  "category": "CELEBRITY",
  "title": "수정된 제목",
  "content": "수정된 본문 내용입니다.",
  "deleteAttachmentIds": [7]
}
```

| 필드                  | 타입   | 필수 | 설명                                       |
| --------------------- | ------ | ---- | ------------------------------------------ |
| `category`            | String | Y    | `FOOD` / `CELEBRITY`                       |
| `title`               | String | Y    | 최대 255자                                 |
| `content`             | String | Y    | 제한 없음                                  |
| `deleteAttachmentIds` | Long[] | N    | 개별 삭제할 기존 이미지 ID 목록            |

> 새 이미지는 기존 이미지(삭제 후) 뒤에 `sort_order` 이어서 부여

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 수정되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                            | HTTP | message                                      |
| ------------------------------- | ---- | -------------------------------------------- |
| 미인증                          | 401  | 로그인이 필요합니다.                         |
| 본인 글 아님 (USER)             | 403  | 접근 권한이 없습니다.                        |
| 존재하지 않는 ID                | 404  | 요청한 리소스를 찾을 수 없습니다.            |
| 이미지 합계 10장 초과           | 400  | 이미지는 최대 10장까지 업로드할 수 있습니다. |

---

### 6.5 갤러리 글 삭제

```
DELETE /api/gallery/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "게시글이 삭제되었습니다.",
  "data": null
}
```

> 삭제 순서: 이미지 파일(로컬+DB) → 댓글(DB) → 게시글(DB)

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| 본인 글 아님 (USER)   | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID      | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

## 7. 문의게시판 API

---

### 7.1 문의게시판 목록 조회

```
GET /api/inquiry
```

**권한**: 전체 (비회원 포함)

**Query Parameters**: [공통 파라미터 1.5 참조] + 아래 추가

| 파라미터 | 타입    | 필수 | 기본값  | 설명                                                 |
| -------- | ------- | ---- | ------- | ---------------------------------------------------- |
| `my`     | Boolean | N    | `false` | `true`이면 본인 작성 글만 조회 (로그인 필요)         |

> `category` 파라미터 미지원 (문의게시판은 카테고리 없음)
> `orderValue`는 `createdAt`만 지원

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "content": [
      {
        "id": 33,
        "title": "문의드립니다",
        "isSecret": false,
        "answerStatus": "PENDING",
        "answerStatusLabel": "미답변",
        "viewCount": 5,
        "createdAt": "2025.04.09 11:00",
        "authorName": "홍길동"
      },
      {
        "id": 32,
        "title": "비밀 문의",
        "isSecret": true,
        "answerStatus": "ANSWERED",
        "answerStatusLabel": "답변완료",
        "viewCount": 2,
        "createdAt": "2025.04.08 09:00",
        "authorName": "김철수"
      }
    ],
    "totalCount": 20,
    "totalPages": 2,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

> 비밀글(`isSecret=true`)도 목록에 포함하여 반환 (제목 표시, 접근 제어는 상세 API에서 처리)
> `my=true` 요청 시 JWT 없으면 401 반환

---

### 7.2 문의게시판 상세 조회

```
GET /api/inquiry/{id}
```

**권한**: 전체 (비밀글은 본인 / ADMIN만)

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": {
    "id": 33,
    "title": "문의드립니다",
    "content": "사이트 이용 중 문의 드립니다...",
    "isSecret": false,
    "answerStatus": "ANSWERED",
    "answerStatusLabel": "답변완료",
    "viewCount": 6,
    "createdAt": "2025.04.09 11:00",
    "updatedAt": "2025.04.09 11:00",
    "authorId": 3,
    "authorName": "홍길동",
    "isEditable": true,
    "answer": {
      "id": 10,
      "content": "안녕하세요. 문의 주신 내용에 대해 답변드립니다...",
      "adminName": "관리자",
      "createdAt": "2025.04.10 10:00",
      "updatedAt": "2025.04.10 10:00"
    }
  }
}
```

| 응답 필드    | 타입    | 설명                                                               |
| ------------ | ------- | ------------------------------------------------------------------ |
| `isEditable` | Boolean | 수정/삭제 가능 여부 (본인+미답변이면 수정 가능, 본인이면 삭제 가능) |
| `answer`     | Object  | 답변 정보 (`answerStatus=ANSWERED`이면 포함, `PENDING`이면 `null`) |

> 호출 시 조회수 +1 처리 (동일 세션 중복 방지)

**에러 케이스**

| 상황                           | HTTP | message                          |
| ------------------------------ | ---- | -------------------------------- |
| 존재하지 않는 ID               | 404  | 요청한 리소스를 찾을 수 없습니다. |
| 비밀글 + 미인증                | 401  | 로그인이 필요합니다.             |
| 비밀글 + 타인 회원             | 403  | 접근 권한이 없습니다.            |

---

### 7.3 문의게시판 글 작성

```
POST /api/inquiry
```

**권한**: USER / ADMIN

**Request Body** (`application/json`)

```json
{
  "title": "문의드립니다",
  "content": "사이트 이용 중 문의 드립니다...",
  "isSecret": true
}
```

| 필드       | 타입    | 필수 | 설명                             |
| ---------- | ------- | ---- | -------------------------------- |
| `title`    | String  | Y    | 최대 255자                       |
| `content`  | String  | Y    | 제한 없음                        |
| `isSecret` | Boolean | Y    | `true`이면 작성자+관리자만 열람  |

**Response 201 Created**

```json
{
  "success": true,
  "message": "문의가 등록되었습니다.",
  "data": {
    "id": 34
  }
}
```

**에러 케이스**

| 상황         | HTTP | message                     |
| ------------ | ---- | --------------------------- |
| 미인증       | 401  | 로그인이 필요합니다.        |
| 필수값 누락  | 400  | 입력값이 올바르지 않습니다. |

---

### 7.4 문의게시판 글 수정

```
PUT /api/inquiry/{id}
```

**권한**: USER(본인, 미답변 상태만)

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Request Body** (`application/json`)

```json
{
  "title": "수정된 문의 제목",
  "content": "수정된 문의 내용입니다.",
  "isSecret": false
}
```

**Response 200 OK**

```json
{
  "success": true,
  "message": "문의가 수정되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                      | HTTP | message                          |
| ------------------------- | ---- | -------------------------------- |
| 미인증                    | 401  | 로그인이 필요합니다.             |
| 본인 글 아님              | 403  | 접근 권한이 없습니다.            |
| 답변 완료된 글            | 403  | 답변이 완료된 문의는 수정할 수 없습니다. |
| 존재하지 않는 ID          | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

### 7.5 문의게시판 글 삭제

```
DELETE /api/inquiry/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 게시글 ID |

**Response 200 OK**

```json
{
  "success": true,
  "message": "문의가 삭제되었습니다.",
  "data": null
}
```

> 게시글 삭제 시 `inquiry_answer`는 `ON DELETE CASCADE`로 자동 삭제

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| 본인 글 아님 (USER)   | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID      | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

### 7.6 관리자 답변 등록

```
POST /api/inquiry/{id}/answer
```

**권한**: ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명            |
| -------- | ---- | --------------- |
| `id`     | Long | 문의게시글 ID   |

**Request Body** (`application/json`)

```json
{
  "content": "안녕하세요. 문의 주신 내용에 대해 답변드립니다..."
}
```

| 필드      | 타입   | 필수 |
| --------- | ------ | ---- |
| `content` | String | Y    |

**Response 201 Created**

```json
{
  "success": true,
  "message": "답변이 등록되었습니다.",
  "data": null
}
```

> 답변 등록 시 `inquiry.answer_status` → `ANSWERED` 로 자동 업데이트

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| ADMIN 아님            | 403  | 접근 권한이 없습니다.            |
| 이미 답변 있음        | 409  | 이미 답변이 등록된 문의입니다.   |
| 존재하지 않는 ID      | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

### 7.7 관리자 답변 수정

```
PUT /api/inquiry/{id}/answer
```

**권한**: ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명            |
| -------- | ---- | --------------- |
| `id`     | Long | 문의게시글 ID   |

**Request Body** (`application/json`)

```json
{
  "content": "수정된 답변 내용입니다."
}
```

**Response 200 OK**

```json
{
  "success": true,
  "message": "답변이 수정되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| ADMIN 아님            | 403  | 접근 권한이 없습니다.            |
| 답변 없음             | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

### 7.8 관리자 답변 삭제

```
DELETE /api/inquiry/{id}/answer
```

**권한**: ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명            |
| -------- | ---- | --------------- |
| `id`     | Long | 문의게시글 ID   |

**Response 200 OK**

```json
{
  "success": true,
  "message": "답변이 삭제되었습니다.",
  "data": null
}
```

> 답변 삭제 시 `inquiry.answer_status` → `PENDING` 으로 자동 업데이트

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| ADMIN 아님            | 403  | 접근 권한이 없습니다.            |
| 답변 없음             | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

## 8. 댓글 API

---

### 8.1 댓글 목록 조회

```
GET /api/comments?boardType={boardType}&postId={postId}
```

**권한**: 전체 (비회원 포함)

**Query Parameters**

| 파라미터    | 타입   | 필수 | 설명                          |
| ----------- | ------ | ---- | ----------------------------- |
| `boardType` | String | Y    | `FREE` / `GALLERY`            |
| `postId`    | Long   | Y    | 게시글 ID                     |

**Response 200 OK**

```json
{
  "success": true,
  "message": "요청이 처리되었습니다.",
  "data": [
    {
      "id": 101,
      "content": "재미있는 글이네요!",
      "authorId": 3,
      "authorName": "홍길동",
      "createdAt": "2025.04.11 10:15",
      "isDeletable": true
    },
    {
      "id": 102,
      "content": "저도 공감해요",
      "authorId": 5,
      "authorName": "김철수",
      "createdAt": "2025.04.11 10:30",
      "isDeletable": false
    }
  ]
}
```

| 응답 필드     | 타입    | 설명                                                   |
| ------------- | ------- | ------------------------------------------------------ |
| `authorId`    | Long    | 작성자 ID (프론트에서 삭제 버튼 표시 여부 판단에 사용) |
| `isDeletable` | Boolean | 삭제 가능 여부 (본인 또는 ADMIN이면 `true`)            |

> 응답은 페이지네이션 없이 전체 목록 반환 (게시글당 댓글 수가 제한적인 서비스 규모)
> `createdAt` 오름차순 정렬 (등록순)

---

### 8.2 댓글 등록

```
POST /api/comments
```

**권한**: USER / ADMIN

**Request Body** (`application/json`)

```json
{
  "boardType": "FREE",
  "postId": 55,
  "content": "재미있는 글이네요!"
}
```

| 필드        | 타입   | 필수 | 설명                       |
| ----------- | ------ | ---- | -------------------------- |
| `boardType` | String | Y    | `FREE` / `GALLERY`         |
| `postId`    | Long   | Y    | 게시글 ID                  |
| `content`   | String | Y    | 댓글 내용 (최대 1000자)    |

**Response 201 Created**

```json
{
  "success": true,
  "message": "댓글이 등록되었습니다.",
  "data": {
    "id": 103,
    "content": "재미있는 글이네요!",
    "authorId": 3,
    "authorName": "홍길동",
    "createdAt": "2025.04.11 11:00",
    "isDeletable": true
  }
}
```

> 등록된 댓글 정보를 바로 반환하여 프론트에서 목록 재조회 없이 UI 즉시 갱신 가능

**에러 케이스**

| 상황              | HTTP | message                     |
| ----------------- | ---- | --------------------------- |
| 미인증            | 401  | 로그인이 필요합니다.        |
| 존재하지 않는 게시글 | 404 | 요청한 리소스를 찾을 수 없습니다. |
| 내용 누락         | 400  | 입력값이 올바르지 않습니다. |
| 최대 글자 초과    | 400  | 댓글은 1000자를 초과할 수 없습니다. |

---

### 8.3 댓글 삭제

```
DELETE /api/comments/{id}
```

**권한**: USER(본인) / ADMIN

**Path Parameters**

| 파라미터 | 타입 | 설명      |
| -------- | ---- | --------- |
| `id`     | Long | 댓글 ID   |

**Response 200 OK**

```json
{
  "success": true,
  "message": "댓글이 삭제되었습니다.",
  "data": null
}
```

**에러 케이스**

| 상황                  | HTTP | message                          |
| --------------------- | ---- | -------------------------------- |
| 미인증                | 401  | 로그인이 필요합니다.             |
| 본인 댓글 아님 (USER) | 403  | 접근 권한이 없습니다.            |
| 존재하지 않는 ID      | 404  | 요청한 리소스를 찾을 수 없습니다.|

---

## 9. 파일 API

---

### 9.1 파일 서빙

```
GET /api/files/{filename}
```

**권한**: 전체 (비회원 포함)

**Path Parameters**

| 파라미터   | 타입   | 설명                                         |
| ---------- | ------ | -------------------------------------------- |
| `filename` | String | 서버 저장 파일명 (`stored_name` 컬럼 값)     |

**Response 200 OK**

```
Content-Type: image/jpeg (또는 파일 타입에 따라 자동 감지)
Body: 파일 바이너리 데이터
```

**에러 케이스**

| 상황               | HTTP | 설명                       |
| ------------------ | ---- | -------------------------- |
| 파일 없음          | 404  | 요청한 파일을 찾을 수 없습니다. |

---

## API 목록 요약

| #  | Method | URI                            | 권한              | 설명                  |
| -- | ------ | ------------------------------ | ----------------- | --------------------- |
| 1  | POST   | `/api/auth/join`               | 전체              | 회원가입              |
| 2  | POST   | `/api/auth/login`              | 전체              | 로그인                |
| 3  | GET    | `/api/main`                    | 전체              | 메인 위젯 조회        |
| 4  | GET    | `/api/notice`                  | 전체              | 공지사항 목록         |
| 5  | GET    | `/api/notice/{id}`             | 전체              | 공지사항 상세         |
| 6  | POST   | `/api/notice`                  | ADMIN             | 공지사항 작성         |
| 7  | PUT    | `/api/notice/{id}`             | ADMIN             | 공지사항 수정         |
| 8  | DELETE | `/api/notice/{id}`             | ADMIN             | 공지사항 삭제         |
| 9  | GET    | `/api/free`                    | 전체              | 자유게시판 목록       |
| 10 | GET    | `/api/free/{id}`               | 전체              | 자유게시판 상세       |
| 11 | POST   | `/api/free`                    | USER / ADMIN      | 자유게시판 작성       |
| 12 | PUT    | `/api/free/{id}`               | USER(본인)/ADMIN  | 자유게시판 수정       |
| 13 | DELETE | `/api/free/{id}`               | USER(본인)/ADMIN  | 자유게시판 삭제       |
| 14 | GET    | `/api/gallery`                 | 전체              | 갤러리 목록           |
| 15 | GET    | `/api/gallery/{id}`            | 전체              | 갤러리 상세           |
| 16 | POST   | `/api/gallery`                 | USER / ADMIN      | 갤러리 작성           |
| 17 | PUT    | `/api/gallery/{id}`            | USER(본인)/ADMIN  | 갤러리 수정           |
| 18 | DELETE | `/api/gallery/{id}`            | USER(본인)/ADMIN  | 갤러리 삭제           |
| 19 | GET    | `/api/inquiry`                 | 전체              | 문의게시판 목록       |
| 20 | GET    | `/api/inquiry/{id}`            | 전체(비밀글 제한) | 문의게시판 상세       |
| 21 | POST   | `/api/inquiry`                 | USER / ADMIN      | 문의 작성             |
| 22 | PUT    | `/api/inquiry/{id}`            | USER(본인+미답변) | 문의 수정             |
| 23 | DELETE | `/api/inquiry/{id}`            | USER(본인)/ADMIN  | 문의 삭제             |
| 24 | POST   | `/api/inquiry/{id}/answer`     | ADMIN             | 답변 등록             |
| 25 | PUT    | `/api/inquiry/{id}/answer`     | ADMIN             | 답변 수정             |
| 26 | DELETE | `/api/inquiry/{id}/answer`     | ADMIN             | 답변 삭제             |
| 27 | GET    | `/api/comments`                | 전체              | 댓글 목록             |
| 28 | POST   | `/api/comments`                | USER / ADMIN      | 댓글 등록             |
| 29 | DELETE | `/api/comments/{id}`           | USER(본인)/ADMIN  | 댓글 삭제             |
| 30 | GET    | `/api/files/{filename}`        | 전체              | 파일 서빙             |
