1.
	Adjustment 表执行下列语句（字段已添加）：
	--update adjustment set update_time = add_time;
	
	--update adjustment set settle_bill_id = cast(relate_id as integer) where tag = 'SETTLEBILL';
	
2.
	升级已结算的退款字段， 升级项：settlebillid
	--/upgradeRefund.xhtml
	
3.
	升级settlement bill 字段 previous_refund_number
	--update settlement_bill set previous_refund_number = 0 where previous_refund_number is null;
	
4.
	提交所有未确认的结算单
	/autoToMerchant.xhtml
	
5.
	更新refundment adjustment的version字段为0
	
	--update adjustment set version = 0 where version is null;
	
	--update refundment set version = 0 where version is null;
	
6. 
	settle Config 增加以下字段;
	--ALTER TABLE settle_config ADD COLUMN vender_no character varying(50);

	--ALTER TABLE settle_config ADD COLUMN vender_name character varying(50);
	--执行 settleconfig-upgrade.sql 升级字段
	
