# ADR 001. Gemini Provider 사용

## Context / Problem

Phase 2 Direct Model Evaluation을 OpenAI로 실행했다.
Router 품질을 보기 전에 Model 특성을 측정해야 했다.

## Measured Evidence

```text
OpenAI 14 / 14 HTTP 429
type = insufficient_quota
code = credit_balance_exhausted
errorCode = RATE_LIMIT
```

호출 간격 5초를 넣어도 결과는 같았다.
이 결과는 MODEL_QUALITY_FAILURE가 아니다.

## Candidates

```text
A. OpenAI credit를 충전하고 같은 Catalog로 재측정
B. Provider를 Gemini로 바꾸고 같은 Dataset으로 재측정
C. 측정을 포기하고 Routing Policy를 먼저 바꾼다
```

## Trade-offs

```text
A: Provider를 유지한다. 이 환경의 credit 문제가 남는다.
B: Model 세대/가격이 달라져 OpenAI 결과와 품질 비교를 하지 못한다.
C: 측정 없이 Policy를 바꾼다. Baseline First에 어긋난다.
```

## Decision

```text
B. Provider를 Gemini generateContent로 바꾼다.
```

## Why

```text
이 API Key로는 OpenAI 호출이 되지 않았다.
측정 없는 Routing 변경은 하지 않는다.
OpenAI 결과는 Provider Failure로 남긴다.
```

## Consequences

```text
Catalog / Client가 Gemini 기준이 된다.
OpenAI 14 Case는 Gemini 품질 측정이 아니다.
```

## Remaining Limitations

```text
Provider는 하나다.
NFR-14의 다 Provider 검증은 하지 않았다.
```
