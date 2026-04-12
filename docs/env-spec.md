# ⚙️ Env Spec

## 김승현 포트폴리오 포털 사이트

---

## 목차

1. [환경 종류 및 개요](#1-환경-종류-및-개요)
2. [Backend 환경 설정](#2-backend-환경-설정)
3. [Frontend 환경 설정](#3-frontend-환경-설정)
4. [데이터베이스 초기 설정](#4-데이터베이스-초기-설정)
5. [로컬 개발 환경 실행 순서](#5-로컬-개발-환경-실행-순서)
6. [환경 변수 보안 관리](#6-환경-변수-보안-관리)

---

## 1. 환경 종류 및 개요

이 프로젝트는 **로컬 개발 환경** 단일 환경을 기준으로 작성한다.

| 항목 | 값 |
|------|----|
| 실행 환경 | 로컬 (개발자 PC) |
| Frontend URL | `http://localhost:5173` |
| Backend URL | `http://localhost:8080` |
| Database | `localhost:3306` |

### 포트 정리

| 구성 요소 | 포트 | 비고 |
|----------|------|------|
| Vue.js (Vite dev server) | 5173 | `npm run dev` |
| Spring Boot | 8080 | embedded Tomcat |
| MySQL | 3306 | 기본 포트 |

---

## 2. Backend 환경 설정

### 2.1 디렉토리 구조

```
backend/
└── src/main/resources/
    ├── application.yml          # 공통 설정
    └── application-local.yml    # 로컬 환경 전용 설정 (DB 비밀번호 등 민감 정보)
```

> `application-local.yml`은 `.gitignore`에 추가하여 Git에 포함되지 않도록 한다.

---

### 2.2 `application.yml` (공통 설정 — Git 커밋 O)

```yaml
# ============================================================
# 서버
# ============================================================
server:
  port: 8080

# ============================================================
# Spring 공통 설정
# ============================================================
spring:
  profiles:
    active: local                          # 기본 활성 프로파일

  # 파일 업로드
  servlet:
    multipart:
      enabled: true
      max-file-size: 20MB                  # 단일 파일 최대 크기 (자유게시판 기준)
      max-request-size: 110MB              # 요청 전체 최대 크기 (파일 5개 × 20MB + 여유분)

  # JPA 공통
  jpa:
    open-in-view: false
    properties:
      hibernate:
        format_sql: true

# ============================================================
# JWT
# ============================================================
jwt:
  expiration: 7200000                      # 만료 시간: 2시간 (밀리초)

# ============================================================
# 파일 업로드 경로
# ============================================================
file:
  upload-dir: ./uploads                    # 업로드 루트 디렉토리 (실행 위치 기준 상대 경로)
  gallery-dir: ${file.upload-dir}/gallery  # 갤러리 이미지 저장 경로
  free-dir: ${file.upload-dir}/free        # 자유게시판 첨부파일 저장 경로

# ============================================================
# 파일 업로드 제한 (서비스 레이어 커스텀 검증용)
# ============================================================
file-policy:
  gallery:
    max-count: 10                          # 갤러리 이미지 최대 장수
    max-size-mb: 10                        # 갤러리 이미지 장당 최대 크기 (MB)
    allowed-extensions: jpg,jpeg,png,gif,webp
  free:
    max-count: 5                           # 자유게시판 첨부파일 최대 개수
    max-size-mb: 20                        # 자유게시판 파일당 최대 크기 (MB)

# ============================================================
# CORS
# ============================================================
cors:
  allowed-origins: http://localhost:5173   # Vue.js 개발 서버

# ============================================================
# 로깅
# ============================================================
logging:
  level:
    root: INFO
    com.portfolio: DEBUG                   # 프로젝트 패키지 DEBUG 레벨
    org.hibernate.SQL: DEBUG              # SQL 쿼리 출력
    org.hibernate.orm.jdbc.bind: TRACE    # SQL 바인딩 파라미터 출력
```

---

### 2.3 `application-local.yml` (로컬 전용 — Git 커밋 X)

```yaml
# ============================================================
# 데이터베이스 (로컬)
# ============================================================
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root                         # 로컬 MySQL 사용자명
    password: 1234                         # 로컬 MySQL 비밀번호 (본인 환경에 맞게 변경)
    driver-class-name: com.mysql/cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate                   # 스키마 검증만 (DDL은 db-schema.sql로 직접 실행)
    show-sql: true

# ============================================================
# JWT Secret Key (로컬)
# ============================================================
jwt:
  secret: local-dev-secret-key-must-be-at-least-32-characters-long

# ============================================================
# 파일 업로드 경로 (로컬 절대 경로로 오버라이드 가능)
# ============================================================
# file:
#   upload-dir: /Users/{username}/portfolio-uploads   # Mac/Linux 절대 경로 예시
#   upload-dir: C:/portfolio-uploads                  # Windows 절대 경로 예시
```

> **주의**: `jwt.secret`은 최소 32자 이상의 임의 문자열이어야 한다. HS256 알고리즘 요구사항.

---

### 2.4 `build.gradle` 주요 의존성

```gradle
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // MySQL
    runtimeOnly 'com.mysql:mysql-connector-j'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

---

### 2.5 `@ConfigurationProperties` 바인딩 클래스

`application.yml`의 커스텀 설정을 Java 클래스로 바인딩한다.

```java
// FileProperties.java
@ConfigurationProperties(prefix = "file")
@Component
@Getter @Setter
public class FileProperties {
    private String uploadDir;
    private String galleryDir;
    private String freeDir;
}

// FilePolicyProperties.java
@ConfigurationProperties(prefix = "file-policy")
@Component
@Getter @Setter
public class FilePolicyProperties {
    private BoardFilePolicy gallery = new BoardFilePolicy();
    private BoardFilePolicy free    = new BoardFilePolicy();

    @Getter @Setter
    public static class BoardFilePolicy {
        private int maxCount;
        private int maxSizeMb;
        private String allowedExtensions; // "jpg,jpeg,png,gif,webp"
    }
}

// JwtProperties.java
@ConfigurationProperties(prefix = "jwt")
@Component
@Getter @Setter
public class JwtProperties {
    private String secret;
    private long expiration;
}

// CorsProperties.java
@ConfigurationProperties(prefix = "cors")
@Component
@Getter @Setter
public class CorsProperties {
    private String allowedOrigins;
}
```

---

### 2.6 CORS 설정 (`CorsConfig.java`)

```java
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(corsProperties.getAllowedOrigins())  // http://localhost:5173
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

### 2.7 파일 서빙 설정 (`WebMvcConfig.java`)

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /api/files/** 요청을 로컬 업로드 디렉토리로 매핑
        registry.addResourceHandler("/api/files/**")
            .addResourceLocations("file:" + fileProperties.getUploadDir() + "/");
    }
}
```

> `AttachmentController`에서 직접 파일을 읽는 방식 대신, Spring의 정적 리소스 핸들러를 활용한다.
> 단, 이 방식은 인증 없이 URL만 알면 누구나 접근 가능하다. 현재 요구사항(파일 접근 공개)에 부합한다.

---

### 2.8 업로드 디렉토리 자동 생성

애플리케이션 시작 시 업로드 디렉토리가 없으면 자동으로 생성한다.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class FileDirectoryInitializer implements ApplicationRunner {

    private final FileProperties fileProperties;

    @Override
    public void run(ApplicationArguments args) {
        createDirectory(fileProperties.getUploadDir());
        createDirectory(fileProperties.getGalleryDir());
        createDirectory(fileProperties.getFreeDir());
    }

    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("디렉토리 생성: {}", dir.getAbsolutePath());
            }
        }
    }
}
```

---

## 3. Frontend 환경 설정

### 3.1 디렉토리 구조

```
frontend/
├── .env                  # 전체 환경 공통 (Git 커밋 O)
├── .env.local            # 로컬 오버라이드 (Git 커밋 X)
└── vite.config.js
```

---

### 3.2 `.env` (공통 — Git 커밋 O)

```dotenv
# API 서버 Base URL
VITE_API_BASE_URL=http://localhost:8080/api
```

> Vite 환경 변수는 반드시 `VITE_` 접두사로 시작해야 클라이언트 코드에서 접근 가능하다.

---

### 3.3 `.env.local` (로컬 전용 — Git 커밋 X)

로컬에서 기본값과 다른 설정이 필요할 때만 생성한다.

```dotenv
# 백엔드 포트를 변경한 경우 오버라이드
# VITE_API_BASE_URL=http://localhost:9090/api
```

---

### 3.4 Axios 인스턴스 설정 (`api/axios.js`)

```javascript
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { useModalStore } from '@/stores/modal';
import router from '@/router';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,  // .env에서 주입
  timeout: 10000,                               // 10초 타임아웃
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request 인터셉터: JWT 헤더 자동 주입
axiosInstance.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response 인터셉터: 401 처리
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();

      const isProtected = router.currentRoute.value.meta.requiresAuth
                       || router.currentRoute.value.meta.requiresAdmin;

      if (isProtected) {
        const currentPath = router.currentRoute.value.fullPath;
        router.push(`/login?ret=${encodeURIComponent(currentPath)}`);
      } else {
        const modalStore = useModalStore();
        modalStore.openLoginModal();
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
```

---

### 3.5 `vite.config.js`

```javascript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),  // @ 경로 별칭 설정
    },
  },
  server: {
    port: 5173,
    // 개발 환경에서 CORS 우회가 필요한 경우 프록시 설정
    // (현재는 백엔드 CORS 설정으로 직접 통신)
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8080',
    //     changeOrigin: true,
    //   },
    // },
  },
});
```

---

### 3.6 `package.json` 주요 의존성

```json
{
  "name": "portfolio-frontend",
  "version": "0.0.1",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

---

## 4. 데이터베이스 초기 설정

### 4.1 MySQL 데이터베이스 및 사용자 생성

```sql
-- MySQL root 계정으로 접속 후 실행

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS portfolio
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- (선택) 전용 계정 생성
-- CREATE USER 'portfolio_user'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON portfolio.* TO 'portfolio_user'@'localhost';
-- FLUSH PRIVILEGES;
```

---

### 4.2 스키마 적용

`db-schema.md`의 DDL을 실행하여 테이블을 생성한다.

```bash
# MySQL CLI에서 직접 실행
mysql -u root -p portfolio < db-schema.sql

# 또는 MySQL Workbench / DataGrip 등 GUI 도구에서 실행
```

---

### 4.3 관리자 계정 초기 데이터 삽입

```sql
USE portfolio;

-- 비밀번호: admin1234 → BCrypt 해시 (Spring Security BCryptPasswordEncoder 사용)
-- 실제 해시값은 아래 Java 코드로 생성하거나, 온라인 BCrypt 생성기 활용
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- System.out.println(encoder.encode("admin1234"));

INSERT INTO users (username, password, name, role, created_at)
VALUES (
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- admin1234
  '관리자',
  'ADMIN',
  NOW()
);
```

> **BCrypt 해시 생성 방법** (Spring Boot 기동 없이 생성하려면):
> ```java
> // 간단한 메인 메서드로 실행
> public static void main(String[] args) {
>     BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
>     System.out.println(encoder.encode("admin1234"));
> }
> ```

---

### 4.4 MySQL 연결 확인

```bash
# 연결 테스트
mysql -u root -p -e "USE portfolio; SHOW TABLES;"

# 예상 출력
# +---------------------+
# | Tables_in_portfolio |
# +---------------------+
# | attachment          |
# | comment             |
# | free_post           |
# | gallery_post        |
# | inquiry             |
# | inquiry_answer      |
# | notice              |
# | users               |
# +---------------------+
```

---

## 5. 로컬 개발 환경 실행 순서

### 5.1 사전 요구사항

| 도구 | 버전 | 확인 명령 |
|------|------|----------|
| Java | 17 이상 | `java -version` |
| Gradle | 8.x (Wrapper 사용 권장) | `./gradlew --version` |
| Node.js | 18 이상 | `node -v` |
| npm | 9 이상 | `npm -v` |
| MySQL | 8.x | `mysql --version` |

---

### 5.2 최초 환경 구성 (한 번만 실행)

```bash
# 1. 저장소 클론
git clone {repository-url}
cd portfolio

# 2. MySQL 데이터베이스 & 스키마 설정
mysql -u root -p -e "
  CREATE DATABASE IF NOT EXISTS portfolio
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
"
mysql -u root -p portfolio < db-schema.sql
mysql -u root -p portfolio < db-seed.sql   # 관리자 계정 삽입

# 3. Backend 환경 파일 생성
cat > backend/src/main/resources/application-local.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: 여기에_본인_MySQL_비밀번호_입력
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

jwt:
  secret: local-dev-secret-key-change-this-to-at-least-32-characters

EOF

# 4. Frontend 의존성 설치
cd frontend
npm install
```

---

### 5.3 매번 실행

```bash
# 터미널 1 — Backend 실행
cd backend
./gradlew bootRun

# 정상 기동 확인 로그:
# Started PortfolioApplication in X.XXX seconds
# 업로드 디렉토리 자동 생성 로그도 함께 출력됨

# 터미널 2 — Frontend 실행
cd frontend
npm run dev

# 정상 기동 확인:
# VITE v5.x.x  ready in XXX ms
# ➜  Local:   http://localhost:5173/
```

---

### 5.4 실행 확인

```bash
# Backend API 헬스 체크
curl http://localhost:8080/api/main
# → { "success": true, "data": { "notice": [...], ... } }

# Frontend 브라우저 접속
open http://localhost:5173
```

---

## 6. 환경 변수 보안 관리

### 6.1 `.gitignore` 설정

**Backend (`backend/.gitignore`)**

```gitignore
# 로컬 환경 설정 (민감 정보 포함)
src/main/resources/application-local.yml

# 업로드 파일 (로컬에서만 존재)
uploads/

# 빌드 결과물
build/
.gradle/
```

**Frontend (`frontend/.gitignore`)**

```gitignore
# 로컬 환경 오버라이드
.env.local
.env.*.local

# 빌드 결과물
dist/
node_modules/
```

---

### 6.2 민감 정보 관리 원칙

| 항목 | 관리 방법 | Git 포함 여부 |
|------|----------|--------------|
| DB 비밀번호 | `application-local.yml` | ❌ |
| JWT Secret Key | `application-local.yml` | ❌ |
| DB URL (localhost) | `application-local.yml` | ❌ |
| API Base URL (localhost) | `.env` | ✅ (민감 정보 아님) |
| 파일 업로드 경로 | `application.yml` (상대 경로) | ✅ |
| 파일 업로드 제한 정책 | `application.yml` | ✅ |
| CORS 허용 Origin (localhost) | `application.yml` | ✅ |
| JWT 만료 시간 | `application.yml` | ✅ |

---

### 6.3 JWT Secret Key 생성 가이드

HS256 알고리즘을 사용하므로 최소 32자(256비트) 이상의 키가 필요하다.

```bash
# macOS / Linux: 안전한 랜덤 키 생성
openssl rand -base64 64

# 예시 출력 (실제 사용 시 본인이 생성한 값으로 교체):
# xK9mP3vQ2nL8rT5wY7uZ1cB4eJ6hN0fA+gDiEoRs==...
```

```yaml
# application-local.yml에 적용
jwt:
  secret: xK9mP3vQ2nL8rT5wY7uZ1cB4eJ6hN0fA_여기는_예시입니다_직접_생성하세요
```

---

### 6.4 환경 설정 파일 요약

| 파일 | 환경 | Git 포함 | 주요 내용 |
|------|------|----------|----------|
| `application.yml` | 공통 | ✅ | 포트, JWT 만료시간, 파일 정책, CORS, 로깅 |
| `application-local.yml` | 로컬 | ❌ | DB URL/계정/비밀번호, JWT Secret Key |
| `.env` | 공통 | ✅ | `VITE_API_BASE_URL` |
| `.env.local` | 로컬 | ❌ | 로컬 API URL 오버라이드 (필요 시) |

---

## 전체 설정값 한눈에 보기

| 설정 항목 | 값 | 위치 |
|----------|-----|------|
| Backend 포트 | `8080` | `application.yml` |
| Frontend 포트 | `5173` | `vite.config.js` |
| MySQL 포트 | `3306` | `application-local.yml` |
| DB 이름 | `portfolio` | `application-local.yml` |
| DB 인코딩 | `utf8mb4` | `application-local.yml` |
| JWT 만료 시간 | `7200000ms` (2시간) | `application.yml` |
| JWT Secret 최소 길이 | 32자 이상 | `application-local.yml` |
| 업로드 루트 경로 | `./uploads` | `application.yml` |
| 갤러리 이미지 최대 장수 | `10` | `application.yml` |
| 갤러리 이미지 장당 최대 | `10MB` | `application.yml` |
| 갤러리 허용 확장자 | `jpg,jpeg,png,gif,webp` | `application.yml` |
| 자유게시판 파일 최대 개수 | `5` | `application.yml` |
| 자유게시판 파일당 최대 | `20MB` | `application.yml` |
| Spring multipart 단일 파일 최대 | `20MB` | `application.yml` |
| Spring multipart 요청 전체 최대 | `110MB` | `application.yml` |
| CORS 허용 Origin | `http://localhost:5173` | `application.yml` |
| API Base URL (Frontend) | `http://localhost:8080/api` | `.env` |
| Axios 타임아웃 | `10000ms` (10초) | `api/axios.js` |
