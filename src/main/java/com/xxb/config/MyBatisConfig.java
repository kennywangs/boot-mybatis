package com.xxb.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xxb.mybatis.MapperRefresh;

@Configuration
public class MyBatisConfig {

	@Bean
	public MapperRefresh getMapperRefresh(SqlSessionFactory factory) throws Exception {
		return new MapperRefresh(factory.getConfiguration());
	}
}
