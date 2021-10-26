package com.example.data.converter;

import static com.example.data.converter.Constants.BASE_PACKAGE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.datastax.spark.connector.japi.CassandraStreamingJavaUtil;
import com.example.data.converter.model.UsageFrequency;
import com.example.data.converter.model.User;
import com.example.data.converter.model.UserRevenue;
import com.example.data.converter.service.InfluxDbService;
import com.example.data.converter.service.UserRepository;
import com.example.data.converter.service.UserRevenueMapper;

import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@EnableAutoConfiguration
@ComponentScan(BASE_PACKAGE)
public class MainApplication {

	private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InfluxDbService influxDbService;

	@Autowired
	private JavaStreamingContext javaStreamingContext;

	@Value("${main-application.timegap-ms}")
	private int timegapMs;

	@Value("${cassandra.table-name}")
	private String tableName;

	@Value("${cassandra.keyspace-name}")
	private String keyspaceName;

	private static Map<String, Object> deserializerParams;

	public static Map<String, String> userRevenueRelationMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("startTime", "start_time");
		mapping.put("endTime", "end_time");
		mapping.put("userId", "user_id");
		mapping.put("userName", "user_name");
		mapping.put("chargePerCall", "charge_per_call");
		mapping.put("totalNumOfCalls", "total_num_of_calls");
		mapping.put("revenue", "revenue");
		return mapping;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			String configDirectory = "conf";
			if (args.length > 0) {
				configDirectory = args[0];
			}
			log.debug("[main] config directory: {}", configDirectory);
			System.setProperty("spring.config.location", configDirectory + "/springboot.yml");
			System.setProperty("logging.config", configDirectory + "/logback.xml");

			ApplicationContext applicationContext = SpringApplication.run(MainApplication.class, args);
			deserializerParams = (Map<String, Object>) applicationContext
					.getBean(Constants.USAGE_FREQUENCY_DESERIALIZER_PARAMS);

			applicationContext.getBean(MainApplication.class).startStreaming(applicationContext);

		} catch (Exception e) {
			log.error("[main]", e);
		}
	}

	@SuppressWarnings({ "serial" })
	private void startStreaming(ApplicationContext applicationContext) throws InterruptedException {
		influxDbService.initInfluxDb();
		StreamingTimerTask streamingTimerTask = applicationContext.getBean(StreamingTimerTask.class);
		new Timer().scheduleAtFixedRate(streamingTimerTask, new Date(), timegapMs);

		log.debug("[startStreaming] deserializerParams: {}", deserializerParams);
		Collection<String> topics = Arrays.asList(Constants.USAGE_FREQUENCY_TOPIC);

		JavaInputDStream<ConsumerRecord<String, UsageFrequency>> directStream = KafkaUtils.createDirectStream(
				javaStreamingContext, LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, UsageFrequency>Subscribe(topics, deserializerParams));

		JavaDStream<UsageFrequency> inputStream = directStream.map(x -> x.value());
		JavaPairDStream<Long, UsageFrequency> mappedStream = inputStream
				.mapToPair(record -> new Tuple2<Long, UsageFrequency>(record.getUserId(), record));
		JavaPairDStream<Long, UsageFrequency> reducedStream = mappedStream
				.reduceByKey((ufreq1, ufreq2) -> ufreq1.add(ufreq2));

		reducedStream.print();
		JavaDStream<UserRevenue> outputStream = reducedStream
				.transform(new Function<JavaPairRDD<Long, UsageFrequency>, JavaRDD<UserRevenue>>() {
					@Override
					public JavaRDD<UserRevenue> call(JavaPairRDD<Long, UsageFrequency> reducedRdd) throws Exception {
						Iterable<User> userList = userRepository.findAll(reducedRdd.keys().collect());
						return reducedRdd.map(new UserRevenueMapper(userList));
					}
				});

		Map<String, String> fieldToColumnMapping = userRevenueRelationMapping();
		CassandraStreamingJavaUtil.javaFunctions(outputStream).writerBuilder(keyspaceName, tableName,
				CassandraJavaUtil.mapToRow(UserRevenue.class, fieldToColumnMapping)).saveToCassandra();
		outputStream.print();

		javaStreamingContext.start();
		javaStreamingContext.awaitTermination();
		javaStreamingContext.stop();
		javaStreamingContext.close();
	}
}
