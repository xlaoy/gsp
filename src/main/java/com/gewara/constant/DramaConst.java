package com.gewara.constant;

import java.util.ArrayList;
import java.util.List;

public final class DramaConst {

	//��ʱjob
	public static final String SYNCDRAMA = "syncDrama";
	public static final String SYNCDRAMACONFIG = "syncDramaConfig";
	public static final String SYNCONLINEORDER = "syncDramaOnlineOrder";
	public static final String SYNCONLINEREFUND = "syncDramaOnlineRefund";
	public static final String SYNCOFFLINEORDER = "syncDramaOfflineOrder";
	public static final String SYNCOFFLINEREFUND = "syncDramaOfflineRefund";
	public static final String SYNPLACE = "syncDramaPlace";
	public static final String SYNSUPPLIER = "syncDramaSupplier";
	public static final String SYNSPLAYITEM = "syncDramaPlayItem";
	
	//�ݳ�����״̬
	public static final String WAITFINISH = "WAITFINISH";//�����
	public static final String WAITAPPROVAL = "WAITAPPROVAL";//�����
	public static final String HASAPPROVAL = "HASAPPROVAL";//�����
	public static final String SETTLEING = "SETTLEING";//������
	public static final String FINISH = "FINISH";//�ѽ���
	public static final String DISCARD = "DISCARD";//�ѷ���
	
	//
	public static final String Y = "Y";
	public static final String N = "N";
	
	//
	public static final String ORDERTIME = "ORDERTIME"; //�µ�ʱ��
	public static final String USETIME = "USETIME"; //ʹ��ʱ��
	
	
	//�۸��������״̬
	public static final String AVAILABLE = "AVAILABLE";//����
	public static final String EXPIRED = "EXPIRED";//����
	
	//������Դ
	public static final String ONLINE = "ONLINE";//����
	public static final String OFFLINE = "OFFLINE";//����
	public static final String PIAOWU = "PIAOWU";//Ʊ��
	
	//������������
	public static final String SYSDATAJOB = "sysdatajob";
	public static final String PLACE = "place";
	public static final String SUPPLIER = "supplier";
	public static final String REPAYUSER = "repayuser";
	
	//��������
	public static final String TN = "TN";
	public static final String WEEKLY = "WEEKLY";
	public static final String MIDDLE = "MIDDLE";
	public static final String MONTHLY = "MONTHLY";
	public static final String PROGRAM = "PROGRAM";
	public static final String SETTLE = "SETTLE";
	
	//���㵥״̬
	public static final String NEW = "NEW";
	public static final String EXCEPTION = "EXCEPTION";//�����쳣
	public static final String FULFILLING = "FULFILLING";
	public static final String WAITINGPAY = "WAITINGPAY";
	public static final String SETTLED = "SETTLED";
	public static final String INVALID = "INVALID";
	
	//��������
	public static final String SETTLEBILL = "SETTLEBILL"; //���㵥
	public static final String PRICEBILL = "PRICEBILL"; //�۸���˵�
	public static final String TIJIBILL = "TIJIBILL";//��Ƴɱ���
	
	//������
	public static final String CARDTYPE_A = "A";//�һ�ȯ
	public static final String CARDTYPE_B = "B";//����ȯ
	public static final String CARDTYPE_C = "C";//����ȯ
	public static final String CARDTYPE_D = "D";//��ֵȯ
	public static final String SOLDTYPE_M = "M";//����ȯ
	public static final List<String> CARDTYPE_LIST = new ArrayList<String>();
	
	//����ȯ����
	public static final String ECARD = "ecard";//
	public static final String PARTNER = "partner";//
	
	//�ؼ�����
	public static final String PERCENT = "percent";//
	public static final String UPRICE = "uprice";//
	
	//��Ŀ���°汾���ʱ��
	public static final int VERINTERVALDAY = 7;//
	public static final int PUTOFFDAY = 3;//
	
	//ϵͳ��ʼ��������
	public static final String STARTSETTLETIME = "2016-04-01";//
	
	public static final String OK = "ok";//
	
	//��Ŀ��֯����
	public static final String GW = "GW";//������
	public static final String WG = "WG";//΢������
	
	static {
		CARDTYPE_LIST.add(CARDTYPE_B);
		CARDTYPE_LIST.add(CARDTYPE_C);
		CARDTYPE_LIST.add(CARDTYPE_D);
	}
}

