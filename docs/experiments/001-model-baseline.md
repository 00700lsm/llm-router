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

공통:

```text
Dataset: evaluation/dataset.json
Case: Simple 3, General 2, Reasoning 2
Routing: 사용하지 않음 (Direct Model Runner)
Prompt: Dataset input 그대로
Quality: Deterministic Checklist (PASS / FAIL)
Runtime Router 변경: 없음
Chat API Retry / Fallback: 없음
Evaluation 호출 간격: 5초
```

OpenAI 실행 (이전):

```text
Models: model-small (gpt-4o-mini), model-large (gpt-4o)
```

Gemini 1차 (gemini-2.5-pro 404):

```text
Models: model-small (gemini-2.5-flash), model-large (gemini-2.5-pro)
```

Gemini 2차 (Human Gate A):

```text
Models: model-small (gemini-2.5-flash), model-large (gemini-3.5-flash)
model-large estimated cost: input 1.50 / output 9.00 per million tokens
Provider: GEMINI generateContent
결과 파일: evaluation/results/001-model-baseline.json
```

OpenAI 결과, gemini-2.5-pro 404 결과, gemini-3.5-flash 결과를
같은 Model의 Before / After 품질 개선으로 비교하지 않는다.

2차 비교는 gemini-2.5-flash vs gemini-3.5-flash다.
같은 세대 Pro vs Flash 비교가 아니다.

---

## Baseline

Phase 1 Baseline Router는 모든 일반 Request를 Default Model로 보낸다.

이 실험은 그 Router를 평가하지 않는다.

---

## Result

### OpenAI 실행

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

### Gemini 재실행

동일 Dataset, Direct Model Runner, 호출 간격 5초.

| Model | Provider Model | 실행 | 성공 | Quality PASS | 평균 Model Latency | Input Token | Output Token | Estimated Cost |
|---|---|---|---|---|---|---|---|---|
| model-small | gemini-2.5-flash | 7 | 7 | 6 / 7 | 10876 ms | 244 | 7357 | 0.00445080 |
| model-large | gemini-2.5-pro | 7 | 0 | 측정 불가 | 측정 불가 | 없음 | 없음 | UNKNOWN |

model-large 7 / 7은 같은 Provider Failure다.

```text
HTTP 404
This model models/gemini-2.5-pro is no longer available to new users
errorCode = PROVIDER_ERROR
```

이 실패를 model-large의 Quality Failure로 기록하지 않는다.

```text
PROVIDER_FAILURE
model unavailable to new users
≠
MODEL_QUALITY_FAILURE
```

model-small Checklist 결과:

| Case | Category | Quality | Latency (ms) | 비고 |
|---|---|---|---|---|
| simple-001 | SIMPLE | PASS | 1772 | |
| simple-002 | SIMPLE | PASS | 6109 | |
| simple-003 | SIMPLE | FAIL | 1334 | JSON을 코드펜스로 감싸서 requireJson 실패 |
| general-001 | GENERAL | PASS | 16328 | |
| general-002 | GENERAL | PASS | 14641 | |
| reasoning-001 | REASONING | PASS | 11095 | |
| reasoning-002 | REASONING | PASS | 24852 | |

simple-003은 Provider 호출은 성공했다.
실패 위치는 Quality Checklist / Prompt 형식이다.

```text
EVALUATION_FAILURE 또는 PROMPT_FAILURE 후보
≠
ROUTING_FAILURE
```

Simple / General / Reasoning 사이 Latency와 Output Token 차이는
model-small에서만 관측됐다.

```text
Simple 평균 Latency: 약 3072 ms
General 평균 Latency: 약 15485 ms
Reasoning 평균 Latency: 약 17974 ms
```

General / Reasoning 답변이 길고 Output Token이 크다.
Checklist는 키워드 충족만 보므로, 긴 답이 더 좋은 품질이라고 해석하지 않는다.

### Gemini 2차 (Human Gate A)

Catalog: `model-large` `gemini-2.5-pro` → `gemini-3.5-flash`.
동일 Dataset, Direct Model Runner, 호출 간격 5초.

| Model | Provider Model | 실행 | 성공 | Quality PASS | 평균 Model Latency | Input Token | Output Token | Estimated Cost |
|---|---|---|---|---|---|---|---|---|
| model-small | gemini-2.5-flash | 7 | 7 | 6 / 7 | 10164 ms | 244 | 6566 | 0.00397620 |
| model-large | gemini-3.5-flash | 7 | 6 | 6 / 7 | 12707 ms | 집계 불가 | 집계 불가 | UNKNOWN |

model-large 집계 Token / Cost가 UNKNOWN인 이유:

```text
reasoning-001 HTTP 503
This model is currently experiencing high demand
errorCode = PROVIDER_ERROR
```

503을 model-large Quality Failure로 기록하지 않는다.

성공한 6 Case만 보면:

```text
model-large Input Token 196
model-large Output Token 5063
model-large Estimated Cost 0.04586100
```

Case별 Checklist:

| Case | Category | small | large | small Latency | large Latency |
|---|---|---|---|---|---|
| simple-001 | SIMPLE | PASS | PASS | 2357 | 5525 |
| simple-002 | SIMPLE | PASS | PASS | 4557 | 4342 |
| simple-003 | SIMPLE | FAIL | PASS | 2107 | 3967 |
| general-001 | GENERAL | PASS | PASS | 16915 | 24140 |
| general-002 | GENERAL | PASS | PASS | 12997 | 18165 |
| reasoning-001 | REASONING | PASS | PROVIDER_FAILURE | 9096 | 측정 불가 |
| reasoning-002 | REASONING | PASS | PASS | 23116 | 20100 |

simple-003:

```text
small: JSON을 코드펜스로 감싸서 requireJson FAIL
large: JSON 객체로 통과
```

Dataset Expected는 바꾸지 않는다.

양쪽 모두 성공한 6 Case:

```text
Checklist: small 5 / 6 PASS, large 6 / 6 PASS
평균 Latency: small 10342 ms, large 12707 ms
Estimated Cost: small 0.00341760, large 0.04586100
```

유형별 Checklist (성공한 호출만):

```text
Simple: small 2 / 3, large 3 / 3
General: small 2 / 2, large 2 / 2
Reasoning: small 2 / 2, large 1 / 1 (다른 1건은 503)
```

Reasoning에서 품질 차이가 커졌다고 말할 측정값은 없다.
양쪽이 모두 답을 낸 Reasoning Case는 1건이고 둘 다 PASS다.

---

## Analysis

확인된 사실:

```text
gemini-3.5-flash는 이 API Key에서 호출된다.

Checklist PASS 수는 두 Model 모두 6 / 7이다.
FAIL 위치가 다르다.
small FAIL = simple-003 형식
large FAIL = reasoning-001 Provider 503

Simple에서 관측된 Checklist 차이는 JSON 형식 1건이다.

General 2건은 둘 다 PASS다.

Reasoning 품질 차이는 이번 실행으로 확인되지 않았다.

양쪽 성공 6 Case에서 large 평균 Latency가 더 길다.
Case마다 방향이 같지는 않다.

같은 6 Case에서 large Estimated Cost가 더 크다.
Catalog 단가가 다르고 Output Token도 있다.

Router는 사용하지 않았다.
```

이번 503을 이유로 Runtime Retry / Fallback을 넣지 않는다.

---

## Candidate

이번 Phase에서 Routing Policy를 바꿀 후보는 없다.

simple-003 형식 FAIL을 Runtime Routing에 쓰지 않는다.

---

## Decision

```text
Router를 변경하지 않는다.

Human Gate A를 적용했다.
model-large provider-model = gemini-3.5-flash

OpenAI credit_balance_exhausted를 Model 품질 결과로 기록하지 않는다.

gemini-2.5-pro 404를 Model 품질 결과로 기록하지 않는다.

reasoning-001 HTTP 503을 Model 품질 결과로 기록하지 않는다.

simple-003 FAIL을 이유로 Dataset Expected를 완화하지 않는다.
```

---

## Remaining Limitation

```text
비교 대상은 gemini-2.5-flash vs gemini-3.5-flash다.
gemini-2.5-pro 특성은 이 API Key로 측정하지 못했다.

Checklist Quality는 키워드/형식 충족만 본다.

reasoning-001 large는 503이라 Token / Cost 집계가 UNKNOWN이다.

HTTP 429와 503을 서로 다른 ErrorCode로 구분하지만,
404 model unavailable과 다른 Provider Error도 PROVIDER_ERROR로 묶인다.

OpenAI 14 Case 결과는 OpenAI 호출 결과이며 Gemini 품질 측정 결과가 아니다.
```
