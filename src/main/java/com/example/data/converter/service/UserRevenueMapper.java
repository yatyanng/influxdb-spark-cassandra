package com.example.data.converter.service;

import org.apache.commons.collections4.IterableUtils;
import org.apache.spark.api.java.function.Function;

import com.example.data.converter.Constants;
import com.example.data.converter.model.UsageFrequency;
import com.example.data.converter.model.User;
import com.example.data.converter.model.UserRevenue;

import scala.Tuple2;

@SuppressWarnings("serial")
public class UserRevenueMapper implements Function<Tuple2<Long, UsageFrequency>, UserRevenue> {

	private Iterable<User> userList;
	
	public UserRevenueMapper(Iterable<User> userList) {
		this.userList = userList;
	}
	
	@Override
	public UserRevenue call(Tuple2<Long, UsageFrequency> tuple) throws Exception {
		Long userId = tuple._1;
		UsageFrequency totalUsageFrequency = tuple._2;
		Long numberOfCalls = totalUsageFrequency.getFrequency();
		User user = IterableUtils.find(userList, candidate -> candidate.getUserId().equals(userId));
		Double revenue = Math.round(user.getChargePerCall() * numberOfCalls * Constants.ONE_HUNDRED_PERCENT)
				/ Constants.ONE_HUNDRED_PERCENT;
		UserRevenue userRevenue = new UserRevenue(totalUsageFrequency.getStartTime(),
				totalUsageFrequency.getEndTime(), user, numberOfCalls, revenue.floatValue());
		return userRevenue;
	}
}