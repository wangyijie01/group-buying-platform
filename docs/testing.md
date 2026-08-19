# 测试说明

## 默认验证命令

```bash
mvn -B -ntp verify
```

根 POM 聚合两个服务，共验证 15 个 Maven 模块。当前默认测试集包含 11 个自动化测试；它们不连接外部数据库、Redis、RabbitMQ 或支付渠道，适合本地提交前和 GitHub Actions 执行。

## 自动化测试覆盖

| 模块 | 测试数 | 主要断言 |
| --- | ---: | --- |
| market-domain | 4 | 通知成功、失败重试、单任务异常隔离、锁竞争延后且不消耗重试次数 |
| market-app | 3 | 生成/透传 `X-Trace-Id`，营销消息队列与 DLQ 参数 |
| trade-domain | 2 | 复用未支付订单，新订单先锁定营销资格再创建支付单 |
| trade-app | 2 | `X-Trace-Id` 响应头，交易消息队列与 DLQ 参数 |

## 历史集成测试

两个 `app` 模块保留了教学阶段的 `cn/bugstack/test/**` 集成与手工测试。它们依赖本地中间件、沙箱账号，部分还包含持续监听或长时间等待，因此默认只编译、不在 Surefire 中自动执行。

需要运行某个历史测试时，请先准备对应基础设施和沙箱配置，再在 IDE 中单独执行。不要把真实支付密钥写回 `application-*.yml`。

## CI 行为

`.github/workflows/backend-ci.yml` 在以下场景触发：

- 推送到 `main` 且后端源码、POM 或工作流发生变化；
- Pull Request 修改后端源码、POM 或工作流；
- 手工触发。

CI 在 Temurin Java 8 下分别运行 market 和 trade 的 `clean verify`，避免一个服务的依赖缓存掩盖另一个服务的问题。

## 后续测试计划

- 使用 Testcontainers 验证 MySQL 条件更新、Redis 库存预占和 RabbitMQ 死信路由。
- 增加支付回调、MQ 重复投递与退款重复请求的契约测试。
- 建立可复现压测场景后，再对外给出吞吐、P95/P99 和数据库访问量等数字。
