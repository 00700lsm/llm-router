# 006. Capability / Long Context 확인

## Problem / Question

Model Capability와 Context Limit이
현재 Routing에 영향을 주는가?

```text
현재 Catalog에서 Capability Mismatch가 발생하는가?

Router는 Capability / Context Limit을 보는가?

Long Context Request에서 현재 동작은 무엇인가?
```

미지원 Model을 고르지 않도록 Filter를 먼저 넣지 않는다.
현재 동작을 확인한다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
현재 Baseline Router는 Request Capability와
Model Capability를 비교하지 않는다.
```

Catalog에 Capability 필드가 있다는 것만으로
Capability Routing이 동작한다고 쓰지 않는다.

---

## Conditions

```text
Production Catalog: src/main/resources/application.yml
Router: Baseline Router
strategy: BASELINE_DEFAULT
evaluation/dataset.json: 변경 없음
Capability Filter: 없음
live Provider 호출: 없음
```

Production Catalog:

| Model | contextLimit | toolCalling | structuredOutput |
|---|---|---|---|
| model-small | 1048576 | true | true |
| model-large | 1048576 | true | true |

두 Model의 Capability 선언은 같다.

Baseline Dataset 7 Case는 Capability Required / Long Context Case가 아니다.
기존 Expected를 바꾸지 않았다.

재현:

```text
src/test/java/com/llmrouter/routing/BaselineRouterTest.java
selectsDefaultEvenWhenDefaultLacksToolCalling
selectsDefaultEvenWhenMessageExceedsContextLimit
```

Test Catalog는 Production Catalog가 아니다.
Test Catalog 결과를 Production Catalog 결과와 섞지 않는다.

---

## Baseline

현재 Routing Rule:

```text
message가 null이 아니면
→ configured default model
```

toolCalling, structuredOutput, contextLimit를 읽지 않는다.

Chat API는 Tool Calling을 실행하지 않는다.
Structured Output API를 요청하지 않는다.

---

## Result

### Production Catalog

Capability 선언이 같다.

```text
toolCalling: 둘 다 true
structuredOutput: 둘 다 true
contextLimit: 둘 다 1048576
```

Baseline Dataset 입력은 이 Context Limit보다 훨씬 짧다.

이 Catalog만 보면 Default Model 선택이
선언된 Capability와 어긋나지 않는다.

### Router 동작 (Test Catalog)

Default Model이 toolCalling=false이고
다른 Model이 toolCalling=true여도:

```text
selectedModel = model-small
strategy = BASELINE_DEFAULT
```

Context Limit이 8이고 message 길이가 그보다 길어도:

```text
selectedModel = model-small
strategy = BASELINE_DEFAULT
```

Router는 미지원 Model / Limit 초과 Request여도 Default를 고른다.

### Baseline Dataset

```text
CAPABILITY_MISMATCH Case: 없음
Long Context Case: 없음
```

simple-003 JSON FAIL은 Prompt 형식이다.
Structured Output Capability Routing 결과가 아니다.

---

## Analysis

### Capability Mismatch

Production Catalog에서는 발생하지 않는다.
두 Model이 같은 Capability를 선언하기 때문이다.

Router는 Request가 Tool을 필요로 하는지도 보지 않는다.
따라서 Production에서 Mismatch가 없다고 해서
Capability Routing이 동작한다고 쓰지 않는다.

Test Catalog에서는 Default가 Tool을 못 해도 Default를 고른다.
이것이 현재 Router의 Capability 동작이다.

```text
FR-09 Capability 기반 후보 제외
= 현재 구현되어 있지 않다
```

### Long Context

Production Dataset / Catalog 조합에서는
Context Limit 초과 Case가 없다.

Test Catalog에서 Limit보다 긴 message도
Router는 Default를 고른다.
Limit 초과를 막아 다른 Model로 보내거나
실패로 돌리지 않는다.

1M token Request를 live로 호출하지 않았다.
이 실험은 Router가 Limit을 쓰는지 확인한 것이다.

### Structured Output

Catalog 플래그는 둘 다 true다.
Runtime은 이 플래그로 Model을 고르지 않는다.

---

## Candidate

이번 Phase에서 Capability Filter를 넣을 후보는 없다.

이유:

```text
Production Catalog가 동질이라 Filter를 넣어도
현재 Dataset에서 선택 결과가 바뀌지 않는다.

Request Capability 신호가 Runtime에 없다.

Model을 하나 꺼서 차이를 만드는 것은
Catalog / 역할 변경이다. Human Gate다.
```

효과가 없었다고 쓰지 않는다.
Filter 비교 실험이 없다.
현재 문제에서는 사용하지 않는다.

---

## Decision

```text
Router를 변경하지 않는다.

Capability 기반 후보 제외를 넣지 않는다.

evaluation/dataset.json에 Capability / Long Context Case를 넣지 않는다.

Production Catalog Capability 값을 실험용으로 바꾸지 않는다.

CAPABILITY_MISMATCH는 현재 Production Dataset/Catalog에서 없다.

Long Context 초과는 현재 Production Dataset에서 없다.

현재 Router는 Capability / Context Limit을 무시한다.
```

---

## Remaining Limitation

```text
Request Capability를 추출하지 않는다.

Chat API는 Tool을 실행하지 않는다.

contextLimit 비교는 Token이 아니라
Test에서 문자 길이로 Router 무시를 재현했다.

1M token live 호출은 하지 않았다.
```
