# 005. Routing Policy 한계 확인

## Problem / Question

현재 Routing Policy가 어떤 Request에서 한계를 보이는가?
더 복잡한 Routing이 필요한가?

```text
현재 Rule만으로 충분한가?

어떤 Request에서 반복적으로 잘못 Routing되는가?

Request 분류 기준이 너무 단순한가?

고정 Rule로 표현하기 어려운 Case가 실제로 존재하는가?

복잡한 Routing을 추가했을 때 얻을 수 있는 이점이 충분한가?
```

이 실험에서 특정 기술을 정답으로 두지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
현재 Dataset에서 Request를 분류하지 않는 Baseline Rule의
반복 Routing Failure가 관측되면,
더 복잡한 Routing을 검토할 근거가 된다.
```

후보 목록에 Semantic Routing / LLM Routing이 있다는 것만으로
필요하다고 단정하지 않는다.

---

## Conditions

새 Provider 호출을 하지 않는다.
Router를 바꾸지 않는다.
Dataset을 확장하지 않는다.

비교에 쓰는 기존 결과:

```text
docs/experiments/002-baseline-routing.md
docs/experiments/003-routing-failure-analysis.md
docs/experiments/004-quality-cost-latency.md
```

공통:

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Router: Baseline Router
strategy: BASELINE_DEFAULT
default-model: model-small
Quality: 기존 expectedCondition Checklist
```

Dataset의 SIMPLE / GENERAL / REASONING은 Evaluation 라벨이다.
Runtime Router는 이 라벨을 사용하지 않는다.

---

## Baseline

현재 Routing Rule:

```text
message가 null이 아니면
→ Model Catalog default model
strategy = BASELINE_DEFAULT
reason = configured default model
```

Request 내용, 유형, Capability, Cost, Latency를 보지 않는다.

---

## Result

측정된 사실만 모은다.

### Router가 실제로 선택한 것 (Phase 3)

```text
7 / 7 selectedModel = model-small
strategy = BASELINE_DEFAULT
```

Request 유형이 달라도 선택은 같다.

### 실패 분류 (Phase 4)

| Case | Failure Type | Router 선택이 정책과 다른가 |
|---|---|---|
| simple-003 | MODEL_QUALITY_FAILURE, ROUTING_FAILURE 후보 | 아니오 |
| reasoning-001 | PROVIDER_FAILURE | 아니오 |
| reasoning-002 | PROVIDER_FAILURE | 아니오 |

현재 정책 위반 ROUTING_FAILURE는 0건이다.

### Trade-off (Phase 5)

```text
Checklist 기준 small로 충분한 Case: 6 / 7

large만 Checklist PASS: simple-003 (JSON 형식 1건)

양쪽 성공 6 Case large Cost ≈ small 13.4배

Reasoning에서 small FAIL / large PASS인 Case: 없음
```

---

## Analysis

### 현재 Rule만으로 충분한가

Checklist 기준으로는 7건 중 6건에서 default model이 충분했다.

충분의 의미는 expectedCondition 충족이다.
답의 미묘한 품질은 이 기준으로 보지 않는다.

남은 1건은 simple-003 형식이다.
이 1건만으로 현재 Rule이 실패했다고 단정하지 않는다.

### 반복적으로 잘못 Routing되는 Request

Router는 Request를 분류하지 않는다.
오분류가 반복된다고 쓸 수 있는 측정이 없다.

같은 선택이 7번 나온 것은 정책 수행이다.
반복 오분류가 아니다.

JSON 형식 Case는 Dataset에 1건이다.
JSON Request가 반복 실패한다고 쓰지 않는다.

Provider 429 2건은 Routing 기준 실패가 아니다.

### Request 분류 기준이 너무 단순한가

현재 Router에는 Request 분류 기준이 없다.
단순한 분류가 틀린 것이 아니라, 분류 자체가 없다.

Dataset 라벨(Simple / General / Reasoning)을
Runtime이 잘못 썼다는 측정은 없다.

유형별 large가 필요하다는 Checklist 차이는
이번 Dataset에서 확인되지 않았다.

### 고정 Rule로 표현하기 어려운 Case

현재 Dataset에 복합 Request, 애매한 의도,
Capability Required Case는 없다.

simple-003은 Prompt에 JSON만 출력하라는 지시가 있다.
이 Case를 고정 Rule로 표현할 수 없는 증거로 쓰지 않는다.

고정 Rule로 표현하기 어려운 Case는
이번 Dataset에서 확인되지 않았다.

### 복잡한 Routing의 이점

Semantic Routing:

```text
Request 의미로 Model을 고른다.

이번 Dataset에는 의미 오분류로 인한
반복 ROUTING_FAILURE가 없다.

도입 이점을 측정할 Failure가 없다.
```

LLM-based Routing:

```text
Router가 추가로 LLM을 호출한다.

Routing Latency와 Cost가 늘어난다.

이번 Dataset에서 그 비용을 상쇄할
반복 Routing Failure가 없다.
```

Rule 세분화 (예: JSON → large):

```text
Policy 변경이다.
근거는 simple-003 1건이다.
Human Gate 없이 구현하지 않는다.
```

효과가 없었다고 쓰지 않는다.
비교 실험이 없다.
현재 문제에서는 사용하지 않는다.

---

## Candidate

구현하지 않는다. 검토만 한다.

```text
후보 A
현재 Baseline 유지
장점: 6 / 7 Checklist PASS, Cost가 더 낮은 쪽
단점: simple-003 형식 FAIL을 그대로 둔다

후보 B
Rule 세분화 (JSON 지시 → large)
장점: 1건 Checklist를 바꿀 후보
단점: Case 1건, Policy 변경, 형식 신호를 Routing에 넣음

후보 C
Semantic / LLM Routing
장점: 이후 복합 Request에 대비할 수 있다
단점: 현재 Failure가 없고, Routing Cost / Latency / 복잡도만 증가한다
```

Human Gate를 열지 않는다.
새 Routing 구조를 선택하지 않기 때문이다.

---

## Decision

```text
Router를 변경하지 않는다.

Semantic Routing을 사용하지 않는다.

LLM-based Routing을 사용하지 않는다.

Cascade를 사용하지 않는다.

현재 문제에서는 사용하지 않았다.
효과가 없었다고 주장하지 않는다.

이유:
현재 정책 위반 ROUTING_FAILURE가 없다.
반복 오분류 측정이 없다.
고정 Rule로 표현 불가한 Case가 Dataset에 없다.
복잡한 Routing의 이점을 측정할 Failure가 없다.
simple-003 1건은 Policy 변경 후보이지
새 Routing 구조 도입 근거가 아니다.
```

적용하지 않았으므로 동일 Dataset 재평가는 하지 않는다.

---

## Remaining Limitation

```text
Dataset Case는 7건이다.
JSON 형식 Case는 1건이다.

Capability / Long Context / 복합 Request는 없다.
이 한계는 Phase 7에서 확인한다.

Checklist는 키워드/형식만 본다.

Router 경로 Reasoning 2건은 429라 Quality가 없다.
Direct Model에서 small Reasoning은 PASS다.
```
