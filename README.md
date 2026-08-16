# LLM Router

하나의 요청을 여러 LLM 중 하나로 전달하고, Router의 판단과 결과를 측정하기 위한 Backend 프로젝트다.

현재 목표는 최고의 Router를 만드는 것이 아니다.

최종 구조도 **측정 가능한 Baseline Router**다.
복잡한 Routing을 넣지 않은 이유를 Experiment와 ADR에 남긴다.

---

## Current Architecture

```text
Client
  ↓
POST /api/v1/chat
  ↓
Chat Service
  ↓
Baseline Router
  ↓
Configured Default Model
  ↓
LLM Gateway
  ↓
Provider Client
  ↓
Chat Response
```

현재 Routing Policy:

```text
모든 일반 Request
→ Configured Default Model
strategy = BASELINE_DEFAULT
```

사용자 응답에는 Answer / Model / Provider만 포함한다.

Routing Reason, Token, Cost, Latency는 Log와 Evaluation 결과에서 확인한다.

---

## 추천 순서

코드를 확인하는 Test와, Router 품질을 보는 Evaluation은 다르다.

```text
1. ./gradlew test
   API Key 없이 코드 동작을 확인한다.

2. .env에 GEMINI_API_KEY를 넣고 ./gradlew bootRun

3. curl로 POST /api/v1/chat 을 호출한다.

4. 필요하면 Evaluation Profile로 Dataset을 실행한다.
```

브라우저 주소창에 URL만 여는 방식으로는 확인할 수 없다.
이 API는 `POST` + JSON Body다.

Test 성공을 Routing 품질 성공으로 해석하지 않는다.

---

## Prerequisites

```text
Java 21
Gradle Wrapper (./gradlew)
```

확인:

```bash
java -version
```

macOS Homebrew OpenJDK 21을 쓰는 경우:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
java -version
```

Windows는 `gradlew.bat`을 사용한다.

---

## Environment Variables

실제 Provider 호출에는 **Gemini API Key**가 필요하다.

```bash
cp .env.example .env
```

`.env` 내용 예:

```text
GEMINI_API_KEY=여기에_Gemini_API_Key를_넣는다
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/v1beta
```

| 변수 | 필수 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | live 호출 시 필수 | 없음 |
| `GEMINI_BASE_URL` | 선택 | `https://generativelanguage.googleapis.com/v1beta` |

`.env`는 gitignore 대상이다. API Key를 Repository에 커밋하지 않는다.

키 발급: [Google AI Studio](https://aistudio.google.com/apikey)

`./gradlew test`는 실제 Provider를 호출하지 않으므로 API Key 없이 실행할 수 있다.

---

## Provider / Model Configuration

Source of Truth는 `src/main/resources/application.yml`이다.

```text
model-small → gemini-2.5-flash
model-large → gemini-3.5-flash
default-model → model-small
```

가격 값은 실제 Billing이 아니라 Evaluation 비교용 예상 단가다.

현재 Provider Client는 Gemini generateContent API 하나다.

---

## Gradle Test

코드가 정상 동작하는지를 검증한다.

전체:

```bash
./gradlew test
```

자주 쓰는 범위:

```bash
./gradlew test --tests com.llmrouter.api.ChatApiIntegrationTest
./gradlew test --tests com.llmrouter.routing.BaselineRouterTest
./gradlew test --tests com.llmrouter.evaluation.EvaluationRunnerTest
./gradlew test --tests com.llmrouter.evaluation.QualityEvaluatorTest
```

Chat API Test는 Provider를 Test Double로 대체한다.
live Gemini 호출이 아니다.

---

## 서버 실행

```bash
./gradlew bootRun
```

기본 URL:

```text
http://localhost:8080
```

Chat API:

```text
POST http://localhost:8080/api/v1/chat
Content-Type: application/json
```

서버가 뜬 뒤 다른 터미널에서 아래 명령을 실행한다.

종료는 서버 터미널에서 `Ctrl+C`다.

---

## curl / URL 호출

### 성공 요청

```bash
curl -sS -X POST 'http://localhost:8080/api/v1/chat' \
  -H 'Content-Type: application/json' \
  -d '{"message":"Java에서 synchronized와 ReentrantLock 차이를 설명해줘."}'
```

성공 응답 예:

```json
{
  "requestId": "...",
  "answer": "...",
  "model": "model-small",
  "provider": "GEMINI"
}
```

서버 Log에서 Routing 결과를 확인한다.

```text
strategy=BASELINE_DEFAULT
model=model-small
reason=configured default model
success=true
```

### 빈 요청 (Validation)

```bash
curl -sS -i -X POST 'http://localhost:8080/api/v1/chat' \
  -H 'Content-Type: application/json' \
  -d '{"message":"   "}'
```

기대:

```text
HTTP 400
error = INVALID_REQUEST
```

### GET으로 브라우저 주소창 접속

```text
http://localhost:8080/api/v1/chat
```

이 URL을 브라우저에서 열면 실패하는 것이 정상이다.
`GET`을 받지 않는다.

브라우저에서 보려면 REST 클라이언트를 쓴다.

- curl (위 명령)
- [HTTPie](https://httpie.io/)
- Postman / Insomnia

HTTPie 예:

```bash
http POST :8080/api/v1/chat message='오늘 날씨가 좋다. 영어로만 번역해줘.'
```

Postman:

```text
Method  POST
URL     http://localhost:8080/api/v1/chat
Header  Content-Type: application/json
Body    raw JSON
        {"message":"Java Stream의 장단점을 알려줘."}
```

### Provider 실패

Key가 없거나 Provider가 거부하면 다른 Model로 바꾸지 않는다.
실패 Error를 그대로 반환한다.

예:

```text
HTTP 429  RATE_LIMIT
HTTP 502  PROVIDER_ERROR
HTTP 504  PROVIDER_TIMEOUT
```

응답 예:

```json
{
  "requestId": "...",
  "error": "RATE_LIMIT",
  "message": "..."
}
```

---

## Evaluation

live Gemini를 호출한다. `.env`의 `GEMINI_API_KEY`가 필요하다.

호출 간격은 5초다. quota 429가 날 수 있다.

Direct Model (Router를 거치지 않음):

```bash
./gradlew bootRun --args='--spring.profiles.active=evaluate-models'
```

결과:

```text
evaluation/results/001-model-baseline.json
```

Baseline Routing (Router 경로):

```bash
./gradlew bootRun --args='--spring.profiles.active=evaluate-routing'
```

결과:

```text
evaluation/results/002-baseline-routing.json
```

Dataset:

```text
evaluation/dataset.json
```

유형: Simple, General, Reasoning

Quality는 Evaluation 전용 Checklist다. Runtime Routing에 사용하지 않는다.

결과 JSON은 gitignore다. 해석은 Experiment에 있다.

측정 기록:

```text
docs/experiments/001-model-baseline.md
docs/experiments/002-baseline-routing.md
docs/experiments/003-routing-failure-analysis.md
docs/experiments/004-quality-cost-latency.md
docs/experiments/005-routing-policy-limit.md
docs/experiments/006-capability.md
docs/experiments/007-provider-failure.md
docs/experiments/008-routing-re-evaluation.md
docs/experiments/009-final-comparison.md
```

---

## Experiment / ADR

```text
docs/experiments/
docs/adr/
```

---

## Known Limitations

```text
모든 일반 Request는 Default Model로만 간다.

Request 난이도를 분류하지 않는다.

Capability / Context Limit을 Routing에 사용하지 않는다.

Provider Failure 시 Fallback / Retry를 하지 않는다.

Quality Judge를 Runtime Routing에 사용하지 않는다.

현재 Provider는 Gemini generateContent API 하나다.

Quality Checklist는 요구사항 충족 여부만 확인한다.
Answer의 미묘한 품질 차이를 완전히 측정하지 않는다.

브라우저 주소창 GET으로는 Chat API를 호출할 수 없다.
```
