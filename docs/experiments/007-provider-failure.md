# 007. Provider Failure 재현

## Problem / Question

Provider Failure가 나면 현재 시스템은 어떻게 동작하는가?
Retry / Fallback이 필요한가?

```text
실패를 숨기고 정상 응답처럼 반환하는가?

같은 Model을 다시 호출하는가?

다른 Model로 넘어가는가?
```

실패할 수 있다는 이유만으로 Fallback을 먼저 넣지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
현재 시스템은 Provider Failure를 ErrorCode로 반환하고
Retry / Fallback을 하지 않는다.
```

---

## Conditions

live 재측정은 완료 조건이 아니다.

이미 관측된 live Failure:

```text
Phase 2 Direct Model
OpenAI HTTP 429 credit_balance_exhausted → RATE_LIMIT
gemini-2.5-pro HTTP 404 unavailable → PROVIDER_ERROR
gemini-3.5-flash reasoning-001 HTTP 503 high demand → PROVIDER_ERROR

Phase 3 Router 경로
reasoning-001 HTTP 429 quota exceeded → RATE_LIMIT
reasoning-002 HTTP 429 quota exceeded → RATE_LIMIT
```

의도적 재현 (Test Double):

```text
src/test/java/com/llmrouter/api/ChatApiIntegrationTest.java
returnsRateLimitWithoutRetry
returnsProviderTimeoutWithoutRetry
returnsProviderErrorWithoutRetry
```

```text
Router 변경: 없음
Retry: 없음
Fallback: 없음
evaluation/dataset.json: 변경 없음
```

Test Double과 live 결과를 같은 실행의 Before / After로 비교하지 않는다.
둘 다 현재 동작 확인에 쓴다.

---

## Baseline

현재 실패 처리:

```text
Provider 호출 실패
      ↓
LlmRouterException (ErrorCode)
      ↓
Chat API Error Response
      ↓
Retry 없음
Fallback 없음
```

ErrorCode:

```text
RATE_LIMIT
PROVIDER_TIMEOUT
PROVIDER_ERROR
```

---

## Result

### Live (이전 Experiment)

| 실행 | Case | HTTP | errorCode | Retry | Fallback |
|---|---|---|---|---|---|
| Direct Model OpenAI | 14 / 14 | 429 | RATE_LIMIT | 없음 | 없음 |
| Direct Model Gemini 1차 | model-large 7 / 7 | 404 | PROVIDER_ERROR | 없음 | 없음 |
| Direct Model Gemini 2차 | reasoning-001 large | 503 | PROVIDER_ERROR | 없음 | 없음 |
| Router 경로 | reasoning-001, 002 | 429 | RATE_LIMIT | 없음 | 없음 |

실패를 성공처럼 바꾸지 않았다.
Quality PASS로 기록하지 않았다.

### Test Double (이번 Phase)

ProviderClient가 예외를 던지면 Chat API는 다음을 반환한다.

| Failure | HTTP | error | Provider 호출 횟수 |
|---|---|---|---|
| RATE_LIMIT | 429 | RATE_LIMIT | 1 |
| PROVIDER_TIMEOUT | 504 | PROVIDER_TIMEOUT | 1 |
| PROVIDER_ERROR | 502 | PROVIDER_ERROR | 1 |

answer 필드는 없다.
requestId는 있다.

두 번째 complete 호출은 없다.

---

## Analysis

현재 동작은 FR-18 / NFR-10에 맞다.
실패 사실과 유형을 반환한다.
정상 응답처럼 만들지 않는다.

### Fallback이 필요한가

관측된 live Failure:

```text
429 quota / credit
→ 같은 Provider의 다른 Model도 같은 할당에 걸릴 수 있다.

404 model unavailable
→ Catalog의 provider-model 문제였다.
→ Human Gate A로 gemini-3.5-flash로 바꿨다.
→ Fallback이 아니라 Catalog 수정이다.

503 high demand
→ 그 실행의 large reasoning-001만이다.
→ 현재 Default는 small이다.
```

Router 경로 429는 Default Model(small)에서 났다.
Fallback 대상은 같은 Provider의 large다.
이번 429가 quota exceeded라서
large로 넘기면 해결된다고 단정하지 않는다.
그 비교 호출을 하지 않았다.

```text
실패가 관측됨
≠
Fallback을 넣는다
```

### Retry가 필요한가

Test Double에서 호출은 1회다.
live 429/404/503도 재시도하지 않았다.

429 quota에 Retry를 넣으면
같은 실패를 반복할 수 있다.
이번 측정으로 Retry 이득을 확인하지 않았다.

---

## Candidate

구현하지 않는다. 검토만 한다.

```text
후보 A
Failure 그대로 반환 (현재)
장점: 실패가 드러난다. 중복 호출이 없다.
단점: 호출한 Request는 복구되지 않는다.

후보 B
같은 Provider 다른 Model Fallback
장점: 503처럼 Model 일시 장애에 후보가 된다.
단점: 같은 Provider quota 429에는 효과가 불확실하다.
      Policy 변경, Human Gate.

후보 C
Retry
장점: 일시 Timeout에 후보가 된다.
단점: quota 429를 키울 수 있다. 추가 Latency.
      Policy 변경, Human Gate.
```

Human Gate를 열지 않는다.
Fallback / Retry를 선택하지 않기 때문이다.

---

## Decision

```text
Retry를 넣지 않는다.

Fallback을 넣지 않는다.

실패를 숨기지 않는 현재 동작을 유지한다.

이유:
실패는 ErrorCode로 관찰된다.
관측된 429는 quota라서 같은 Provider Fallback 근거가 부족하다.
404는 Catalog 수정으로 이미 다뤘다.
Retry / Fallback 비교 실험이 없다.

현재 문제에서는 사용하지 않았다.
효과가 없었다고 쓰지 않는다.
```

적용하지 않았으므로 Failure Case를 해결 코드로 다시 실행하지 않는다.

---

## Remaining Limitation

```text
Timeout live 호출은 이번 Phase에서 새로 만들지 않았다.
Timeout은 Test Double과 HTTP 매핑으로 확인했다.

다른 Provider Model Fallback은
현재 Catalog에 Provider가 하나라 확인할 수 없다.

HTTP 429 quota와 호출 제한은 같은 RATE_LIMIT다.
```
