# 003. Routing Failure 분석

## Problem / Question

Phase 3에서 수집한 실패는
실제로 Router 문제인가, 다른 계층의 문제인가?

```text
실패한 Case의 Failure Type은 무엇인가?

Router가 현재 정책과 다르게 Model을 선택했는가?

같은 실패가 Model Quality / Prompt / Provider 문제인가?

Cost / Latency Failure가 실제로 있는가?

Capability Mismatch Case가 현재 Dataset에 있는가?
```

최종 Answer가 나쁘다는 이유로 Routing Policy를 바꾸지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
Phase 3 실패를 ROUTING_FAILURE 하나로 묶으면
실패 위치가 섞인다.
```

현재 Routing Policy는 모든 일반 Request를 Default Model로 보낸다.
정책대로 선택했는 것과, 그 선택이 Quality를 만족하는 것은 다른 질문이다.

---

## Conditions

새 Provider 호출을 하지 않는다.

비교 대상:

```text
Router 경로: docs/experiments/002-baseline-routing.md
            evaluation/results/002-baseline-routing.json

Direct Model: docs/experiments/001-model-baseline.md
              evaluation/results/001-model-baseline.json
              Gemini 2차 (model-small = gemini-2.5-flash,
                          model-large = gemini-3.5-flash)
```

공통:

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Router: Baseline Router
strategy: BASELINE_DEFAULT
default-model: model-small
Quality: 기존 expectedCondition Checklist
Runtime Router 변경: 없음
Prompt 변경: 없음
Dataset Expected 변경: 없음
```

두 실험은 동일 Dataset이지만 실행 시점이 다르다.
같은 호출의 Before / After가 아니다.

Allowed Models / Max Cost / Max Latency Expected는 Dataset에 없다.
이 값이 없는 축은 Evaluation Failure로 단정하지 않는다.

---

## Baseline

현재 Routing Policy:

```text
모든 일반 Request
→ Configured Default Model
strategy = BASELINE_DEFAULT
reason = configured default model
```

Phase 3 Router 결과:

```text
7 / 7 selectedModel = model-small

호출 성공 5 / 7
Quality PASS 4 / 7

실패:
simple-003 Quality FAIL (JSON 형식)
reasoning-001 HTTP 429 RATE_LIMIT
reasoning-002 HTTP 429 RATE_LIMIT
```

이 실험은 Router를 개선하지 않는다.

---

## Result

분류에 사용한 측정값이다.

### Router 경로 (Phase 3)

| Case | Category | Selected Model | success | Quality | errorCode |
|---|---|---|---|---|---|
| simple-001 | SIMPLE | model-small | true | PASS | |
| simple-002 | SIMPLE | model-small | true | PASS | |
| simple-003 | SIMPLE | model-small | true | FAIL | |
| general-001 | GENERAL | model-small | true | PASS | |
| general-002 | GENERAL | model-small | true | PASS | |
| reasoning-001 | REASONING | model-small | false | FAIL | RATE_LIMIT |
| reasoning-002 | REASONING | model-small | false | FAIL | RATE_LIMIT |

simple-003 Quality reason:

```text
answer is not a JSON object
```

### Direct Model (Phase 2, Gemini 2차)

| Case | Category | small Quality | large Quality |
|---|---|---|---|
| simple-001 | SIMPLE | PASS | PASS |
| simple-002 | SIMPLE | PASS | PASS |
| simple-003 | SIMPLE | FAIL | PASS |
| general-001 | GENERAL | PASS | PASS |
| general-002 | GENERAL | PASS | PASS |
| reasoning-001 | REASONING | PASS | PROVIDER_FAILURE (HTTP 503) |
| reasoning-002 | REASONING | PASS | PASS |

simple-003 Direct 결과:

```text
small: JSON을 코드펜스로 감싸서 requireJson FAIL
large: JSON 객체로 통과
```

Router는 7 / 7에서 현재 정책과 다른 Model을 고르지 않았다.

---

## Analysis

분류 기준:

```text
Answer Quality 낮음 또는 호출 실패
        ↓
Router가 현재 정책과 다르게 선택했는가?
        ↓ NO
선택된 Model 호출이 Provider에서 실패했는가?
        ↓
아니면 선택된 Model의 Checklist 실패인가?
        ↓
같은 Prompt에서 다른 Model은 통과하는가?
```

현재 정책 위반으로서의 ROUTING_FAILURE와,
다른 Model이면 Checklist가 달라지는 ROUTING_FAILURE 후보를 구분한다.

### Case 분류

| Case | Router 선택 | 관측 | Failure Type | ROUTING_FAILURE 후보 |
|---|---|---|---|---|
| simple-001 | model-small, 정책과 일치 | Quality PASS | 없음 | 아니오 |
| simple-002 | model-small, 정책과 일치 | Quality PASS | 없음 | 아니오 |
| simple-003 | model-small, 정책과 일치 | 호출 성공, requireJson FAIL | MODEL_QUALITY_FAILURE | 예 |
| general-001 | model-small, 정책과 일치 | Quality PASS | 없음 | 아니오 |
| general-002 | model-small, 정책과 일치 | Quality PASS | 없음 | 아니오 |
| reasoning-001 | model-small, 정책과 일치 | HTTP 429 | PROVIDER_FAILURE | 아니오 |
| reasoning-002 | model-small, 정책과 일치 | HTTP 429 | PROVIDER_FAILURE | 아니오 |

### simple-003

측정된 사실:

```text
Router는 configured default model을 선택했다.
Provider 호출은 성공했다.
Checklist는 requireJson FAIL이다.
Direct Model에서 small은 같은 형식으로 FAIL, large는 PASS다.
```

해석:

```text
현재 정책 위반 ROUTING_FAILURE
= 아니오
Router는 BASELINE_DEFAULT를 그대로 수행했다.

MODEL_QUALITY_FAILURE
= 예
선택된 Model이 JSON 형식 제약을 지키지 못했다.

ROUTING_FAILURE 후보
= 예
같은 Prompt에서 model-large는 Checklist PASS다.

PROMPT_FAILURE
= 단정하지 않음
Prompt는 JSON만 출력하라고 명시한다.
지시가 없어서 실패한 측정은 아니다.
```

DESIGN 21 예시는 이 Case를 ROUTING_FAILURE 후보로 본다.
후보다. 현재 정책이 틀렸다는 확정이 아니다.

simple-003 FAIL을 Runtime Routing 신호로 쓰지 않는다.

### reasoning-001 / reasoning-002

측정된 사실:

```text
Router 경로: HTTP 429 quota exceeded, errorCode = RATE_LIMIT
Selected Model = model-small
Quality / Latency / Cost는 이번 Router 실행에서 측정 불가

Direct Model small: 두 Case 모두 Checklist PASS
```

해석:

```text
PROVIDER_FAILURE
≠
MODEL_QUALITY_FAILURE
≠
ROUTING_FAILURE
```

Direct Model에서 small이 Reasoning Checklist를 통과한 기록이 있다.
이번 429를 이유로 Reasoning에 큰 Model이 필요하다고 단정하지 않는다.

HTTP 429를 Fallback / Retry 도입 근거로 쓰지 않는다.
이번 Phase에서 Provider Failure를 재현한 것이고, 해결 코드를 넣지 않는다.

### Cost / Latency

Dataset Expected에 Max Cost / Max Latency가 없다.

```text
COST_INEFFICIENCY
= 현재 Dataset 기준으로 확인되지 않음

이유:
Router는 모든 Case에서 model-small을 선택했다.
불필요하게 고비용 Model을 고른 Case는 없다.
```

```text
LATENCY_FAILURE
= 현재 Dataset 기준으로 확인되지 않음

이유:
Max Latency Expected가 없다.
Latency 값은 Phase 3에 있으나 Failure로 판정할 기준이 없다.
```

Phase 5에서 Quality / Cost / Latency Trade-off를 본다.
이 값이 없다고 해서 Trade-off가 없다고 주장하지 않는다.

### Capability

```text
CAPABILITY_MISMATCH
= 현재 Dataset에 실제 Case 없음

이유:
Capability Required Case를 이 Phase에서 넣지 않았다.
```

없는 Case를 만들었다고 쓰지 않는다.

### 유형별 실제 Case

| Failure Type | 현재 Dataset에서 존재 | Case |
|---|---|---|
| ROUTING_FAILURE (현재 정책 위반) | 없음 | |
| ROUTING_FAILURE 후보 (다른 Model이면 Checklist PASS) | 있음 | simple-003 |
| MODEL_QUALITY_FAILURE | 있음 | simple-003 |
| PROVIDER_FAILURE | 있음 | reasoning-001, reasoning-002 |
| CAPABILITY_MISMATCH | 없음 | Dataset에 Capability Case 없음 |
| COST_INEFFICIENCY | 없음 | 고비용 Model 과다 선택 없음 |
| LATENCY_FAILURE | 없음 | Max Latency Expected 없음 |
| EVALUATION_FAILURE | 없음 | Checklist는 정의대로 동작 |

Quality PASS 4건은 Failure Type이 없다.

---

## Candidate

이번 Phase에서 Routing Policy를 바꿀 후보는 없다.

simple-003을 Routing 신호로 쓰는 것은 Policy 변경이다.
Human Gate 없이 구현하지 않는다.

reasoning 429를 이유로 Fallback / Retry를 넣는 것도
이 Phase의 완료 조건이 아니다.

다음 Phase에서 검토할 문제:

```text
Phase 5
Quality / Cost / Latency Trade-off

질문:
small로 충분한 Case는 무엇인가?
simple-003에서 large를 쓰면 Checklist는 통과하지만
Cost / Latency 증가는 얼마인가?
그 증가를 Routing Policy 변경 근거로 볼 것인가?
```

Semantic Routing, Cascade, LLM Routing은
이 분류만으로 도입하지 않는다.

---

## Decision

```text
Router를 변경하지 않는다.

Failure Type Enum을 Runtime에 넣지 않는다.

실패 Case를 Dataset에서 삭제하지 않는다.

simple-003 FAIL을 이유로 Expected를 완화하지 않는다.

HTTP 429를 Model 품질 결과로 기록하지 않는다.

현재 정책 위반 ROUTING_FAILURE는 없다.

simple-003은 MODEL_QUALITY_FAILURE로 기록하고,
ROUTING_FAILURE 후보는 따로 남긴다.
```

---

## Remaining Limitation

```text
Router 경로와 Direct Model 경로는 실행 시점이 다르다.

Reasoning 2건은 Router 경로 Quality / Latency / Cost가 없다.

Checklist Quality는 키워드/형식 충족만 본다.

Capability Required Case는 현재 Dataset에 없다.

Max Cost / Max Latency Expected는 없다.

HTTP 429를 RATE_LIMIT로 묶고 있어 quota와 호출 제한이 같은 ErrorCode다.
```
