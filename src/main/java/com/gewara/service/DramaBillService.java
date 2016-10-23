package com.gewara.service;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.gewara.model.acl.User;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaBaseData;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;



public interface DramaBillService {

	//�ݳ���ѯ
	ModelMap queryDrama(Drama drama, Long dramaversionid, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//�ݳ����ò�ѯ
	ModelMap queryDramaConfig(DramaConfig config, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//����ͨ������
	String passeDramConfig(List<DramaConfig> configList);
	
	//��ѯ��������
	ModelMap queryDramaBaseData(DramaBaseData dbData, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//�ݳ����㵥��ѯ
	ModelMap queryDramaSettleBill(DramaSettleBill bill, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//�ݳ����㵥�۸��˵���ѯ
	ModelMap queryDramaPriceBill(Long recordid, ModelMap model);
	
	//����
	void payBill(List<DramaSettleBill> billList, String optUser);
	
	//���¸���
	String rePayBill(Long recordid, String optUser);
	
	//��ѯ��
	ModelMap queryReqMoenyBill(DramaSettleBill bill, User user, ModelMap model);
	
	//��������
	ModelMap exportOrder(Long recordid, ModelMap model);
	
	//�����˵�
	ModelMap exportRefund(Long recordid, ModelMap model);
	
	//���������Ż�
	ModelMap exportDiscount(Long recordid, ModelMap model);
	
	//����������Ŀ
	ModelMap exportOrderItem(Long recordid, ModelMap model);
	
	//��ѯ����
	ModelMap queryDramaOrder(String tradeno, String orderform, ModelMap model);
	
	//��Ƴɱ�����
	ModelMap queryJitiBill(DramaJitiBill bill, Integer pageNo, Integer pageSize, String url, String isXls, ModelMap model);
}
