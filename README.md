# LLM Router

하나의 요청을 여러 LLM 중 하나로 전달하고, Router의 판단과 결과를 측정하기 위한 Backend 프로젝트다.

현재 목표는 최고의 Router를 만드는 것이 아니다.

최종 구조도 **측정 가능한 Baseline Router**다.
복잡한 Routing을 넣지 않은 이유를 Experiment와 ADR에 남긴다.

---

## Current Architecture

Phase 1 Baseline:

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

## Run

Java 21과 Gradle Wrapper가 필요하다.

```bash
./gradlew bootRun
```

기본 포트는 `8080`이다.

---

## Environment Variables

실제 Provider 호출에는 **Gemini API Key**가 필요하다.

프로젝트 루트에 `.env` 파일을 만들고 아래 변수명을 사용한다.

```text
.env
```

필수 변수:

```text
GEMINI_API_KEY
```

선택 변수:

```text
GEMINI_BASE_URL
```

기본값:

```text
https://generativelanguage.googleapis.com/v1beta
```

예시:

```bash
cp .env.example .env
```

`.env` 내용 예:

```text
GEMINI_API_KEY=여기에_Gemini_API_Key를_넣는다
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/v1beta
```

`.env`는 gitignore 대상이다. API Key를 Repository에 커밋하지 않는다.

템플릿 파일은 `.env.example`이다. 여기에는 실제 키를 넣지 않는다.

테스트는 실제 Provider를 호출하지 않으므로 API Key 없이 실행할 수 있다.

키 발급: [Google AI Studio](https://aistudio.google.com/apikey)


---

## Provider / Model Configuration

Model Catalog의 Source of Truth는 `src/main/resources/application.yml`이다.

현재 등록 Model:

```text
model-small → gemini-2.5-flash
model-large → gemini-3.5-flash
default-model → model-small
```

가격 값은 실제 Billing이 아니라 Evaluation 비교용 예상 단가다.

현재 Provider Client는 Gemini generateContent API 하나를 사용한다.

두 번째 Provider를 위한 Factory / Registry는 만들지 않았다.

---

## API Usage

```http
POST /api/v1/chat
Content-Type: application/json
```

```json
{
  "message": "Java에서 synchronized와 ReentrantLock 차이를 설명해줘."
}
```

성공 응답:

```json
{
  "requestId": "...",
  "answer": "...",
  "model": "model-small",
  "provider": "GEMINI"
}
```

`message`가 없거나 비어 있으면 `INVALID_REQUEST`로 거절한다.

Provider 호출이 실패하면 다른 Model로 바꾸지 않고 실패를 반환한다.

---

## Test

```bash
./gradlew test
```

현재 Test는 코드가 정상 동작하는지를 검증한다.

Test 성공을 Routing 품질 성공으로 해석하지 않는다.

---

## Evaluation

Phase 2 Direct Model Evaluation은 Router를 거치지 않고
동일 Dataset을 각 Model에 직접 실행한다.

```bash
./gradlew bootRun --args='--spring.profiles.active=evaluate-models'
```

결과는 다음 파일에 저장한다.

```text
evaluation/results/001-model-baseline.json
```

Phase 3 Baseline Routing Evaluation은 Router 경로로 Dataset을 실행한다.

```bash
./gradlew bootRun --args='--spring.profiles.active=evaluate-routing'
```

결과는 다음 파일에 저장한다.

```text
evaluation/results/002-baseline-routing.json
```

실행 전에 `.env`에 `GEMINI_API_KEY`가 있어야 한다.

Dataset:

```text
evaluation/dataset.json
```

현재 유형:

```text
Simple
General
Reasoning
```

Quality는 Evaluation 전용 Checklist다. Runtime Routing에 사용하지 않는다.

Router 경로 실행 Test:

```bash
./gradlew test --tests com.llmrouter.evaluation.EvaluationRunnerTest
```

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

## Experiment Location

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
```
