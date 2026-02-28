# 单人技术仓库：极简功能清单与实现思路（无中间件版）

## 1. 目标

以最简单方案实现一个可用的接口测试平台：

- 只保留核心能力：用例管理、执行、报告、定时任务。
- **不引入 Redis / RabbitMQ / MinIO 等中间件**。
- 仅使用：`Spring Boot + PostgreSQL + Karate`。

---

## 2. 极简技术选型

### 必选

- Java 21
- Spring Boot 3.x（Web、Validation、Actuator）
- PostgreSQL（唯一外部依赖）
- Karate（执行引擎）

### 可选（不是当前阶段）

- Flyway（数据库迁移）
- Swagger/OpenAPI（接口文档）

> 不做前后端分离复杂页面，先以后端 API + Swagger 页面为主。

---

## 3. 功能清单（最小实现）

## 3.1 P0（必须）

1. 环境与变量管理（环境、变量覆盖）
2. 用例管理（Karate 脚本）
3. JSON/YAML 数据集管理
4. 套件管理（多个用例顺序执行）
5. 手动触发执行（Run）
6. 执行结果与报告查询（通过率、失败详情）
7. 基础健康检查与日志

## 3.2 P1（后续）

1. 定时执行（`@Scheduled` + cron）
2. CI 触发接口（简单 token 鉴权）
3. 失败重试（仅网络异常）

## 3.3 P2（再后续）

1. OpenAPI 导入 case
2. 报告对比（与上一次 run）
3. 简单通知（Webhook）

---

## 4. 实现思路（按能力域）

## 4.1 资产管理（Case/DataSet/Suite）

### 实现

- `t_test_case`：保存 Karate 脚本文本。
- `t_test_case_data_set`：保存 JSON/YAML 文本。
- `t_test_suite` + `t_test_suite_case`：保存执行顺序。

### 原则

- 所有资产都存数据库，不依赖 Git 文件扫描。
- 用例更新时 `version_no + 1`。
- 数据集保存前做格式校验（JSON/YAML）。

## 4.2 执行引擎（Karate）

### 实现

- 调用 `POST /api/suites/{id}/run` 创建一次 run。
- 服务内同步执行（MVP），按套件顺序逐条跑 case。
- 每条 case 执行后写入结果表。

### 原则

- 先单线程执行，稳定后再做并发。
- 失败即记录，不中断整个 run（可配置）。

## 4.3 报告能力

### 实现

- `GET /api/runs/{id}`：run 概览。
- `GET /api/runs/{id}/report`：case 级明细。
- 趋势统计用 SQL 聚合（按天成功率）。

### 原则

- 请求/响应快照截断存储（避免数据膨胀）。
- 日志中对 token/password 做脱敏。

## 4.4 调度能力（P1）

### 实现

- `t_test_plan` 保存 cron。
- Spring `@Scheduled` 每分钟扫描启用计划并触发 run。

### 原则

- 不引入队列，直接数据库轮询。
- 一个计划同一时间只允许一个运行实例（避免重入）。

---

## 5. 极简架构图（文字版）

`Client -> Spring Boot API -> PostgreSQL`

同一个 Spring Boot 进程内包含：

1. 资产管理 API
2. 执行服务（调用 Karate）
3. 报告聚合
4. 定时调度（P1）

---

## 6. 最小 API 清单

### 6.1 环境与变量

- `POST /api/environments`
- `GET /api/environments`
- `POST /api/variables`
- `GET /api/variables?environmentId=`

### 6.2 用例与数据集

- `POST /api/cases`
- `PUT /api/cases/{id}`
- `POST /api/cases/{id}/datasets`
- `GET /api/cases/{id}/datasets`

### 6.3 套件与执行

- `POST /api/suites`
- `POST /api/suites/{id}/cases`
- `POST /api/suites/{id}/run`
- `GET /api/runs/{id}`
- `GET /api/runs/{id}/report`

### 6.4 计划（P1）

- `POST /api/plans`
- `PATCH /api/plans/{id}/enable`

---

## 7. 本周直接开工建议

1. 先把 `Case + DataSet + Suite + Run` 四个核心模块做完。
2. 先打通“手动执行 + 报告查询”，不做定时和 CI。
3. 再补 P1：`@Scheduled` 定时执行。

---

该方案优先保证“做得出来、跑得起来、后续可扩展”，适合单人持续迭代。
