package com.gewara.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.gewara.enums.CheckBillStatusEnums;

/**
 * 
 * @ClassName: SettleStatusConstant  
 * @Description:����״̬��¼
 * @author yujun.su@gewara.com
 * @date 2014-9-28 ����10:51:01
 */
public class SettleConstant {
	
	public static final String hostname = "db41.gewara.cn";
	/***
	 * ����ʱ������
	 */
	
	public static final String SETTLE_BASE_ORDERTIME = "ORDERTIME";
	public static final String SETTLE_BASE_USETIME = "USETIME";
	/**
	 * ��Ʒ����״̬����
	 */
	public static final String IS_GOODS_SETTLE_Y = "Y";
	/**
	 * ��Ʒ����״̬����
	 */
	public static final String IS_GOODS_SETTLE_N = "N";
	
	/**
	 * ��ѯ��Ʒ������ϸ��������Դ
	 * CB����checkbill�����˵�
	 */
	public static final String CB_GOODS_DETAIL_FROM = "CB";
	/**
	 * ��ѯ��Ʒ������ϸ��������Դ
	 * SB����settleBill�����㵥
	 */	
	public static final String SB_GOODS_DETAIL_FROM = "SB";
	
	/**
	 * ͨ���ѽ��㣬����
	 */
	public static final String CHANNEL_CYCLE_SETTLE_MONTH = "MONTH";
	
	/**
	 * ͨ���ѽ��� ��������
	 */
	public static final String CHANNEL_CYCLE_SETTLE_DAYS = "DAYS";
	
	/**
	 * ���㵥����
	 * 
	 */
	public static final String TAG_SETTLEMENTBILL_CHANNEL = "CHANNEL";
	
	public static final String TAG_SETTLEMENTBILL_GOODS = "GOODS";
	
	public static final String TAG_SETTLEMENTBILL_TICKET = "TICKET";
	
	/**
	 * �Ƿ���Ч
	 */
	/**
	 * ��Ч
	 */
	public static final String STATUS_VALID = "Y";
	
	/**
	 * ��Ч
	 */
	public static final String STATUS_INVALID = "N";
	
	/**
	 * ��������
	 */
	public final static String ORDER_TYPE_TICKET = "ticket";
	public final static String ORDER_TYPE_GOODS = "goods";
	
	public final static String CHANNEL_SETTLE_METHOD_TICKET = "TICKET";
	public final static String CHANNEL_SETTLE_METHOD_ORDER = "ORDER";
	
	public final static String SUBMIT_MARK = "submit_mark";
	/**
	 * ����regionname
	 */
	public final static String GSP_CACHE_REGIONNAME = "service";
	
	/**
	 * �Զ�����״̬����
	 */
	public static final String SYSTEM = "system";
	
	public static final String HFH = "HFH";
	public static final String MTX = "MTX";
	public static final String STPF = "STPF";
//	public static final String JY = "JY";
	public static final String MJY = "MJY";
//	public static final String XFLH = "XFLH";
	public static final String XFLH2 = "XFLH2";
	
	public static final List<String> OPEN_TYPES = Arrays.asList(new String[]{HFH,MTX,STPF,XFLH2,MJY});
	//����״̬
	public static final Map<String,String> AUTORECONSTATUSMAP = new HashMap<String,String>();
	
	//���˶�
	public static final String AUTO_RECON_STATUS_WAIT_PROCESS = "WAIT_PROCESS";
	
	//�˶���
	public static final String AUTO_RECON_STATUS_PROCESSING = "PROCESSING";
	
	//�˶����
	public static final String AUTO_RECON_STATUS_FINISH = "FINISH";
	
	//����ʧ��
	public static final String AUTO_RECON_STATUS_FAILURE = "FAILURE";
	
	
	
	//�������˽��
	//����
	public static final String AUTO_RECON_RESULT_NORMAL = "NORMAL";
	//�쳣
	public static final String AUTO_RECON_RESULT_ABNORMAL = "ABNORMAL";
	
	/**
	 * ͨ���ѹ�Ӧ������
	 */
	/**
	 * ϵͳ��
	 */
	public static final String CHANNEL_VENDOR_SYS = "SYS";
	
	/**
	 * Ժ��
	 */
	public static final String CHANNEL_VENDOR_THEATRES = "THEATRES";
	
	/**
	 * ӰԺ
	 */
	public static final String CHANNEL_VENDOR_CINEMA = "CINEMA";
	
	
	//��Ʊ״̬
	public static final String INVOSTANEW = "INVOSTANEW";//�����
	public static final String INVOSTAPASS = "INVOSTAPASS";//���ͨ��
	public static final String INVOSTAUNPASS = "INVOSTAUNPASS";//��˲�ͨ��
	public static final String INVOSTAAUTH = "INVOSTAAUTH";//����֤
	public static final String INVOSTAUNAUTH = "INVOSTAUNAUTH";//��֤ʧ��
	public static final String INVOSTADIKOU = "INVOSTADIKOU";//�ѵֿ�
	public static final String INVOSTAUNDIKOU = "INVOSTAUNDIKOU";//�ֿ�ʧ��
	
	public static final Map<String,String> BILLTYPEMAP = new HashMap<String,String>();
	public static final Map<String,String> NOTICKETSETTLEBILLSTATUSMAP = new HashMap<String,String>();
	public static final Map<String,String> CHANNELVENDORTYPE = new TreeMap<String,String>();
	public static final Set<String> UNSETTLECATE = new HashSet<String>();
	public static final Map<String,String> INVOICESTATUSMAP = new HashMap<String,String>();
	
	public static final List<String> WDCATE = new ArrayList<String>(); 
	
	static{
		
		WDCATE.add("WD2");
		WDCATE.add("WD3");
		
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_WAIT_PROCESS, "������");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_PROCESSING, "������");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_FINISH, "�������");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_FAILURE, "����ʧ��");
		
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_TICKET, "ӰƱ���㵥");
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_GOODS, "��Ʒ���㵥");
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_CHANNEL, "ͨ���ѽ��㵥");
		
		/**
		 * ��ӰƱ���㵥״̬��ʾ
		 */
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.GEWACONFIRMED.getStatus(), "���̻��������");
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.READJUST.getStatus(), "�����������");
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.WAITINGPAY.getStatus(), CheckBillStatusEnums.WAITINGPAY.getDisplay());
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.SETTLED.getStatus(),CheckBillStatusEnums.SETTLED.getDisplay());
		
		/**
		 * ͨ���̼�������ʾ
		 */
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_SYS, "ϵͳ��");
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_THEATRES, "Ժ��");
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_CINEMA, "ӰԺ");
		
		
		UNSETTLECATE.add("PNX");
		UNSETTLECATE.add("GEWA");
		UNSETTLECATE.add("GPTBS");
		UNSETTLECATE.add("TXPC");
		UNSETTLECATE.add("WP");
		
		INVOICESTATUSMAP.put(INVOSTANEW, "�����");
		INVOICESTATUSMAP.put(INVOSTAPASS, "���ͨ��");
		INVOICESTATUSMAP.put(INVOSTAUNPASS, "��˲�ͨ��");
		INVOICESTATUSMAP.put(INVOSTAAUTH, "����֤");
		INVOICESTATUSMAP.put(INVOSTAUNAUTH, "��֤ʧ��");
		INVOICESTATUSMAP.put(INVOSTADIKOU, "�ѵֿ�");
		INVOICESTATUSMAP.put(INVOSTAUNDIKOU, "�ֿ�ʧ��");
	}
	
	/**
	 * �쳣ԭ��
	 */
	public static final String AUTO_RECON_REASON_NOTFOUND = "�������޴˶���";
	
	public static final String AUTO_RECON_REASON_UNKNOWN = "δ֪ԭ��";
	
	public static final String AUTO_RECON_REASON_SETTLED = "�˶����ѽ���,���㵥��Ϊ��{0}";
	
	public static final String AUTO_RECON_REASON_NOTRANGE = "�������ڽ���������";
	
	public static final String AUTO_RECON_REASON_REFUND = "����������Ʊ";
	
	public static final String AUTO_RECON_REASON_MOREORD = "һ���������Ӧ�������";
	
	public static final String AUTO_RECON_REASON_NORECONCODE = "�̻�����û�ж�����";
	
	public static final String AUTO_RECON_REASON_NQVOTES = "Ʊ����һ��";
	
	public static final String AUTO_RECON_REASON_NQAMOUNT = "�����һ��";

	/**
	 * �����������Դ
	 */
	public static final String OPERATE_TYPE_SYNC = "SYNC";
	
	public static final String OPERATE_TYPE_ADD = "ADD";
	
	public static final String OPERATE_TYPE_MODIFY = "MODIFY";
	
	/**
	 * ��Ʒ ͨ���ѽ���״̬����
	 */
	
	public static final String APPLY_SETTLE_WAITAPPLY = "WAITAPPLY";
	
	public static final String APPLY_SETTLE_CHECKING = "CHECKING";
	
	public static final String APPLY_SETTLE_WAITPAY = "WAITPAY";
	
	public static final String APPLY_SETTLE_PAYED = "PAYED";
	
	/**
	 * ��������
	 */
	public static final String ADJUST_TYPE_SETTLEMENT = "SETTLEBILL";
	
	public static final String ADJUST_TYPE_PLACE = "PLACE";
	
	/**
	 * ���״̬
	 */
	public static final String ADJUST_STATUS_APPROVED = "APPROVED"; 
	
	public static final String ADJUST_STATUS_REJECTED = "REJECTED";
	
	public static final String ADJUST_STATUS_NEW = "NEW";
	
	/**
	 * ͨ������
	 */
	public static final String CHANNEL_OPERATE_TYPE_ADD = "ADD";
	
	public static final String CHANNEL_OPERATE_TYPE_DEL = "DEL";
	
	public static final String CHANNEL_OPERATE_TYPE_UPDATE = "UPDATE";
	
	/**
	 * ������
	 */
	/**
	 * ӰԺ
	 */
	public static final String REQ_BILL_PLACE = "PLACE";
	/**
	 * ϵͳ��-Ժ��
	 */
	public static final String REQ_BILL_SYS_THRE = "SYS_THRE";
	
	/**
	 * SFTP�ֶ�
	 */
	public static final String SFTP_REQ_HOST = "host";
	public static final String SFTP_REQ_PORT = "port";
	public static final String SFTP_REQ_USERNAME = "username";
	public static final String SFTP_REQ_PASSWORD = "password";
	public static final int SFTP_DEFAULT_PORT = 22;
	
	public static final String COMM_Y = "Y";
	public static final String COMM_N = "N";
	
	/**
	 * ��������־
	 */
	public static final String WANDA_ACTIVITY = "WANDA_ACTIVITY"; //�����
	public static final String WANDA_NOT_ACTIVITY = "WANDA_NOT_ACTIVITY"; //���ǻ��
	public static final Long HEPING_OUTER_PLACEID = Long.valueOf(299977977); //��ƽ�ⲿӰԺid
	
	/**
	 * ��������
	 */
	public static final String ZL = "ZL"; //ֱ����
	public static final String XN = "XN"; //���ⳡ
	public static final String GW = "GW"; //����Ʊ��
	public static final String WP = "WP"; //����Ʊ��
	
	/**
	 * ���ʽ
	 */
	public static final String BC = "BC"; //����
	public static final String MEGER = "MEGER"; //�ϲ�
	public static final String SETTLE = "SETTLE"; //���㵥����
	public static final String RONGDUAN = "RONGDUAN"; //�۶�
	public static final String DRAMA = "DRAMA"; //�۶�
	
	public static final String HEPING_ROMNAME = "IMAX"; //
	
	/**
	 * �����б�����״̬
	 */
	public static final String NOPAY = "NOPAY"; //δ����
	public static final String YESPAY = "YESPAY"; //�Ѹ���
	public static final String SELLET = "SELLET"; //�ѽ���
	public static final String PASS = "PASS"; //�ѹ���
	
	/**
	 * ���ѶԽ�����״̬
	 */
	public static final String JSSUBMIT = "JSSUBMIT"; //���ύ
	public static final String YYGET = "YYGET"; //�����ѻ�ȡ
	public static final String YYCONFIRM = "YYCONFIRM"; //������ȷ��
	public static final String YYPAYSECCUSS = "YYPAYSECCUSS"; //���Ѹ���ɹ�
	
	/**
	 * job״̬
	 */
	public static final String RUNNING = "RUNNING"; //
	public static final String FINISH = "FINISH"; //
	
	/**
	 * ͨ���ѽ��㣬����
	 */
	public static final String MONTH = "MONTH";
	
	/**
	 * ͨ���ѽ��� ��������
	 */
	public static final String DAY = "DAY";
	public static final String WPDAY = "WPDAY";
	
	//ӰԺϵͳ��������
	public static final String SYSCATEGORY = "SYSCATEGORY";
	public static final String MEGERPAYVENDER = "MEGERPAYVENDER";
	public static final String INVOICEADMIN = "INVOICEADMIN";
	public static final String RESETADJUST = "RESETADJUST";
	
	//
	public static final String ZYFP = "ZYFP";
	public static final String PTFP = "PTFP";
	
	public static final String XGMNSR = "XGMNSR";
	public static final String YBNSR = "YBNSR";
	public static final String JYZS = "JYZS";
	
	public static final Long WPPARTNERID = 50001886l;
}
