-- pet-platform MySQL 8 initialization script
-- Recommended database name: pet_platform
-- Character set: utf8mb4

CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='管理员表';

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT '用户' COMMENT '角色',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    intro VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_status (status),
    INDEX idx_user_create_time (create_time)
) COMMENT='前台用户表';

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='宠物分类表';

CREATE TABLE pet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '宠物名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    breed VARCHAR(100) DEFAULT NULL COMMENT '品种',
    age INT DEFAULT NULL COMMENT '年龄（月）',
    gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    description TEXT COMMENT '描述',
    health_status VARCHAR(100) DEFAULT NULL COMMENT '健康状态',
    vaccine_info VARCHAR(255) DEFAULT NULL COMMENT '疫苗信息',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_pet_category_id (category_id),
    INDEX idx_pet_status (status),
    INDEX idx_pet_create_time (create_time)
) COMMENT='宠物表';

CREATE TABLE post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1已通过 2已驳回',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_post_user_id (user_id),
    INDEX idx_post_status (status),
    INDEX idx_post_create_time (create_time)
) COMMENT='帖子表';

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pet_id BIGINT NOT NULL COMMENT '宠物ID',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1待处理 2已完成 3已取消 4已退款',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    address VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_pet_id (pet_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_create_time (create_time)
) COMMENT='订单表';

CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type VARCHAR(30) NOT NULL COMMENT '通知类型：new_order/refund/appointment/deliver',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_notice_is_read (is_read),
    INDEX idx_notice_type (type),
    INDEX idx_notice_create_time (create_time)
) COMMENT='后台通知表';

CREATE TABLE user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    tag VARCHAR(30) DEFAULT NULL COMMENT '地址标签',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址：1是 0否',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_address_user_id (user_id),
    INDEX idx_user_address_is_default (is_default)
) COMMENT='用户收货地址表';

CREATE TABLE order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    old_status TINYINT DEFAULT NULL COMMENT '旧状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID（管理员）',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人名称',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_order_status_log_order_id (order_id)
) COMMENT='订单状态流转日志表';

ALTER TABLE pet
    ADD CONSTRAINT fk_pet_category_id FOREIGN KEY (category_id) REFERENCES category(id);

ALTER TABLE post
    ADD CONSTRAINT fk_post_user_id FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES user(id),
    ADD CONSTRAINT fk_orders_pet_id FOREIGN KEY (pet_id) REFERENCES pet(id);

ALTER TABLE user_address
    ADD CONSTRAINT fk_user_address_user_id FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE notice
    ADD CONSTRAINT fk_notice_order_id FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE order_status_log
    ADD CONSTRAINT fk_order_status_log_order_id FOREIGN KEY (order_id) REFERENCES orders(id);

INSERT INTO admin_user (username, password, nickname, phone, email, avatar, status, last_login_time)
VALUES
('admin', '$2a$10$abcdefghijklmnopqrstuv', '超级管理员', '13800000001', 'admin@pethub.com', 'https://via.placeholder.com/60', 1, '2026-04-09 09:00:00'),
('reviewer', '$2a$10$abcdefghijklmnopqrstuv', '审核员', '13800000002', 'reviewer@pethub.com', 'https://via.placeholder.com/60', 1, '2026-04-09 08:30:00');

INSERT INTO user (avatar, username, password, phone, email, role, status, intro, create_time)
VALUES
('https://via.placeholder.com/60', 'xiaoshuai', '$2a$10$abcdefghijklmnopqrstuv', '13800138000', 'test@qq.com', '用户', 1, '喜欢养猫和狗', '2026-04-02 14:00:00'),
('https://via.placeholder.com/60', 'xiaomei', '$2a$10$abcdefghijklmnopqrstuv', '13800138001', 'xiaomei@qq.com', '用户', 1, '最近在研究布偶猫', '2026-04-03 10:20:00'),
('https://via.placeholder.com/60', 'laowang', '$2a$10$abcdefghijklmnopqrstuv', '13800138002', 'laowang@qq.com', '用户', 0, '家里有一只金毛', '2026-04-04 09:15:00');

INSERT INTO category (name, status, remark, create_time)
VALUES
('犬类', 1, '狗狗分类', '2026-04-02 12:00:00'),
('猫类', 1, '猫咪分类', '2026-04-02 12:10:00'),
('鸟类', 1, '鸟类分类', '2026-04-02 12:20:00'),
('其他', 0, '其他宠物分类', '2026-04-02 12:30:00');

INSERT INTO pet (name, category_id, breed, age, gender, price, stock, cover_url, description, health_status, vaccine_info, status, create_time)
VALUES
('金毛', 1, '金毛犬', 12, '公', 3000.00, 1, 'https://via.placeholder.com/60', '性格温顺，适合家庭饲养', '健康', '已接种三针', 1, '2026-04-02 10:00:00'),
('布偶猫', 2, '布偶猫', 8, '母', 4500.00, 2, 'https://via.placeholder.com/60', '颜值高，性格粘人', '健康', '已完成基础疫苗', 1, '2026-04-03 11:00:00'),
('虎皮鹦鹉', 3, '虎皮鹦鹉', 6, '公', 399.00, 5, 'https://via.placeholder.com/60', '活泼好动，适合新手', '健康', '无需疫苗', 0, '2026-04-04 15:30:00');

INSERT INTO post (user_id, cover_url, title, content, like_count, comment_count, status, reject_reason, create_time)
VALUES
(1, 'https://via.placeholder.com/80', '第一次养金毛要注意什么？', '想问一下大家，第一次养金毛需要准备哪些东西？', 25, 8, 0, NULL, '2026-04-02 15:00:00'),
(2, 'https://via.placeholder.com/80', '布偶猫掉毛严重吗？', '最近想养布偶猫，想先了解一下掉毛情况。', 18, 5, 1, NULL, '2026-04-03 16:10:00'),
(3, 'https://via.placeholder.com/80', '宠物店购买注意事项', '第一次去宠物店买宠物，有哪些坑需要避开？', 9, 2, 2, '内容质量较低，请补充更多细节', '2026-04-04 19:20:00');

INSERT INTO orders (order_no, user_id, pet_id, amount, status, contact_name, contact_phone, address, remark, create_time)
VALUES
('PET202604020001', 1, 1, 3000.00, 1, '黄同学', '13800138000', '北京市朝阳区xx路xx号', '周末联系我', '2026-04-02 16:00:00'),
('PET202604030001', 2, 2, 4500.00, 2, '李同学', '13800138001', '上海市浦东新区xx路xx号', '工作日晚上可联系', '2026-04-03 13:30:00'),
('PET202604040001', 3, 3, 399.00, 4, '王先生', '13800138002', '广州市天河区xx路xx号', '尽快处理退款', '2026-04-04 18:40:00');

INSERT INTO notice (type, title, content, is_read, order_id, create_time)
VALUES
('new_order', '您有一笔新订单待处理', '用户 xiaoshuai 提交了金毛订单，订单号：PET202604020001', 0, 1, '2026-04-02 18:30:00'),
('deliver', '订单已进入待处理状态', '订单 PET202604020001 需要尽快确认并联系用户', 0, 1, '2026-04-02 18:40:00'),
('refund', '您有一笔退款申请待处理', '订单 PET202604040001 发起退款申请，请尽快处理', 1, 3, '2026-04-04 19:00:00');

INSERT INTO order_status_log (order_id, old_status, new_status, operator_id, operator_name, remark, create_time)
VALUES
(1, 0, 1, 1, 'admin', '管理员确认订单，进入待处理', '2026-04-02 16:10:00'),
(2, 1, 2, 1, 'admin', '订单已完成交付', '2026-04-03 17:00:00'),
(3, 3, 4, 2, 'reviewer', '用户申请退款，审核通过', '2026-04-04 19:10:00');

CREATE TABLE ai_chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '会话标题',
    last_message_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_ai_chat_session_user_id (user_id),
    INDEX idx_ai_chat_session_last_message_time (last_message_time)
) COMMENT='AI 会话表';

CREATE TABLE ai_chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色 user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    images TEXT DEFAULT NULL COMMENT '图片 JSON 数组',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_ai_chat_message_session_id (session_id),
    INDEX idx_ai_chat_message_create_time (create_time)
) COMMENT='AI 会话消息表';

ALTER TABLE ai_chat_session
    ADD CONSTRAINT fk_ai_chat_session_user_id FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE ai_chat_message
    ADD CONSTRAINT fk_ai_chat_message_session_id FOREIGN KEY (session_id) REFERENCES ai_chat_session(id);

SET FOREIGN_KEY_CHECKS = 1;
