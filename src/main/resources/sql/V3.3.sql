ALTER TABLE gsp.download_recorder
ADD COLUMN amount varchar(50),
ADD COLUMN venderno varchar(50),
ADD COLUMN status varchar(20)
ADD COLUMN opt_user varchar(100);


ALTER TABLE gsp.meger_pay
ALTER COLUMN total_amount TYPE numeric(10,2);

update  gsp.download_recorder set special = 'SETTLE' where bill_type = 'PAYBILL' and (special = '' or special is null);

ALTER TABLE gsp.baochang
ALTER COLUMN success_amount TYPE numeric(10,2),
ADD COLUMN pre_amount numeric(10,2),
ADD COLUMN pre_pay varchar(10);

ALTER TABLE gsp.settlement_bill
ALTER COLUMN bc_prepay TYPE numeric(10,2),
ADD COLUMN bc_bucha numeric(10,2);

--´ýÖ´ÐÐ
CREATE TABLE gsp.bc_req_bill (
record_id int4 NOT NULL,
pjt_code varchar(50) COLLATE "default",
related_id varchar(50) COLLATE "default",
movie_name varchar(500) COLLATE "default",
pjt_desc varchar(500) COLLATE "default",
vendor_no varchar(50) COLLATE "default",
vendor_name varchar(500) COLLATE "default",
amount numeric(10,2),
opt_user varchar(100) COLLATE "default",
req_time timestamp(6),
place_id varchar(50)
);
ALTER TABLE gsp.bc_req_bill ADD PRIMARY KEY (record_id);


ALTER TABLE gsp.baochang
ADD COLUMN reqbill_status varchar(10),
ADD COLUMN pjt_code varchar(500);

update gsp.baochang set reqbill_status = 'N';

ALTER TABLE gsp.gewa_order
ADD COLUMN user_baochang varchar(10);





