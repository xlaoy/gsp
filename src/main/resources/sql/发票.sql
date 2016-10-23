CREATE TABLE gsp.invoice_config (
recordid int4 NOT NULL,
placeid varchar(20) COLLATE default,
vendercode varchar(20) COLLATE default,
vendername varchar(1000) COLLATE default,
taxrate numeric(10,2),
invoicetype varchar(10) COLLATE default,
taxcondition varchar(10) COLLATE default,
updatetime timestamp(6)
);

ALTER TABLE gsp.invoice_config ADD PRIMARY KEY (recordid);

/*********/
ALTER TABLE "gsp"."invoice_config" ADD COLUMN "content" varchar(1000);


--����߱��ֶ�
create or replace view view4js_ticket_order as
select RECORDID, TRADE_NO, CATEGORY, HFHPASS, DESCRIPTION2, STATUS, ORDER_TYPE, CINEMAID,
RELATEDID, MOVIEID, ADDTIME, PLAYTIME, QUANTITY, TOTALCOST, SETTLE, UPDATETIME, OTHERINFO,
paidtime, memberid, otherfee, discount, discount_reason, wabi
FROM TICKET_ORDER;

--��Ӷ������ֶ�
ALTER TABLE "gsp"."gewa_order"
ADD COLUMN "wabi" numeric(10,2),
ADD COLUMN "discount" numeric(10,2);

--�½����㵥��չ��
CREATE TABLE "gsp"."settlebill_extend" (
"recordid" int4 NOT NULL,
"billingamount" numeric(10,2),
"bcprepay" numeric(10,2),
"bcbucha" numeric(10,2)
);
ALTER TABLE "gsp"."settlebill_extend" ADD PRIMARY KEY ("recordid");

--����ն��˵���Ʊ����ֶ�
ALTER TABLE "gsp"."check_bill"
ADD COLUMN "billingamount" numeric(10,2);


/*************/
CREATE TABLE "gsp"."invoice" (
"invoicecode" varchar(50) COLLATE "default" NOT NULL,
"invoicetype" varchar(20) COLLATE "default",
"invoicedate" timestamp(6),
"addtime" timestamp(6),
"vendercode" varchar(20) COLLATE "default",
"vendername" varchar(500) COLLATE "default",
"taxrate" numeric(10,2),
"taxamount" numeric(10,2),
"invoiceamount" numeric(10,2),
"status" varchar(20) COLLATE "default",
"content" varchar(1000) COLLATE "default",
"settleids" varchar(2000) COLLATE "default",
"deductibleamount" numeric(10,2),
"optuser" varchar(50) COLLATE "default",
"updatetime" timestamp(6),
"excludetaxamount" numeric(10,2)
);
ALTER TABLE "gsp"."invoice" ADD PRIMARY KEY ("invoicecode");



CREATE TABLE "gsp"."invoice_settlerelate" (
"recordid" int4 NOT NULL,
"invoicecode" varchar(50) COLLATE "default",
"settleid" int4,
"placeid" varchar(50) COLLATE "default"
);
ALTER TABLE "gsp"."invoice_settlerelate" ADD PRIMARY KEY ("recordid");

----------
create or replace view view4js_ticket_order as
select RECORDID, TRADE_NO, CATEGORY, HFHPASS, DESCRIPTION2, STATUS, ORDER_TYPE, CINEMAID,
RELATEDID, MOVIEID, ADDTIME, PLAYTIME, QUANTITY, TOTALCOST, SETTLE, UPDATETIME, OTHERINFO,
paidtime, memberid, otherfee, discount, discount_reason, wabi, ALIPAID, GEWAPAID
FROM TICKET_ORDER;
--
ALTER TABLE "gsp"."gewa_order" RENAME "wabi" TO "alipaid";


--- --- --- 6-23 --- --- ---
ALTER TABLE "gsp"."settlement_bill"
DROP COLUMN "bc_prepay",
DROP COLUMN "bc_bucha",
ADD COLUMN "has_jizhang" varchar(10);

update gsp.settlement_bill set has_jizhang = 'N';

ALTER TABLE "gsp"."invoice"
ADD COLUMN "dikoumonth" varchar(10);

ALTER TABLE "gsp"."invoice_settlerelate"
ADD COLUMN "vendercode" varchar(20);

CREATE TABLE "gsp"."settle_jiti" (
"recordid" int4 NOT NULL,
"placename" varchar(500) COLLATE "default",
"vendername" varchar(500) COLLATE "default",
"vendercode" varchar(20) COLLATE "default",
"billtype" varchar(20) COLLATE "default",
"starttime" timestamp(6),
"endtime" timestamp(6),
"playtype" varchar(20) COLLATE "default",
"special" varchar(20) COLLATE "default",
"num" int4,
"amount" numeric(10,2),
"kpamount" numeric(10,2),
"taxamount" numeric(10,2),
"exclutax" numeric(10,2),
"taxrate" numeric(10,2),
"updatetime" timestamp(6)
);
ALTER TABLE "gsp"."settle_jiti" ADD PRIMARY KEY ("recordid");


--------6.30
ALTER TABLE "gsp"."channel_settleconfig"
ADD COLUMN "syscode" varchar(100),
ADD COLUMN "timecut" varchar(20),
ADD COLUMN "settletime" varchar(20);

