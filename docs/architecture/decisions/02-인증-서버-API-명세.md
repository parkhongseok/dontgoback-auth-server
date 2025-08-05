# 인증 서버 API 명세: 토큰 발급 & 공개키 제공

Date: 2025-08-03  
Status: Accepted

<br/>

## 맥락

DontGoBack 프로젝트의 마이크로서비스 환경에서 서버 간 인증 통신을 위해,  
**인증 서버(dontgoback-auth-server)** 는 다음 두 가지 핵심 기능을 제공합니다:

1. 등록된 서버에 대해 **JWT를 발급**하는 토큰 발급 API
2. JWT 검증에 사용될 **공개키를 제공**하는 공개키 API

이 두 기능은 서버 간 신뢰 기반의 통신을 구축하는 핵심이며,  
API 요청 방식과 인증 구조를 명확히 정의해둘 필요가 있습니다.

<br/>
<br/>

## 결정

### 1. 토큰 발급 API

| 항목            | 설명                                                             |
| --------------- | ---------------------------------------------------------------- |
| **Endpoint**    | `POST /msa/auth/token`                                           |
| **RequestBody** | JSON 형태로 `clientId`, `clientSecret` 전달                      |
| **인증 방식**   | `clientId`, `clientSecret`을 기반으로 등록된 클라이언트인지 검증 |
| **응답 형태**   | 성공 시 JWT 문자열을 그대로 반환 (`Content-Type: text/plain`)    |
| **예외 처리**   | - 잘못된 `clientId` 또는 `secret`: `401 UNAUTHORIZED`            |
|                 | - 내부 에러: `500 INTERNAL SERVER ERROR`                         |

**예시 요청:**

```http
POST /msa/auth/token
Content-Type: application/json

{
  "clientId": "server-1",
  "clientSecret": "abc123..."
}
```

**예시 응답:**

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

<br/>
<br/>

### 2. 공개키 제공 API

| 항목          | 설명                                                       |
| ------------- | ---------------------------------------------------------- |
| **Endpoint**  | `GET /msa/auth/public-key`                                 |
| **인증 방식** | 없음 (누구나 접근 가능)                                    |
| **응답 형태** | Base64 인코딩된 공개키 문자열 (`Content-Type: text/plain`) |
| **예외 처리** | - 공개키 초기화 실패: `500 INTERNAL SERVER ERROR`          |

**예시 요청:**

```http
GET /msa/auth/public-key
```

**예시 응답:**

```
HIhqGDdcKCAQEArKIOt...restofkey...
```

<br/>
<br/>
<br/>

## 결과

- CoreService는 `clientId`/`clientSecret`을 통해 **신뢰 가능한 서버로 인증**받고, 토큰을 발급받을 수 있습니다.
- 발급된 JWT는 확장 서버 또는 기타 마이크로서비스에서 **공개키를 통해 안전하게 검증** 가능합니다.
- 인증 서버는 **비공개키는 외부에 노출되지 않고**, **공개키만을 안전하게 제공**하는 구조로 설계되었습니다.
- 모든 마이크로서비스 간 통신은 이 인증 체계를 기반으로 이루어질 수 있도록 확장 가능합니다.
