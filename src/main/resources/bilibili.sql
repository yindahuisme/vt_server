-- 玩家头像状态表
CREATE TABLE IF NOT EXISTS bilibili.`user_ico_state`(
   `user_name` VARCHAR(128) NOT NULL,-- 玩家名
   `update_date` VARCHAR(32) NOT null,-- 头像最近下载日期
   PRIMARY KEY ( `user_name` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 玩家功勋历史记录
CREATE TABLE IF NOT EXISTS bilibili.`user_feat_his`(
   `user_name` VARCHAR(128) NOT NULL,
   `update_time` VARCHAR(32) NOT null,
   `feat` int NOT null,
   PRIMARY KEY ( `user_name` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;