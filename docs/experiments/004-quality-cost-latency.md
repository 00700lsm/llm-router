# 004. Quality / Cost / Latency Trade-off

## Problem / Question

Router가 가장 좋은 Model을 고르는 시스템이 아니라
요구 품질을 만족하면서 Cost와 Latency를 어떻게 균형 잡을지 확인한다.

```text
작은 Model로 충분한 Request는 무엇인가?

큰 Model을 사용했을 때 Quality가 실제로 얼마나 증가하는가?

Quality 증가에 비해 Cost 증가는 어느 정도인가?

Latency 증가는 정당화되는가?

같은 품질을 더 낮은 비용으로 처리할 수 있는 Case가 있는가?
```

단순히 Large Model이 더 좋은 답을 냈다고 해서
Large Model 선택을 정답으로 두지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
현재 Dataset에서 small이 Checklist를 만족하는 Case가 있고,
그 Case에서 large는 Cost가 더 크다.
```

이름이나 가격만 보고 large가 더 낫다고 단정하지 않는다.

---

## Conditions

새 Provider 호출을 하지 않는다.

비교 대상:

```text
Direct Model: docs/experiments/001-model-baseline.md
              evaluation/results/001-model-baseline.json
              Gemini 2차
```

공통:

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Routing: 사용하지 않음 (Direct Model Runner)
model-small: gemini-2.5-flash
             input 0.15 / output 0.60 per million tokens
model-large: gemini-3.5-flash
             input 1.50 / output 9.00 per million tokens
Prompt: Dataset input 그대로
Quality: 기존 expectedCondition Checklist
Runtime Router 변경: 없음
Dataset Expected 변경: 없음
종합 Score: 없음
```

Estimated Cost는 Catalog 단가 기반 예상값이다. 실제 Billing이 아니다.

Router 경로 결과와 이 표를 섞지 않는다.

Allowed Models / Max Cost / Max Latency Expected는 Dataset에 없다.
이 값이 없다고 해서 Trade-off가 없다고 쓰지 않는다.
Failure로 판정하지도 않는다.

---

## Baseline

현재 Routing Policy:

```text
모든 일반 Request
→ Configured Default Model = model-small
strategy = BASELINE_DEFAULT
```

이 실험은 그 Policy를 바꾸지 않는다.
Direct Model 결과로 Trade-off만 본다.

---

## Result

측정값은 Phase 2 Gemini 2차 Direct Model 실행이다.

### Case별 Quality / Latency / Cost

| Case | Category | small Quality | large Quality | small Latency (ms) | large Latency (ms) | small Cost | large Cost |
|---|---|---|---|---|---|---|---|
| simple-001 | SIMPLE | PASS | PASS | 2357 | 5525 | 0.00000780 | 0.00008700 |
| simple-002 | SIMPLE | PASS | PASS | 4557 | 4342 | 0.00001575 | 0.00023850 |
| simple-003 | SIMPLE | FAIL | PASS | 2107 | 3967 | 0.00002160 | 0.00019800 |
| general-001 | GENERAL | PASS | PASS | 16915 | 24140 | 0.00113670 | 0.01320000 |
| general-002 | GENERAL | PASS | PASS | 12997 | 18165 | 0.00077130 | 0.01164300 |
| reasoning-001 | REASONING | PASS | PROVIDER_FAILURE | 9096 | 측정 불가 | 0.00055860 | UNKNOWN |
| reasoning-002 | REASONING | PASS | PASS | 23116 | 20100 | 0.00146445 | 0.02049450 |

reasoning-001 large는 HTTP 503이다.
이 행의 Cost / Latency 비교는 하지 않는다.
503을 Quality 차이로 기록하지 않는다.

### 양쪽 호출이 성공한 6 Case

```text
Checklist: small 5 / 6 PASS, large 6 / 6 PASS
평균 Latency: small 10342 ms, large 12707 ms
Estimated Cost 합: small 0.00341760, large 0.04586100
```

Cost 합 비율:

```text
large / small ≈ 13.4
```

이 6 Case에 simple-003이 들어 있다.
small의 1 FAIL은 JSON 형식이다.

### Case별 Cost / Latency 비율

양쪽 성공 Case만. Quality와 합친 Score는 만들지 않는다.

| Case | Quality (small / large) | Cost 비율 (large/small) | Latency 비율 (large/small) |
|---|---|---|---|
| simple-001 | PASS / PASS | 11.2 | 2.34 |
| simple-002 | PASS / PASS | 15.1 | 0.95 |
| simple-003 | FAIL / PASS | 9.2 | 1.88 |
| general-001 | PASS / PASS | 11.6 | 1.43 |
| general-002 | PASS / PASS | 15.1 | 1.40 |
| reasoning-002 | PASS / PASS | 14.0 | 0.87 |

Latency 방향은 Case마다 다르다.
simple-002, reasoning-002는 large가 더 짧다.
나머진 large가 더 길다.

---

## Analysis

### 작은 Model로 충분한 Case

Checklist PASS를 "충분"의 측정값으로 쓴다.
답의 미묘한 품질 차이는 이 Checklist로 측정하지 않았다.

```text
simple-001
simple-002
general-001
general-002
reasoning-001
reasoning-002
```

6 / 7 Case에서 small이 Checklist를 만족한다.

reasoning-001은 large 호출이 실패했다.
small PASS만으로 large가 더 필요하다고 단정하지 않는다.

### 큰 Model이 필요한 Case

Checklist 기준으로 large만 PASS인 Case:

```text
simple-003
```

측정된 차이:

```text
small: JSON을 코드펜스로 감싸서 requireJson FAIL
large: JSON 객체로 통과
```

이 1건을 Reasoning 품질 때문에 큰 Model이 필요하다고 해석하지 않는다.

General 2건, Reasoning에서 양쪽이 답을 낸 1건은 둘 다 PASS다.
유형이 커질수록 Quality 차이가 커졌다는 측정값은 없다.

### Quality 증가는 얼마인가

양쪽 성공 6 Case:

```text
Checklist PASS
small 5 / 6
large 6 / 6
```

차이는 simple-003 형식 1건이다.

둘 다 PASS인 5 Case에서 Quality가 증가했다고 쓰지 않는다.
Checklist가 그 차이를 측정하지 않는다.

긴 답을 더 좋은 품질로 보지 않는다.
general-001은 large Output Token이 더 적은데도 Cost는 더 크다.

### Cost 증가

양쪽 성공 6 Case에서 large Estimated Cost 합은 small의 약 13.4배다.

Case별 비율은 약 9.2 ~ 15.1이다.

원인:

```text
Catalog 단가가 다르다.
input 0.15 → 1.50
output 0.60 → 9.00
```

Output Token이 줄어도 단가가 커서 Cost가 커진 Case가 있다.

둘 다 PASS인 Case에서 large를 고르면
Checklist 결과는 같고 Cost만 커진다.

### Latency

평균은 large가 더 길다.

```text
양쪽 성공 6 Case
small 10342 ms
large 12707 ms
```

Case마다 방향이 같다고 쓰지 않는다.

Latency 증가가 정당화되는지는
Max Latency Expected가 없어 Failure로 판정하지 않는다.
둘 다 PASS인 Case에서 large가 더 긴 것은
Checklist 이득 없이 Latency만 늘어난 관측이다.

### 같은 품질, 더 낮은 비용

둘 다 Checklist PASS인 Case:

```text
simple-001
simple-002
general-001
general-002
reasoning-002
```

이 5건은 small이 같은 Checklist 결과를 더 낮은 Cost로 냈다.

현재 Router는 이 5건을 포함해 모든 일반 Request를 small로 보낸다.
불필요하게 고비용 Model을 고른 관측은 없다.

simple-003만 small FAIL / large PASS다.
이 1건의 Checklist 이득을 위해 전체 Case를 large로 바꾸면
나머지 PASS Case의 Cost가 약 9 ~ 15배로 커진다.

---

## Candidate

이번 Phase에서 Routing Policy를 바꿀 후보는 없다.

검토만 한 선택:

```text
항상 large
→ 둘 다 PASS인 Case에서 Cost가 커진다.
→ Checklist 이득은 simple-003 1건이다.

유형별 large (예: Reasoning → large)
→ Reasoning Checklist 차이는 이번 측정에 없다.
→ Cost는 커진다.

simple-003만 다른 Model
→ Policy 변경이다.
→ JSON 형식 1건을 Routing 신호로 쓰는 것이다.
→ Human Gate 없이 구현하지 않는다.
```

Semantic Routing, Cascade, 종합 Score는
이 Trade-off만으로 도입하지 않는다.

---

## Decision

```text
Router를 변경하지 않는다.

Quality / Cost / Latency를 한 Score로 합치지 않는다.

Max Cost / Max Latency를 Dataset Expected에 넣지 않는다.

작은 Model로 충분한 Case는 Checklist 기준 6 / 7이다.

큰 Model만 Checklist를 통과한 Case는 simple-003 1건이다.
이 1건을 큰 Model이 필요하다고 단정하지 않는다.

현재 Baseline(default = small)을 바꿀 측정 근거는 부족하다.
```

---

## Remaining Limitation

```text
비교는 gemini-2.5-flash vs gemini-3.5-flash다.

Checklist Quality는 키워드/형식 충족만 본다.

reasoning-001 large는 503이라 Trade-off 비교가 없다.

Max Cost / Max Latency Expected는 없다.

Estimated Cost는 Catalog 단가 예상값이다.

Direct Model 실행과 Router 경로는 시점이 다르다.
이 실험은 Direct Model 비교다.
```
