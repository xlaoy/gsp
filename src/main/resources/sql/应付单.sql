--添加download_recorder表字段
ALTER TABLE gsp.download_recorder
ADD COLUMN field_1 numeric(10) DEFAULT 0;

COMMENT ON COLUMN gsp.download_recorder.field_1 IS '暂估数据的冲销金额';

--添加channel_settleconfig表字段
ALTER TABLE gsp.channel_settleconfig
ADD COLUMN pjt_code varchar(10);

COMMENT ON COLUMN gsp.channel_settleconfig.pjt_code IS '项目编号';