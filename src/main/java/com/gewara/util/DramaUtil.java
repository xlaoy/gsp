package com.gewara.util;

import java.util.List;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaVersion;

public class DramaUtil {

	/**
	 * recordidsƴ��
	 * @param recordids
	 * @return
	 */
	public static <T extends Object> String sqlinList(List<T> recordids, Class<T> clz) {
		boolean isstr = false; 
		if("java.lang.String".equals(clz.getName())) {
			isstr = true;
		}
		StringBuilder sb = new StringBuilder(" (");
		for(int i = 0; i < recordids.size(); i++) {
			T recordid = recordids.get(i);
			if(i == (recordids.size() - 1)) {
				if(isstr) {
					sb.append("'" + recordid + "'");
				} else {
					sb.append(recordid + "");
				}
			} else {
				if(isstr) {
					sb.append("'" + recordid + "',");
				} else {
					sb.append(recordid + ",");
				}
			}
		}
		sb.append(") ");
		return sb.toString();
	}
	
	/**
	 * recordidsƴ��
	 * @param recordids
	 * @return
	 */
	public static <T extends Object> String sqlinSet(Set<T> recordids, Class<T> clz) {
		boolean isstr = false; 
		if("java.lang.String".equals(clz.getName())) {
			isstr = true;
		}
		StringBuilder sb = new StringBuilder(" (");
		int i = 0;
		for(T recordid : recordids) {
			if(i == (recordids.size() - 1)) {
				if(isstr) {
					sb.append("'" + recordid + "'");
				} else {
					sb.append(recordid + "");
				}
			} else {
				if(isstr) {
					sb.append("'" + recordid + "',");
				} else {
					sb.append(recordid + ",");
				}
			}
			i++;
		}
		sb.append(") ");
		return sb.toString();
	}
	
	/**
	 * ��ȡ�����б��ѯ����
	 * @param dv
	 * @return
	 */
	public static DetachedCriteria getDramaPlayItemDetaByDV(DramaVersion dv) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class);
		query.add(Restrictions.eq("dramaid", dv.getDramaid()));
		query.add(Restrictions.ge("playtime", dv.getStarttime()));
		//����ʱ����05-28,��ʵ���ݳ�ʱ�������05-28���컹�У����Խ���ʱ��Ҫ�ӳ�һ��
		query.add(Restrictions.lt("playtime", DateUtil.addDay(dv.getEndtime(), 1)));
		query.addOrder(Order.asc("playtime"));
		return query;
	}
	
}
