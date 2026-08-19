# 运维手册

本文给出本地演示环境的启动、巡检、死信处置和常见故障排查步骤。默认端口与 `.env.example` 保持一致。

## 启动与停止

1. 复制环境变量模板：`Copy-Item .env.example .env`。
2. 启动 MySQL、Redis 与 RabbitMQ：`docker compose up -d`。
3. 等待容器健康：`docker compose ps`。
4. 分别启动营销服务 `:8091` 和交易服务 `:8070`。
5. 停止基础设施：`docker compose down`。

`docker compose down` 不删除数据卷；只有显式附加 `--volumes` 才会删除本地数据库和消息数据。执行该操作前请确认不再需要这些数据。

## 基础巡检

| 检查项 | 营销服务 | 交易服务 | 预期结果 |
| --- | --- | --- | --- |
| 健康检查 | `http://127.0.0.1:8091/actuator/health` | `http://127.0.0.1:8070/actuator/health` | HTTP 200，状态为 `UP` |
| 应用信息 | `http://127.0.0.1:8091/actuator/info` | `http://127.0.0.1:8070/actuator/info` | 返回应用名称与描述 |
| Prometheus | `http://127.0.0.1:8091/actuator/prometheus` | `http://127.0.0.1:8070/actuator/prometheus` | 返回文本格式指标 |

RabbitMQ 管理台默认地址为 `http://127.0.0.1:15672`，用户名和密码来自 `.env` 的 `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD`。

每个 HTTP 响应都会返回 `X-Trace-Id`。调用方也可传入不超过 64 个字符、只包含字母、数字、点、下划线或短横线的同名请求头，便于关联日志。

## 消息重试模型

- 监听器执行失败后，由 Spring AMQP 在消费端进行最多 3 次退避重试。
- 业务队列配置 `default-requeue-rejected=false`，达到上限后不再无限回队。
- RabbitMQ 根据队列的 `x-dead-letter-exchange` 与 `x-dead-letter-routing-key` 将消息路由到对应 `.dlq`。
- 本地 `notify_task` 仍负责业务级补偿，定时扫描最多执行 5 次；二者分别覆盖消费异常和跨服务通知失败。

当前死信队列：

| 归属服务 | 业务队列 | 死信队列 |
| --- | --- | --- |
| market | `group_buy_market_queue_2_topic_team_success` | `group_buy_market_queue_2_topic_team_success.dlq` |
| market | `group_buy_market_queue_2_topic_team_refund` | `group_buy_market_queue_2_topic_team_refund.dlq` |
| trade | `s_pay_mall_queue_2_order_pay_success` | `s_pay_mall_queue_2_order_pay_success.dlq` |
| trade | `s_pay_mall_queue_2_topic_team_success` | `s_pay_mall_queue_2_topic_team_success.dlq` |
| trade | `s_pay_mall_queue_2_topic_team_refund` | `s_pay_mall_queue_2_topic_team_refund.dlq` |

## 死信队列处理

1. 在 RabbitMQ 管理台记录队列名、消息数量、`x-death`、routing key、业务主键和异常时间，不要立即清空队列。
2. 通过 `X-Trace-Id`、订单号或队伍 ID 检查上下游状态，判断是配置错误、依赖不可用、数据不合法还是代码缺陷。
3. 修复根因后，先在隔离环境用单条消息验证消费者幂等性。
4. 将消息重新发布到原业务交换机和原 routing key；不要直接投递到业务队列，以保留正常路由规则。
5. 核对业务状态和通知任务状态，再确认 DLQ 消息已减少且没有再次进入死信。

当前仓库没有自动 DLQ 重放接口，这是有意的安全边界：人工处置可避免未经检查的毒消息循环重放。后续计划提供带审计记录的受控重放后台。

## 常见故障

### MySQL 初始化未执行

初始化脚本只会在 MySQL 数据卷首次创建时运行。若已存在旧卷，请先备份数据；确认可删除后再重建数据卷，或手动执行两个服务的 SQL 脚本。

### RabbitMQ 连接失败

应用连接端口应为 `5672`，`15672` 只用于管理台。检查 `.env`、`docker compose ps` 和 RabbitMQ 用户配置是否一致。

### 支付配置报错

本地默认 `ALIPAY_ENABLED=false`。只有联调支付宝沙箱时才启用，并确保应用 ID、商户私钥、支付宝公钥、回调地址和网关地址完整。

### 服务健康但业务失败

健康检查只证明进程和必要依赖可用。继续使用响应中的 `X-Trace-Id` 检索日志，并检查订单、拼团队伍、通知任务与 DLQ 状态是否一致。

## 备份建议

- 演示环境的重要数据至少备份两个 MySQL 库和 RabbitMQ 定义。
- 修改消息拓扑、SQL 或状态机前，先保存可回滚的数据库快照。
- 不要把 `.env`、真实私钥、访问令牌或生产数据提交到 Git。
