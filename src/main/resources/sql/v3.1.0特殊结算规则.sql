--修改settle_config表
ALTER TABLE gsp.settle_config
ADD COLUMN play_type varchar(50),
ADD COLUMN pay_vender_no varchar(50),
ADD COLUMN pay_vender_name varchar(50);

update gsp.settle_config set play_type = 'N', pay_vender_no = vender_no, pay_vender_name = vender_name;

--修改settle_config_upd表
ALTER TABLE gsp.settle_config_upd
ADD COLUMN play_type varchar(50),
ADD COLUMN pay_vender_no varchar(50),
ADD COLUMN pay_vender_name varchar(50);

update gsp.settle_config_upd set play_type = 'N', pay_vender_no = vender_no, pay_vender_name = vender_name;


--修改gewa_order表
ALTER TABLE gsp.gewa_order
ADD COLUMN special varchar(100);

--修改check_bill
ALTER TABLE gsp.check_bill
ADD COLUMN special varchar(100);

--修改settlement_bill
ALTER TABLE gsp.settlement_bill
ADD COLUMN special varchar(100);



--创建合并付款记录表meger_pay
CREATE TABLE gsp.meger_pay (
min_settle_billid int4 NOT NULL,
query_start_time timestamp(6),
query_end_time timestamp(6),
pay_vender_no varchar(50),
pay_time timestamp(6),
relate_settle_billid varchar(1000),
total_amount numeric(10),
opt_user varchar(50)
);

ALTER TABLE gsp.meger_pay ADD PRIMARY KEY (min_settle_billid);
