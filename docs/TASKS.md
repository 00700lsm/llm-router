# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 1
Baseline Router 구현
```

상태:

```text
DONE
```

목표:

```text
측정 가능한 Baseline Router가 존재하는 것
```

이 Phase의 목표는 Routing 품질을 높이는 것이 아니다.

---

# 2. 현재 Repository 상태

이미 존재하는 것:

```text
Spring Boot 3.5.4 / Java 21

POST /api/v1/chat

Model Catalog

Baseline Router

LLM Gateway / Provider Client

Usage / Cost / Latency

evaluation/dataset.json

Evaluation Runner 최소 골격

README
```

Phase 1 구현은 기존 Spring Boot 골격 위에 완료했다.

---

# 3. Phase 1 기본 흐름

구현해야 하는 최소 흐름:

```text
POST /api/v1/chat
        ↓
Request Validation
        ↓
Chat Service
        ↓
Baseline Router
        ↓
Configured Default Model
        ↓
LLM Gateway
        ↓
Provider Client
        ↓
Model Response
        ↓
Usage / Cost / Latency
        ↓
Chat Response
```

---

# 4. Phase 1에서 하지 않을 것

다음 기능은 ROADMAP에 있어도 이번 TASK에 포함하지 않는다.

```text
Semantic Routing

LLM-based Routing

Model Cascade

Routing Cache

Dynamic Threshold

Automatic Fallback

Retry Orchestration

Capability 기반 후보 제외

LangGraph

Multi-Agent

두 번째 Provider를 위한 Factory / Registry

Quality Judge를 Runtime Routing에 사용

Direct Model Comparison 실험

Routing Failure 분석

Quality / Cost / Latency Trade-off 분석
```

위 항목은 Phase 2 이후 문제 확인 대상이다.

구현 중에 필요해 보여도 선제 추가하지 않는다.

---

# 5. Human Gate

Phase 1 Routing Policy는 DESIGN에서 이미 결정되어 있다.

```text
모든 일반 Request
→ Configured Default Model
strategy = BASELINE_DEFAULT
```

따라서 다음 결정은 다시 Human Gate를 열지 않는다.

```text
Baseline Routing 전략 선택
```

아래 항목은 Phase 1에서 바꾸지 않는다.
바꾸려면 구현 전에 Human Gate를 연다.

```text
Routing Policy 변경

Model 역할 변경

Fallback / Retry 도입

Quality / Cost / Latency 기준 변경

Evaluation Dataset Expected 변경

새 Routing Architecture 도입
```

실제 Provider 이름과 Model ID는 Architecture 결정이 아니라
Application Configuration 값이다.

구현 시 DESIGN의 Catalog 구조를 따르고,
실제 Provider / Model 값은 설정으로 둔다.

---

# 6. 작업 목록

상태 표기:

```text
TODO
IN PROGRESS
DONE
BLOCKED
```

완료 표시는 코드 작성이 아니라
해당 Task의 완료 조건을 만족했을 때만 한다.

---

## T1-01. Model Catalog

상태: `DONE`

목적:

Router가 선택할 수 있는 Model 정보를 Application Configuration에서 관리한다.

구현 범위:

```text
ModelDefinition

Model Catalog

최소 2개 이상 Model 설정

enabled 구분

Capability Metadata

Cost Metadata

routing.default-model 설정
```

최소 필드:

```text
id
provider
displayName
enabled
contextLimit
toolCalling
structuredOutput
inputCostPerMillion
outputCostPerMillion
```

하지 않을 것:

```text
관리자 API

Database

미래 Provider를 위한 과도한 추상화
```

완료 조건:

```text
설정에서 Model을 읽을 수 있다.

Enabled / Disabled를 구분할 수 있다.

Default Model을 조회할 수 있다.

Model Catalog Unit Test가 있다.
```

관련:

```text
REQUIREMENTS FR-02, FR-03, FR-08
DESIGN 5, 6, 23
```

---

## T1-02. Baseline Router

상태: `DONE`

목적:

Request를 받아 Model 하나를 선택하고, 선택 이유를 남긴다.

구현 범위:

```text
RoutingDecision

Baseline Router

Configured Default Model 선택
```

RoutingDecision 최소 필드:

```text
requestId
selectedModel
provider
strategy
reason
```

현재 고정값:

```text
strategy = BASELINE_DEFAULT
reason = configured default model
```

하지 않을 것:

```text
Request 난이도 분류

Semantic / Embedding 비교

LLM Judge Routing

미래 Strategy Enum 미리 추가
```

잘못된 Configuration도 검증한다.

```text
Default Model이 없다

Default Model이 disabled다

Default Model ID가 Catalog에 없다
```

완료 조건:

```text
Request를 입력하면 Model 하나를 선택한다.

선택된 Model과 Routing Reason을 확인할 수 있다.

Default Model 선택 Test가 있다.

잘못된 Configuration Test가 있다.
```

관련:

```text
REQUIREMENTS FR-04, FR-07
DESIGN 3, 4.3, 7
```

---

## T1-03. Usage / Cost / Latency

상태: `DONE`

목적:

Model 호출 결과를 Quality / Cost / Latency로 따로 관찰할 수 있게 한다.

구현 범위:

```text
Usage

Cost Calculator

Model Latency

End-to-End Latency
```

Cost 계산:

```text
estimatedCost
=
inputTokens × inputCostPerToken
+
outputTokens × outputCostPerToken
```

Provider가 Usage를 주지 않으면 추정값으로 채우지 않는다.

```text
Cost = UNKNOWN
```

하지 않을 것:

```text
실제 Billing 정산

Routing Latency를 핵심 Metric으로 사용
```

완료 조건:

```text
Input / Output / Total Token을 담을 수 있다.

예상 Cost를 계산할 수 있다.

Usage 부재를 UNKNOWN으로 구분할 수 있다.

Cost Calculator Unit Test가 있다.
```

관련:

```text
REQUIREMENTS FR-10, FR-11, FR-12
DESIGN 13, 14
```

---

## T1-04. LLM Gateway / Provider Client

상태: `DONE`

목적:

Router가 선택한 Model을 실제 Provider에 전달하고,
공통 Model Response로 변환한다.

구현 범위:

```text
ModelResponse

LLM Gateway

Provider Client 1개
```

ModelResponse 최소 필드:

```text
content
model
provider
usage
latency
success
error
```

제약:

```text
현재 Provider가 하나면 Factory / Registry를 미리 만들지 않는다.

Router는 Provider SDK 세부 Request를 알지 않는다.

Provider Failure 시 다른 Model로 전환하지 않는다.
```

Test:

```text
실제 Provider 호출과 분리된 Request / Response Mapping Test
```

완료 조건:

```text
선택된 Model을 Provider에 전달할 수 있다.

공통 ModelResponse로 변환할 수 있다.

성공 / 실패를 구분할 수 있다.

Mapping Test가 외부 API에 의존하지 않는다.
```

관련:

```text
REQUIREMENTS FR-05, FR-06
DESIGN 8, 9, 10, 16
```

---

## T1-05. Chat API / Chat Service

상태: `DONE`

목적:

자연어 Request를 받아 Baseline 흐름을 실행하고 공통 응답을 반환한다.

구현 범위:

```text
POST /api/v1/chat

Request Validation

Request ID 생성

Chat Service 오케스트레이션

Chat Response
```

Request:

```json
{
  "message": "Java에서 synchronized와 ReentrantLock 차이를 설명해줘."
}
```

사용자 응답 최소 필드:

```json
{
  "requestId": "...",
  "answer": "...",
  "model": "model-small",
  "provider": "OPENAI"
}
```

사용자 응답에 넣지 않는 값:

```text
routingReason
token
cost
latency
```

이 값들은 Log / Evaluation 쪽에서 확인한다.

Validation:

```text
message 없음 → 거절
message 공백 → 거절
```

하지 않을 것:

```text
Conversation Memory

Session

Controller 내부 Routing Rule
```

완료 조건:

```text
유효한 Request를 받을 수 있다.

빈 Request를 거절할 수 있다.

선택된 Model의 응답을 반환할 수 있다.

Request Validation Test가 있다.
```

관련:

```text
REQUIREMENTS FR-01, FR-06
DESIGN 4.1, 4.2, 11, 12
```

---

## T1-06. Error 처리 / Observability

상태: `DONE`

목적:

실패를 숨기지 않고, Routing 판단을 Request 단위로 추적할 수 있게 한다.

Error 구분:

```text
INVALID_REQUEST
MODEL_NOT_FOUND
MODEL_DISABLED
PROVIDER_ERROR
PROVIDER_TIMEOUT
RATE_LIMIT
UNKNOWN_ERROR
```

현재 실패 처리:

```text
Error 발생
 ↓
기록
 ↓
호출자에게 실패 반환
```

Request 단위로 남길 값:

```text
requestId
selectedModel
provider
routingStrategy
routingReason
modelLatency
endToEndLatency
inputTokens
outputTokens
estimatedCost
success / failure
```

하지 않을 것:

```text
Automatic Retry

Automatic Fallback

API Key / Secret Logging

전체 Prompt / Response 원문 저장 구조
```

완료 조건:

```text
실패 시 다른 Model로 바꾸지 않고 실패를 반환한다.

Routing 결과와 기본 Metric을 Log에서 확인할 수 있다.

Secret이 Log에 남지 않는다.
```

관련:

```text
REQUIREMENTS FR-07, NFR-01, NFR-06, NFR-07, NFR-12
DESIGN 15, 16
```

---

## T1-07. Test

상태: `DONE`

목적:

코드가 정상 동작하는지 검증한다.
이 Test 성공을 Routing 품질 성공으로 해석하지 않는다.

Unit Test:

```text
Request Validation

Model Catalog

Baseline Router

Cost Calculator
```

Provider Client Test:

```text
Request / Response Mapping
외부 API 비의존
```

Integration Test:

```text
Chat API
 ↓
Router
 ↓
LLM Gateway
 ↓
Response
```

Integration Test는 실제 Provider 호출 없이
Test Double로 전체 연결을 확인할 수 있어야 한다.

완료 조건:

```text
ROADMAP Phase 1 Test 항목을 커버한다.

테스트만으로 Baseline 품질이 좋다고 문서에 적지 않는다.
```

관련:

```text
REQUIREMENTS 11. Test
DESIGN 26
ROADMAP Phase 1 Test
```

---

## T1-08. Evaluation 최소 골격

상태: `DONE`

목적:

Phase 2 측정을 위한 실행 입구를 만든다.
이번 Phase에서 Routing 품질을 평가하지 않는다.

구현 범위:

```text
evaluation/ Dataset 파일 위치

최소 1개 Case

Dataset을 읽는 Runner

최소 1 Case를 Router 경로로 실행
```

Case 최소 필드:

```text
id
category
input
expectedCondition
```

하지 않을 것:

```text
Quality Judge 구현

Direct Model Comparison 실험 실행

Failure Type 분석

Metric을 높이기 위한 Dataset 조작

Expected Model 하나를 정답으로 고정
```

README에는 다음 중 하나를 명확히 적는다.

```text
현재 실행 방법
또는
실행 예정 구조
```

완료 조건:

```text
Dataset 파일이 Repository에 있다.

최소 1 Case를 실행할 수 있는지 Test로 확인한다.

Phase 2 측정 실험은 시작하지 않는다.
```

관련:

```text
REQUIREMENTS FR-13
DESIGN 17, 18, 19.1, 26 Evaluation Test
ROADMAP Phase 1 README
```

---

## T1-09. README / 문서 동기화

상태: `DONE`

목적:

사람이 프로젝트를 실행하고 검증할 수 있게 한다.

README에 포함할 내용:

```text
Project Goal

Current Architecture

Run

Environment Variables

Provider / Model Configuration

API Usage

Test

Evaluation

Experiment Location

Known Limitations
```

Evaluation이 최소 수준이면 실행 예정 구조를 숨기지 않고 적는다.

문서 규칙:

```text
코드와 DESIGN이 다르면 DESIGN을 현재 구현에 맞게 수정한다.

없는 미래 구조를 DESIGN에 넣지 않는다.

REQUIREMENTS / ROADMAP의 Phase 범위를 구현에 맞춰 완화하지 않는다.
```

완료 조건:

```text
README만 보고 실행 / 설정 / API / Test 방법을 알 수 있다.

DESIGN이 현재 코드와 모순되지 않는다.
```

관련:

```text
REQUIREMENTS 11. README
DESIGN 28, 29
ROADMAP Phase 1 README
```

---

# 7. 권장 구현 순서

```text
T1-01 Model Catalog
      ↓
T1-02 Baseline Router
      ↓
T1-03 Usage / Cost / Latency
      ↓
T1-04 LLM Gateway / Provider Client
      ↓
T1-05 Chat API / Chat Service
      ↓
T1-06 Error 처리 / Observability
      ↓
T1-07 Test
      ↓
T1-08 Evaluation 최소 골격
      ↓
T1-09 README / 문서 동기화
```

한 Task를 완료하기 전에 다음 Phase 기능을 섞지 않는다.

---

# 8. Phase 1 완료 조건

ROADMAP Phase 1 완료 조건과 같다.

```text
Request를 받을 수 있다.

최소 2개 이상의 Model을 관리할 수 있다.

Router가 Model 하나를 선택할 수 있다.

선택된 Model을 실제 호출할 수 있다.

응답을 반환할 수 있다.

Routing 결과와 기본 Metric을 확인할 수 있다.

README에서 실행 / 테스트 방법을 확인할 수 있다.
```

다음만으로는 Phase 1을 DONE으로 두지 않는다.

```text
컴파일이 된다

Application이 기동된다

Test 파일만 추가했다

품질이 좋아 보인다
```

---

# 9. Git Checkpoint

Phase 1 완료 후 예상 Commit:

```text
feat: implement baseline llm router
```

문제 재현 코드와 해결 코드를 한 Commit에 섞을 일이 있으면
이 Phase에서는 해당 해결을 넣지 않는다.

Baseline 구현이 끝나면 그 상태를 먼저 남긴다.

---

# 10. 다음 Phase

Phase 1이 완료된 뒤에만 연다.

```text
Phase 2
Model별 Baseline 특성 측정
```

Phase 2에서 확인할 문제:

```text
Simple / General / Reasoning Request에서 Model별 차이가 있는가?

Latency / Token / Cost 차이는 어느 정도인가?
```

Phase 1 TASK를 닫기 전에 Phase 2 작업을 이 문서에 미리 채워 넣지 않는다.
