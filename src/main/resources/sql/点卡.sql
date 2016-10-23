CREATE OR REPLACE VIEW WEBDATA.VIEW_PARTNER_CARD_ORDER AS 
select recordid, partnerid, outtradeno, tradeno, status, relatedid, cardno, cardpass, orderamount, carduseamount,
outamount, ordertime, addtime, updatetime, cardtype, ordertype, cinemaid, cinemaname, movieid,
moviename, edition, movieitemid, roomid, playroom, playtime, ticketnum from WEBDATA.PARTNER_CARD_ORDER;

grant select on WEBDATA.VIEW_PARTNER_CARD_ORDER to gspview;

CREATE OR REPLACE VIEW WEBDATA.VIEW_PARTNER_CARD_PAYMENT AS 
SELECT recordid, partnerid, partnerkey, outtradeno, tradeno, tradetime, relatedid, cardno, cardpass, cardtype, amount, tradetype,
refundtime, refundtype, refundreason FROM WEBDATA.PARTNER_CARD_PAYMENT;

grant select on WEBDATA.VIEW_PARTNER_CARD_PAYMENT to gspview;

INSERT INTO gsp.sync_mark ("tag", "last_execute_time", "modify_time") 
VALUES ('syncPointCardOrder', '2016-09-10 00:00:00', '2016-09-10 00:00:00');


CREATE TABLE "gsp"."pointcard_order" (
"recordid" int4 NOT NULL,
"partnerid" int4,
"outtradeno" varchar(50) COLLATE "default",
"tradeno" varchar(50) COLLATE "default",
"status" varchar(20) COLLATE "default",
"relatedid" int4,
"cardno" varchar(20) COLLATE "default",
"orderamount" numeric(10,2),
"outamount" numeric(10,2),
"carduseamount" numeric(10,2),
"ordertime" timestamp(6),
"updatetime" timestamp(6),
"cardtype" varchar(20) COLLATE "default",
"ordertype" varchar(20) COLLATE "default",
"cinemaid" int4,
"cinemaname" varchar(500) COLLATE "default",
"ticketnum" int4,
"playtime" timestamp(6),
"otherinfo" varchar(1000) COLLATE "default"
);
CREATE UNIQUE INDEX "UK_CARD_ORDER_TRADENO" ON "gsp"."pointcard_order" USING btree (tradeno);
CREATE UNIQUE INDEX "UK_PARTNER_ORDER_TRADENO" ON "gsp"."pointcard_order" USING btree (partnerid, outtradeno);
ALTER TABLE "gsp"."pointcard_order" ADD PRIMARY KEY ("recordid");



CREATE TABLE "gsp"."pointcard_placebill" (
"recordid" int4 NOT NULL,
"cinemaid" int4,
"ctype" varchar(10) COLLATE "default",
"starttime" timestamp(6),
"endtime" timestamp(6),
"successnum" int4,
"successamount" numeric(10,2),
"refundnum" int4,
"refundamount" numeric(10,2),
"totalnum" int4,
"totalamount" numeric(10,2),
"kpamount" numeric(10,2),
"cinemaname" varchar(500) COLLATE "default",
"relatedbillid" int4,
"partnerid" int4
);
ALTER TABLE "gsp"."pointcard_placebill" ADD PRIMARY KEY ("recordid");


CREATE TABLE "gsp"."pointcard_settlebill" (
"recordid" int4 NOT NULL,
"ctype" varchar(10) COLLATE "default",
"starttime" timestamp(6),
"endtime" timestamp(6),
"successnum" int4,
"successamount" numeric(10,2),
"refundnum" int4,
"refundamount" numeric(10,2),
"totalnum" int4,
"totalamount" numeric(10,2),
"kpamount" numeric(10,2),
"relatedbillid" int4,
"partnerid" int4
);
ALTER TABLE "gsp"."pointcard_settlebill" ADD PRIMARY KEY ("recordid");

