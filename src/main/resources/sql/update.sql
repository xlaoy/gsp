update refundment set old_settle =  90 where tradeno = '1130630150921256';
update refundment set old_settle =  86 where tradeno = '1130707001314129';
update refundment set old_settle =  55 where tradeno = '1130720083752329';
update refundment set old_settle =  90 where tradeno = '1130727100043972';
update refundment set old_settle =  120 where tradeno = '1130809214245137';
update refundment set old_settle =  66 where tradeno = '1130811113940733';


ALTER TABLE gsp.gewa_order ALTER COLUMN outer_id TYPE character varying(150);
ALTER TABLE gsp.gewa_order_his ALTER COLUMN outer_id TYPE character varying(150);

