update gsp.download_recorder set download_count = 0 where settlement_id in (10274,10277,10355,10369,9014,9214,9960) and bill_type='PAYBILL' and download_count = 1;

update gsp.download_recorder set max_down_count = max_down_count + 1 where settlement_id = 9960 and bill_type = 'PAYBILL';

update gsp.download_recorder set max_down_count = max_down_count + 1 where settlement_id in(10704,10704) and bill_type = 'PAYBILL';

delete from gsp.adjustment where record_id = 667;