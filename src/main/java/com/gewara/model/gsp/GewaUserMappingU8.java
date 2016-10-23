package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class GewaUserMappingU8 extends BaseObject{
	/**  
	 * @Fields serialVersionUID  
	 */  
	private static final long serialVersionUID = -8664417758785174382L;

	private Long recordId;
	
	private String gewaLoginName;
	
	private String gewaRealName;
	
	private Timestamp createTime;
	
	private String createUser;
	
	private Timestamp updateTime;
	
	private String updateUser;
	
	
	/**  
	 * @return recordId  
	 */
	public Long getRecordId() {
		return recordId;
	}


	/**  
	 * @param set recordId 
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}


	/**  
	 * @return gewaLoginName  
	 */
	public String getGewaLoginName() {
		return gewaLoginName;
	}


	/**  
	 * @param set gewaLoginName 
	 */
	public void setGewaLoginName(String gewaLoginName) {
		this.gewaLoginName = gewaLoginName;
	}


	/**  
	 * @return gewaRealName  
	 */
	public String getGewaRealName() {
		return gewaRealName;
	}


	/**  
	 * @param set gewaRealName 
	 */
	public void setGewaRealName(String gewaRealName) {
		this.gewaRealName = gewaRealName;
	}


	/**  
	 * @return createTime  
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}


	/**  
	 * @param set createTime 
	 */
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}


	/**  
	 * @return createUser  
	 */
	public String getCreateUser() {
		return createUser;
	}


	/**  
	 * @param set createUser 
	 */
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}


	/**  
	 * @return updateTime  
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}


	/**  
	 * @param set updateTime 
	 */
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}


	/**  
	 * @return updateUser  
	 */
	public String getUpdateUser() {
		return updateUser;
	}


	/**  
	 * @param set updateUser 
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	@Override
	public Serializable realId() {
		return recordId;
	}

	public GewaUserMappingU8() {
		super();
	}

	public GewaUserMappingU8(String gewaLoginName, String gewaRealName,
			Timestamp createTime, String createUser, Timestamp updateTime,
			String updateUser) {
		super();
		this.gewaLoginName = gewaLoginName;
		this.gewaRealName = gewaRealName;
		this.createTime = createTime;
		this.createUser = createUser;
		this.updateTime = updateTime;
		this.updateUser = updateUser;
	}
}
