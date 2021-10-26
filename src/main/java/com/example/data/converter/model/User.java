package com.example.data.converter.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "user", schema = "public")
public class User implements Serializable {

	@Id
	@Column(name = "user_id", columnDefinition = "bigserial")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(name = "name")
	private String name;

	@Column(name = "charge_per_call")
	private Float chargePerCall;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
