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
VALUES ('0344', '�����»�����Ӱ��-�����µ�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '14000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0350', '�����»�����Ӱ��-���˵�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '6000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0371', '�����»�����Ӱ��-��ʢ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '5000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0372', '�����»�����Ӱ��-��ɽ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '6000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0406', '���ݷ���Ӱ��-��ӳǵ�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0407', '���ݷ���Ӱ��-���ѵ�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0422', '������ӰԺ��-ͬ�µ�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0717', 'ƽ����ϷԺ', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0718', '��خ�����Ļ����ĵ�ӰԺ', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '8000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0727', '���ݽ����ܹ�˾', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '600000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0733', '������ϵͳ', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0753', '���ݷ���Ӱ��-�ַ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '30000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0788', '����˼�ܹ���Ӱ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0883', '�麣�����', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '1000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0907', '���ݷ������Ӱ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0909', '���Ժ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '400000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('0910', '��˼��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1150', '���ڹ���Ӱ��-��ɳ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '3000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1553', '�������ɹ���Ӱ��-����ص�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '2000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1582', '���Ժ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '0.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1772', '��ĻӰ��(����ʱ����õ꣩', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '30000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('1963', '17.5Ժ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '200000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('2162', '�Ͼ��½ֿڹ���Ӱ��', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '25000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
INSERT INTO gsp.rd_prevent (vendor_code, vendor_name, total_amount, canuse_amount, waitingpay_amount, waitingpay_relation, uncount_amount, uncount_relation, surplus_amount, waring_amount, status, start_time, end_time, update_time) 
VALUES ('2460', '���ݷ���Ӱ��-�ߵµ�', '0.00', '0.00', '0.00', '', '0.00', '', '0.00', '20000.00', 'N', '2016-3-1 00:00:00', '2016-3-1 00:00:00', '2016-3-1 00:00:00');
