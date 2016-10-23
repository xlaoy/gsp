--创建lastmax_endtime表
CREATE TABLE gsp.lastmax_endtime (
config_id varchar(50) COLLATE "default" NOT NULL,
tag varchar(50) COLLATE "default",
last_settle_id int4,
last_check_id int4,
last_max_endtime timestamp(6),
update_time timestamp(6)
);

ALTER TABLE gsp.lastmax_endtime ADD PRIMARY KEY (config_id);
