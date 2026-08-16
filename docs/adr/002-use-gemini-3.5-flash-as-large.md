# ADR 002. model-large를 gemini-3.5-flash로 둔다

## Context / Problem

Gemini 1차에서 model-large = gemini-2.5-pro 로 Direct Model Evaluation을 실행했다.

## Measured Evidence

```text
model-large 7 / 7 HTTP 404
This model models/gemini-2.5-pro is no longer available to new users
errorCode = PROVIDER_ERROR
```

이 실패를 large의 Quality Failure로 기록하지 않았다.

## Candidates

```text
A. model-large provider-model을 이 Key에서 호출되는 Model로 바꾼다
B. large 측정을 포기하고 small만으로 Router를 평가한다
C. 404를 이유로 Fallback을 넣는다
```

## Trade-offs

```text
A: 두 Model 비교가 가능해진다. 같은 세대 Pro vs Flash 비교는 아니다.
B: Trade-off를 볼 Catalog가 한 Model이 된다.
C: Catalog 오류를 Fallback으로 숨긴다.
```

## Decision

```text
A. Human Gate A
model-large = gemini-3.5-flash
input 1.50 / output 9.00 per million tokens
```

## Why

```text
404는 Model 품질이 아니라 Catalog의 provider-model 문제다.
비교할 두 번째 Model이 필요했다.
Fallback으로 404를 가리지 않는다.
```

## Consequences

```text
이후 Direct / Router 비교는
gemini-2.5-flash vs gemini-3.5-flash 다.

gemini-2.5-pro 특성은 이 Key로 측정하지 못했다.

2차 실행: large 6 / 7 성공.
reasoning-001 large는 HTTP 503.
```

## Remaining Limitations

```text
비교 대상이 Flash vs Flash다.
OpenAI / gemini-2.5-pro 결과와 품질 Before / After를 만들지 않는다.
```
