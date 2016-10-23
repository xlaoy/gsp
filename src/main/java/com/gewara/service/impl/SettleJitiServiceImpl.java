package com.gewara.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.InvoiceConfig;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleJiti;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.DaoService;
import com.gewara.service.SettleJitiService;

@Service("settleJitiServiceImpl")
public class SettleJitiServiceImpl implements SettleJitiService {

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Override
	public void updateJiti(SettlementBill bill) {
		if(bill == null) {
			return;
		}
		if(SettleConstant.COMM_Y.equals(bill.getHasJizhang())) {
			return;
		}
		SettleJiti jiti = daoService.getObject(SettleJiti.class, bill.getRecordId());
		if(jiti == null) {
			jiti = new SettleJiti();
		}
		String tag = bill.getTag();
		jiti.setRecordid(bill.getRecordId());
		jiti.setBilltype(tag);
		jiti.setStarttime(bill.getStartTime());
		jiti.setEndtime(bill.getEndTime());
		jiti.setNum(bill.getOrderTotalNumber().longValue());
		jiti.setAmount(bill.getOrderTotalAmount());
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(extend != null) {
			jiti.setKpamount(extend.getBillingamount());
		}
		if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)) {
			Long channelcfgid = bill.getRelateId();
			ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, channelcfgid);
			if(csc != null) {
				jiti.setVendercode(csc.getVendorCode());
				jiti.setVendername(csc.getVendorName());
				String ventype = csc.getVendorType();
				jiti.setPlaytype(ventype);
				if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(ventype)) {
					List<VendorCinemaRelation> vcrlist = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", channelcfgid);
					if(CollectionUtils.isNotEmpty(vcrlist)) {
						VendorCinemaRelation vcr = vcrlist.get(0);
						Place p = daoService.getObject(Place.class, vcr.getCinemaRecordId());
						if(p != null) {
							jiti.setPlacename(p.getName());
						}
					}
				}
			}
		} else {
			String placeid = bill.getConfigId();
			Place p = daoService.getObject(Place.class, placeid);
			if(p != null) {
				jiti.setPlacename(p.getName());
			}
			SettleConfig sc = daoService.getObject(SettleConfig.class, placeid);
			if(sc != null) {
				jiti.setVendercode(sc.getVenderNo());
				jiti.setVendername(sc.getVenderName());
			}
			if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(tag)) {
				jiti.setSpecial(bill.getSpecial());
				jiti.setPlaytype(bill.getPlayType());
				List<InvoiceConfig> isrlist = daoService.getObjectListByField(InvoiceConfig.class, "placeid", placeid);
				if(CollectionUtils.isNotEmpty(isrlist)) {
					InvoiceConfig invc = isrlist.get(0);
					double taxrate = invc.getTaxrate() == null ? 0.0 : invc.getTaxrate();
					jiti.setTaxrate(taxrate);
					taxrate = taxrate / 100;
					double tmp = 1 + taxrate;
					double kpamount = jiti.getKpamount() == null ? 0.0 : jiti.getKpamount();
					double exclutax = Double.valueOf(String.format("%.2f", kpamount / tmp));
					jiti.setExclutax(exclutax);
					jiti.setTaxamount(Double.valueOf(String.format("%.2f", exclutax * taxrate)));
				}
			}
		}
		daoService.saveObject(jiti);
	}
}
