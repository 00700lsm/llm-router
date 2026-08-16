# 001. Model Baseline 특성 측정

## Problem / Question

Router를 평가하기 전에, 현재 Catalog의 Model들이
동일 Dataset에서 어떤 특성을 보이는가?

```text
Simple Request에서 Model별 품질 차이가 있는가?

General Request에서는 차이가 있는가?

Reasoning Request에서는 차이가 커지는가?

Model별 Latency / Token / Cost 차이는 어느 정도인가?
```

---

## Hypothesis

이름이나 가격만 보고 성능을 가정하지 않는다.

확인이 필요한 가설은 다음 정도다.

```text
현재 Dataset에서 Model 간 Quality / Latency / Cost 차이가
실제로 관측될 수 있다.
```

---

## Conditions

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Models: model-small (gpt-4o-mini), model-large (gpt-4o)
Routing: 사용하지 않음 (Direct Model Runner)
Prompt: Dataset input 그대로
Quality: Deterministic Checklist (PASS / FAIL)
Runtime Router 변경: 없음
Chat API Retry / Fallback: 없음
Evaluation 호출 간격: 5초
```

---

## Baseline

Phase 1 Baseline Router는 모든 일반 Request를 Default Model로 보낸다.

이 실험은 그 Router를 평가하지 않는다.

---

## Result

Provider까지 호출했다.

1차 실행: 14 / 14 `RATE_LIMIT`

2차 실행: Evaluation 호출 간격 5초를 넣고 재실행.

```text
14 / 14 실패
HTTP 429
type = insufficient_quota
code = credit_balance_exhausted
message = You have no credits remaining
```

| Model | 실행 | 성공 | Quality PASS | Token | Cost |
|---|---|---|---|---|---|
| model-small | 7 | 0 | 측정 불가 | 없음 | UNKNOWN |
| model-large | 7 | 0 | 측정 불가 | 없음 | UNKNOWN |

호출 간격을 넣어도 결과는 같다.

이 결과는 Model Quality Failure가 아니다.

```text
PROVIDER_FAILURE
credit_balance_exhausted
≠
MODEL_QUALITY_FAILURE
```

---

## Analysis

핵심 질문에 답할 측정값이 없다.

확인된 사실:

```text
API Key는 Provider에 도달한다.

실패 원인은 계정 크레딧 부족이다.

연속 호출 대기로 해결되지 않는다.

Router를 거치지 않는 경로에서도 Provider Failure는 실패로 남는다.
```

아직 확인되지 않은 것:

```text
Simple / General / Reasoning에서 Model별 품질 차이

Model별 Latency / Token / Cost 차이
```

이번 실패를 이유로 Fallback이나 Retry를 Runtime에 넣지 않는다.

---

## Candidate

크레딧이 있는 계정으로 같은 Dataset을 재실행한다.

이번 Phase에서 Routing Policy를 바꿀 후보는 없다.

---

## Decision

```text
Router를 변경하지 않는다.

credit_balance_exhausted를 Model 품질 결과로 기록하지 않는다.

크레딧이 있는 뒤에 동일 조건으로 재측정한다.
```

---

## Remaining Limitation

```text
실제 Model 특성이 측정되지 않았다.

Checklist Quality는 키워드/형식 충족만 본다.

HTTP 429를 RATE_LIMIT로 묶고 있어 quota 소진과 호출 제한이 같은 ErrorCode다.

Phase 2 완료 조건은 아직 만족하지 않는다.
```
