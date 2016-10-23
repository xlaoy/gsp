--修改settlement_bill表字段
ALTER TABLE gsp.settlement_bill
ADD COLUMN play_type varchar(100),
ADD COLUMN bc_prepay numeric(10, 2);
COMMENT ON COLUMN gsp.settlement_bill.play_type IS '场次类型';
COMMENT ON COLUMN gsp.settlement_bill.bc_prepay IS '包场预付费';

--修改check_bill表字段
ALTER TABLE gsp.check_bill
ADD COLUMN play_type varchar(100);
COMMENT ON COLUMN gsp.check_bill.play_type IS '场次类型';

--修改数据
update gsp.settlement_bill set play_type = 'ZL' where tag = 'TICKET';

--修改download_recorder表字段
ALTER TABLE gsp.download_recorder
ADD COLUMN special varchar(50);
COMMENT ON COLUMN gsp.download_recorder.special IS '特殊标示';

--创建包场表
CREATE TABLE gsp.baochang (
record_id int4 NOT NULL,
place_id varchar(50) COLLATE "default",
dept_name varchar(200) COLLATE "default",
play_id int4,
play_type varchar(50) COLLATE "default",
film_name varchar(500) COLLATE "default",
play_time timestamp(6),
bc_num numeric(10),
bc_amount numeric(10, 2),
success_num numeric(10),
success_amount numeric(10, 2),
success_rate numeric(10, 2),
status varchar(50) COLLATE "default",
settle_id int4,
update_time timestamp(6),
bc_type varchar(50) COLLATE "default",
pay_time timestamp(6),
opt_user varchar(100) COLLATE "default",
special  varchar(100) COLLATE "default"
);

COMMENT ON COLUMN gsp.baochang.record_id IS '包场id';
COMMENT ON COLUMN gsp.baochang.place_id IS '影院id';
COMMENT ON COLUMN gsp.baochang.dept_name IS '部门名称';
COMMENT ON COLUMN gsp.baochang.play_id IS '场次id';
COMMENT ON COLUMN gsp.baochang.play_type IS '场次类型';
COMMENT ON COLUMN gsp.baochang.film_name IS '电影名称';
COMMENT ON COLUMN gsp.baochang.play_time IS '放映时间';
COMMENT ON COLUMN gsp.baochang.bc_num IS '包场票数';
COMMENT ON COLUMN gsp.baochang.bc_amount IS '包场金额';
COMMENT ON COLUMN gsp.baochang.success_num IS '实际出票数量';
COMMENT ON COLUMN gsp.baochang.success_amount IS '订单结算成本';
COMMENT ON COLUMN gsp.baochang.success_rate IS '出票完成率';
COMMENT ON COLUMN gsp.baochang.status IS '状态';
COMMENT ON COLUMN gsp.baochang.settle_id IS '关联结算单号';
COMMENT ON COLUMN gsp.baochang.bc_type IS '包场类型';
COMMENT ON COLUMN gsp.baochang.pay_time IS '付款时间';
COMMENT ON COLUMN gsp.baochang.opt_user IS '付款人';

ALTER TABLE gsp.baochang ADD PRIMARY KEY (record_id);

--添加同步标志
insert into gsp.sync_mark values ('sysBaoChang', '2016-01-01', null);

---------
ALTER TABLE "gsp"."baochang"
ADD COLUMN "user_amount" numeric(10,2),
ADD COLUMN "total_num" numeric(10),
ADD COLUMN "refund_num" numeric(10),
ADD COLUMN "refund_amount" numeric(10,2),
ADD COLUMN "kpamount" numeric(10,2);


--------
ALTER TABLE "gsp"."baochang"
ADD COLUMN "goods_num" int4,
ADD COLUMN "goods_amount" numeric(10),
ADD COLUMN "goodsSettleId" int4;

CREATE OR REPLACE VIEW VIEW_BAOCHANG_OPI AS
SELECT RECORDID,MPID,MOVIEID,MOVIENAME,CINEMAID,CINEMANAME,PREAMOUNT,PREPAY,ADDTIME,UPDATETIME,ROOMNAME,PLAYTIME,OPENTYPE,TYPE,SEATNUM,AMOUNT,ISBAOCHANG,GOODSAMOUNT,GOODSMONEY 
FROM WEBDATA.BAOCHANG_OPI;


ALTER TABLE "gsp"."gewa_order"
ADD COLUMN "reldiscount" numeric(10);

