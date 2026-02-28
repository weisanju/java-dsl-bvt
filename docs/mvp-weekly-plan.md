# Java + Karate 接口测试平台 MVP（极简版排期）

## 1. 原则

- 单人维护，优先“能跑起来”。
- 单体架构，不拆服务，不引入额外中间件。
- 依赖仅保留 PostgreSQL（+ 应用本身）。

## 2. MVP 范围（P0）

1. 环境与变量管理
2. 用例（Karate）与数据集（JSON/YAML）管理
3. 套件编排（顺序执行）
4. 手动触发执行
5. 执行报告查询

## 3. 非目标（本期不做）

- Redis / RabbitMQ / MinIO
- 分布式 Runner
- 复杂权限体系
- 企业通知与审批

## 4. 5 周排期（最短路径）

## Week 1：工程初始化 + 最小 DDL

### 目标
- 起一个可运行的 Spring Boot 服务。
- 建好核心表结构。

### 动作
- 建立分层结构（controller/service/repository）。
- 落地 `docs/platform-mvp-ddl.sql` 最小表。
- 健康检查、统一异常处理。

### 验收
- 服务可启动，数据库可读写。

---

## Week 2：Case 与 DataSet 管理

### 目标
- 完成用例和数据集 CRUD。

### 动作
- 实现 `t_test_case`、`t_test_case_data_set` API。
- JSON/YAML 入库前语法校验。
- 用例版本号自动递增。

### 验收
- 能创建/修改 case，并绑定数据集。

---

## Week 3：Suite 编排 + 手动执行

### 目标
- 从套件触发执行并记录结果。

### 动作
- 实现 `t_test_suite`、`t_test_suite_case`。
- 实现 `POST /api/suites/{id}/run`。
- 顺序调用 Karate，执行结果落库。

### 验收
- 一次 run 可完整执行多个 case 并保存状态。

---

## Week 4：报告查询

### 目标
- 能看 run 概览和失败详情。

### 动作
- 实现 `GET /api/runs/{id}`。
- 实现 `GET /api/runs/{id}/report`。
- 保存失败断言、耗时、请求响应摘要。

### 验收
- 能通过报告快速定位失败原因。

---

## Week 5：定时任务（P1 轻量）

### 目标
- 支持最基础定时回归。

### 动作
- 增加 `t_test_plan`。
- `@Scheduled` 扫描可执行计划并触发 run。

### 验收
- 可按 cron 自动触发并产生 run 记录。

## 5. 最小交付标准

- 能创建 case + dataset + suite。
- 能手动触发执行并查看报告。
- 能配置并执行至少一个定时计划。
