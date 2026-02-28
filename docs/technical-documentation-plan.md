# 单人技术仓库：极简功能清单与实现思路（QLExpress 自定义引擎版）

## 1. 目标

以最简单方案实现可用的接口测试平台，并满足你提出的两个约束：

1. **不引入过多中间件**（只保留 PostgreSQL）。
2. **执行与规则引擎自定义实现**，表达式统一使用阿里巴巴 **QLExpress**。

---

## 2. 极简技术选型

### 必选

- Java 21
- Spring Boot 3.x（Web、Validation、Actuator、Scheduling）
- PostgreSQL（唯一外部依赖）
- QLExpress（表达式引擎）
- JDK HttpClient（HTTP 执行）

### 可选（不是当前阶段）

- Flyway（迁移管理）
- Swagger/OpenAPI（接口文档）

> 不上 Redis、MQ、对象存储，避免早期运维复杂度。

---

## 3. 功能清单（最小实现）

## 3.1 P0（必须）

1. 环境与变量管理（环境、变量覆盖）
2. 用例管理（自定义 case_definition，内含 steps）
3. JSON/YAML 数据集管理
4. 套件管理（多个用例顺序执行）
5. 手动触发执行（Run）
6. 执行报告查询（通过率、失败步骤、失败表达式）
7. 基础健康检查与日志

## 3.2 P1（后续）

1. 定时执行（`@Scheduled` + cron）
2. CI 触发接口（简单 token 鉴权）
3. 失败重试（仅网络异常）

## 3.3 P2（再后续）

1. OpenAPI 导入 case
2. 报告对比（与上一次 run）
3. Webhook 通知

---

## 4. 自定义引擎设计（QLExpress 为核心）

## 4.1 引擎列表

1. **ContextEngine**  
   合并环境变量 + 数据集 + 运行时变量，生成统一上下文 `Map<String, Object>`。

2. **RequestRenderEngine**  
   用 QLExpress 计算 URL/Headers/Body 中的表达式并渲染请求。

3. **HttpExecuteEngine**  
   发送 HTTP 请求并返回标准响应对象（status、headers、body、elapsed）。

4. **AssertEngine（QLExpress）**  
   执行断言表达式列表，产出 pass/fail、失败表达式、失败原因。

5. **ExtractEngine（QLExpress）**  
   从响应中提取变量写回上下文，供后续 step 使用。

6. **OrchestratorEngine**  
   串联上述引擎按 step 顺序执行，写入 run/result。

## 4.2 QLExpress 约定

- 断言表达式示例：`status == 200 && body.code == 0`
- 提取表达式示例：`token = body.data.token`
- 条件执行示例：`skipWhen = env != 'test'`

> 通过注册自定义函数（如 `jsonPath(body, "$.data.id")`）增强表达能力。

---

## 5. 最小 API 清单

### 5.1 环境与变量

- `POST /api/environments`
- `GET /api/environments`
- `POST /api/variables`
- `GET /api/variables?environmentId=`

### 5.2 用例与数据集

- `POST /api/cases`
- `PUT /api/cases/{id}`
- `POST /api/cases/{id}/datasets`
- `GET /api/cases/{id}/datasets`

### 5.3 套件与执行

- `POST /api/suites`
- `POST /api/suites/{id}/cases`
- `POST /api/suites/{id}/run`
- `GET /api/runs/{id}`
- `GET /api/runs/{id}/report`

### 5.4 计划（P1）

- `POST /api/plans`
- `PATCH /api/plans/{id}/enable`

---

## 6. Mermaid 架构图

完整图表请看：`docs/architecture-mermaid.md`

---

## 7. 本周直接开工建议

1. 先定义 `case_definition` JSON 结构（step/request/assert/extract）。
2. 优先实现 `ContextEngine + HttpExecuteEngine + AssertEngine` 三个核心引擎。
3. 打通“手动执行 -> 结果入库 -> 报告查询”主链路。
4. 第 2 周再补 `ExtractEngine` 与 `@Scheduled` 计划任务。

---

该方案优先保证“做得出来、跑得起来、后续可扩展”，适合单人持续迭代。
