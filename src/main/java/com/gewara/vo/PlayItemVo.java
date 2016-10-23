package com.gewara.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class PlayItemVo implements Serializable {

	private static final long serialVersionUID = 2813012172098474757L;

	private Long playid;
	private String movieName;
	private Timestamp playtime;
	private Integer num;
	private Double amount;
	private Double discount;
	private Double disrate;
	private Integer disnum;
	private List<Long> settleid;
	
	public Integer getDisnum() {
		return disnum;
	}
	public void setDisnum(Integer disnum) {
		this.disnum = disnum;
	}
	public Long getPlayid() {
		return playid;
	}
	public void setPlayid(Long playid) {
		this.playid = playid;
	}
	public String getMovieName() {
		return movieName;
	}
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	public Timestamp getPlaytime() {
		return playtime;
	}
	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Double getDiscount() {
		return discount;
	}
	public void setDiscount(Double discount) {
		this.discount = discount;
	}
	public Double getDisrate() {
		return new BigDecimal((discount / amount) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	public void setDisrate(Double disrate) {
		this.disrate = disrate;
	}
	public List<Long> getSettleid() {
		return settleid;
	}
	public void setSettleid(List<Long> settleid) {
		this.settleid = settleid;
	}
	
	
}
