--sync_mark��������
insert into sync_mark values(
'sysDiffPriceOrder',
'2015-09-14 00:00:00',
null
);

--������۶�����
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

COMMENT ON COLUMN "gsp"."diff_price_order"."trade_no" IS '������';
COMMENT ON COLUMN "gsp"."diff_price_order"."actual_price" IS 'ʵ�ʽ����';
COMMENT ON COLUMN "gsp"."diff_price_order"."quantity" IS 'Ʊ��';
COMMENT ON COLUMN "gsp"."diff_price_order"."update_time" IS '����ʱ��';
COMMENT ON COLUMN "gsp"."diff_price_order"."mp_id" IS '����id';
COMMENT ON COLUMN "gsp"."diff_price_order"."check_bill_id" IS 'ӰƱ���˵�����';
COMMENT ON COLUMN "gsp"."diff_price_order"."settlt_bill_id" IS 'ӰƱ���㵥����';

ALTER TABLE "gsp"."diff_price_order" ADD PRIMARY KEY ("trade_no");

--��������˵���
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

COMMENT ON COLUMN "gsp"."diff_price_bill"."check_bill_id" IS 'ӰƱ���˵�����';
COMMENT ON COLUMN "gsp"."diff_price_bill"."settlt_bill_id" IS 'ӰƱ���㵥����';
COMMENT ON COLUMN "gsp"."diff_price_bill"."start_time" IS '�������ڿ�ʼʱ��';
COMMENT ON COLUMN "gsp"."diff_price_bill"."end_time" IS '�������ڽ���ʱ��';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_order_number" IS '���Ʊ����';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_refund_number" IS '���Ʊ��Ʊ����';
COMMENT ON COLUMN "gsp"."diff_price_bill"."diff_amount" IS '���';
COMMENT ON COLUMN "gsp"."diff_price_bill"."descm" IS '��ע';

ALTER TABLE "gsp"."diff_price_bill" ADD PRIMARY KEY ("check_bill_id");


--��Ӷ��˵����ֶ�
ALTER TABLE "gsp"."check_bill"
ADD COLUMN "diff_price_number" numeric DEFAULT 0,
ADD COLUMN "diff_price_amount" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_number" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_amount" numeric DEFAULT 0;

COMMENT ON COLUMN "gsp"."check_bill"."diff_price_number" IS '�����Ʊ��';

COMMENT ON COLUMN "gsp"."check_bill"."diff_price_amount" IS '����ܽ��';

COMMENT ON COLUMN "gsp"."check_bill"."succ_ticket_number" IS '������Ʊ��';

COMMENT ON COLUMN "gsp"."check_bill"."succ_ticket_amount" IS '�����ܽ��';

--��ӽ��㵥���ֶ�
ALTER TABLE "gsp"."settlement_bill"
ADD COLUMN "diff_price_number" numeric DEFAULT 0,
ADD COLUMN "diff_price_amount" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_number" numeric DEFAULT 0,
ADD COLUMN "succ_ticket_amount" numeric DEFAULT 0;

COMMENT ON COLUMN "gsp"."settlement_bill"."diff_price_number" IS '�����Ʊ��';

COMMENT ON COLUMN "gsp"."settlement_bill"."diff_price_amount" IS '����ܽ��';

COMMENT ON COLUMN "gsp"."settlement_bill"."succ_ticket_number" IS '������Ʊ��';

COMMENT ON COLUMN "gsp"."settlement_bill"."succ_ticket_amount" IS '�����ܽ��';


--���Ժ�߹�ϵ���ֶ�
ALTER TABLE "gsp"."vendor_cinema_relation"
ADD COLUMN "create_time" timestamp;

COMMENT ON COLUMN "gsp"."vendor_cinema_relation"."create_time" IS '����ʱ��';



--����Ժ�ߴ���ʱ���ֶ�
update vendor_cinema_relation set create_time = '2015-09-30';
