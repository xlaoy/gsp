--���download_recorder���ֶ�
ALTER TABLE gsp.download_recorder
ADD COLUMN field_1 numeric(10) DEFAULT 0;

COMMENT ON COLUMN gsp.download_recorder.field_1 IS '�ݹ����ݵĳ������';

--���channel_settleconfig���ֶ�
ALTER TABLE gsp.channel_settleconfig
ADD COLUMN pjt_code varchar(10);

COMMENT ON COLUMN gsp.channel_settleconfig.pjt_code IS '��Ŀ���';