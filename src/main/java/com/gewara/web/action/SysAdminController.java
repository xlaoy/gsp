package com.gewara.web.action;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.model.acl.User;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.LoggerUtils;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class SysAdminController extends AnnotationController{
	@Autowired@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate;
	@Autowired
	@RequestMapping(value="/admin/sysmgr/execQuery.xhtml", method=RequestMethod.GET)
	public String query(){
		return "admin/sysmgr/execQuery.vm";
	}
	@RequestMapping(value="/platform/queryTradeNoAndPass.xhtml", method=RequestMethod.GET)
	public String goQueryTradeNoAndPass(){
		return "admin/sysmgr/tradeNoAndPass.vm";
	}
	@RequestMapping(value="/platform/queryTradeNoAndPass.xhtml", method=RequestMethod.POST)
	public String queryTradeNoAndPass(String passes, Timestamp start, Timestamp end, String cinemaId, ModelMap model, String isXls, HttpServletResponse response, HttpServletRequest request){
		if (StringUtils.isBlank(passes))
			return "admin/sysmgr/tradeNoAndPass.vm";
		if (!checkSpecialUrl(request))
			return "bad request...#";
		Object[] passArray = passes.split(",");
		
		String in = "?";
		if (passArray.length >1)
			in = in + StringUtils.repeat(",?", passArray.length - 1);
		
		String sql = " select HFHPASS, trade_no FROM WEBDATA.view4js_ticket_order where HFHPASS IN (" + in +  ") and addtime >= ? and addtime < ? and CINEMAID = ? order by HFHPASS";
		List<Map<String, Object>> queryForList = shJdbcTemplate.queryForList(sql, passArray, start, end, cinemaId);
		Map<String, String> map = new HashMap<String, String>();
		for (Map<String, Object> m : queryForList){
			map.put(m.get("HFHPASS").toString(), m.get("TRADE_NO").toString());
		}
		model.put("arrayList", passArray);
		model.put("map", map);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "订单号确认号",  response);
			return "admin/sysmgr/tradeNoAndPass.vm";
		}
		return "admin/sysmgr/tradeNoAndPass.vm";
	}
	
	
	@RequestMapping(value="/admin/sysmgr/printTables.xhtml")
	@ResponseBody
	public String printTables(){
		//结果与select 'GPTBS.'||table_name||'.'||column_name from user_tab_columns t order by table_name,column_name做对比
		Map<String, ClassMetadata> allData = hibernateTemplate.getSessionFactory().getAllClassMetadata();
		List<String> all = new ArrayList<String>();
		for(Entry<String, ClassMetadata> entry : allData.entrySet()){
			AbstractEntityPersister persister = (AbstractEntityPersister)allData.get(entry.getKey());
			List<String> columns = getColumns(persister, false);
			columns.addAll(Arrays.asList(persister.getIdentifierColumnNames()));
			for(String column: columns){
				all.add(StringUtils.upperCase(persister.getTableName() + "." + column));
			}
		}
		Collections.sort(all);
		return "success";
	}
	private List<String> getColumns(AbstractEntityPersister persister, boolean filter){
		List<String> columns = new ArrayList<String>();
		int len =  persister.getPropertyNames().length;
		for(int i=0;i<len;i++){
			String[] s = persister.getPropertyColumnNames(i);
			
			if(s==null || s[0]==null) continue;
			String label = s[0];
			if(filter && StringUtils.containsIgnoreCase(label, "pass") ||
					StringUtils.containsIgnoreCase(label, "privatekey") ||
					StringUtils.containsIgnoreCase(label, "secretkey")) continue;
			columns.add(label);
		}
		return columns;
	}
	@RequestMapping(value="/admin/sysmgr/execQuery.xhtml", method = RequestMethod.POST)
	public String query(HttpServletRequest request, HttpServletResponse res, Integer count, String source, String sql, final String contentType,
			String queryAll, ModelMap model) throws IOException {
		User user = getLogonUser(request);
		String ip = WebUtils.getRemoteIp(request);
		if(!GewaIpConfig.allowOffice(ip)) {
			res.sendError(400, "bad request!!!!!!!!");
			return null;
		}
		sql = sql.trim();
		dbLogger.error("[" + user.getUsername() + "]exec query:" + sql);
//		HibernateTemplate ht = StringUtils.isBlank(source) ? hibernateTemplate : shHibernateTemplate; 
		JdbcTemplate jt = StringUtils.isBlank(source) ? jdbcTemplate : shJdbcTemplate;
		if (StringUtils.containsIgnoreCase(sql, "delete") || StringUtils.containsIgnoreCase(sql, "trancate") || !StringUtils.startsWithIgnoreCase(sql, "select"))
			return forwardMessage(model, "error");
		if(!StringUtils.equals(source, "SHVIEW")){
			if(!StringUtils.containsIgnoreCase(sql, "limit")){
				sql = sql + " limit 100 ";
			}
		}
		
		if (StringUtils.contains(contentType, "xls")) {
			download("xls", res);
		} else {
			res.setContentType("text/html");
		}
		final Writer writer = res.getWriter();
		String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
				+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
				+ "<head>"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
				+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://static.gewara.cn/component/css/default.css\" />"
				+ "</head><body>";
		writer.write(header);
		if(count == null) count = 10000;
		final int max = StringUtils.isNotBlank(queryAll) ? count : 100;
		
		try {
			jt.query(sql, new ResultSetExtractor<String>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException, DataAccessException {
					RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper();
					try {
						int rowcount = 0;
						ResultSetMetaData md = rs.getMetaData();
						List<String> columnList = new ArrayList<String>();
						for (int i = 1; i <= md.getColumnCount(); i++) {
							columnList.add(md.getColumnLabel(i));
						}
						writer.write("<table class=\"table\">");
						writer.write("<tr>");
						for (String column : columnList) {
							writer.write("<td>" + column + "</td>");
						}
						writer.write("</tr>");
						while (rs.next() && rowcount < max) {
							Map<String, Object> row = mapper.mapRow(rs, rowcount++);
							writer.write("<tr>");
							for (String column : columnList) {
								String v = row.get(column) == null ? "" : row.get(column).toString();
								if (!StringUtils.equals(contentType, "xls"))
									v = VmUtils.escapeHtml(v);
								// bob 20110901更新, 防止数字切换为科学计数法
								writer.write("<td style=\"vnd.ms-excel.numberformat:@\">" + v + "</td>");
							}
							writer.write("</tr>");
						}
						writer.write("</table></body></html>");
					} catch (Exception e) {
						return LoggerUtils.getExceptionTrace(e, 50);
					}
					return null;
				}
			});
		} catch (Exception e) {
			return forwardMessage(model, LoggerUtils.getExceptionTrace(e, 50));
		}
		return null;
	}
}
