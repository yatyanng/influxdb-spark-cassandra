package com.example.data.converter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TimerTask;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.example.data.converter.model.UsageFrequency;
import com.example.data.converter.model.UserRevenue;
import com.example.data.converter.service.InfluxDbService;

public class StreamingTimerTask extends TimerTask {

	private static final Logger log = LoggerFactory.getLogger(StreamingTimerTask.class);

	@Value("${main-application.timegap-ms}")
	private int timegapMs;

	@Value("${cassandra.table-name}")
	private String tableName;

	@Value("${cassandra.keyspace-name}")
	private String keyspaceName;

	@Autowired
	private KafkaTemplate<String, UsageFrequency> kafkaTemplate;

	@Autowired
	private InfluxDbService influxDbService;

	@Autowired
	private JavaStreamingContext javaStreamingContext;

	private Long startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis();

	public static Map<String, String> columnMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("start_time", "startTime");
		mapping.put("end_time", "endTime");
		mapping.put("user_id", "userId");
		mapping.put("user_name", "userName");
		mapping.put("charge_per_call", "chargePerCall");
		mapping.put("total_num_of_calls", "totalNumOfCalls");
		mapping.put("revenue", "revenue");
		return mapping;
	}

	@Override
	public void run() {
		Long endTime = startTime + timegapMs;
		try {
			influxDbService.writeToInfluxDb(startTime);
			kafkaTemplate.setDefaultTopic(Constants.USAGE_FREQUENCY_TOPIC);
			List<UsageFrequency> usageFrequencyList = influxDbService.readFromInfluxDb(startTime, endTime);
			usageFrequencyList.stream().forEach(usageFrequency -> {
				kafkaTemplate.sendDefault(String.valueOf(usageFrequency.hashCode()), usageFrequency);
			});
			kafkaTemplate.flush();

			JavaRDD<UserRevenue> userRevenueRdd = CassandraJavaUtil.javaFunctions(javaStreamingContext.sparkContext())
					.cassandraTable(keyspaceName, tableName,
							CassandraJavaUtil.mapRowTo(UserRevenue.class, columnMapping()));

			SparkSession spark = SparkSession.builder().sparkContext(javaStreamingContext.sparkContext().sc())
					.getOrCreate();
			Dataset<Row> userRevenueDataSet = spark.createDataFrame(userRevenueRdd, UserRevenue.class);

			userRevenueDataSet.createOrReplaceTempView("userRevenue");

			Dataset<Row> revenueDataSet = spark
					.sql("SELECT from_unixtime(startTime/1000,'yyyy-MM-dd HH:mm:ss') as startTs, "
							+ "from_unixtime(endTime/1000,'yyyy-MM-dd HH:mm:ss') as endTs, "
							+"userId, userName, chargePerCall, totalNumOfCalls, revenue FROM userRevenue WHERE startTime = "
							+ (startTime - timegapMs));

			revenueDataSet.show();
		} catch (Exception e) {
			log.error("[run]", e);
		}

		startTime = endTime;
	}
}