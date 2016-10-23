/** 
 */
package com.gewara.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.service.TestInterface;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Mar 21, 2014  5:59:03 PM
 */
@Service("testInterface")
public class TestInterfaceImpl implements TestInterface{
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	/* (non-Javadoc)
	 * @see com.gewara.service.TestInterface#testA()
	 */
	@Override
	public void testA() {
		String sql = " select count(1) from settle_config where open_type = 'WD' ";
		int r1 = jdbcTemplate.queryForObject(sql, Integer.class);
		System.out.println(r1);
		System.out.println(r1);
		int r2 = jdbcTemplate.queryForObject(sql, Integer.class);
		System.out.println(r2);
		
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.TestInterface#testB()
	 */
	@Override
	public void testB() {
		String sql = " delete from  settle_config where record_id  = '7,TICKET'";
		jdbcTemplate.update(sql);
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.TestInterface#testC()
	 */
	@Override
	public void testC() {
		// TODO Auto-generated method stub
		
	}

}
