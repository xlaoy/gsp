/** 
 */
package test;

import java.sql.Timestamp;

import com.gewara.util.DateUtil;


/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  May 27, 2014  5:34:56 PM
 */
public class Testtemp {
	public static void main(String[] args) {
		int recordid = 59;
		Timestamp start = DateUtil.parseTimestamp("2014-01-01 00:00:00");
		for (int i = 0; i < 89; i++){
			
			String sb  = new String(" INSERT INTO check_bill" +
												"(record_id, starttime,endtime,place_type, place_id, status, order_total_number, order_total_amount, " +
												"refund_total_number, refund_total_amount, adjust_total_number, adjust_total_amount, config_id, settlement_id," +
												" unrefund_total_number, unrefund_total_amount, adjust_reason, admininfo) values (");
			Timestamp end = DateUtil.addDay(start, 1);
			sb = sb + recordid + ",'" + DateUtil.formatTimestamp(start) + "','" + DateUtil.formatTimestamp(end) + "','TICKET',37947638, 'NEW',0,0,0,0,0,0,'37947638,TICKET', null, 0,0,null,'man insertion');"; 
			System.out.println(sb);
			recordid++;
			start = end;
		}
	}
}
