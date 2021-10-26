package com.example.data.converter.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.example.data.converter.*;
import com.example.data.converter.service.InfluxDbService;

@Configuration
@Lazy
@ComponentScan(basePackages = { Constants.BASE_PACKAGE })
public class InfluxDbConfig {

	@Value("${influxdb-client.url}")
	private String url;

	@Value("${influxdb-client.user}")
	private String user;

	@Value("${influxdb-client.password}")
	private String password;

	@Bean
	@Scope(Constants.SINGLETON)
	public InfluxDB influxDB() {
		return InfluxDBFactory.connect(url, user, password);
	}

	@Bean
	@Scope(Constants.SINGLETON)
	public InfluxDbService influxDbService() {
		return new InfluxDbService();
	}
}
