begin;
update gewa_order set is_settled= 'N', check_bill_id = null where check_bill_id in(
					select record_id from check_bill where settlement_id in(
						select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00'  order by end_time desc));

update refundment set is_settled= 'N', check_bill_id = null, settle_bill_id = null where check_bill_id in(
					select record_id from check_bill where settlement_id in(
						select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00'  order by end_time desc));

update adjustment set is_settled = 'N', settle_bill_id = null where settlement_id in(
					select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00'  order by end_time desc);
					
delete from check_bill where record_id in(
					select record_id from check_bill where settlement_id in(
						select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00'  order by end_time desc));
						
						
delete  from settlement_bill where record_id in (select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00' order by end_time desc);

create table gewa_order_backup as select * from gewa_order where check_bill_id in(
										select record_id from check_bill where settlement_id in(
											select record_id from settlement_bill where status = 'GEWACONFIRMED' and end_time >= '2014-04-01 00:00:00'))
											
											
update gewa_order o set is_settled= 'N', check_bill_id = null 
	where o.deal_time > '2014-02-01' and not exists (select record_id from check_bill cb where cb.record_id = o.check_bill_id) 
	
	
update refundment re set is_settled= 'N', check_bill_id = null, settle_bill_id = null 
	where not exists (select record_id from check_bill cb where cb.record_id = re.check_bill_id);

update check_bill set order_total_number = 1, order_total_amount = 25 where record_id = 157465;
update check_bill set order_total_number = 2, order_total_amount = 50 where record_id = 157910;
update check_bill set order_total_number = 2, order_total_amount = 50 where record_id = 161581;
update check_bill set order_total_number = 2, order_total_amount = 50 where record_id = 164335;
update check_bill set order_total_number = 2, order_total_amount = 50 where record_id = 164784;
update check_bill set order_total_number = 4, order_total_amount = 100 where record_id = 165682;
update settlement_bill set order_total_number = 22,order_total_amount=550 where record_id = 6037;
update check_bill set order_total_number = 2, order_total_amount =120 where record_id = 168472
update settlement_bill set order_total_number = 5, order_total_amount = 340 where record_id = 6076

											
