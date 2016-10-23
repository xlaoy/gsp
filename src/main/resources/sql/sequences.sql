-- Sequence: gsp.seq_check_bill

-- DROP SEQUENCE gsp.seq_check_bill;

CREATE SEQUENCE gsp.seq_check_bill
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1222
  CACHE 1;
ALTER TABLE gsp.seq_check_bill
  OWNER TO gsp;
  
-- Sequence: gsp.seq_settlement_bill

-- DROP SEQUENCE gsp.seq_settlement_bill;

CREATE SEQUENCE gsp.seq_settlement_bill
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 3
  CACHE 1;
ALTER TABLE gsp.seq_settlement_bill
  OWNER TO gsp;

