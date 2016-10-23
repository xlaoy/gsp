--sync_mark插入数据
insert into sync_mark values(
'sysDiffPriceOrder',
'2015-09-14 00:00:00',
null
);

--创建异价订单表
DROP TABLE IF EXISTS "gsp"."diff_price_order";
CREATE TABLE "gsp"."diff_price_order" (
"trade_no" varchar(25) COLLATE "default" NOT NULL,
"actual_price" numeric DEFAULT 0,
"quantity" int4 DEFAULT 0,
"update_time" timestamp(6),
"mp_id" int4 DEFAULT 0,
"check_bill_id" int4 DEFAULT 0,
"settlt_bill_id" int4 DEFAULT 0,
"field1" varchar COLLATE "default",
"field2" varchar COLLATE "default"
);

COMMENT ON COLUMN "gsp"."diff_price_order"."trade_no" IS '订单号';
COMMENT ON COLUMN "gsp"."diff_price_order"."actual_price" IS '实际结算价';
COMMENT ON COLUMN "gsp"."diff_price_order"."quantity" IS '票数';
COMMENT ON COLUMN "gsp"."diff_price_order"."update_time" IS '更新时间';
COMMENT ON COLUMN "gsp"."diff_price_order"."mp_id" IS '场次id';
COMMENT ON COLUMN "gsp"."diff_price_order"."check_bill_id" IS '影票日账单单号';
COMMENT ON COLUMN "gsp"."diff_price_order"."settlt_bill_id" IS '影票结算单单号';

ALTER TABLE "gsp"."diff_price_order" ADD PRIMARY KEY ("trade_no");

--创建异价账单表
DROP TABLE IF EXISTS "gsp"."diff_price_bill";
CREATE TABLE "gsp"."diff_price_bill" (
"check_bill_id" int4 DEFAULT 0 NOT NULL,
"settlt_bill_id" int4 DEFAULT 0,
"start_time" timestamp(6),
"end_time" timestamp(6),
"diff_order_number" int4 DEFAULT 0,
"diff_refund_number" int4 DEFAULT 0,
"diff_amount" numeric(20) DEFAULT 0,
"descm" varchar COLLATE "default"
);

COMMENT ON COLUMN "gsp"."diff_price_bill"."check_bill_id" IS '影票日账单单号';
COMMENT ON COLUMN "gsp"."diff_price_bill"."settlt_bill_id" IS '影票结算单单号';
COMMENT ON COLUMN "gsp"."diff_price_bill"."start_time" IS '对账周期开始时间';
COMMENT ON COLUMN "gsp"."diff_price_bill"."end_time" IS '对账周期结束时间';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_order_number" IS '异价票数量';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_refund_number" IS '异价票退票数量';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_amount" IS '差额';
COMMENT ON COLUMN "gsp"."diff_price_bill"."descm" IS '备注';

ALTER TABLE "gsp"."diff_price_bill" ADD PRIMARY KEY ("check_bill_id");


--添加对账单表字段
ALTER TABLE "gsp"."check_bill"
ADD COLUMN "diff_price_number" numeric DEFAULT 0,
ADD COLUMN "diff_price_amount" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_number" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_amount" numeric DEFAULT 0;

COMMENT ON COLUMN "gsp"."check_bill"."diff_price_number" IS '异价总票数';

COMMENT ON COLUMN "gsp"."check_bill"."diff_price_amount" IS '异价总金额';

COMMENT ON COLUMN "gsp"."check_bill"."succ_ticket_number" IS '卖出总票数';

COMMENT ON COLUMN "gsp"."check_bill"."succ_ticket_amount" IS '卖出总金额';

--添加结算单表字段
ALTER TABLE "gsp"."settlement_bill"
ADD COLUMN "diff_price_number" numeric DEFAULT 0,
ADD COLUMN "diff_price_amount" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_number" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_amount" numeric DEFAULT 0;

COMMENT ON COLUMN "gsp"."settlement_bill"."diff_price_number" IS '异价总票数';

COMMENT ON COLUMN "gsp"."settlement_bill"."diff_price_amount" IS '异价总金额';

COMMENT ON COLUMN "gsp"."settlement_bill"."succ_ticket_number" IS '卖出总票数';

COMMENT ON COLUMN "gsp"."settlement_bill"."succ_ticket_amount" IS '卖出总金额';


--添加院线关系表字段
ALTER TABLE "gsp"."vendor_cinema_relation"
ADD COLUMN "create_time" timestamp;

COMMENT ON COLUMN "gsp"."vendor_cinema_relation"."create_time" IS '创建时间';



--更新院线创建时间字段
update vendor_cinema_relation set create_time = '2015-09-30';
