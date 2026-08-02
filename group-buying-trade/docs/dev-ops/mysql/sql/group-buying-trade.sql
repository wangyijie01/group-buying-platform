# Community Group Buying Platform - trade schema
# The seed rows below are synthetic and contain no payment credentials.

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `group-buying-trade` DEFAULT CHARACTER SET utf8mb4;
USE `group-buying-trade`;

DROP TABLE IF EXISTS `pay_order`;

CREATE TABLE `pay_order` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `product_id` varchar(16) NOT NULL COMMENT '商品ID',
  `product_name` varchar(64) NOT NULL COMMENT '商品名称',
  `order_id` varchar(16) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_amount` decimal(8,2) unsigned DEFAULT NULL COMMENT '订单金额',
  `status` varchar(32) NOT NULL COMMENT '订单状态：create、pay_wait、pay_success、deal_done、close',
  `pay_url` varchar(2014) DEFAULT NULL COMMENT '支付跳转信息',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `market_type` tinyint(1) DEFAULT NULL COMMENT '营销类型：0无营销、1拼团营销',
  `market_deduction_amount` decimal(8,2) DEFAULT NULL COMMENT '营销优惠金额',
  `pay_amount` decimal(8,2) NOT NULL COMMENT '实付金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_product_id` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `pay_order` WRITE;
/*!40000 ALTER TABLE `pay_order` DISABLE KEYS */;

INSERT INTO `pay_order`
  (`id`, `user_id`, `product_id`, `product_name`, `order_id`, `order_time`, `total_amount`, `status`, `pay_url`, `pay_time`, `market_type`, `market_deduction_amount`, `pay_amount`, `create_time`, `update_time`)
VALUES
  (1, 'demo-user-001', '100001', '社区生鲜体验装', 'DEMO20250802001', '2025-08-02 10:00:00', 100.00, 'pay_wait', NULL, NULL, 1, 10.00, 90.00, '2025-08-02 10:00:00', '2025-08-02 10:00:00'),
  (2, 'demo-user-002', '100001', '社区生鲜体验装', 'DEMO20250802002', '2025-08-02 10:05:00', 100.00, 'pay_success', NULL, '2025-08-02 10:06:00', 1, 10.00, 90.00, '2025-08-02 10:05:00', '2025-08-02 10:06:00');

/*!40000 ALTER TABLE `pay_order` ENABLE KEYS */;
UNLOCK TABLES;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
