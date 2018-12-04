package com.miracle.common.transaction.log.aop.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.miracle.common.transaction.util.LogPatternMatchUtils;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.ClassTransactional;
import com.miracle.common.transaction.annotation.LogTransactional;
import com.miracle.common.transaction.annotation.api.Propagation;

@Component
public class LogTransactionHandler {
	
	@Autowired
	private LogTransactionAttributes transactionAttributes;
	
	public TransactionProperties handle(Method method) {
		TransactionProperties ret = new TransactionProperties();
		LogTransactional logTransactional = method.getAnnotation(LogTransactional.class);
		if (logTransactional != null) {
			ret.setModule(logTransactional.module());
			ret.setPropagation(logTransactional.propagation());
			return ret;
		}
		@SuppressWarnings("rawtypes")
		Class clazz = method.getDeclaringClass();
		@SuppressWarnings("unchecked")
		ClassTransactional classTransactional = (ClassTransactional) clazz.getAnnotation(ClassTransactional.class);
		if (classTransactional == null) {
			return null;
		}
		Propagation propa = getPropagation(method.getName());
		if (propa == null) {
			ret.setModule(classTransactional.module());
			ret.setPropagation(classTransactional.defaultPropagation());
			return ret;
		} else {
			ret.setModule(classTransactional.module());
			ret.setPropagation(propa);
			return ret;
		}
	}
	
	
	private Propagation getPropagation(String methodName) {
		Map<String, String> map = transactionAttributes.getAttributes();
		for (String key : map.keySet()) {
			if (LogPatternMatchUtils.simpleMatch(key, methodName)) {
				String propStr = map.get(key);
				return Propagation.parsePropaByString(propStr);
			}
		}
		return null;
	}
	
	
	@Data
	static class TransactionProperties {
		Propagation propagation;
		String module;
	}
}	
