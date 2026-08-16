# ADR 003. Baseline Default Routing을 유지한다

## Context / Problem

Phase 3~8에서 Baseline Router를 평가하고
실패를 분류하고 Trade-off와 Capability / Provider Failure를 봤다.

Routing Policy를 바꿀지 결정해야 했다.

## Measured Evidence

```text
7 / 7 selectedModel = model-small
정책 위반 ROUTING_FAILURE = 0

Checklist 기준 small로 충분한 Case = 6 / 7
large만 PASS = simple-003 JSON 형식 1건

양쪽 성공 6 Case large Cost ≈ small 13.4배

Reasoning Checklist 품질 차이 측정 없음

Production Catalog Capability는 두 Model이 같다
Provider 429는 quota
```

## Candidates

```text
A. Baseline Default 유지
B. 항상 large
C. 유형별 / JSON Rule 세분화
D. Semantic 또는 LLM Routing
E. Fallback / Retry / Cascade
```

## Trade-offs

```text
A: Cost가 낮은 쪽을 유지. simple-003 형식 FAIL은 남는다.
B: Checklist 이득 1건. 나머지 PASS Case Cost가 커진다.
C: Case 1건을 Routing 신호로 쓴다. Policy 변경.
D/E: 현재 Failure로 이점을 측정하지 못했다. Complexity만 증가한다.
```

## Decision

```text
A. strategy = BASELINE_DEFAULT 를 유지한다.

Semantic Routing, LLM Routing, Cascade,
Fallback, Retry, Capability Filter를 넣지 않는다.
```

## Why

```text
반복 오분류가 없다.
small로 충분한 Case가 다수다.
형식 1건과 quota 429는 새 Routing 구조의 근거가 아니다.

현재 문제에서 사용하지 않는다.
효과가 없었다고 쓰지 않는다.
```

## Consequences

```text
최종 DESIGN은 Baseline Router만 기록한다.
사용하지 않은 기술을 미래 구조처럼 넣지 않는다.
simple-003 FAIL과 429는 Dataset / Experiment에 남는다.
```

## Remaining Limitations

```text
Checklist 한계, Reasoning Router 경로 429,
Capability Case 부재, Provider 하나.
```
