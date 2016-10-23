/** 
 */
package com.gewara.web.action;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.util.MiscUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Feb 20, 2014  12:07:43 PM
 */
@Controller
public class PictureController extends AnnotationController{
	
	@RequestMapping("/financeChop.xhtml")
	public void getFinanceChop(HttpServletRequest request, HttpServletResponse response){
		boolean checkedResult = checkPerimession(request);
		if (!checkedResult)
			return;
		
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		ServletOutputStream os;
		DataInputStream is ;
		String imgPath = this.getClass().getClassLoader().getResource("financechop.png").getPath();
		try {
			os = response.getOutputStream();
			is = new DataInputStream(new FileInputStream(imgPath));
			IOUtils.copy(is, os);
			os.flush();
			os.close();
		} catch (IOException e) {
			dbLogger.error("", e);
		}
	}
	/**
	 * @param request
	 * @return
	 */
	private boolean checkPerimession(HttpServletRequest request) {
		String referer = request.getHeader("referer");
		User logonUser = getLogonUser(request);
		if (logonUser != null)
			return true;
		
		HttpSession session = request.getSession();
		Map<String, Object> dataMap = (Map<String, Object>) session
												.getAttribute("dataMap");
		if (dataMap != null && dataMap.get("id") != null)
			return true;
		
		dbLogger.warn("∑«∑®«Î«Û£∫" + referer);
		return false;
	}
	
	@RequestMapping("/qrcode.xhtml")
	public void showPicture(Long recordId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean checkedResult = checkPerimession(request);
		if (!checkedResult)
			return;
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		if (bill == null)
			return;
		
		List allowedStatus = Arrays.asList(CheckBillStatusEnums.SETTLED.getStatus(), CheckBillStatusEnums.WAITINGPAY.getStatus());
		if (!allowedStatus.contains(bill.getStatus()) )
			return;
		
		String encodedData = encodeSettleBillData_V1(bill);
		
		try{
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			generateQR2DCode(encodedData, response.getOutputStream());
		}catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	/**
	 * 
	 * encode settlement bill info and check bill which is belong to this settlement bill
	 * rule: sb.recordId + "|" +(cb.recordId + "|" +  cb.orderTotalNumber + "|" 
	 * 		 + cb.orderTotalAmount + "|" + cb.refundTotalNumber + "|" + cb.refundTotalAmount + "|" + cb.adjustTotalNumber + "|"
	 * 		 + cb.adjustTotalAmount + "@" ) * checkbill.count 
	 * @param bill
	 * @return
	 */
	public static final String password = "asdasd212sdadsadsf";
/*	@Deprecated
	private String encodeSettleBillData(SettlementBill bill) {
		Long recordId = bill.getRecordId();
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", recordId);
		StringBuffer sb = new StringBuffer("" + recordId + "|");
		for (CheckBill cb : checkBills){
			sb.append(cb.getRecordId() + "|");
			sb.append(DateUtil.format(cb.getStart(), "yyyy-MMdd" + "|"));
			sb.append(cb.getOrderTotalNumber() + "|");
			sb.append(cb.getOrderTotalAmount() + "|");
			sb.append(cb.getRefundTotalNumber() + "|");
			sb.append(cb.getRefundTotalAmount() + "|");
			sb.append(cb.getAdjustTotalNumber() + "|");
			sb.append(cb.getAdjustTotalAmount() + "|");
		}
		sb.append("@");
		String data = "GewaraGSP:" + sb.toString();
		byte[] aeSdecrypt = MiscUtil.AESencrypt(data, password);
		return MiscUtil.parseByte2HexStr(aeSdecrypt);
	}
*/	
	private String encodeSettleBillData_V1(SettlementBill bill){
		Long recordId = bill.getRecordId();
		StringBuffer sb = new StringBuffer(recordId + "|");
		sb.append(bill.getStartTime() + "|" + bill.getEndTime() + "|" + 
				  bill.getOrderTotalNumber() + "|" + bill.getOrderTotalAmount() + "|" + bill.getConfigId());
		sb.append("@");
		String data = "GewaraGSP:" + sb.toString();
		byte[] aeSdecrypt = MiscUtil.AESencrypt(data, password);
		return MiscUtil.parseByte2HexStr(aeSdecrypt);
		
	}
	private void generateQR2DCode(String data, ServletOutputStream sos) throws Exception {
    	MultiFormatWriter formatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = formatWriter.encode(data, BarcodeFormat.QR_CODE, 220, 220, null);     
        MatrixToImageWriter.writeToStream(bitMatrix, "png", sos);  
		sos.flush();
		sos.close();
	}
}
