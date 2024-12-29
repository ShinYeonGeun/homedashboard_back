package com.lotus.homeDashboard.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.interceptor.PreProcessInterceptor;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Bean
	public PreProcessInterceptor preProcessInterceptor() {
		return new PreProcessInterceptor();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(preProcessInterceptor()).excludePathPatterns("/favicon.ico", "*/**/*.css", "*/**/*.js", "/error");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
//        .allowCredentials(true)
		.exposedHeaders(Keys.ACCESS_TOKEN.getKey());
//		WebMvcConfigurer.super.addCorsMappings(registry);
	}
	
	
	
}
