--��������
CREATE SEQUENCE gsp.seq_autorecon;

--����
--�Զ����˻�����Ϣ��
CREATE TABLE gsp.autoreconciledinfo
(
   record_id integer,
   settlement_id integer,
   file_name character varying(80),
   file_path character varying(100),
   open_type character varying(10),
   status character varying(20),
   place_id character varying(20),
   remark character varying(50),
   upload_time timestamp without time zone,
   upload_user character varying(20),
   update_time timestamp without time zone,
   update_user character varying(20),
   gewa_ordernumber integer,
   gewa_orderamount integer,
   merchant_ordernumber integer,
   merchant_orderamount integer,
   CONSTRAINT pk_autorecon PRIMARY KEY (record_id),
   CONSTRAINT uni_settlementid UNIQUE (settlement_id)
) ;


--���˽��ʵ��
create sequence gsp.seq_autoanalysobject;

CREATE TABLE gsp.autoanalysobject
(
   record_id integer,
   settlement_id integer,
   movie_name character varying(50),
   movie_hall character varying(20),
   use_date character varying(10),
   use_time character varying(10),
   show_date character varying(10),
   show_time character varying(10),
   votes integer,
   total_price integer,
   recon_code character varying(50),
   order_status character varying(10),
   remark character varying(50),
   CONSTRAINT pk_autoanalysobject PRIMARY KEY (record_id)
);



--�޸ĵ�����
ALTER TABLE gsp.adjustment ADD COLUMN attachfile_path character varying(100);


--U8��������û�ӳ������
create sequence gsp.seq_gewauser_mapping_u8;

CREATE TABLE gsp.gewa_user_mapping_u8
(
   record_id integer,
   gewa_login_name character varying(50),
   gewa_real_name character varying(50),
   create_time timestamp without time zone,
   create_user character varying(20),
   update_time timestamp without time zone,
   update_user character varying(20),
   CONSTRAINT pk_gewa_u8_map PRIMARY KEY (record_id),
   CONSTRAINT uni_login_name UNIQUE (gewa_login_name)
) ;

--���ݳ�ʼ��
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'yuanyuan.kang', '��ԲԲ', now(), 'system', now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'qi.chen', '����', now(), 'system', now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'ruomian.liang', '������', now(), 'system',now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'ying.qiu', '��Ӣ', now(), 'system', now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'jing.zu', '�澲', now(), 'system', now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'jacker.cheng', '�̼���', now(), 'system',now(), 'system');
INSERT INTO gsp.gewa_user_mapping_u8
(record_id, gewa_login_name, gewa_real_name, create_time, create_user, update_time, update_user)
VALUES(nextval('seq_gewauser_mapping_u8'), 'yueting.tian', '��h��', now(), 'system', now(), 'system');



CREATE TABLE gsp.settle_config_upd
(
  place_type character varying(10),
  place_id integer,
  settle_cycle character varying(10),
  settle_value integer,
  offset_value integer,
  settle_base character varying, -- �����µ�ʱ�� or ����ʱ��
  first_settle timestamp without time zone,
  status character varying(10),
  record_id character varying(50) NOT NULL,
  settle_time character varying(15),
  zhangtao_no character varying(50),
  open_type character varying(20),
  vender_no character varying(50),
  vender_name character varying(50),
  apply_user character varying(50),
  approve_user character varying(50),
  is_goods_settle character(1) DEFAULT 'N',
  operate_type character varying(10),
  CONSTRAINT settle_config_upd_pkey PRIMARY KEY (record_id )
);
