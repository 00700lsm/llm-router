# 008. Routing 구조 재평가

## Problem / Question

지금까지의 Experiment 기준으로
현재 Router는 REQUIREMENTS를 어느 정도 만족하는가?

```text
Routing Failure는 줄었는가?

Quality가 실제로 좋아졌는가?

Cost가 줄었는가?

Latency가 악화되지는 않았는가?

새 Complexity가 실제 결과로 정당화되는가?

아직 해결되지 않은 Requirement는 무엇인가?
```

추가 기술을 넣었다고 개선됐다고 쓰지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
현재 Router는 Phase 1 Baseline과 같은 Routing Policy다.
Policy를 바꾸지 않았다면 Routing Metric 개선을 주장할 수 없다.
```

---

## Conditions

새 Provider 호출을 하지 않는다.
Router를 바꾸지 않는다.
Dataset을 바꾸지 않는다.

비교:

```text
Baseline Router = Phase 1
strategy = BASELINE_DEFAULT
default-model = model-small

Current Router = 같은 Policy
```

동일 Dataset:

```text
evaluation/dataset.json
Simple 3, General 2, Reasoning 2
```

측정 출처:

```text
001-model-baseline.md        Direct Model
002-baseline-routing.md      Router 경로
003-routing-failure-analysis.md
004-quality-cost-latency.md
005-routing-policy-limit.md
006-capability.md
007-provider-failure.md
```

Catalog / Provider는 Routing Policy와 별개다.

```text
Provider: OpenAI → Gemini
model-large: gemini-2.5-pro → gemini-3.5-flash (Human Gate A)
```

이 변경을 Routing Quality 개선으로 쓰지 않는다.

---

## Baseline

Phase 1 Routing:

```text
모든 일반 Request
→ Configured Default Model
strategy = BASELINE_DEFAULT
reason = configured default model
```

Phase 3 Router 경로 측정이 Baseline Routing 평가 값이다.

```text
7 / 7 selectedModel = model-small
호출 성공 5 / 7
Quality PASS 4 / 7
```

---

## Result

### Routing

| 항목 | Baseline | Current |
|---|---|---|
| strategy | BASELINE_DEFAULT | BASELINE_DEFAULT |
| default | model-small | model-small |
| Dataset 7 Case 선택 | 전부 model-small | 전부 model-small |
| 정책 위반 ROUTING_FAILURE | 0 | 0 |

현재 Router는 Baseline Router와 같다.

### Quality

Router 경로 (Phase 3):

```text
Quality PASS 4 / 7
simple-003 FAIL (JSON 형식)
reasoning-001 / 002는 RATE_LIMIT이라 Quality 측정 불가
```

Direct Model (Phase 2, Gemini 2차):

```text
small 6 / 7 PASS (FAIL = simple-003)
large 6 / 7 PASS (FAIL = reasoning-001 HTTP 503)
```

Routing Policy를 바꾸지 않았으므로
Router 경로 Quality가 좋아졌다고 쓰지 않는다.

### Cost

양쪽 성공 6 Case Direct Model:

```text
small Estimated Cost 합 0.00341760
large Estimated Cost 합 0.04586100
비율 ≈ 13.4
```

현재 Policy는 모든 일반 Request를 small로 보낸다.
불필요하게 large를 고른 Case는 없다.

Routing 변경으로 Cost가 줄었다고 쓰지 않는다.
Policy가 그대로이기 때문이다.

### Latency

Router 경로 Phase 3 성공 5건은 측정값이 있다.
Routing Policy 변경 전후 비교는 없다.
같은 Policy다.

복잡한 Router를 넣지 않아서
Routing Latency가 커졌다는 측정도 없다.

### Capability

```text
Catalog 필드: 있음
Runtime Filter: 없음
Production Mismatch Case: 없음
```

### Failure Handling

```text
ErrorCode로 반환
Retry: 없음
Fallback: 없음
```

---

## Analysis

### Routing Failure는 줄었는가

줄일 정책 위반 ROUTING_FAILURE가 없었다.

simple-003은 MODEL_QUALITY_FAILURE이고
ROUTING_FAILURE 후보다.
Policy를 바꾸지 않아 이 후보도 그대로다.

### Quality가 실제로 좋아졌는가

Router 경로 Checklist는 Phase 3 값이 현재 값이다.
좋아졌다고 쓰지 않는다.

### Cost가 줄었는가

Routing 변경이 없으므로 줄었다고 쓰지 않는다.
small default는 둘 다 PASS인 Case에서 더 낮은 Cost 쪽이다.
이것은 Phase 5 관측이지 Before / After 절감이 아니다.

### Latency가 악화되었는가

Routing Complexity를 넣지 않았다.
Routing Latency 악화 측정은 없다.

### 새 Complexity가 정당화되는가

Routing에 새 Complexity를 넣지 않았다.
정당화할 대상이 없다.

### 미충족 / 사용하지 않은 Requirement

충족:

```text
FR-01 ~ FR-08 요청, Catalog, Baseline Routing, 호출, 응답, 관측, Capability 필드
FR-10 ~ FR-13 Token, Cost, Latency, Evaluation 실행
FR-15 ~ FR-18 Direct 비교, Quality Checklist, Failure 확인, Provider Failure 확인
```

부분:

```text
FR-14 Allowed Models / Max Cost / Max Latency Expected는 Dataset에 없다.
      Quality expectedCondition은 있다.
```

구현하지 않음. 현재 문제에서 사용하지 않음:

```text
FR-09 Capability 기반 후보 제외
      Production Mismatch Case가 없다.

FR-19 Fallback
      관측된 429는 quota라 같은 Provider Fallback 근거가 부족하다.

FR-20 Routing 전략 변경
      반복 정책 위반 Failure가 없어 바꾸지 않았다.
      바꿀 수 있는 구조는 Baseline Router 교체 지점이다.

FR-21 Model Cascade
      Checklist 차이는 simple-003 형식 1건이다.
      Cascade 도입 근거로 쓰지 않는다.
```

FR-22는 동일 Dataset으로 다시 측정할 수 있다.
이번 Phase는 Policy가 같아서 전략 간 Before / After가 아니다.

### 사용하지 않은 기술

| 기술 | 이유 |
|---|---|
| Semantic Routing | 반복 오분류 측정이 없다 |
| LLM-based Routing | Routing Cost / Latency를 상쇄할 Failure가 없다 |
| Model Cascade | 유형별 Quality 차이 측정이 없다 |
| Routing Cache | Routing이 Default lookup이라 Cache 문제를 재현하지 않았다 |
| Dynamic Threshold | Threshold가 없다 |
| Fallback | 같은 Provider quota 근거가 부족하다 |
| Retry | quota 429에 이득을 측정하지 않았다 |
| LangGraph / Multi-Agent | 여러 Loop가 문제로 확인되지 않았다 |

효과가 없었다고 쓰지 않는다.
비교 실험이 없는 항목은 현재 문제에서 사용하지 않았다고 쓴다.

---

## Candidate

이번 Phase에서 Routing 구조를 바꿀 후보는 없다.

Phase 9에서 최종 회고한다.

---

## Decision

```text
Router를 변경하지 않는다.

현재 구조는 Phase 1 Baseline과 같다.

Routing Quality / Cost / Latency가 개선됐다고 쓰지 않는다.

새 Routing 기술을 넣지 않은 것은 실패가 아니다.

미충족은 FR-09, FR-19, FR-20 미적용, FR-21 미적용이다.
이 항목은 현재 Failure 근거가 부족해 사용하지 않았다.
```

---

## Remaining Limitation

```text
Router 경로 Reasoning 2건은 429라 Quality가 없다.

Checklist는 키워드/형식만 본다.

Capability / Long Context Case는 Baseline Dataset에 없다.

Max Cost / Max Latency Expected는 없다.

Provider는 Gemini 하나다.

Estimated Cost는 Catalog 단가 예상값이다.
```
