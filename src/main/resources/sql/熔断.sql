CREATE TABLE gsp.rd_prevent (
vendor_code varchar(50) NOT NULL,
vendor_name varchar(500) COLLATE "default",
total_amount numeric(20,2),
canuse_amount numeric(20,2),
waitingpay_amount numeric(20,2),
waitingpay_relation text COLLATE "default",
uncount_amount numeric(20,2),
uncount_relation text COLLATE "default",
surplus_amount numeric(20,2),
waring_amount numeric(20,2),
status varchar(50) COLLATE "default",
start_time timestamp(6),
end_time timestamp(6),
update_time timestamp(6)
);
ALTER TABLE gsp.rd_prevent ADD PRIMARY KEY (vendor_code);


CREATE TABLE gsp.rd_pay (
record_id int4 NOT NULL,
vendor_code varchar(50) COLLATE "default",
vendor_name varchar(500) COLLATE "default",
pay_before numeric(20,2),
pay_amount numeric(20,2),
pay_after numeric(20,2),
pay_time timestamp(6),
opt_user varchar(50) COLLATE "default",
pay_way varchar(10) COLLATE "default",
pay_content text
);
ALTER TABLE gsp.rd_pay ADD PRIMARY KEY (record_id);


CREATE TABLE gsp.rd_buckle (
record_id int4 NOT NULL,
related_id int4,
place_id varchar(50) COLLATE "default",
place_name varchar(500) COLLATE "default",
special varchar(100) COLLATE "default",
cycle varchar(200) COLLATE "default",
confirm_time timestamp(6),
buckle_time timestamp(6),
buckle_before numeric(20,2),
buckle_amount numeric(20,2),
buckle_after numeric(20,2),
vendor_code varchar(50) COLLATE "default",
vendor_name varchar(500) COLLATE "default"
);
ALTER TABLE gsp.rd_buckle ADD PRIMARY KEY (record_id);


CREATE TABLE gsp.rd_daycount (
record_id int4 NOT NULL,
vendor_code varchar(50) COLLATE "default",
vendor_name varchar(500) COLLATE "default",
start_time timestamp(6),
end_time timestamp(6),
amount numeric(20,2),
num int4
);
ALTER TABLE gsp.rd_daycount ADD PRIMARY KEY (record_id);


INSERT INTO gsp.gsp_job (tag, status, update_time) VALUES ('rd-autoTomerchant', 'FINISH', '2016-03-04');


INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0344', '北京新华国际影城-大钟寺店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '14000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0350', '北京新华国际影城-大兴店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '6000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0371', '北京新华国际影城-宝盛店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '5000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0372', '北京新华国际影城-房山店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '6000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0406', '广州飞扬影城-天河城店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0407', '广州飞扬影城-正佳店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0422', '保利电影院线-同德店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0717', '平安大戏院', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0718', '番禺市桥文化中心电影院', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '8000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0727', '广州金逸总公司', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '600000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0733', '满天星系统', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0753', '广州飞扬影城-乐峰店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '30000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0788', '广州思哲国际影城', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0883', '珠海大会堂', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '1000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0907', '广州泛洋国际影城', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0909', '横店院线', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '400000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0910', '沃思达', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1150', '万众国际影城-南沙店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '3000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1553', '西安博纳国际影城-新天地店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1582', '万达院线', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1772', '巨幕影城(光谷资本大厦店）', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '30000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1963', '17.5院线', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '200000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('2162', '南京新街口国际影城', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '25000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('2460', '广州飞扬影城-高德店', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
