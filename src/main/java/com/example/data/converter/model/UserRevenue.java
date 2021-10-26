package com.example.data.converter.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

@SuppressWarnings("serial")
public class UserRevenue implements Serializable {

	private Long startTime;
	private Long endTime;
	private Long userId;
	private String userName;
	private Float chargePerCall;
	private Long totalNumOfCalls;
	private Float revenue;

	public UserRevenue(Long startTime, Long endTime, Long userId, String userName, Float chargePerCall,
			Long totalNumOfCalls, Float revenue) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.userId = userId;
		this.userName = userName;
		this.chargePerCall = chargePerCall;
		this.totalNumOfCalls = totalNumOfCalls;
		this.revenue = revenue;
	}

	public UserRevenue(Long startTime, Long endTime, User user, Long totalNumOfCalls, Float revenue) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.setUserId(user.getUserId());
		this.setUserName(user.getName());
		this.setChargePerCall(user.getChargePerCall());
		this.totalNumOfCalls = totalNumOfCalls;
		this.revenue = revenue;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public Long getTotalNumOfCalls() {
		return totalNumOfCalls;
	}

	public void setTotalNumOfCalls(Long totalNumOfCalls) {
		this.totalNumOfCalls = totalNumOfCalls;
	}

	public Float getRevenue() {
		return revenue;
	}

	public void setRevenue(Float revenue) {
		this.revenue = revenue;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Float getChargePerCall() {
		return chargePerCall;
	}

	public void setChargePerCall(Float chargePerCall) {
		this.chargePerCall = chargePerCall;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
