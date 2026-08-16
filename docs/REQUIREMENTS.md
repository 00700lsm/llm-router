# LLM Router - Requirements

## 1. 프로젝트 목적

LLM Router는 하나의 요청을 여러 LLM 중 하나로 전달하고,
**요청 특성에 맞는 Model을 선택하는 과정을 직접 구현하고 평가하기 위한 Backend 프로젝트**다.

이번 프로젝트의 목적은 Model을 많이 연결하는 것이 아니다.

핵심은 다음과 같은 문제를 직접 확인하는 것이다.

```text
모든 요청을 같은 Model로 처리해야 할까?

간단한 요청과 복잡한 요청은 같은 Model이 적절할까?

작은 Model로 충분한 요청을 큰 Model에 보내고 있지는 않을까?

Model을 바꾸면 품질은 실제로 얼마나 달라질까?

품질이 좋아진 만큼 Cost와 Latency도 증가할까?

Router가 선택한 Model이 정말 적절했는지 어떻게 평가할까?

Provider나 Model 호출이 실패하면 현재 구조는 어떻게 동작할까?

Routing 전략을 복잡하게 만들 만큼 실제 실패 Case가 존재할까?
```

따라서 처음부터 Semantic Routing, Model Cascade, LLM-based Routing 같은 기능을 모두 넣지 않는다.

먼저 가장 단순한 Router를 구현하고
Evaluation Dataset을 이용해 실제 Routing 결과와 Model별 품질을 측정한 뒤
필요한 기능을 하나씩 검토한다.

---

# 2. 서비스 범위

LLM Router는 사용자가 자연어 요청을 전달하면
Router가 요청을 분석하여 현재 사용 가능한 LLM 중 하나를 선택하고
해당 Model의 응답을 반환하는 AI Backend다.

기본 흐름은 다음과 같다.

```text
User Request
      ↓
LLM Router
      ↓
Model Selection
      ↓
Selected Model
      ↓
LLM Provider
      ↓
Response
```

Router가 다루는 핵심 대상은 두 가지다.

```text
Request
+
Model
```

Request에는 다음과 같은 차이가 있을 수 있다.

```text
단순한 요청

일반적인 질의

복잡한 추론

긴 Context

특정 Capability 필요

Cost가 중요한 요청

Latency가 중요한 요청
```

Model 역시 서로 다른 특성을 가질 수 있다.

```text
Quality

Latency

Input / Output Cost

Context Window

Tool Calling 지원 여부

Structured Output 지원 여부

Provider
```

이번 프로젝트에서는 실제 ChatGPT와 같은 범용 서비스를 구현하지 않는다.

Router의 판단과 Evaluation을 실험할 수 있는 범위까지만 구현한다.

---

# 3. 주요 구성 요소

LLM Router는 최종적으로 다음 구성 요소를 가질 수 있다.

## 3.1 Request API

사용자가 자연어 요청을 전달하는 API다.

예:

```http
POST /api/v1/chat
```

예:

```json
{
  "message": "Java의 synchronized와 ReentrantLock 차이를 설명해줘."
}
```

초기에는 단일 사용자 요청을 기준으로 한다.

Conversation Memory나 장기 Session 기능은 Baseline 범위에 포함하지 않는다.

---

## 3.2 Router

Request를 입력받아 호출할 Model을 결정한다.

```text
Request
   ↓
Router
   ↓
Routing Decision
   ↓
Selected Model
```

초기 Baseline에서는 가능한 한 단순한 Routing 기준으로 시작한다.

구체적인 Routing 방식은 DESIGN 및 Experiment에서 결정한다.

---

## 3.3 Model Catalog

Router가 선택할 수 있는 Model 정보를 관리한다.

Model마다 최소한 다음과 같은 정보를 확인할 수 있어야 한다.

```text
Model ID

Provider

Display Name

Enabled

Context Limit

Tool Calling 지원 여부

Structured Output 지원 여부

Input Cost

Output Cost
```

Model의 모든 기능을 완벽하게 Metadata로 모델링할 필요는 없다.

Routing과 Evaluation 실험에 필요한 정보만 관리한다.

---

## 3.4 LLM Client

선택된 Model을 실제 Provider API에 전달한다.

```text
Selected Model
      ↓
LLM Client
      ↓
Provider API
      ↓
Model Response
```

Provider별 요청 형식은 다를 수 있지만,
Router가 Provider 세부 구현에 직접 의존하지 않도록 역할을 구분한다.

단, 미래 Provider 확장을 이유로 과도한 추상화를 Baseline부터 만들지는 않는다.

---

## 3.5 Routing Result

Router의 판단 결과를 확인하기 위한 데이터다.

최소한 다음 내용을 확인할 수 있어야 한다.

```text
Selected Model

Provider

Routing Reason
```

Evaluation 또는 Debug 목적에서는 추가 정보를 확인할 수 있다.

```text
Request ID

Routing Strategy

Routing Latency

Model Latency

End-to-End Latency

Input Token

Output Token

Estimated Cost

Success / Failure
```

---

## 3.6 Evaluation Dataset

Router와 Model의 결과를 반복적으로 측정하기 위한 평가 데이터다.

이번 프로젝트에서 매우 중요한 구성 요소다.

예:

| Case | 요청 유형 | 기대 조건 |
|---|---|---|
| routing-001 | Simple | 저비용 Model로도 충분 |
| routing-002 | General | 일반 답변 품질 기준 만족 |
| routing-003 | Reasoning | 복잡한 추론 품질 기준 만족 |
| routing-004 | Long Context | Context 길이 처리 가능 |
| routing-005 | Tool Required | Tool Calling 지원 Model 필요 |

Baseline부터 Final까지 가능한 한 동일한 Dataset을 이용한다.

---

# 4. Model 데이터 구조

Model Catalog는 Routing 실험이 가능할 정도로 단순하게 구성한다.

예:

```json
{
  "id": "model-small",
  "provider": "PROVIDER_A",
  "displayName": "Model Small",
  "enabled": true,
  "contextLimit": 128000,
  "toolCalling": true,
  "structuredOutput": true,
  "inputCostPerMillion": 0.0,
  "outputCostPerMillion": 0.0
}
```

주요 필드는 다음과 같다.

| 필드 | 의미 |
|---|---|
| `id` | Router 내부 Model 식별자 |
| `provider` | LLM Provider |
| `displayName` | 표시용 Model 이름 |
| `enabled` | Routing 후보 포함 여부 |
| `contextLimit` | 처리 가능한 Context 크기 |
| `toolCalling` | Tool Calling 지원 여부 |
| `structuredOutput` | Structured Output 지원 여부 |
| `inputCostPerMillion` | Input Token 기준 비용 |
| `outputCostPerMillion` | Output Token 기준 비용 |

실제 Provider의 모든 Parameter를 저장할 필요는 없다.

Router와 Evaluation에 필요한 최소 정보만 관리한다.

---

# 5. Request 유형

Evaluation을 위해 Request를 몇 가지 유형으로 구분할 수 있다.

이 분류는 Router 구현 자체의 정답이 아니라
실험과 분석을 위한 기준이다.

## 5.1 Simple

복잡한 추론이 필요하지 않은 요청이다.

예:

```text
"이 문장을 영어로 번역해줘."

"다음 문장을 한 줄로 요약해줘."

"JSON 형식으로 바꿔줘."
```

---

## 5.2 General

일반적인 설명이나 질의다.

예:

```text
"REST와 RPC 차이를 설명해줘."

"Java Stream의 장단점을 알려줘."
```

---

## 5.3 Reasoning

여러 조건을 함께 고려하거나 상대적으로 복잡한 추론이 필요한 요청이다.

예:

```text
"이 Architecture의 장애 가능성을 분석하고 개선 후보를 비교해줘."

"다음 동시성 코드에서 Race Condition이 발생할 가능성을 분석해줘."
```

---

## 5.4 Long Context

긴 입력을 포함하는 요청이다.

예:

```text
긴 로그 분석

긴 코드 Review

긴 문서 요약
```

Model의 Context Limit과 실제 처리 가능성을 확인할 수 있어야 한다.

---

## 5.5 Capability Required

특정 Model Capability가 필요한 요청이다.

예:

```text
Tool Calling

Structured Output
```

Router가 Capability를 지원하지 않는 Model을 선택하지 않는지 확인한다.

---

# 6. 기능 요구사항

## FR-01. 사용자 요청 수신

사용자는 자연어 요청을 전달할 수 있어야 한다.

```http
POST /api/v1/chat
```

예:

```json
{
  "message": "Java에서 volatile이 필요한 이유를 설명해줘."
}
```

요청이 없거나 비어 있다면 거절한다.

---

## FR-02. Model 등록

Router가 사용할 Model을 등록하거나 설정할 수 있어야 한다.

초기에는 관리자 UI를 구현하지 않는다.

다음과 같은 방식이면 충분하다.

```text
Application Configuration

또는

Configuration File

또는

Database Seed

또는

Test Fixture
```

중요한 것은 동일한 Model 구성을 반복적으로 만들 수 있어야 한다는 것이다.

---

## FR-03. Model 활성화 상태 관리

Model은 Routing 후보에 포함할지 여부를 구분할 수 있어야 한다.

```text
Enabled
→ Routing 후보

Disabled
→ Routing 제외
```

Model 비활성화가 필요한 이유는 장애, 실험, 비용 제한 등 다양할 수 있다.

구체적인 운영 UI는 구현하지 않는다.

---

## FR-04. Baseline Routing

사용자 Request를 입력받아 하나의 Model을 선택할 수 있어야 한다.

```text
Request
   ↓
Router
   ↓
Selected Model
```

초기 Baseline Routing은 단순한 전략으로 시작한다.

Baseline 이전에 Semantic Routing이나 LLM-based Routing을 구현하지 않는다.

---

## FR-05. 선택된 Model 호출

Router가 선택한 Model을 실제로 호출할 수 있어야 한다.

```text
Router
   ↓
Selected Model
   ↓
LLM Client
   ↓
Provider
```

Model 호출 성공 시 최종 응답을 반환한다.

---

## FR-06. 공통 응답

Provider나 Model이 달라도 사용자에게 반환되는 기본 응답 구조는 일관되어야 한다.

예:

```json
{
  "answer": "...",
  "model": "model-small",
  "provider": "PROVIDER_A"
}
```

사용자용 응답에 모든 Debug 정보를 노출할 필요는 없다.

---

## FR-07. Routing 결과 확인

Evaluation 또는 Debug 목적으로 Router의 판단 결과를 확인할 수 있어야 한다.

최소한 다음 정보를 확인한다.

```text
Request ID

Selected Model

Provider

Routing Reason
```

가능하면 다음 정보도 확인한다.

```text
Routing Strategy

Routing Latency

Model Latency

End-to-End Latency
```

---

## FR-08. Model Capability 확인

Model마다 Routing에 필요한 Capability 정보를 확인할 수 있어야 한다.

최소한 다음 유형을 고려할 수 있다.

```text
Context Limit

Tool Calling

Structured Output
```

모든 Provider Capability를 일반화할 필요는 없다.

실험에 필요한 Capability만 관리한다.

---

## FR-09. Capability 기반 후보 제외

Request가 특정 Capability를 필요로 하는 경우
해당 Capability를 지원하지 않는 Model을 Routing 후보에서 제외할 수 있어야 한다.

예:

```text
Request
Tool Calling 필요

Model A
Tool Calling 지원

Model B
Tool Calling 미지원

↓
Model B 제외
```

구체적인 Model 선택 방식은 이후 Routing 전략에 따라 달라질 수 있다.

---

## FR-10. Token 사용량 확인

Provider가 Usage 정보를 제공하는 경우 다음 값을 확인할 수 있어야 한다.

```text
Input Token

Output Token

Total Token
```

Model 간 Token 사용량을 비교할 수 있어야 한다.

---

## FR-11. Cost 계산

Model별 가격 정보와 Token Usage를 이용하여
요청당 예상 비용을 계산할 수 있어야 한다.

예:

```text
Input Token Cost
+
Output Token Cost
=
Estimated Request Cost
```

정확한 과금 시스템을 구현하는 것은 목적이 아니다.

Routing 전략 비교를 위한 예상 비용이면 충분하다.

---

## FR-12. Latency 측정

각 Request에서 최소한 다음 값을 확인할 수 있어야 한다.

```text
Model Latency

End-to-End Latency
```

Router 자체의 비용을 비교할 필요가 생기면 Routing Latency를 별도로 측정할 수 있어야 한다.

---

## FR-13. Evaluation 실행

미리 정의한 Evaluation Dataset을 이용해 Router를 반복적으로 실행할 수 있어야 한다.

```text
Evaluation Dataset
       ↓
Router
       ↓
Selected Model
       ↓
LLM
       ↓
Evaluation Result
```

각 Case의 결과를 개별적으로 확인할 수 있어야 한다.

---

## FR-14. Routing 결과 평가

Evaluation Dataset을 이용해 Router의 선택 결과를 평가할 수 있어야 한다.

단순히:

```text
Expected Model
vs
Actual Model
```

만 비교하지 않는다.

Case에 따라 다음 조건을 사용할 수 있다.

```text
Allowed Model

Required Capability

Quality Requirement

Latency Requirement

Cost Requirement
```

여러 Model이 요구사항을 만족할 수 있는 Case를 허용한다.

---

## FR-15. Model 직접 비교

동일한 Request를 여러 Model에 직접 실행해 결과를 비교할 수 있어야 한다.

예:

```text
Request
   ↓
Model A
Model B
Model C
```

최소한 다음 값을 비교할 수 있어야 한다.

```text
Response

Quality

Latency

Token

Cost
```

이를 통해 Router의 Model 선택이 실제로 합리적인지 확인한다.

---

## FR-16. Answer Quality 평가

Routing 결과와 별개로
선택된 Model의 Answer 품질을 평가할 수 있어야 한다.

최소한 다음과 같은 기준을 사용할 수 있다.

```text
요청을 제대로 이해했는가?

핵심 요구사항을 충족했는가?

사실과 다른 내용을 생성하지 않았는가?

요구한 형식을 지켰는가?
```

구체적인 Quality 평가 방식은 Evaluation 설계 단계에서 결정한다.

Routing Failure와 Model Quality Failure를 구분할 수 있어야 한다.

---

## FR-17. Routing Failure 확인

Evaluation 기준을 만족하지 못한 Routing Case를 확인할 수 있어야 한다.

예:

```text
Case ID

Request Type

Selected Model

Expected Condition

Failure Reason
```

Failure Case는 이후 Experiment에서 다시 재현할 수 있어야 한다.

---

## FR-18. Provider Failure 확인

Model 또는 Provider 호출이 실패한 경우
실패 사실과 유형을 확인할 수 있어야 한다.

예:

```text
Timeout

Rate Limit

Provider Error

Model Unavailable

Invalid Request
```

Baseline에서는 Failure를 자동으로 복구할 필요는 없다.

먼저 실패 상태를 관찰할 수 있어야 한다.

---

## FR-19. Fallback

Provider 또는 Model Failure가 실제 문제로 확인되면
다른 Model로 요청을 전달하는 Fallback을 적용할 수 있어야 한다.

```text
Primary Model
      ↓
Failure
      ↓
Fallback Model
```

단, Fallback은 Baseline에 포함하지 않는다.

실제 Failure Experiment 이후 적용 여부를 결정한다.

---

## FR-20. Routing 전략 변경

Baseline Routing에서 반복적인 실패가 확인되면
다른 Routing 전략을 적용할 수 있어야 한다.

후보 예:

```text
Rule-based Routing

Capability-based Routing

Semantic Routing

LLM-based Routing
```

특정 전략 자체가 요구사항은 아니다.

최종 요구사항은 **현재 Routing 실패를 해결하기 위해 필요한 경우 Routing Policy를 교체하거나 확장할 수 있어야 한다**는 것이다.

---

## FR-21. Model Cascade

고성능 Model을 모든 요청에 사용하는 것이 비효율적이라는 문제가 확인되면
여러 Model을 단계적으로 사용하는 구조를 검토할 수 있어야 한다.

예:

```text
Request
   ↓
Small Model
   ↓
Quality 부족
   ↓
Large Model
```

Cascade 사용 자체가 요구사항은 아니다.

Quality, Cost, Latency를 함께 비교한 뒤 도입 여부를 결정한다.

---

## FR-22. Routing Evaluation 비교

Routing 전략을 변경한 뒤
동일한 Evaluation Dataset으로 결과를 다시 측정할 수 있어야 한다.

```text
Baseline Router
       ↓
Evaluation

Routing 변경
       ↓
동일 Evaluation

Before / After 비교
```

기술 적용 자체를 개선으로 판단하지 않는다.

실제 Metric 결과로 판단한다.

---

# 7. 초기 Router 처리 흐름

Baseline은 최대한 단순하게 구성한다.

```text
POST /api/v1/chat
        ↓
Request Validation
        ↓
Baseline Router
        ↓
Model Selection
        ↓
LLM Client
        ↓
Selected Model
        ↓
Answer
```

Evaluation 시에는 다음 정보를 함께 수집한다.

```text
Selected Model

Routing Reason

Latency

Token Usage

Estimated Cost
```

초기에는 다음 기능을 사용하지 않는다.

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

이 기능들은 실제 Baseline 평가에서 문제가 확인된 이후에만 검토한다.

---

# 8. Baseline에서 처리할 요청

초기 Router에서는 **Text 기반 일반 요청**을 중심으로 처리한다.

예:

```text
"이 문장을 영어로 번역해줘."

"REST와 RPC 차이를 설명해줘."

"Java 코드에서 Race Condition 가능성을 분석해줘."

"다음 내용을 세 문장으로 요약해줘."
```

Baseline Evaluation에는 난이도가 다른 요청을 포함한다.

```text
Simple

General

Reasoning
```

다음 요청은 초기 Baseline의 핵심 대상이 아니다.

```text
Image Input

Audio Input

실제 Tool 실행

복잡한 Multi-step Agent Workflow

장기 Conversation Memory

실시간 Web Search
```

필요성이 이후 Experiment에서 확인되면 별도 범위로 검토한다.

---

# 9. 비기능 요구사항

## NFR-01. Routing 품질 측정 가능성

Routing 품질을 사람의 느낌만으로 판단하지 않는다.

동일한 Evaluation Dataset을 이용해 반복적으로 측정할 수 있어야 한다.

예:

```text
Routing Success Rate

Capability Match Rate

Requirement Satisfaction Rate
```

Metric은 실제 Evaluation 설계에 맞게 선택한다.

Metric 이름을 많이 만드는 것이 목적은 아니다.

---

## NFR-02. Routing과 Generation 분리 평가

최종 Answer가 좋지 않을 때 원인을 구분할 수 있어야 한다.

```text
적절하지 않은 Model 선택
→ Routing Failure
```

```text
의도한 Model 선택
하지만 Answer 품질 부족
→ Model / Generation Failure
```

LLM Router 전체를 하나의 Black Box로 평가하지 않는다.

---

## NFR-03. Quality / Cost / Latency 분리

다음 값을 하나의 숫자로 합쳐서 품질을 설명하지 않는다.

```text
Quality

Cost

Latency
```

각 값을 별도로 확인할 수 있어야 한다.

필요하다면 이후 Trade-off를 비교한다.

---

## NFR-04. 재현 가능한 평가

같은 Routing 전략과 같은 Dataset을 사용했을 때
가능한 한 동일한 조건으로 평가할 수 있어야 한다.

다음 조건을 기록한다.

```text
Evaluation Dataset

Routing Strategy

Model 목록

Model Version

Prompt

Generation Parameter

Quality Evaluation 기준

실행 시각
```

외부 LLM 특성상 완전히 동일한 Response를 보장할 필요는 없다.

비교 가능한 조건을 남기는 것이 목적이다.

---

## NFR-05. Routing 변경의 측정

다음과 같이 기술 적용 자체를 개선으로 판단하지 않는다.

```text
Semantic Routing 적용
→ 개선
```

이 아니라:

```text
Baseline
   ↓
Semantic Routing
   ↓
동일 Dataset 평가
   ↓
Quality / Cost / Latency 비교
```

결과로 판단한다.

Metric이 동일하거나 나빠질 수도 있으며
그 결과도 그대로 기록한다.

---

## NFR-06. Latency 관측

최소한 다음 값을 확인할 수 있어야 한다.

```text
Model Latency

End-to-End Latency
```

Routing 전략 자체의 비용이 의미 있게 증가하면:

```text
Routing Latency
```

도 분리하여 확인할 수 있어야 한다.

현재 사용하지 않는 단계의 Metric은 필요하지 않다.

---

## NFR-07. 비용 관측

상용 LLM API를 사용하는 경우 다음 정보를 확인할 수 있어야 한다.

```text
Input Token

Output Token

요청당 예상 비용

Model별 평균 비용

Evaluation 전체 예상 비용
```

실제 Billing 시스템 수준의 정확성을 목표로 하지 않는다.

Model 및 Routing 전략 비교가 가능한 수준이면 충분하다.

---

## NFR-08. Capability 안전성

Request가 특정 Capability를 요구한다면
해당 Capability를 지원하지 않는 Model로 요청을 보내지 않아야 한다.

예:

```text
Tool Calling 필요
+
Tool Calling 미지원 Model
→ Routing 제외
```

---

## NFR-09. 불필요한 고비용 Model 호출 관측

작은 Model로도 요구사항을 만족할 수 있는 Request를
항상 고비용 Model로 보내는 구조를 좋은 결과로 판단하지 않는다.

예:

```text
Simple Request

Model Small
Quality 기준 충족

그런데

Model Large 선택
```

이 경우 Cost Inefficiency 후보로 분석할 수 있어야 한다.

---

## NFR-10. 실패 안전성

Provider나 Model 호출에 실패했을 때
정상 처리된 것처럼 Response를 만들어내지 않아야 한다.

실패 상태와 원인을 확인할 수 있어야 한다.

Fallback이 없는 Baseline이라면 실패를 그대로 반환해도 된다.

---

## NFR-11. 동일 조건 비교

Routing 구조나 Model 선택 기준을 변경할 때
가능한 한 동일한 평가 조건을 유지한다.

```text
동일 Evaluation Dataset

동일 Model 목록

동일 Prompt

동일 Generation Parameter

동일 Quality 기준
```

하나의 변수를 비교하는 Experiment에서는 가능하면 다른 조건을 변경하지 않는다.

---

## NFR-12. 관측 가능성

최종 Answer만 저장하는 것이 아니라
필요 시 다음 흐름을 추적할 수 있어야 한다.

```text
Request
   ↓
Routing Input
   ↓
Routing Decision
   ↓
Selected Model
   ↓
Provider
   ↓
Model Response
   ↓
Token / Cost / Latency
   ↓
Evaluation Result
```

이를 통해 왜 특정 결과가 발생했는지 분석할 수 있어야 한다.

---

## NFR-13. Evaluation Dataset 보호

Baseline 이후 Metric을 높이기 위해
Evaluation Dataset을 현재 구현에 맞춰 변경하지 않는다.

다음을 하지 않는다.

```text
실패 Case 삭제

어려운 Request 삭제

Actual Route에 맞춰 Expected 조건 변경

현재 Router가 성공하도록 Quality 기준 완화
```

Dataset 자체의 오류가 확인되면
변경 이유와 기존 결과와의 비교 가능성을 명확하게 기록한다.

---

## NFR-14. Provider 종속성 제한

Routing 핵심 로직이 특정 Provider API 형식에 직접 의존하지 않도록 역할을 구분한다.

하지만:

```text
미래에 Provider가 많이 추가될 수도 있다
```

는 이유만으로 Baseline부터 복잡한 Framework를 만들지 않는다.

현재 요구사항을 만족하는 가장 단순한 구조를 우선한다.

---

# 10. Evaluation Dataset 요구사항

이번 프로젝트에서는 Evaluation Dataset을 부가적인 테스트 데이터로 취급하지 않는다.

**Baseline 구현의 일부**로 본다.

Dataset에는 최소한 다음 유형의 Request를 포함한다.

## 10.1 Simple Request

복잡한 추론이 필요하지 않은 요청이다.

예:

```text
"이 문장을 영어로 번역해줘."

"다음 내용을 한 줄로 요약해줘."
```

여러 Model이 모두 충분히 처리할 수 있는지 확인한다.

---

## 10.2 General Request

일반적인 지식이나 설명 요청이다.

예:

```text
"REST와 RPC 차이를 설명해줘."
```

---

## 10.3 Reasoning Request

복잡한 분석이나 추론이 필요한 요청이다.

예:

```text
"이 Architecture의 장애 가능성을 분석하고 해결 후보를 비교해줘."
```

Simple Request와 비교해 Model별 품질 차이가 실제로 발생하는지 확인한다.

---

## 10.4 Long Context Request

긴 Context가 포함된 요청이다.

Model별 Context 제한이나 긴 입력 처리 차이를 확인한다.

---

## 10.5 Capability Request

특정 Capability가 필요한 요청이다.

예:

```text
Tool Calling Required

Structured Output Required
```

지원하지 않는 Model이 선택되지 않는지 확인한다.

---

## 10.6 Cost-sensitive Request

둘 이상의 Model이 품질 기준을 만족하지만
비용 차이가 의미 있게 발생하는 Case다.

```text
Model A
Quality PASS
Cost 낮음

Model B
Quality PASS
Cost 높음
```

Router가 필요 이상으로 비싼 Model을 선택하는지 확인한다.

---

## 10.7 Latency-sensitive Request

품질뿐 아니라 응답 속도가 중요한 Case다.

```text
Quality PASS
+
Latency Requirement
```

를 함께 확인한다.

---

## 10.8 Ambiguous Routing Request

둘 이상의 Model이 모두 적절한 Case다.

이 유형에서는 특정 Model 하나를 정답으로 강제하지 않는다.

예:

```text
Allowed Models
- Model A
- Model B

Required Quality
PASS

Maximum Cost
...

Maximum Latency
...
```

Router 품질을 Dataset 작성자의 취향으로 평가하지 않기 위한 Case다.

---

## 10.9 Provider Failure Case

Model 호출 실패를 의도적으로 재현하는 Case를 포함할 수 있다.

초기 목적은 자동 복구가 아니다.

```text
Failure가 발생했을 때
현재 시스템이 어떻게 동작하는가?
```

를 확인하는 것이 먼저다.

---

# 11. Baseline 완료 조건

초기 Router 단계에서는 다음 조건을 만족하면 Baseline 구현이 완료된 것으로 판단한다.

### Request API

```text
자연어 Request를 받을 수 있다.

빈 Request를 Validation할 수 있다.
```

### Model

```text
최소 두 개 이상의 Model을 설정할 수 있다.

Model의 활성 / 비활성 상태를 구분할 수 있다.

Routing에 필요한 기본 Capability 정보를 확인할 수 있다.
```

### Router

```text
Request를 입력받아 Model 하나를 선택할 수 있다.

선택된 Model을 확인할 수 있다.

Routing Reason을 확인할 수 있다.
```

### LLM

```text
선택된 Model을 실제 호출할 수 있다.

Model Response를 사용자에게 반환할 수 있다.
```

### Observability

```text
Model Latency를 확인할 수 있다.

End-to-End Latency를 확인할 수 있다.

Token Usage를 확인할 수 있다.

예상 Cost를 계산할 수 있다.
```

Provider에서 제공하지 않는 값은 명확히 표시할 수 있다.

### Evaluation

```text
Evaluation Dataset이 존재한다.

Dataset을 반복 실행할 수 있다.

Routing 결과를 확인할 수 있다.

Model별 결과를 직접 비교할 수 있다.

실패 Case를 확인할 수 있다.
```

### Test

```text
Request Validation Test

Model Selection Test

LLM Client Test

Router Integration Test

Evaluation 실행 Test
```

### README

Baseline 완료 시점부터 최소한 다음 내용을 README에서 확인할 수 있어야 한다.

```text
프로젝트 실행 방법

필요한 환경 변수

Model / Provider 설정 방법

Chat API 호출 방법

Test 실행 방법

Evaluation 실행 방법
```

이 단계에서는 Routing 품질이 좋을 필요는 없다.

**측정 가능한 Baseline Router가 존재하는 것**이 완료 조건이다.

---

# 12. Baseline에서 의도적으로 해결하지 않을 문제

초기 구현에서 다음 문제를 발견하더라도 바로 해결하지 않는다.

```text
Simple Request에 고비용 Model 선택

Reasoning Request에 작은 Model 선택

Model별 Answer 품질 차이

Cost 비효율

Latency 문제

Routing 기준의 오분류

Provider Failure

Fallback 부재

Long Context 처리 실패

특정 Capability Routing 실패
```

먼저 Evaluation 결과에 실제 Failure Case로 남긴다.

그 후 Experiment 단계에서 문제별로 접근한다.

---

# 13. 이후 실험에서 확인할 문제

Baseline 이후에는 대략 다음 흐름으로 진행한다.

```text
Baseline Router
      ↓
Model별 Baseline 평가
      ↓
Routing 결과 평가
      ↓
Failure Case 분석
      ↓
Request 난이도 / 특성 분석
      ↓
Routing 기준 검토
      ↓
Quality / Cost Trade-off 확인
      ↓
Latency Trade-off 확인
      ↓
Capability Routing 확인
      ↓
Provider Failure 재현
      ↓
Fallback 필요성 검토
      ↓
고정 Rule의 한계 확인
      ↓
Semantic / LLM Routing 필요성 검토
      ↓
Cascade 필요성 검토
      ↓
최종 비교
```

여기서 중요한 점은:

```text
Phase 도달
=
해당 기술 반드시 도입
```

이 아니라는 것이다.

예를 들어 단순 Rule만으로 Evaluation 기준을 충분히 만족한다면
Semantic Routing을 추가하지 않을 수도 있다.

작은 Model과 큰 Model의 품질 차이가 거의 없다면
복잡한 Model Cascade를 만들 필요가 없을 수도 있다.

Provider Failure가 현재 실험에서 의미 있는 문제가 아니라면
Fallback을 최종 구조에 포함하지 않을 수도 있다.

LLM-based Routing이 품질은 조금 좋아지지만
Routing Latency와 Cost가 크게 증가한다면 사용하지 않을 수도 있다.

---

# 14. 이번 프로젝트에서 제외할 기능

LLM Router의 학습 목적과 직접 관계없는 기능은 구현하지 않는다.

다음 기능은 초기 프로젝트 범위에서 제외한다.

```text
회원가입

로그인 UI

사용자별 권한 체계

결제 시스템

실제 Billing 정산

관리자 Dashboard

Frontend UI

Conversation 장기 저장

Long-term Memory

Fine-tuning

Model Training

Image Generation

Voice Interface

대규모 Agent Platform

대규모 Traffic을 위한 분산 Router

Production 수준 SLA
```

또한 처음부터 다음 기술을 사용하지 않는다.

```text
Semantic Routing

LLM-based Routing

Model Cascade

Routing Cache

Dynamic Threshold

Automatic Retry Orchestration

LangGraph

Multi-Agent
```

실제 문제가 확인되고 프로젝트 학습 범위 안에서 필요성이 생긴 경우에만 후보로 검토한다.

---

# 15. LLM Router를 어떻게 성공으로 판단할 것인가

이번 프로젝트에서 가장 피하고 싶은 결과는 다음과 같다.

```text
Model 여러 개 연결

↓

Router가 요청을 나눔

↓

응답 잘 나옴

↓

LLM Router 성공
```

대신 성공 기준을 여러 단계로 나눈다.

```text
Routing

요청 특성에 맞는 Model을 선택했는가?


Capability

요청에 필요한 Capability를 만족하는 Model인가?


Quality

선택된 Model의 Answer가 요구 기준을 만족했는가?


Cost

같은 품질을 더 낮은 비용으로 처리할 수 있었는가?


Latency

품질 개선에 비해 응답 시간이 과도하게 증가하지 않았는가?


Failure

잘못된 결과가 Routing 문제인지 Model 문제인지 구분할 수 있는가?


Efficiency

Routing 전략의 복잡도 증가가 실제 품질 / 비용 / Latency 개선으로 이어졌는가?
```

하나의 숫자로 모든 Router 품질을 설명하려고 하지 않는다.

---

# 16. 정리

LLM Router의 초기 구조 자체는 단순하다.

```text
Request
   ↓
Router
   ↓
Selected Model
   ↓
LLM
   ↓
Answer
```

하지만 실제 학습은 이 구조를 평가하는 시점부터 시작된다.

앞으로는 다음 질문에 답해볼 예정이다.

> Simple Request는 작은 Model로 충분할까?

> Reasoning Request에서는 Model별 품질 차이가 실제로 얼마나 발생할까?

> Router의 Model 선택을 어떤 기준으로 정답이라고 판단할 수 있을까?

> Expected Model 하나를 정답으로 두는 방식은 적절할까?

> Quality를 유지하면서 Cost를 줄일 수 있을까?

> 더 좋은 Model을 선택했을 때 증가한 Latency는 정당화될까?

> Capability가 다른 Model을 Router가 안전하게 구분할 수 있을까?

> Provider Failure가 발생했을 때 Fallback이 실제로 필요할까?

> 단순 Rule Routing은 어떤 Request에서 실패할까?

> Semantic Routing이나 LLM-based Routing을 추가할 만큼 실패 차이가 발생할까?

> Model Cascade는 실제 Cost를 줄이면서 Quality를 유지할 수 있을까?

그리고 이번 프로젝트에서는 결과 자체만큼 다음을 중요하게 본다.

> **Router가 잘 동작하는 것처럼 보이는 것이 아니라, 실제 Evaluation Dataset과 Metric을 통해 어떤 Routing 판단이 좋았고 무엇이 실패했으며 Quality·Cost·Latency가 어떻게 달라졌는지를 설명할 수 있어야 한다.**
