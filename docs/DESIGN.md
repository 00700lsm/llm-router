# LLM Router - Design

## 1. 문서 목적

이 문서는 LLM Router의 **현재 Baseline 설계**를 설명한다.

`REQUIREMENTS.md`가 무엇을 만족해야 하는지 정의하고  
`ROADMAP.md`가 어떤 문제를 어떤 순서로 확인할지 정의한다면,

`DESIGN.md`는 현재 Phase에서 실제로 구현할 구조와 각 Component의 책임을 설명한다.

현재 프로젝트는 **Phase 1 - Baseline Router 구현 단계**를 기준으로 한다.

따라서 이후 Phase에서 검토할 수 있는 다음 구조는 현재 DESIGN에 포함하지 않는다.

```text
Semantic Routing

LLM-based Routing

Model Cascade

Routing Cache

Dynamic Threshold

Automatic Fallback

Retry Orchestration

LangGraph

Multi-Agent
```

이 기능들은 실제 Evaluation에서 필요성이 확인되고
Human Gate를 통해 선택된 이후에만 DESIGN에 반영한다.

---

# 2. 현재 Architecture

현재 Baseline 구조는 다음과 같다.

```text
Client
  ↓
Chat API
  ↓
Chat Service
  ↓
Baseline Router
  ↓
Model Catalog
  ↓
Routing Decision
  ↓
LLM Gateway
  ↓
Provider
  ↓
Selected Model
  ↓
Model Response
  ↓
Usage / Latency / Cost
  ↓
Chat Response
```

Evaluation은 사용자 요청 흐름과 분리한다.

```text
Evaluation Dataset
        ├─ Evaluation Runner
        │      ↓
        │   Baseline Router
        │
        └─ Direct Model Runner
               ↓
            Model A / Model B
               ↓
            Quality / Latency / Token / Cost
```

현재 구조의 핵심은 복잡한 Routing이 아니다.

```text
Request
↓
어떤 Model이 선택됐는지 확인 가능
↓
Model 호출
↓
Quality / Latency / Token / Cost 측정 가능
```

한 상태를 만드는 것이다.

---

# 3. Baseline Routing Policy

현재 Baseline에서는 Request의 난이도를 정교하게 판단하지 않는다.

초기 Router는 **설정된 기본 Model을 선택하는 가장 단순한 전략**으로 시작한다.

```text
Request
   ↓
Enabled Model 확인
   ↓
Configured Default Model
   ↓
Selected Model
```

이 방식을 사용하는 이유는 Baseline부터:

```text
Simple Request 분류

Reasoning Request 분류

Semantic Similarity

LLM Judge Routing
```

같은 판단 로직을 넣으면
어떤 기능 때문에 결과가 좋아지거나 나빠졌는지 기준점을 만들기 어렵기 때문이다.

따라서 초기 상태에서는:

```text
모든 Request
→ Default Model
```

을 명확한 Baseline으로 사용한다.

이후 Evaluation에서:

```text
Simple Request에 과도한 비용이 발생한다.

Reasoning Request에서 품질이 부족하다.

Request별 Model 선택이 필요하다.
```

같은 Failure가 실제로 확인되면 Routing Policy 변경을 검토한다.

Routing Policy 변경은 Human Gate 대상이다.

현재 Policy 한계 확인:

```text
docs/experiments/005-routing-policy-limit.md
```

현재는 Baseline Default를 유지한다.

---

# 4. Component 구성

## 4.1 Chat API

사용자 Request를 받는 진입점이다.

예:

```http
POST /api/v1/chat
```

Request 예:

```json
{
  "message": "Java에서 synchronized와 ReentrantLock 차이를 설명해줘."
}
```

현재 역할:

```text
Request Validation

Chat Service 호출

공통 Response 반환
```

Chat API는 Routing Policy를 직접 알지 않는다.

```text
Controller
≠
Router
```

로 역할을 분리한다.

---

## 4.2 Chat Service

사용자 Request 처리 흐름을 조정한다.

```text
Request
   ↓
Router
   ↓
Routing Decision
   ↓
LLM Gateway
   ↓
Model Response
   ↓
Response 생성
```

현재 Chat Service의 역할은 다음 정도로 제한한다.

```text
Routing 요청

Model 호출

Latency 측정

Usage 수집

Cost 계산

최종 Response 조합
```

Chat Service 내부에 Model 선택 Rule을 직접 작성하지 않는다.

Model 선택 책임은 Router에 둔다.

---

## 4.3 Baseline Router

현재 Model 선택을 담당한다.

입력:

```text
Chat Request

Model Catalog
```

출력:

```text
Routing Decision
```

현재 Baseline Decision은 다음 정도의 정보를 가진다.

```text
selectedModel

provider

strategy

reason
```

예:

```json
{
  "selectedModel": "model-small",
  "provider": "GEMINI",
  "strategy": "BASELINE_DEFAULT",
  "reason": "configured default model"
}
```

Baseline Router는 다음 기능을 수행하지 않는다.

```text
Prompt 의미 분석

Embedding 생성

Semantic Similarity 비교

LLM을 이용한 Request 분류

Quality 예측

Dynamic Threshold

Fallback 판단

Capability 기반 후보 제외

Context Limit 검사
```

Capability / Context Limit 확인:

```text
docs/experiments/006-capability.md
```

Provider Failure 시 현재 동작:

```text
docs/experiments/007-provider-failure.md
```

---

# 5. Model Catalog

Model Catalog는 Router가 사용할 수 있는 Model 정보를 제공한다.

현재는 별도 관리자 기능이나 Database를 만들지 않는다.

가장 단순하게:

```text
Application Configuration
```

을 Source of Truth로 사용한다.

예시 개념:

```yaml
llm-router:
  routing:
    default-model: model-small
  provider:
    base-url: ${GEMINI_BASE_URL:https://generativelanguage.googleapis.com/v1beta}
    api-key: ${GEMINI_API_KEY:}
  models:
    - id: model-small
      provider: GEMINI
      display-name: Gemini 2.5 Flash
      provider-model: gemini-2.5-flash
      enabled: true
      context-limit: 1048576
      tool-calling: true
      structured-output: true
      input-cost-per-million: 0.15
      output-cost-per-million: 0.60

    - id: model-large
      provider: GEMINI
      display-name: Gemini 3.5 Flash
      provider-model: gemini-3.5-flash
      enabled: true
      context-limit: 1048576
      tool-calling: true
      structured-output: true
      input-cost-per-million: 1.50
      output-cost-per-million: 9.00
```

`id`는 Router 내부 식별자다.

실제 Provider 호출에는 `provider-model`을 사용한다.

가격 값은 실제 Billing이 아니라 Evaluation 비교용 예상 단가다.

Model Catalog의 책임:

```text
Model 목록 제공

Model ID 조회

Provider 확인

Enabled 확인

Capability Metadata 제공

Cost Metadata 제공
```

Model Catalog는 다음을 판단하지 않는다.

```text
어떤 Request가 어려운가?

어떤 Model이 더 좋은가?

현재 Request에 어떤 Model을 써야 하는가?
```

그 판단은 Router의 책임이다.

---

# 6. Model Definition

현재 Model 정보는 Routing과 Evaluation에 필요한 최소 항목만 가진다.

개념적인 구조:

```text
ModelDefinition

id

provider

displayName

providerModel

enabled

contextLimit

toolCalling

structuredOutput

inputCostPerMillion

outputCostPerMillion
```

각 필드의 의미:

| Field | 역할 |
|---|---|
| `id` | Router 내부 Model 식별자 |
| `provider` | Model Provider |
| `displayName` | 로그 / Evaluation 표시용 이름 |
| `providerModel` | Provider API에 전달하는 Model 이름 |
| `enabled` | Routing 후보 사용 여부 |
| `contextLimit` | Context 제한 |
| `toolCalling` | Tool Calling 지원 여부 |
| `structuredOutput` | Structured Output 지원 여부 |
| `inputCostPerMillion` | Input Token 기준 Cost |
| `outputCostPerMillion` | Output Token 기준 Cost |

Provider가 제공하는 모든 옵션을 ModelDefinition에 옮기지 않는다.

현재 Routing / Evaluation에 사용하지 않는 Metadata는 추가하지 않는다.

---

# 7. Routing Decision

Routing 결과를 단순 Model ID 하나로 반환하지 않는다.

왜 해당 Model이 선택됐는지 추적할 수 있도록
별도의 Routing Decision을 사용한다.

개념적인 구조:

```text
RoutingDecision

requestId

selectedModel

provider

strategy

reason
```

현재 Baseline 예:

```text
strategy
BASELINE_DEFAULT

reason
configured default model
```

향후 Routing 전략이 실제로 변경된다면
`strategy`와 `reason` 역시 현재 구현에 맞게 갱신한다.

현재 사용하지 않는 미래 Strategy 이름을 미리 Enum에 추가하지 않는다.

예:

```text
SEMANTIC

CASCADE

LLM_ROUTING
```

을 아직 구현하지 않았다면 미리 추가하지 않는다.

---

# 8. LLM Gateway

LLM Gateway는 선택된 Model을 실제 Provider에 전달하는 역할을 한다.

```text
Routing Decision
      ↓
LLM Gateway
      ↓
Provider Client
      ↓
Model
```

Router는 Provider SDK의 세부 Request 형식을 알지 않는다.

```text
Router
→ Model을 선택

LLM Gateway
→ 선택된 Model을 호출
```

로 역할을 나눈다.

---

# 9. Provider Client

Provider Client는 실제 외부 LLM API와 통신한다.

개념적인 흐름:

```text
Chat Request
+
Model Definition
      ↓
Provider Request 생성
      ↓
Provider API
      ↓
Provider Response
      ↓
공통 Model Response 변환
```

Provider마다 다음 정보의 이름이나 형식이 다를 수 있다.

```text
Model ID

Input Message

Output Text

Usage

Error

Latency
```

따라서 Provider Response를 그대로 Controller까지 전달하지 않는다.

공통 Model Response로 변환한다.

현재 Provider Client는 Gemini generateContent API 하나를 사용한다.

미래 확장만을 위해 Factory / Registry 계층을 미리 만들지 않는다.

실제 두 번째 Provider가 추가되는 시점에 필요한 수준으로 확장한다.

---

# 10. Model Response

Provider 차이에 관계없이 내부에서는 공통 Response 형태를 사용한다.

개념적인 구조:

```text
ModelResponse

content

model

provider

usage

latency

success

error
```

Usage:

```text
inputTokens

outputTokens

totalTokens
```

Error가 없는 정상 Response:

```text
success = true
content = ...
```

Provider 호출 실패:

```text
success = false
error = ...
```

현재 Baseline에서는 다른 Model로 자동 전환하지 않는다.

```text
Provider Failure
↓
Failure 반환 / 기록
```

까지만 수행한다.

---

# 11. Chat Response

사용자용 Response에는 내부 Evaluation 정보를 모두 노출하지 않는다.

기본 Response는 다음 정도로 유지한다.

```json
{
  "answer": "...",
  "model": "model-small",
  "provider": "GEMINI"
}
```

필요하면 Request 추적을 위한 ID를 포함할 수 있다.

```json
{
  "requestId": "...",
  "answer": "...",
  "model": "model-small",
  "provider": "GEMINI"
}
```

다음과 같은 정보는 Debug / Evaluation 결과에서 확인한다.

```text
Routing Reason

Routing Strategy

Token

Cost

Latency

Failure Type
```

---

# 12. Request 처리 흐름

현재 Chat Request의 전체 처리 순서는 다음과 같다.

```text
1. Client Request
       ↓
2. Request Validation
       ↓
3. Request ID 생성
       ↓
4. Baseline Router 호출
       ↓
5. Model Catalog에서 Default Model 확인
       ↓
6. Routing Decision 생성
       ↓
7. LLM Gateway 호출
       ↓
8. Provider API 호출
       ↓
9. Model Response 수신
       ↓
10. Usage 수집
       ↓
11. Cost 계산
       ↓
12. Latency 기록
       ↓
13. Chat Response 반환
```

Router가 직접 Provider API를 호출하지 않는다.

```text
Routing
```

과:

```text
Execution
```

을 분리한다.

---

# 13. Cost 계산

현재 Cost 계산은 실제 Billing 시스템이 아니다.

Evaluation에서 Model별 비용을 비교하기 위한 예상 Cost다.

기본 계산:

```text
Input Cost
=
Input Token
×
Input Cost Per Token
```

```text
Output Cost
=
Output Token
×
Output Cost Per Token
```

```text
Estimated Cost
=
Input Cost
+
Output Cost
```

Model Catalog에는 비교를 위한 단가를 저장한다.

Provider가 Usage를 제공하지 않는 경우:

```text
Cost = UNKNOWN
```

처럼 명확하게 구분한다.

추정 Token 값을 실제 Provider Usage처럼 취급하지 않는다.

---

# 14. Latency 측정

현재 최소한 다음 시간을 측정한다.

```text
Model Latency

End-to-End Latency
```

구조:

```text
Client Request
      ↓
[ End-to-End Start ]

Router

LLM Gateway
      ↓
[ Model Start ]

Provider

[ Model End ]
      ↓
Response 조합

[ End-to-End End ]
```

초기 Baseline Router 자체는 매우 단순하므로
Routing Latency를 핵심 Metric으로 사용하지 않는다.

향후 Routing 계산이 복잡해져
Router 자체의 비용이 의미 있게 발생하면 별도로 측정한다.

---

# 15. Logging / Observability

최종 Answer만 확인해서는 Routing 문제를 분석하기 어렵기 때문에
Request 단위로 최소한 다음 정보가 연결되어야 한다.

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

예:

```text
requestId=abc123
strategy=BASELINE_DEFAULT
model=model-small
provider=GEMINI
modelLatency=820ms
endToEndLatency=845ms
inputTokens=120
outputTokens=240
estimatedCost=...
success=true
```

Log에 API Key나 Secret을 남기지 않는다.

전체 Prompt / Response 원문 Logging은
필요성과 개인정보 / 민감정보 위험을 고려해 별도로 판단한다.

현재 DESIGN에서는 무조건 전체 원문을 저장하는 구조를 두지 않는다.

---

# 16. Error 처리

현재 Baseline에서 Error는 크게 다음 정도로 구분한다.

```text
INVALID_REQUEST

MODEL_NOT_FOUND

MODEL_DISABLED

PROVIDER_ERROR

PROVIDER_TIMEOUT

RATE_LIMIT

UNKNOWN_ERROR
```

현재 흐름:

```text
Error 발생
   ↓
Error 정보 기록
   ↓
호출자에게 실패 반환
```

자동 Retry나 Fallback은 수행하지 않는다.

이후 Provider Failure Experiment에서
실제 필요성이 확인되면 변경한다.

---

# 17. Evaluation 구조

Evaluation은 일반 Chat API와 목적이 다르다.

Chat API:

```text
사용자 Request
↓
Router
↓
Selected Model
↓
Answer
```

Evaluation:

```text
Dataset
↓
반복 실행
↓
결과 수집
↓
Expected Condition 비교
↓
Metric / Failure 분석
```

따라서 Evaluation Runner를 별도의 흐름으로 둔다.

---

# 18. Evaluation Dataset

현재 Dataset은 Repository 내부 파일로 관리한다.

```text
evaluation/
├── dataset.json
└── results/
```

Database는 필요하지 않다.

Case는 최소한 다음 정보를 표현할 수 있어야 한다.

```text
id

category

input

expectedCondition
```

예:

```json
{
  "id": "routing-001",
  "category": "SIMPLE",
  "input": "다음 문장을 영어로 번역해줘.",
  "expectedCondition": {
    "minimumQuality": "PASS"
  }
}
```

Expected Model 하나를 모든 Case의 필수값으로 두지 않는다.

---

# 19. Evaluation 실행 방식

Evaluation에서는 두 종류의 실행이 필요하다.

## 19.1 Router Evaluation

현재 Router가 실제로 어떤 Model을 선택하는지 확인한다.

```text
Evaluation Case
      ↓
Router
      ↓
Selected Model
      ↓
Model Response
      ↓
Result
```

확인 값:

```text
Selected Model

Routing Reason

Quality

Latency

Token

Cost

success / error
```

Quality Result는 Evaluation 전용 Checklist다.
Runtime Routing에는 사용하지 않는다.

실행:

```text
./gradlew bootRun --args='--spring.profiles.active=evaluate-routing'
```

결과는 `evaluation/results/002-baseline-routing.json`에 저장한다.

---

## 19.2 Direct Model Evaluation

Router를 거치지 않고 동일 Request를 각 Enabled Model에 직접 실행한다.

```text
Evaluation Case
        ├─ Model A
        ├─ Model B
        └─ ...
```

이 경로에서는 Baseline Router를 사용하지 않는다.

확인 값:

```text
Quality

Model Latency

End-to-End Latency

Input Token

Output Token

Estimated Cost
```

실행:

```text
./gradlew bootRun --args='--spring.profiles.active=evaluate-models'
```

결과는 `evaluation/results/001-model-baseline.json`에 저장한다.

Router Evaluation 결과와 Direct Model Evaluation 결과를 섞지 않는다.

Case별 Quality / Cost / Latency 비교는 Experiment에서 수행한다.

```text
docs/experiments/004-quality-cost-latency.md
```

Quality / Cost / Latency를 하나의 Score로 합치지 않는다.

---

# 20. Quality 평가

현재 Quality Evaluator는 Evaluation 전용 Deterministic Checklist다.

```text
Model Response
      ↓
Quality Evaluator
      ↓
PASS / FAIL + reason
```

현재 확인 항목:

```text
mustInclude

mustNotInclude

requireNoHangul

maxChars

requireJson
```

이 결과는 Runtime Routing에 사용하지 않는다.

Baseline Runtime에 Judge Model을 추가하지 않는다.

이 Checklist는 요구사항 충족 여부를 확인한다.
Answer의 미묘한 품질 차이를 완전히 측정한다고 해석하지 않는다.

---

# 21. Failure 분류

현재 Evaluation Result는 실행 성공/실패, Selected Model,
Quality Checklist, Latency, Token, Cost, Error Code를 남긴다.

Failure Type Enum은 Runtime과 Evaluation Result에 넣지 않았다.

분류는 Experiment에서 수행한다.

```text
docs/experiments/003-routing-failure-analysis.md
```

사용 중인 Failure Type:

```text
ROUTING_FAILURE

MODEL_QUALITY_FAILURE

CAPABILITY_MISMATCH

LATENCY_FAILURE

COST_INEFFICIENCY

PROVIDER_FAILURE

EVALUATION_FAILURE
```

현재 Dataset에서 확인한 위치:

```text
현재 정책 위반 ROUTING_FAILURE: 없음
ROUTING_FAILURE 후보: simple-003
MODEL_QUALITY_FAILURE: simple-003
PROVIDER_FAILURE: reasoning-001, reasoning-002
CAPABILITY_MISMATCH: Production Catalog / Dataset에서 없음
COST_INEFFICIENCY / LATENCY_FAILURE: 현재 Dataset에 실제 Case 없음
```

Capability 필드는 Catalog에 있다. Runtime Routing에는 쓰지 않는다.

Provider Failure는 ErrorCode로 반환한다. Retry / Fallback은 없다.

예:

```text
Request
   ↓
Default Model 선택
   ↓
Quality FAIL

다른 Model 직접 실행
   ↓
Quality PASS

→ ROUTING_FAILURE 후보
```

이 예시는 simple-003에 해당한다.
현재 정책 위반으로 확정하지 않았다.

반대로:

```text
의도한 Model 선택
↓
Provider 호출 실패 (HTTP 429)

→ PROVIDER_FAILURE
→ Routing 문제라고 단정하지 않음
```

Failure 분류는 Evaluation / Experiment에서 수행한다.

Router 자체가 자기 결과를 자동 평가하도록 만들지 않는다.

---

# 22. 현재 Data 저장

현재 Baseline에서는 별도 Application Database를 사용하지 않는다.

관리 대상:

```text
Model Catalog
→ Configuration

Evaluation Dataset
→ Repository File

Evaluation Result
→ File 또는 Console Output

Runtime Routing
→ Log
```

현재 요구사항에서는 다음 데이터의 영속 저장이 필요하지 않다.

```text
사용자 Conversation

장기 Session

User Profile

Routing History Database

Billing History
```

Evaluation 과정에서 Routing Result 저장 필요성이 커지면
그때 저장 구조를 검토한다.

---

# 23. Secret / Configuration

Provider API Key는 코드나 Repository에 저장하지 않는다.

```text
Environment Variable
또는
외부 Configuration
```

으로 주입한다.

프로젝트 루트 `.env`에서 읽는다.

```text
GEMINI_API_KEY

GEMINI_BASE_URL
```

`.env`는 `DotEnvEnvironmentPostProcessor`가
`application.yml` placeholder 해석 전에 로드한다.
등록 위치는 `META-INF/spring.factories`다.

`.env`는 gitignore 대상이다.

Model 정보와 가격 정보처럼 Secret이 아닌 설정은
Application Configuration에서 관리할 수 있다.

다음 정보는 Commit하지 않는다.

```text
API Key

Access Token

Secret
```

---

# 24. Package / Module 책임

구현 언어의 실제 Package 이름은 Repository 구조에 맞추되
논리적으로 다음 책임을 분리한다.

```text
api
│
├─ Chat API
└─ Request / Response

chat
│
└─ Chat Service

routing
│
├─ Baseline Router
└─ Routing Decision

model
│
├─ Model Definition
└─ Model Catalog

llm
│
├─ LLM Gateway
├─ Provider Client
└─ Model Response

metrics
│
├─ Usage
├─ Cost Calculator
└─ Estimated Cost

evaluation
│
├─ Evaluation Dataset
├─ Evaluation Runner
├─ Direct Model Runner
├─ Quality Evaluator
└─ Evaluation Result
```

Evaluation Runner는 Dataset을 읽고 Router 경로로 Case를 실행한다.

Direct Model Runner는 Router를 거치지 않고 Enabled Model에 같은 Case를 실행한다.

Package를 나누기 위해 불필요한 Interface를 만들지는 않는다.

책임이 실제로 분리될 필요가 있는 곳만 분리한다.

---

# 25. 현재 의존 방향

핵심 의존 방향은 다음과 같다.

```text
API
 ↓
Application / Chat Service
 ↓
Router
 ↓
Model Catalog
```

그리고 실행 경로:

```text
Application / Chat Service
 ↓
LLM Gateway
 ↓
Provider Client
```

Evaluation:

```text
Evaluation Runner
 ├─ Router
 └─ LLM Gateway

Direct Model Runner
 └─ LLM Gateway
```

Router가 다음을 직접 의존하지 않도록 한다.

```text
HTTP Controller

Provider SDK 세부 Request

Evaluation Result 저장 방식
```

---

# 26. 현재 Test 구조

Baseline에서는 최소한 다음 계층을 검증한다.

## Unit Test

```text
Request Validation

Model Catalog

Baseline Router

Cost Calculator

Quality Evaluator
```

Baseline Router의 가장 중요한 Test는 단순하다.

```text
Configured Default Model
=
Selected Model
```

Disabled Default Model 같은 잘못된 Configuration도 검증할 수 있다.

---

## Provider Client Test

실제 Provider 호출과 분리하여
Request / Response Mapping을 확인할 수 있어야 한다.

외부 API를 모든 Unit Test에서 호출하지 않는다.

---

## Integration Test

```text
Chat API
↓
Router
↓
LLM Gateway
↓
Response
```

전체 흐름이 연결되는지 확인한다.

---

## Evaluation Test

Evaluation Dataset을 읽고
Router 경로와 Direct Model 경로를 실행할 수 있는지 확인한다.

Test 성공을 Routing Quality 성공이나 Model 품질 성공으로 해석하지 않는다.

---

# 27. 현재 포함하지 않는 구조

현재 DESIGN에는 다음 구조가 존재하지 않는다.

```text
Semantic Router

Embedding Model

Vector Store

LLM Request Classifier

Model Cascade

Quality Reviewer

Automatic Retry

Automatic Fallback

Routing Cache

Dynamic Threshold

LangGraph

Planner Agent

Multi-Agent
```

ROADMAP에 등장하더라도 현재 Architecture가 아니다.

실제 Failure와 Metric을 통해 필요성이 확인되고
Human Gate에서 선택된 뒤에만 이 문서를 수정한다.

---

# 28. DESIGN 갱신 기준

다음과 같이 현재 Architecture의 의미가 달라지는 변경이 발생하면
DESIGN.md를 갱신한다.

```text
Routing Policy 변경

새 Provider 실제 추가

Model Catalog 구조 변경

Capability Filter 실제 추가

Fallback 실제 도입

Retry 실제 도입

새 Routing Strategy 실제 도입

Evaluation 실행 구조 변경
```

반대로 다음 변경은 반드시 Architecture 문서 수정이 필요한 것은 아니다.

```text
DTO Field 이름 변경

단순 Validation 수정

Logging 문구 변경

Test 추가

작은 Refactoring
```

DESIGN은 Commit History가 아니다.

현재 코드가 어떻게 동작하는지를 설명하는 문서로 유지한다.

---

# 29. 현재 Baseline 요약

현재 Phase 1 Architecture를 한 번 더 줄이면 다음과 같다.

```text
                Model Configuration
                       ↓
                  Model Catalog
                       ↓
Client
  ↓
Chat API
  ↓
Chat Service
  ↓
Baseline Router
  ↓
Default Model
  ↓
LLM Gateway
  ↓
Provider
  ↓
Model Response
  ↓
Usage / Cost / Latency
  ↓
Chat Response
```

Evaluation은 별도 경로다.

```text
Evaluation Dataset
       ├─ Evaluation Runner → Baseline Router
       └─ Direct Model Runner → Enabled Models
              ↓
       Evaluation Result
```

현재 Baseline에서 의도적으로 단순하게 남겨둔 부분은 다음과 같다.

```text
모든 일반 Request
→ Configured Default Model

Provider Failure
→ 실패 반환

Quality Evaluation
→ Runtime이 아닌 Evaluation 단계에서 수행
```

이 단순한 구조를 먼저 구현하고 측정한다.

그 결과 실제 문제가 확인됐을 때만
다음 Architecture 변경을 검토한다.
