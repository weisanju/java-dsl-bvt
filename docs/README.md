# 文档导航（Docs Index）

本目录用于维护接口测试平台的技术文档（单人维护版本）。

## 已完成

- [功能清单与实现思路（QLExpress + TDD）](./technical-documentation-plan.md)
- [MVP 按周排期（TDD 驱动）](./mvp-weekly-plan.md)
- [TDD 开发指南](./tdd-development-guide.md)
- [数据库表结构草案（极简 DDL）](./platform-mvp-ddl.sql)
- [Mermaid 架构设计图](./architecture-mermaid.md)

## 推荐阅读顺序

1. 先读 `technical-documentation-plan.md`（看功能清单与实现策略）
2. 再读 `tdd-development-guide.md`（看 TDD 实操规则）
3. 再读 `architecture-mermaid.md`（看系统组件图与执行时序）
4. 再读 `mvp-weekly-plan.md`（看每周执行顺序）
5. 最后看 `platform-mvp-ddl.sql`（看最小数据落地）

## 文档使用约定

1. 文档与代码同仓库维护，随 PR 同步更新。
2. 单人场景下，优先保证“文档可执行”，避免过度模板化。
3. 每周至少同步一次文档，避免代码与文档脱节。
4. 文档标题需清晰、命名需规范（英文小写 + 中划线）。
