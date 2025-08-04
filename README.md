# dontgoback-auth-server

# 1. 프로젝트 개요

### 소개

`dontgoback-auth-server`는 **서버 간 통신을 위한 인증 토큰 발급**과   
**JWT 검증용 공개키 제공**을 담당하는 마이크로서비스 인증 서버입니다.

이 프로젝트는 `DontGoBack` 마이크로서비스 아키텍처(MSA) 환경에서 공통 인증 인프라의 역할을 하며,   
Core 서버 및 기타 확장 서버들과의 신뢰 기반 통신을 위하여 **비대칭키(RS256) 기반**의 **JWT 발급 시스템**을 구현하였습니다.   

서버는 내부적으로 개인 키를 활용하여 JWT를 생성하고, 외부에는 공개 키만을 노출하여 타 서버들이 안전하게 토큰의 유효성을 검증할 수 있도록 구성하였습니다.

개발 및 배포는 경량화된 인프라(Raspberry Pi)를 기반으로 하며, 테스트 및 배포 자동화 환경도 함께 구성해나가고 있습니다.

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

- CoreService GitHub 주소:
  [https://github.com/parkhongseok/projectDontGoBack](https://github.com/parkhongseok/projectDontGoBack)

<br/><br/><br/>

# 2. 주요 기능

### ① 비대칭키 기반 JWT 발급

- `/msa/auth/token` 엔드포이트를 통해,
  등록된 클라이언트 ID/시크린이 검증되면 **RS256 서명된 JWT**를 생성합니다.
- 서명에 사용되는 개인키는 서버 내부에서만 접근 가능하게 설계되어 있습니다.

### ② JWT 검증용 공개키 제공

- `/msa/auth/public-key` 엔드포이트에서 **Base64 인코딩된 공개키**를 제공합니다.
- 타 서버는 이 키로 JWT 서명을 검증할 수 있습니다.

### ③ 테스트 작성 및 검증

아래 컨트롤러들을 대상으로 단위 테스트를 작성하고,
**MockMvc + Mockito 기반의 검증 환경**을 구성했습니다.

| 컨트롤러                   | 테스트 항목                            |
| -------------------------- | -------------------------------------- |
| `ApiV1TokenController`     | 유효한 클라이언트 요청 → JWT 발급 성공 |
|                            | 잘못된 clientId or secret → 401 반환   |
| `ApiV1PublicKeyController` | 공개키 초기화 실패 → 500 오류 반환     |
|                            | 정상 요청 → 공개키 JSON 응답           |

테스트 시에는 Spring Security 필터를 제거하고,
**@WebMvcTest + @MockitoBean 조합**으로 단위 테스트 환경을 구성했습니다.
테스트 전용 `.yml` 프로파일도 분류되어 있습니다.

### ④ 빌드 및 배포 자동화 (예정)

- Docker 기반으로 커테이너를 구성할 예정입니다.
- GitHub Actions를 활용한 CI/CD 자동화를 계획 중입니다.

<br/><br/><br/>

# 3. 아키텍처

### 목차   
  - 01.MSA 대응을 위한 비대칭키 인증 전략
  - 02.API 명세: 토큰 발급 & 공개키 제공
  - 03.인증 서버 테스트 전략

<br/>
<br/>

본 프로젝트의 아키텍처 결정 기록은 [`docs/architecture/decisions`](./docs/architecture/decisions) 디렉터리에 정리되어 있습니다.

ADR은 각 결정의 **맥락**, **결정**, **결과** 를 중심으로 작성되어 서비스 구조에 대한 명확한 의사결정 환경을 제공합니다.


<br/>
<br/>


