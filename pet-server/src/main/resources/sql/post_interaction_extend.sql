CREATE TABLE post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_post_like_post_user (post_id, user_id),
    INDEX idx_post_like_post_id (post_id),
    INDEX idx_post_like_user_id (user_id)
) COMMENT='帖子点赞表';

CREATE TABLE post_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_post_comment_post_id (post_id),
    INDEX idx_post_comment_user_id (user_id)
) COMMENT='帖子评论表';

ALTER TABLE post_like
    ADD CONSTRAINT fk_post_like_post_id FOREIGN KEY (post_id) REFERENCES post(id),
    ADD CONSTRAINT fk_post_like_user_id FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE post_comment
    ADD CONSTRAINT fk_post_comment_post_id FOREIGN KEY (post_id) REFERENCES post(id),
    ADD CONSTRAINT fk_post_comment_user_id FOREIGN KEY (user_id) REFERENCES user(id);
