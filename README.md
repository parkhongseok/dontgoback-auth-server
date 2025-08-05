# dontgoback-auth-server

# 1. 프로젝트 개요

### 소개

`dontgoback-auth-server`는 **서버 간 통신을 위한 인증 토큰 발급**과  
**JWT 검증용 공개키 제공**을 담당하는 마이크로서비스 인증 서버입니다.

이 프로젝트는 `DontGoBack` 마이크로서비스 아키텍처(MSA) 환경에서 공통 인증 인프라의 역할을 하며,  
Core 서버 및 기타 확장 서버들과의 신뢰 기반 통신을 위하여 **비대칭키(RS256) 기반**의 **JWT 발급 시스템**을 구현하였습니다.

- 인증 서버는 **개인키(private key)** 로 JWT를 서명하고,

- 확장 서버들에서는 **공개키(public key)** 만을 안전하게 제공하여 타 서비스가 JWT의 유효성을 검증할 수 있도록 합니다.

개발 및 배포는 **경량화된 인프라(Raspberry Pi)**를 기반으로 하며,
단위테스트와 통합 테스트를 실행하고,
Docker + GitHub Actions 기반 CI/CD 자동화도 함께 구축하고 있습니다.

### 기간

- 2025.08.01 \~ (진행 중)

### 인원

- 개인 프로젝트

### 기술 스탭

|      번류      |            도구            |  버전  |
| :------------: | :------------------------: | :----: |
|      언어      |            Java            |   21   |
|    Backend     |        Spring Boot         | 3.4.0  |
|  인증/암호화   |        jjwt (RS256)        | 0.11.5 |
|     테스트     | JUnit5 / Mockito / MockMvc |  최신  |
| Infrastructure |        Raspberry Pi        |   -    |
|     DevOps     |  GitHub Actions / Docker   |   -    |

### 연관 프로젝트

- 중심 서비스 서버 GitHub 주소:
  [https://github.com/parkhongseok/projectDontGoBack](https://github.com/parkhongseok/projectDontGoBack)

- 확장 서비스 서버 GitHub 주소:
  [https://github.com/parkhongseok/dontgoback-extension-server](https://github.com/parkhongseok/dontgoback-extension-server)

<br/><br/><br/>

# 2. 주요 기능

### ① 비대칭키 기반 JWT 발급

- `POST /msa/auth/token`
- 등록된 `clientId`, `clientSecret` 검증 후 **개인키로 서명된 JWT** 발급
- 응답은 `Content-Type: text/plain` 으로 **JWT 문자열 그대로 반환**

<br/>

### ② JWT 검증용 공개키 제공

- `/msa/auth/public-key` 엔드포이트에서 **Base64 인코딩된 공개키**를 제공합니다.
- 타 서버는 이 키로 JWT 서명을 검증할 수 있습니다.

<br/>

### ③ 테스트 작성 및 검증

단위 테스트 + 통합 테스트를 모두 구성하여 안전성과 신뢰도를 확보했습니다.

#### 1) 단위 테스트

- 테스트 시에는 Spring Security 필터를 제거하고,
- **@WebMvcTest + @MockitoBean 조합**으로 단위 테스트 환경을 구성했습니다.
  | 컨트롤러 | 테스트 항목 |
  | -------------------------- | ---------------------------------------------------------------------- |
  | `ApiV1TokenController` | 유효한 클라이언트 요청 → JWT 발급 및 BASE64 인코딩 → `text/plain` 응답 |
  | | 잘못된 clientId or secret → 401 반환 |
  | `ApiV1PublicKeyController` | 공개키 초기화 실패 → 500 오류 반환 |
  | | 정상 요청 → 공개키 `text/plain` 응답 |

#### 2) 통합 테스트:

- 실제 PemKeyLoader, TokenProvider, TestTokenVerifier 를 통한 JWT 생성–검증 전체 흐름 검증

  > 테스트 전용 .yml 프로파일 분리

<br/>

### ④ 빌드 및 배포 자동화 (진행 중)

- Docker 기반 컨테이너화 예정
- GitHub Actions 기반 CI/CD 자동화 구축 중

<br/><br/><br/>

# 3. 아키텍처

### 목차

- 01.MSA 대응을 위한 비대칭키 인증 전략
- 02.API 명세: 토큰 발급 & 공개키 제공
- 03.인증 서버 테스트 전략 (단위 + 통합 테스트 포함)

<br/>
<br/>

본 프로젝트의 아키텍처 결정 기록은 [`docs/architecture/decisions`](./docs/architecture/decisions) 디렉터리에 정리되어 있습니다.

ADR은 각 결정의 **맥락**, **결정**, **결과** 를 중심으로 작성되어 서비스 구조에 대한 명확한 의사결정 환경을 제공합니다.

<br/>
<br/>
