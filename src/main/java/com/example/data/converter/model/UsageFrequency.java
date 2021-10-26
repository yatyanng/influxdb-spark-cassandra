package com.example.data.converter.model;

import java.io.Serializable;
import java.time.Instant;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
@Measurement(name = "usage")
public class UsageFrequency implements Serializable {

	@Column(name = "time")
	@JsonIgnore
	private Instant time;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "frequency")
	private Long frequency;

	private Long startTime, endTime;

	public UsageFrequency add(UsageFrequency usageFrequency) {
		if (usageFrequency != null && usageFrequency.getFrequency() != null) {
			frequency += usageFrequency.getFrequency();
		}
		return this;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Instant getTime() {
		return time;
	}

	public void setTime(Instant time) {
		this.time = time;
	}

	public Long getFrequency() {
		return frequency;
	}

	public void setFrequency(Long frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the startTime
	 */
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
