# Java + Karate 接口测试平台 技术文档规划

## 1. 文档规划目标

本规划用于指导 MVP 阶段（8 周）技术文档建设，目标如下：

1. 建立可持续的文档体系，覆盖研发、测试、运维、治理全链路。
2. 保证“需求变更 -> 设计落地 -> 开发交付 -> 上线运维”每一步有据可查。
3. 为新成员提供可快速上手的知识入口，减少口口相传导致的信息损耗。
4. 形成可审计、可追踪、可版本化的文档资产。

## 2. 文档受众与使用场景

### 2.1 受众角色
- 产品/项目经理：看范围、里程碑、风险、验收。
- 后端/前端/测试开发：看架构设计、接口契约、开发规范、测试策略。
- DevOps/SRE：看部署方案、监控告警、应急预案、容量规划。
- 安全/合规：看权限模型、密钥策略、审计方案。

### 2.2 主要场景
- 立项评审：方案可行性、边界、资源投入。
- 研发执行：按文档开发，减少返工。
- 变更管理：通过 ADR、变更记录明确影响面。
- 发布运维：上线步骤、回滚流程、故障处置。

## 3. 文档体系分层（信息架构）

建议按以下 7 层建设文档：

1. **L0 - 项目总览层**  
   项目目标、范围、路线图、术语、里程碑。
2. **L1 - 业务与需求层**  
   用户故事、功能清单、非功能需求、验收标准。
3. **L2 - 架构设计层**  
   系统架构、模块边界、关键流程、技术选型、ADR。
4. **L3 - 契约与数据层**  
   API 规范、错误码、数据模型、DDL、数据字典。
5. **L4 - 实施与交付层**  
   开发规范、分支策略、CI/CD、测试策略、发布流程。
6. **L5 - 运维与治理层**  
   部署、监控、告警、排障、应急、容量。
7. **L6 - 合规与审计层**  
   权限、审计日志、密钥治理、变更记录、文档审计轨迹。

## 4. 文档目录规划（仓库落地）

```text
docs/
├── README.md                         # 文档导航（后续可补）
├── technical-documentation-plan.md   # 本文档（总规划）
├── mvp-weekly-plan.md                # MVP 按周排期（已完成）
├── platform-mvp-ddl.sql              # DDL 草案（已完成）
├── architecture/
│   ├── system-architecture.md        # 系统架构设计
│   ├── module-design.md              # 模块职责与边界
│   ├── sequence-core-flows.md        # 核心时序（触发/执行/回传）
│   └── adrs/
│       ├── ADR-0001-test-engine.md   # 执行引擎选型决策
│       ├── ADR-0002-message-queue.md # 任务队列选型决策
│       └── ADR-0003-storage.md       # 报告存储选型决策
├── api/
│   ├── openapi.yaml                  # 对外 API 契约
│   ├── api-style-guide.md            # API 风格规范
│   └── error-code.md                 # 错误码规范
├── data/
│   ├── data-model.md                 # 概念模型与实体关系
│   ├── migration-strategy.md         # 数据迁移策略
│   └── data-dictionary.md            # 字段字典
├── engineering/
│   ├── coding-standard.md            # 代码规范
│   ├── branching-and-release.md      # 分支与发布策略
│   ├── ci-cd-pipeline.md             # CI/CD 说明
│   └── test-strategy.md              # 测试策略（单测/集成/回归）
├── operations/
│   ├── deployment-guide.md           # 部署手册
│   ├── runbook.md                    # 运维 runbook
│   ├── monitoring-alerting.md        # 监控与告警
│   ├── incident-response.md          # 故障应急响应
│   └── capacity-planning.md          # 容量规划
└── governance/
    ├── rbac-and-audit.md             # 权限与审计方案
    ├── secret-management.md          # 密钥管理策略
    ├── change-log.md                 # 变更记录
    └── document-review-policy.md     # 文档评审制度
```

> 当前仓库可先补齐 P0 文档，再逐步扩展到完整结构。

## 5. P0/P1/P2 文档清单矩阵

## 5.1 P0（MVP 必须完成）

| 编号 | 文档 | 目标 | 负责人角色 | 截止周 |
|---|---|---|---|---|
| DOC-P0-01 | `mvp-weekly-plan.md` | 确定范围、节奏、验收 | PM + TL | Week 1 |
| DOC-P0-02 | `architecture/system-architecture.md` | 明确系统边界与技术栈 | 架构师/后端 TL | Week 2 |
| DOC-P0-03 | `architecture/module-design.md` | 划清服务职责与依赖 | 后端 TL | Week 2 |
| DOC-P0-04 | `api/openapi.yaml` | 统一 API 契约 | 后端 + 前端 | Week 3 |
| DOC-P0-05 | `api/error-code.md` | 统一错误码与异常语义 | 后端 | Week 3 |
| DOC-P0-06 | `platform-mvp-ddl.sql` | 明确数据结构与约束 | 后端 + DBA | Week 3 |
| DOC-P0-07 | `engineering/ci-cd-pipeline.md` | 明确流水线与质量门禁 | DevOps | Week 4 |
| DOC-P0-08 | `engineering/test-strategy.md` | 明确测试分层与覆盖目标 | 测试开发 | Week 4 |
| DOC-P0-09 | `operations/deployment-guide.md` | 标准化部署与回滚 | DevOps | Week 6 |
| DOC-P0-10 | `operations/runbook.md` | 常见故障定位与处理 | SRE/DevOps | Week 7 |
| DOC-P0-11 | `governance/rbac-and-audit.md` | 权限与审计追踪落地 | 安全 + 后端 | Week 7 |

## 5.2 P1（建议在 MVP 后补齐）

- API 风格规范（api-style-guide）
- 数据字典（data-dictionary）
- 监控告警细则（monitoring-alerting）
- 事件响应手册（incident-response）
- 变更日志制度（change-log）

## 5.3 P2（二期）

- 容量规划模型（capacity-planning）
- 跨团队接入指南（integration handbook）
- 合规审计专项（security baseline）

## 6. 按周文档排期（与研发排期对齐）

| 周次 | 研发主线 | 文档主线 | 产出 |
|---|---|---|---|
| Week 1 | 基线搭建 | 项目总览与排期文档 | P0-01（已完成） |
| Week 2 | 鉴权与项目空间 | 系统架构 + 模块设计 | P0-02, P0-03 |
| Week 3 | 环境变量与数据模型 | OpenAPI + 错误码 + DDL | P0-04, P0-05, P0-06（DDL 已完成） |
| Week 4 | 用例管理 | CI/CD + 测试策略 | P0-07, P0-08 |
| Week 5 | 套件与执行中心 | 核心流程时序文档 | sequence-core-flows |
| Week 6 | 调度与 Runner | 部署手册 | P0-09 |
| Week 7 | 报告与通知 | Runbook + RBAC/Audit | P0-10, P0-11 |
| Week 8 | 联调与上线 | 文档验收与归档 | 文档评审记录与发布说明 |

## 7. 文档模板与编写规范

## 7.1 通用模板结构（每份技术文档建议包含）

1. 背景与目标  
2. 范围与非目标  
3. 方案设计  
4. 关键流程/时序  
5. 数据结构/接口契约  
6. 风险与降级方案  
7. 验收标准  
8. 版本与变更记录

## 7.2 命名规范

- 使用小写英文和中划线：`module-design.md`
- ADR 命名：`ADR-xxxx-title.md`
- SQL 命名（迁移）：`V{version}__{description}.sql`

## 7.3 版本管理规则

- 文档与代码同仓库、同分支策略。
- 重要文档变更必须走 PR 评审，至少 1 名相关角色批准。
- 重大决策（架构、存储、队列、安全）必须产生 ADR。

## 8. 文档评审与发布流程

### 8.1 状态机

`Draft -> In Review -> Approved -> Released -> Deprecated`

### 8.2 评审要求

- 架构类文档：后端 TL + 架构师评审。
- API 契约：后端 + 前端 + 测试三方评审。
- 运维文档：DevOps + 值班负责人评审。
- 安全文档：安全负责人评审。

### 8.3 发布节奏

- 每周固定一次文档审计（建议周五）。
- 与迭代发布同步生成《文档发布说明》。

## 9. 文档质量门禁（DoD for Docs）

每份 P0 文档需满足：

1. 内容完整：模板 8 个章节齐全（可按需简化，但需说明）。
2. 可执行：步骤可操作，不依赖“口头补充”。
3. 可追踪：关联需求、代码 PR、上线记录。
4. 可验证：给出验收条件或样例。
5. 可维护：包含版本号、更新时间、负责人。

## 10. 角色分工（RACI）

| 文档类型 | Responsible | Accountable | Consulted | Informed |
|---|---|---|---|---|
| 架构设计 | 架构师/后端 TL | 技术负责人 | 前端、测试、DevOps | 全员 |
| API 契约 | 后端 | 技术负责人 | 前端、测试 | 全员 |
| DDL/数据文档 | 后端/DBA | 后端 TL | 测试、DevOps | 全员 |
| CI/CD 与运维 | DevOps/SRE | 运维负责人 | 后端、测试 | 全员 |
| 权限与审计 | 安全/后端 | 技术负责人 | DevOps、测试 | 全员 |

## 11. 风险与对策

1. **风险：文档滞后于代码**  
   对策：PR 模板增加“文档更新检查项”，未更新禁止合并。

2. **风险：文档质量不一致**  
   对策：统一模板 + 周审机制 + 样例文档。

3. **风险：跨角色理解偏差**  
   对策：关键文档进行跨职能评审（开发/测试/运维联合评审）。

4. **风险：知识沉淀不足**  
   对策：强制故障复盘产出 runbook 条目并入库。

## 12. 本周可立即执行的动作（Next Actions）

1. 新建以下 P0 文档占位文件：  
   - `docs/architecture/system-architecture.md`  
   - `docs/architecture/module-design.md`  
   - `docs/api/openapi.yaml`  
   - `docs/api/error-code.md`  
   - `docs/engineering/ci-cd-pipeline.md`  
   - `docs/engineering/test-strategy.md`
2. 在项目 PR 模板增加“是否更新相关文档”勾选项。
3. 在每周例会增加“文档状态追踪”固定议程（10 分钟）。
4. 设立文档负责人（Doc Owner）并在文档头部标注联系人。

---

本规划文件用于指导 MVP 阶段的文档建设。进入二期后，建议按“能力域”继续扩展（性能、成本、合规、跨团队接入）。
