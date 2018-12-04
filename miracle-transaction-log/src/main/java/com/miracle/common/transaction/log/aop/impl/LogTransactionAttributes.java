package com.miracle.common.transaction.log.aop.impl;

import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix="melot.transaction")
public class LogTransactionAttributes {
	
	private Map<String, String> attributes;
}
