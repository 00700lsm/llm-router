# LLM Router

하나의 요청을 여러 LLM 중 하나로 전달하고, Router의 판단과 결과를 측정하기 위한 Backend 프로젝트다.

현재 목표는 최고의 Router를 만드는 것이 아니다.

**측정 가능한 Baseline Router가 존재하는 것**이 현재 목표다.

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

| Name | Required | Description |
|---|---|---|
| `OPENAI_API_KEY` | 실제 Provider 호출 시 필요 | OpenAI API Key |
| `OPENAI_BASE_URL` | 선택 | 기본값 `https://api.openai.com/v1` |

API Key는 Repository에 저장하지 않는다. `.env`는 gitignore 대상이다.

테스트는 실제 Provider를 호출하지 않으므로 API Key 없이 실행할 수 있다.

---

## Provider / Model Configuration

Model Catalog의 Source of Truth는 `src/main/resources/application.yml`이다.

현재 등록 Model:

```text
model-small → gpt-4o-mini
model-large → gpt-4o
default-model → model-small
```

가격 값은 실제 Billing이 아니라 Evaluation 비교용 예상 단가다.

현재 Provider Client는 OpenAI Chat Completions API 하나를 사용한다.

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
  "provider": "OPENAI"
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
export OPENAI_API_KEY=...
./gradlew bootRun --args='--spring.profiles.active=evaluate-models'
```

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

결과는 다음 파일에 저장한다.

```text
evaluation/results/001-model-baseline.json
```

Quality는 Evaluation 전용 Checklist다. Runtime Routing에 사용하지 않는다.

Router 경로 실행 Test:

```bash
./gradlew test --tests com.llmrouter.evaluation.EvaluationRunnerTest
```

측정 기록:

```text
docs/experiments/001-model-baseline.md
```

---

## Experiment Location

```text
docs/experiments/
```

---

## Known Limitations

```text
모든 일반 Request는 Default Model로만 간다.

Request 난이도를 분류하지 않는다.

Provider Failure 시 Fallback / Retry를 하지 않는다.

Quality Judge를 Runtime Routing에 사용하지 않는다.

현재 Provider는 OpenAI-compatible Chat Completions 하나다.

Quality Checklist는 요구사항 충족 여부만 확인한다.
Answer의 미묘한 품질 차이를 완전히 측정하지 않는다.
```
