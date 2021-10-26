package com.example.data.converter.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.example.data.converter.Constants;

@Configuration
@Lazy
@ComponentScan(basePackages = { Constants.BASE_PACKAGE })
public class SparkConfig {

	private static final Logger log = LoggerFactory.getLogger(SparkConfig.class);

	@Value("${main-application.spark.timegap-ms}")
	private int timegapMs;

	@Value("${main-application.spark.master-url}")
	private String masterUrl;

	@Value("${main-application.spark.worker-jars-path}")
	private String workerJarsPath;

	@Bean
	@Scope(Constants.SINGLETON)
	public JavaStreamingContext javaStreamingContext() {
		log.debug("[javaStreamingContext] masterUrl: {}", masterUrl);
		SparkConf sparkConf = new SparkConf().setAppName("influxdb-client-test").setMaster(masterUrl);
		log.debug("[javaStreamingContext] workerJarsPath: {}", workerJarsPath);
		if (StringUtils.isNotBlank(workerJarsPath)) {
			File workerJarsFolder = new File(workerJarsPath);
			List<String> jars = Arrays.asList(workerJarsFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean accepted = name.endsWith(".jar");
					if (accepted) {
						log.debug("[javaStreamingContext] workerJar: {}", name);
					}
					return accepted;
				}
			})).stream().map(x -> new File(workerJarsPath, x).getPath()).collect(Collectors.toList());
			sparkConf = sparkConf.setJars(jars.toArray(new String[0]));
		}
		return new JavaStreamingContext(sparkConf, new Duration(timegapMs));
	}
}
