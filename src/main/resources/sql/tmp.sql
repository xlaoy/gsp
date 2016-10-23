ALTER TABLE "gsp"."place" ADD COLUMN "owner" varchar(50), ADD COLUMN "theatres" varchar(20);

ALTER TABLE "gsp"."drama_orderonline_item" ADD COLUMN "refundpricebillid" int4;
ALTER TABLE "gsp"."drama_orderoffline_item" ADD COLUMN "refundpricebillid" int4;

ALTER TABLE "gsp"."autoreconciledinfo" ALTER COLUMN "remark" TYPE varchar(500) COLLATE "default";


CREATE SEQUENCE "gsp"."seq_newautorecen"
 INCREMENT 1
 MINVALUE 1000
 MAXVALUE 99999999999999999
 START 1000
 CACHE 100;

CREATE TABLE "gsp"."autorecon_menchtemp" (
"recordid" int4 NOT NULL,
"settleid" int4,
"tradeno" varchar(50),
"recencode" varchar(50),
"num" numeric(10,0),
"price" numeric(10,2),
"amount" numeric(10,2),
PRIMARY KEY ("recordid")
);

CREATE TABLE "gsp"."autorecon_menchorder" (
"recordid" int4 NOT NULL,
"settleid" int4,
"tradeno" varchar(50),
"recencode" varchar(50),
"num" int4,
"price" numeric(10,2),
"amount" numeric(10,2),
PRIMARY KEY ("recordid")
);

CREATE TABLE "gsp"."autorecon_gworder" (
"recordid" int4 NOT NULL,
"settleid" int4,
"tradeno" varchar(50),
"recencode" varchar(50),
"num" int4,
"oprice" numeric(10,2),
"oamount" numeric(10,2),
"yprice" numeric(10,2),
"yamount" numeric(10,2),
"price" numeric(10,2),
"amount" numeric(10,2),
PRIMARY KEY ("recordid")
);

CREATE TABLE "gsp"."autorecon_result" (
"recordid" int4 NOT NULL,
"settleid" int4,
"recencode" varchar(50),
"num" int4,
"mtradeno" varchar(50),
"mprice" numeric(10,2),
"mamount" numeric(10,2),
"gtradeno" varchar(50),
"oprice" numeric(10,2),
"oamount" numeric(10,2),
"yprice" numeric(10,2),
"yamount" numeric(10,2),
"gprice" numeric(10,2),
"gamount" numeric(10,2),
PRIMARY KEY ("recordid")
);

delete from autoreconciledinfo;

---
ALTER TABLE "gsp"."diff_price_order"
DROP COLUMN "field1",
DROP COLUMN "field2";

ALTER TABLE "gsp"."diff_price_order"
ADD COLUMN "refund" varchar(10),
ADD COLUMN "refund_settleid" int4;

ALTER TABLE "gsp"."gewa_order"
ALTER COLUMN "reldiscount" TYPE numeric(10,2),
ADD COLUMN "placeallowance" numeric(10,2);

--
ALTER TABLE "gsp"."settlebill_extend"
ADD COLUMN "placeallowance" numeric(10,2);

ALTER TABLE "gsp"."check_bill"
ADD COLUMN "placeallowance" numeric(10,2);






