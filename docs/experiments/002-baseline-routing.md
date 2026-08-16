# 002. Baseline Routing 평가

## Problem / Question

Phase 1 Baseline Router는 현재 Dataset에서
실제로 어떤 Model을 선택하고, 그 결과가
Expected Condition을 만족하는가?

```text
Router는 어떤 Model을 선택하는가?

Simple Request에서 불필요하게 큰 Model을 선택하는가?

Reasoning Request에서 충분한 Model을 선택하는가?

Routing 결과가 Quality / Cost / Latency 조건을 만족하는가?
```

---

## Hypothesis

현재 Routing Policy는 모든 일반 Request를 Default Model로 보낸다.

확인이 필요한 가설은 다음 정도다.

```text
Baseline Router는 Dataset의 모든 Case에서
configured default model을 선택한다.
```

이름이나 가격만 보고 Routing이 잘못됐다고 단정하지 않는다.

---

## Conditions

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Router: Baseline Router
strategy: BASELINE_DEFAULT
default-model: model-small (gemini-2.5-flash)
Prompt: Dataset input 그대로
Quality: 기존 expectedCondition Checklist
Runtime Router 변경: 없음
Chat API Retry / Fallback: 없음
Evaluation 호출 간격: 5초
결과 파일: evaluation/results/002-baseline-routing.json
```

Allowed Models / Max Cost / Max Latency를
Dataset Expected에 새로 넣지 않았다.

Direct Model Evaluation 결과와 섞지 않는다.

---

## Baseline

```text
모든 일반 Request
→ Configured Default Model
strategy = BASELINE_DEFAULT
reason = configured default model
```

이 실험은 Router를 개선하지 않는다.

---

## Result

전체 7 Case를 Router 경로로 실행했다.

Selected Model:

| Case | Category | Selected Model | Strategy |
|---|---|---|---|
| simple-001 | SIMPLE | model-small | BASELINE_DEFAULT |
| simple-002 | SIMPLE | model-small | BASELINE_DEFAULT |
| simple-003 | SIMPLE | model-small | BASELINE_DEFAULT |
| general-001 | GENERAL | model-small | BASELINE_DEFAULT |
| general-002 | GENERAL | model-small | BASELINE_DEFAULT |
| reasoning-001 | REASONING | model-small | BASELINE_DEFAULT |
| reasoning-002 | REASONING | model-small | BASELINE_DEFAULT |

```text
selectedModelCounts
model-small = 7
```

Quality / Latency / Cost:

| Case | success | Quality | Latency (ms) | Input Token | Output Token | Estimated Cost | errorCode |
|---|---|---|---|---|---|---|---|
| simple-001 | true | PASS | 1391 | 28 | 6 | 0.00000780 | |
| simple-002 | true | PASS | 4224 | 45 | 12 | 0.00001395 | |
| simple-003 | true | FAIL | 1671 | 36 | 27 | 0.00002160 | |
| general-001 | true | PASS | 16166 | 10 | 1944 | 0.00116790 | |
| general-002 | true | PASS | 13538 | 10 | 1460 | 0.00087750 | |
| reasoning-001 | false | FAIL | 측정 불가 | 없음 | 없음 | UNKNOWN | RATE_LIMIT |
| reasoning-002 | false | FAIL | 측정 불가 | 없음 | 없음 | UNKNOWN | RATE_LIMIT |

실패 Case:

```text
simple-003
Provider 호출 성공
Quality FAIL
reason = answer is not a JSON object
selectedModel = model-small

reasoning-001
HTTP 429 quota exceeded
errorCode = RATE_LIMIT
selectedModel = model-small

reasoning-002
HTTP 429 quota exceeded
errorCode = RATE_LIMIT
selectedModel = model-small
```

집계:

```text
실행 7
호출 성공 5
Quality PASS 4
```

---

## Analysis

Router 선택:

```text
7 / 7 model-small
strategy = BASELINE_DEFAULT
```

Simple에서 불필요하게 큰 Model을 선택했는가?

```text
아니오.
Simple 3건 모두 model-small이다.
```

Reasoning에서 충분한 Model을 선택했는가?

```text
Router는 Reasoning도 model-small을 선택했다.

이번 실행에서 Reasoning 2건은 HTTP 429라
Quality / Latency / Cost를 측정하지 못했다.

충분한 Model인지는 이번 실행으로 단정하지 않는다.
```

Quality 조건:

```text
호출에 성공한 5건 중 Checklist PASS는 4건이다.

simple-003 FAIL은 JSON 형식이다.
Router가 Default Model을 선택한 것과 별개다.
```

simple-003을 ROUTING_FAILURE로 단정하지 않는다.

```text
정책상 Default Model 선택
+
해당 Model 답이 형식 제약을 못 지킴
≠
이번 Phase에서 Routing Policy를 바꾼다
```

reasoning-001 / reasoning-002는 PROVIDER_FAILURE 후보다.

```text
HTTP 429 quota exceeded
≠
MODEL_QUALITY_FAILURE
≠
ROUTING_FAILURE
```

이번 실패를 이유로 Fallback / Retry / Routing Policy 변경을 하지 않는다.

Dataset Expected를 결과에 맞춰 바꾸지 않는다.

---

## Candidate

이번 Phase에서 Routing Policy를 바꿀 후보는 없다.

실패 Case는 Phase 4에서 Failure Type을 분리한다.

---

## Decision

```text
Router를 변경하지 않는다.

실패 Case를 Dataset에서 삭제하지 않는다.

simple-003 FAIL을 이유로 Expected를 완화하지 않는다.

HTTP 429를 Model 품질 결과로 기록하지 않는다.
```

---

## Remaining Limitation

```text
Capability Required Case는 현재 Dataset에 없다.

Reasoning 2건은 429라 Router 경로 Quality / Latency / Cost가 없다.

Checklist Quality는 키워드/형식 충족만 본다.

HTTP 429를 RATE_LIMIT로 묶고 있어 quota와 호출 제한이 같은 ErrorCode다.

Allowed Models / Max Cost / Max Latency Expected는 이번 Phase에서 추가하지 않았다.
```
