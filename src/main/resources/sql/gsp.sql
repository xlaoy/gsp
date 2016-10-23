CREATE TABLE gsp.joblock
(
  jobname character varying(80) NOT NULL,
  firetime character varying(20),
  ip character varying(15),
  status character varying(10),
  nextfire character varying(20) NOT NULL,
  CONSTRAINT pk_joblock PRIMARY KEY (jobname , nextfire)
);


-- Table: gsp.adjustment

-- DROP TABLE gsp.adjustment;

CREATE TABLE gsp.adjustment
(
  record_id integer,
  tag character varying(10),
  relate_id character varying(30),
  amount integer,
  comments character varying, -- 500
  operator character varying(25),
  add_time timestamp without time zone,
  status character varying(10),
  is_settled character varying(10),
  check_bill_id integer
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.adjustment
  OWNER TO gsp;
COMMENT ON COLUMN gsp.adjustment.comments IS '500';

-- Table: gsp.check_bill

-- DROP TABLE gsp.check_bill;

CREATE TABLE gsp.check_bill
(
  record_id integer NOT NULL,
  endtime timestamp without time zone,
  starttime timestamp without time zone,
  place_type character varying(10), -- 影院 or 运动 or 演出
  place_id integer,
  status character varying(10),
  order_total_number integer,
  order_total_amount integer,
  refund_total_number integer,
  refund_total_amount integer,
  adjust_total_number integer,
  adjust_total_amount integer,
  config_id character varying(50),
  CONSTRAINT check_bill_pkey PRIMARY KEY (record_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.check_bill
  OWNER TO gsp;
COMMENT ON TABLE gsp.check_bill
  IS '对账单';
COMMENT ON COLUMN gsp.check_bill.place_type IS '影院 or 运动 or 演出';

-- Table: gsp.gewa_order

-- DROP TABLE gsp.gewa_order;

CREATE TABLE gsp.gewa_order
(
  tradeno character varying(25) NOT NULL,
  place_id bigint,
  item_id bigint,
  play_id bigint,
  deal_time timestamp without time zone,
  use_time timestamp without time zone, -- 消费时间  （场次时间， 使用时间等）
  quantity integer,
  total_cost integer,
  is_settled character varying(10),
  order_type character varying(10),
  check_bill_id bigint,
  order_status character varying(20),
  is_adjusted character varying(10),
  CONSTRAINT gewa_order_pkey PRIMARY KEY (tradeno )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.gewa_order
  OWNER TO gsp;
COMMENT ON COLUMN gsp.gewa_order.use_time IS '消费时间  （场次时间， 使用时间等）';

-- Table: gsp.gewaconfig

-- DROP TABLE gsp.gewaconfig;

CREATE TABLE gsp.gewaconfig
(
  ckey character varying(15) NOT NULL,
  description character varying(100) NOT NULL,
  updatetime timestamp without time zone NOT NULL,
  content character varying(2000),
  CONSTRAINT pk_gewaconfig PRIMARY KEY (ckey )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.gewaconfig
  OWNER TO gsp;
-- Table: gsp.place

-- DROP TABLE gsp.place;

CREATE TABLE gsp.place
(
  place_id integer, -- 业务系统中的主键
  place_type character varying(10),
  city_code character varying(10),
  name character varying(30),
  update_time timestamp without time zone,
  belong_to integer, -- 隶属于某商户ID
  record_id character varying(50) NOT NULL,
  is_config character varying(10),
  CONSTRAINT place_pkey PRIMARY KEY (record_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.place
  OWNER TO gsp;
COMMENT ON COLUMN gsp.place.place_id IS '业务系统中的主键';
COMMENT ON COLUMN gsp.place.belong_to IS '隶属于某商户ID';

-- Table: gsp.refundment

-- DROP TABLE gsp.refundment;

CREATE TABLE gsp.refundment
(
  tradeno character varying(25) NOT NULL,
  order_type character varying(10),
  refund_time timestamp without time zone,
  old_settle integer,
  new_settle integer,
  is_settled character varying(10),
  check_bill_id integer,
  place_type character varying,
  relate_id integer,
  CONSTRAINT refundment_pkey PRIMARY KEY (tradeno )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.refundment
  OWNER TO gsp;
-- Table: gsp.settle_config

-- DROP TABLE gsp.settle_config;

CREATE TABLE gsp.settle_config
(
  place_type character varying(10),
  place_id integer,
  settle_cycle character varying(10),
  settle_value integer,
  offset_value integer,
  settle_base character varying, -- 基于下单时间 or 消费时间
  first_settle timestamp without time zone,
  status character varying(10),
  record_id character varying(50) NOT NULL,
  settle_time character varying(15),
  CONSTRAINT settle_config_pkey PRIMARY KEY (record_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.settle_config
  OWNER TO gsp;
COMMENT ON COLUMN gsp.settle_config.settle_base IS '基于下单时间 or 消费时间';

-- Table: gsp.settlement_bill

-- DROP TABLE gsp.settlement_bill;

CREATE TABLE gsp.settlement_bill
(
  record_id integer,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  relate_id integer,
  order_total_number integer,
  order_total_amount integer,
  refund_total_number integer,
  refund_total_amount integer,
  adjust_total_number integer,
  adjust_total_amount integer,
  status character varying(10),
  update_time timestamp without time zone,
  last_operator character varying(25),
  config_id character varying(50),
  tag character varying
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.settlement_bill
  OWNER TO gsp;
-- Table: gsp.sync_mark

-- DROP TABLE gsp.sync_mark;

CREATE TABLE gsp.sync_mark
(
  tag character varying(50),
  last_execute_time timestamp without time zone,
  modify_time timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gsp.sync_mark
  OWNER TO gsp;
