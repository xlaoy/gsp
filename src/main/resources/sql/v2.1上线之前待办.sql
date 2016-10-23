CREATE TABLE gsp.channel_settleconfig(
            record_id INTEGER NOT NULL ,
            vendor_name CHARACTER VARYING(50) ,
            vendor_code CHARACTER VARYING(50) ,
            settle_cycle CHARACTER VARYING(20) ,
            settle_days INTEGER ,
            settle_money FLOAT4 ,
            order_percent FLOAT4 ,
            first_settledate TIMESTAMP without TIME ZONE ,
            create_time TIMESTAMP without TIME ZONE ,
            create_user CHARACTER VARYING(50) ,
            update_time TIMESTAMP without TIME ZONE ,
            update_user CHARACTER VARYING(50) ,
            is_settlere_fund CHARACTER VARYING(10) DEFAULT 'N' ,
            next_exetime TIMESTAMP without TIME ZONE ,
            status CHARACTER VARYING(10) DEFAULT 'Y' ,
            CONSTRAINT channel_settleconfig_key PRIMARY KEY(record_id)
        );
ALTER TABLE  gsp.channel_settleconfig  OWNER TO gsp;
alter table gsp.channel_settleconfig add column verify_status CHARACTER VARYING(10) DEFAULT 'N';

create SEQUENCE gsp.seq_channel_settle_config;
ALTER TABLE  gsp.seq_channel_settle_config  OWNER TO gsp;

CREATE TABLE gsp.vendor_cinema_relation(
            record_id INTEGER NOT NULL ,
            cinema_recordid CHARACTER VARYING(50) ,
            vendor_recordid INTEGER ,
            CONSTRAINT vendor_cinema_relation_key PRIMARY KEY(record_id)
        );
ALTER TABLE  gsp.vendor_cinema_relation  OWNER TO gsp;

ALTER TABLE gsp.gewa_order ADD COLUMN take_time timestamp without time zone;
ALTER TABLE gsp.gewa_order ADD COLUMN is_channel_settled CHARACTER VARYING(10) DEFAULT 'N';
ALTER TABLE gsp.gewa_order ADD COLUMN channel_check_bill_id INTEGER;
ALTER TABLE gsp.refundment ADD COLUMN is_channel_settled CHARACTER VARYING(10) DEFAULT 'N';
ALTER TABLE gsp.refundment ADD COLUMN channel_check_bill_id INTEGER;
ALTER TABLE gsp.refundment ADD COLUMN channel_settle_bill_id INTEGER;
ALTER TABLE gsp.settle_config add COLUMN is_Goods_Settle CHAR(1) DEFAULT 'N';


ALTER TABLE gsp.adjustment ADD COLUMN channel_settle_bill_id INTEGER;
ALTER TABLE gsp.adjustment ADD COLUMN is_channel_settled CHARACTER VARYING(10) DEFAULT 'N';