package com.example.data.converter.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.example.data.converter.model.UsageFrequency;

public class InfluxDbService {

	private static final Logger log = LoggerFactory.getLogger(InfluxDbService.class);

	@Autowired
	private InfluxDB influxDB;

	@Value("${influxdb-client.db-name}")
	private String dbName;

	@Value("${influxdb-client.retention-policy}")
	private String retentionPolicy;

	@Value("${influxdb-client.test-write.number-of-samples}")
	private int numberOfSamples;

	@Value("${influxdb-client.test-write.number-of-users}")
	private int numberOfUsers;

	@Value("${influxdb-client.test-write.max-frequency}")
	private int maxFrequency;

	@Value("${main-application.timegap-ms}")
	private int timegapMs;

	@SuppressWarnings("deprecation")
	public void initInfluxDb() {
		log.debug("[initInfluxDb] dbName: {}", dbName);

		influxDB.createDatabase(dbName);
		influxDB.setDatabase(dbName);
		log.debug("[initInfluxDb] retentionPolicy: {}", retentionPolicy);

		influxDB.createRetentionPolicy(retentionPolicy, dbName, "30d", "30m", 2, true);
		influxDB.setRetentionPolicy(retentionPolicy);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
	}

	@SuppressWarnings("deprecation")
	public void deinitInfluxDb() {
		influxDB.deleteDatabase(dbName);
		influxDB.dropRetentionPolicy(retentionPolicy, dbName);
	}

	public void writeToInfluxDb(Long startTime) {
		for (int i = 0; i < numberOfSamples; i++) {
			try {
				Point usagePoint = Point.measurement("usage")
						.time(startTime + Double.valueOf(Math.random() * timegapMs).longValue(), TimeUnit.MILLISECONDS)
						.addField("user_id", Double.valueOf(Math.random() * numberOfUsers).longValue() + 1)
						.addField("frequency", Double.valueOf(Math.random() * maxFrequency).intValue() + 1).build();
				influxDB.write(usagePoint);
				log.debug("[writeToInfluxDb] usagePoint: {}", usagePoint);
			} catch (Exception e) {
				log.error("[writeToInfluxDb]", e);
			}
		}
		influxDB.flush();
	}

	public List<UsageFrequency> readFromInfluxDb(Long startTime, Long endTime) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String influxQL = "SELECT * FROM usage where time >= '" + sdf.format(startTime) + "' and time < '"
				+ sdf.format(endTime) + "'";
		log.debug("[readFromInfluxDb] influxQL: {}", influxQL);
		Query query = new Query(influxQL, dbName);
		log.debug("[readFromInfluxDb] query: {}", query);
		QueryResult queryResult = influxDB.query(query);
		log.info("[readFromInfluxDb] queryResult.getResults(): {}", queryResult.getResults());
		InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
		List<UsageFrequency> list = resultMapper.toPOJO(queryResult, UsageFrequency.class);
		list.stream().forEach(usageFrequency -> {
			usageFrequency.setStartTime(startTime);
			usageFrequency.setEndTime(endTime);
		});
		return list;
	}
}
