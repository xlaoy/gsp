package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;


public interface DramaDoColleService {

	//�ֶ�ͬ���ݳ�
	String syncDramaByDramaid(List<Long> dramaidlist);
	
	//�ֶ�ͬ������
	String syncPlayItemByDramaid(List<Long> dramaidlist);
	
	//�ֶ�ͬ�����ð汾
	String syncDramConfigByDramaId(Long dramaid);
	
	//����ͬ���ݳ���Ŀ�汾����
	void updateDrama(List<Map<String, Object>> mapList);
	
	//����ͬ���ݳ���Ŀ���ð汾����
	String processDramaVersionConfig(Map<String, Object> configmap, boolean sysjob);
	
	//�����ݳ�������Ϣ
	String updateDramConfig(DramaConfig dconfig, boolean sysjob);
	
	//���ɽ��㵥��
	String createSettleBill(DramaConfig config);
	
	//���ó�ʼ������
	void initDramaBill(DramaSettleBill bill);
	
	//���㵥��
	String fullSettleBill(DramaSettleBill bill, boolean sysjob);
	
	//���㵥��
	String fullJitiBill(DramaJitiBill bill);
}
