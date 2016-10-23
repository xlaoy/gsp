package com.gewara.vo;

import java.io.Serializable;

public class JobVo implements Serializable {

	private static final long serialVersionUID = 140289311887153098L;

	private String jobname;
	private String firetime;
	private String nextfire;
	private String ip;
	private String status;
	
	public String getJobname() {
		return jobname;
	}
	public void setJobname(String jobname) {
		this.jobname = jobname;
	}
	public String getFiretime() {
		return firetime;
	}
	public void setFiretime(String firetime) {
		this.firetime = firetime;
	}
	public String getNextfire() {
		return nextfire;
	}
	public void setNextfire(String nextfire) {
		this.nextfire = nextfire;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
